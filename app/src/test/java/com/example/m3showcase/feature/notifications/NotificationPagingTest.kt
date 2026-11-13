package com.example.m3showcase.feature.notifications

import com.example.m3showcase.core.common.DispatcherProvider
import com.example.m3showcase.core.connectivity.ConnectivityObserver
import com.example.m3showcase.core.connectivity.NetworkMonitor
import com.example.m3showcase.core.connectivity.NetworkStatus
import com.example.m3showcase.core.domain.model.BankError
import com.example.m3showcase.core.domain.model.PageRequest
import com.example.m3showcase.feature.notifications.data.FakeNotificationRemoteDataSource
import com.example.m3showcase.feature.notifications.data.NotificationRepositoryImpl
import com.example.m3showcase.feature.notifications.domain.model.NotificationFilter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Repository + fake API together: the cursor contract the Paginator relies on. */
class NotificationPagingTest {

    private class FakeConnectivity(val status: MutableStateFlow<NetworkStatus>) : ConnectivityObserver {
        override fun observe() = status
        override fun currentStatus() = status.value
    }

    private fun TestScope.repository(online: MutableStateFlow<NetworkStatus> = MutableStateFlow(NetworkStatus.Available)): NotificationRepositoryImpl {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dispatchers = object : DispatcherProvider {
            override val main: CoroutineDispatcher = dispatcher
            override val io: CoroutineDispatcher = dispatcher
            override val default: CoroutineDispatcher = dispatcher
        }
        val monitor = NetworkMonitor(FakeConnectivity(online), backgroundScope)
        return NotificationRepositoryImpl(FakeNotificationRemoteDataSource(monitor, dispatchers))
    }

    @Test
    fun `walking every page returns each notification once, newest first`() = runTest {
        val repo = repository()
        val seen = mutableListOf<Long>()
        val ids = mutableListOf<String>()
        var cursor: String? = null
        do {
            val page = repo.getPage(NotificationFilter.All, PageRequest(cursor, 15)).getOrThrow()
            seen += page.items.map { it.timestamp }
            ids += page.items.map { it.id }
            assertEquals(FakeNotificationRemoteDataSource.SEED_COUNT, page.totalCount)
            cursor = page.nextCursor
        } while (cursor != null)

        assertEquals(FakeNotificationRemoteDataSource.SEED_COUNT, ids.size)
        assertEquals(ids.distinct(), ids)
        assertEquals(seen.sortedDescending(), seen)
    }

    @Test
    fun `marking read under the Unread filter does not skip the next page`() = runTest {
        val repo = repository()
        val first = repo.getPage(NotificationFilter.Unread, PageRequest(null, 4)).getOrThrow()
        assertEquals(6, first.totalCount)
        first.items.forEach { repo.markAsRead(it.id) }

        val second = repo.getPage(NotificationFilter.Unread, PageRequest(first.nextCursor, 4)).getOrThrow()
        assertEquals(2, second.items.size) // the remaining two, not zero
        assertNull(second.nextCursor)
    }

    @Test
    fun `category filter only returns that category`() = runTest {
        val page = repository().getPage(NotificationFilter.Security, PageRequest(null, 50)).getOrThrow()
        assertTrue(page.items.isNotEmpty())
        assertTrue(page.items.all { it.category.name == "Security" })
    }

    @Test
    fun `offline maps to NoInternet`() = runTest {
        val repo = repository(MutableStateFlow(NetworkStatus.Unavailable))
        val result = repo.getPage(NotificationFilter.All, PageRequest(null, 10))
        assertEquals(BankError.NoInternet, result.exceptionOrNull())
    }
}
