package com.example.m3showcase.core.paging

import com.example.m3showcase.core.domain.model.BankError
import com.example.m3showcase.core.domain.model.Page
import com.example.m3showcase.core.domain.model.PageRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaginatorTest {

    /** Offset-paged fake server over 0 until [total]. Failures can be queued per call. */
    private class FakeSource(var total: Int = 25, private val latencyMs: Long = 100) {
        val requests = mutableListOf<PageRequest>()
        val failures = ArrayDeque<Throwable>()
        var startAt = 0 // shift to simulate new items arriving at the top

        suspend fun load(request: PageRequest): Result<Page<Int>> {
            requests += request
            delay(latencyMs)
            failures.removeFirstOrNull()?.let { return Result.failure(it) }
            val offset = request.cursor?.toInt() ?: 0
            val all = (startAt until startAt + total).toList()
            val items = all.drop(offset).take(request.size)
            val next = (offset + items.size).takeIf { it < all.size }
            return Result.success(Page(items, next?.toString(), totalCount = all.size))
        }
    }

    private fun TestScope.paginator(source: FakeSource, pageSize: Int = 10) =
        // The test scope itself: advanceUntilIdle() does not run work in backgroundScope, and every load finishes.
        Paginator(this, pageSize, keyOf = { it }, loadPage = source::load)

    @Test
    fun `start loads the first page once`() = runTest {
        val source = FakeSource()
        val p = paginator(source)
        p.start(); p.start()
        assertTrue(p.state.value.isInitialLoading)
        advanceUntilIdle()

        with(p.state.value) {
            assertFalse(isInitialLoading)
            assertEquals((0 until 10).toList(), items)
            assertEquals(25, totalCount)
            assertFalse(endReached)
        }
        assertEquals(1, source.requests.size)
    }

    @Test
    fun `loadNext appends pages until the end and then stops`() = runTest {
        val source = FakeSource()
        val p = paginator(source)
        p.start(); advanceUntilIdle()

        p.loadNext(); advanceUntilIdle()
        p.loadNext(); advanceUntilIdle()
        assertEquals((0 until 25).toList(), p.state.value.items)
        assertTrue(p.state.value.endReached)

        p.loadNext(); advanceUntilIdle()
        assertEquals(3, source.requests.size)
    }

    @Test
    fun `loadNext while a page is loading is ignored`() = runTest {
        val source = FakeSource()
        val p = paginator(source)
        p.start(); advanceUntilIdle()

        p.loadNext(); runCurrent()
        assertTrue(p.state.value.isAppending)
        p.loadNext(); p.loadNext()
        advanceUntilIdle()

        assertEquals(2, source.requests.size)
        assertEquals(20, p.state.value.items.size)
    }

    @Test
    fun `append error keeps items, blocks auto load, and retry recovers`() = runTest {
        val source = FakeSource()
        val p = paginator(source)
        p.start(); advanceUntilIdle()

        source.failures += BankError.NoInternet
        p.loadNext(); advanceUntilIdle()
        assertEquals(BankError.NoInternet, p.state.value.appendError)
        assertEquals(10, p.state.value.items.size)

        p.loadNext(); advanceUntilIdle() // scrolling again must not retry by itself
        assertEquals(2, source.requests.size)

        p.retry(); advanceUntilIdle()
        assertNull(p.state.value.appendError)
        assertEquals(20, p.state.value.items.size)
    }

    @Test
    fun `initial error shows full screen error and retry reloads`() = runTest {
        val source = FakeSource()
        source.failures += BankError.Timeout
        val p = paginator(source)
        p.start(); advanceUntilIdle()
        assertEquals(BankError.Timeout, p.state.value.initialError)
        assertTrue(p.state.value.items.isEmpty())

        p.retry(); advanceUntilIdle()
        assertNull(p.state.value.initialError)
        assertEquals(10, p.state.value.items.size)
    }

    @Test
    fun `refresh replaces items and resets the cursor`() = runTest {
        val source = FakeSource()
        val p = paginator(source)
        p.start(); advanceUntilIdle()
        p.loadNext(); advanceUntilIdle()

        source.startAt = 100
        p.refresh(); runCurrent()
        assertTrue(p.state.value.isRefreshing)
        assertEquals(20, p.state.value.items.size) // old items stay visible meanwhile
        advanceUntilIdle()

        assertFalse(p.state.value.isRefreshing)
        assertEquals((100 until 110).toList(), p.state.value.items)
        assertNull(source.requests.last().cursor)
    }

    @Test
    fun `failed refresh keeps items and reports the error once`() = runTest {
        val source = FakeSource()
        val p = paginator(source)
        p.start(); advanceUntilIdle()

        source.failures += BankError.NoInternet
        p.refresh(); advanceUntilIdle()

        assertFalse(p.state.value.isRefreshing)
        assertEquals(10, p.state.value.items.size)
        assertEquals(BankError.NoInternet, p.refreshErrors.first())
    }

    @Test
    fun `refresh cancels an in-flight page so it never lands on fresh data`() = runTest {
        val source = FakeSource()
        val p = paginator(source)
        p.start(); advanceUntilIdle()

        p.loadNext(); runCurrent()
        source.startAt = 100
        p.refresh(); advanceUntilIdle()

        assertEquals((100 until 110).toList(), p.state.value.items)
        assertFalse(p.state.value.isAppending)
    }

    @Test
    fun `reset clears items and shows the initial loader`() = runTest {
        val source = FakeSource()
        val p = paginator(source)
        p.start(); advanceUntilIdle()

        source.total = 3
        p.reset()
        assertTrue(p.state.value.isInitialLoading)
        assertTrue(p.state.value.items.isEmpty())
        advanceUntilIdle()

        assertEquals(listOf(0, 1, 2), p.state.value.items)
        assertTrue(p.state.value.endReached)
    }

    @Test
    fun `duplicates across pages are dropped`() = runTest {
        val source = FakeSource()
        val p = paginator(source)
        p.start(); advanceUntilIdle()

        source.startAt = -3 // three new items arrived at the top: page 2 now repeats 7, 8, 9
        p.loadNext(); advanceUntilIdle()

        val items = p.state.value.items
        assertEquals(items.distinct(), items)
        assertEquals(17, items.size)
    }

    @Test
    fun `empty first page is empty and at the end`() = runTest {
        val p = paginator(FakeSource(total = 0))
        p.start(); advanceUntilIdle()
        assertTrue(p.state.value.isEmpty)
        assertTrue(p.state.value.endReached)
        assertFalse(p.state.value.canLoadMore)
    }

    @Test
    fun `updateItems edits locally and returns the previous list`() = runTest {
        val p = paginator(FakeSource())
        p.start(); advanceUntilIdle()

        val before = p.updateItems { list -> list.filter { it % 2 == 0 } }
        assertEquals(10, before.size)
        assertEquals(5, p.state.value.items.size)
        assertNotNull(p.state.value.totalCount)
    }
}
