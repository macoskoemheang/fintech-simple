package com.example.m3showcase.core.domain.model

/**
 * What to load next. [cursor] is opaque to everyone except the data source that issued it:
 * an offset, a page number or a server token all fit. null means "first page".
 */
data class PageRequest(val cursor: String?, val size: Int)

/** One page of results. [nextCursor] == null means there is nothing after this page. */
data class Page<T>(
    val items: List<T>,
    val nextCursor: String?,
    /** Total matching items on the server, when the API reports it. */
    val totalCount: Int? = null,
) {
    val isLast: Boolean get() = nextCursor == null
}
