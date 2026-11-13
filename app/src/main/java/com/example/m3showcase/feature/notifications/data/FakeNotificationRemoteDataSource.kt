package com.example.m3showcase.feature.notifications.data

import com.example.m3showcase.core.common.DispatcherProvider
import com.example.m3showcase.core.connectivity.NetworkMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Wire format, as a REST API would send it. */
data class NotificationDto(
    val id: String,
    val title: String,
    val body: String,
    val category: String,
    val createdAt: Long,
    val read: Boolean,
)

data class NotificationPageDto(
    val data: List<NotificationDto>,
    /** `createdAt` of the last item; pass it as `before` for the next page. null on the last page. */
    val nextBefore: Long?,
    val total: Int,
)

/**
 * Stand-in for `GET /notifications?before=&limit=&filter=`, newest first, with network latency
 * and the offline check.
 *
 * Keyset paging ("items older than X") instead of offset paging: when an item is marked read
 * under the Unread filter, or a new one arrives at the top, offsets shift and a page would
 * skip or repeat rows. A timestamp cursor stays correct. Timestamps are unique in this inbox.
 */
class FakeNotificationRemoteDataSource(
    private val networkMonitor: NetworkMonitor,
    private val dispatchers: DispatcherProvider,
) {
    private val mutex = Mutex()
    private val inbox: MutableList<NotificationDto> = seed(System.currentTimeMillis())

    suspend fun getNotifications(before: Long?, limit: Int, filter: String): NotificationPageDto = request {
        val matching = inbox.filter { it.matches(filter) }.sortedByDescending { it.createdAt }
        val older = if (before == null) matching else matching.filter { it.createdAt < before }
        val pageItems = older.take(limit)
        val hasMore = older.size > pageItems.size
        NotificationPageDto(pageItems, nextBefore = pageItems.lastOrNull()?.createdAt?.takeIf { hasMore }, total = matching.size)
    }

    suspend fun markRead(id: String): Unit = request(latencyMs = 250) {
        val i = inbox.indexOfFirst { it.id == id }
        if (i >= 0) inbox[i] = inbox[i].copy(read = true)
    }

    suspend fun markAllRead(): Unit = request(latencyMs = 400) {
        inbox.replaceAll { it.copy(read = true) }
    }

    private fun NotificationDto.matches(filter: String) = when (filter) {
        "UNREAD" -> !read
        "TRANSACTION", "SECURITY", "PROMOTION" -> category == filter
        else -> true
    }

    private suspend fun <T> request(latencyMs: Long = 800, block: () -> T): T = withContext(dispatchers.io) {
        if (!networkMonitor.isOnline) throw IOException("Unable to resolve host")
        delay(latencyMs)
        if (!networkMonitor.isOnline) throw IOException("Connection reset")
        mutex.withLock { block() }
    }

    companion object {
        const val SEED_COUNT = 64

        /** Deterministic inbox: one notification every ~11 hours, the newest 6 unread. */
        internal fun seed(now: Long): MutableList<NotificationDto> {
            val names = listOf("Chan Sopheap", "Lim Vanna", "Keo Ratana", "Meas Sreyneang", "ACME Co., Ltd.")
            val shops = listOf("Brown Coffee BKK1", "Phsar Thmey Shop", "Smart Axiata", "EDC", "Lucky Supermarket")
            return MutableList(SEED_COUNT) { i ->
                val createdAt = now - TimeUnit.MINUTES.toMillis(25L + i * 660L)
                val amount = "%,.2f".format(5 + (i * 37 % 480) + (i % 4) * 0.25)
                val (category, title, body) = when (i % 7) {
                    0, 3 -> Triple("TRANSACTION", "Money received", "You received \$$amount from ${names[i % names.size]} to Savings USD.")
                    1, 4 -> Triple("TRANSACTION", "Payment successful", "You paid \$$amount to ${shops[i % shops.size]} with KHQR.")
                    2 -> Triple("SECURITY", "New sign-in", "Your account was opened on a new device. If this wasn't you, lock your account in Settings.")
                    5 -> Triple("PROMOTION", "Cashback weekend", "Get 5% cashback on QR payments at partner cafés this weekend. Up to \$10 per account.")
                    else -> Triple("SYSTEM", "Scheduled maintenance", "Some services may be briefly unavailable on Sunday from 1:00 to 3:00 AM.")
                }
                NotificationDto(id = "ntf-%03d".format(i), title = title, body = body, category = category, createdAt = createdAt, read = i >= 6)
            }
        }
    }
}
