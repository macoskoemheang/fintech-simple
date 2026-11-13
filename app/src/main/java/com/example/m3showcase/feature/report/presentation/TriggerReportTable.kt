package com.example.m3showcase.feature.report.presentation

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.m3showcase.feature.report.domain.model.TriggerRow
import androidx.compose.ui.res.stringResource
import com.example.m3showcase.R

private val NoWidth = 44.dp
private val TriggerTypeWidth = 170.dp
private val TotalWidth = 56.dp
private val DayWidth = 40.dp
private val RowHeight = 44.dp

/**
 * Report table where "No." and "Trigger Type" stay fixed on the left while
 * "Total" and the day columns scroll horizontally. Rows scroll vertically.
 *
 * Every row uses the same [ScrollState], so the header, the body rows and the
 * footer always scroll together.
 */
@Composable
fun TriggerReportTable(
    rows: List<TriggerRow>,
    modifier: Modifier = Modifier,
    dayCount: Int = rows.firstOrNull()?.dailyCounts?.size ?: 30,
    /** Header text for 1-based day column [day]; defaults to the number itself. */
    dayLabel: (day: Int) -> String = { it.toString() },
    onCountClick: (row: TriggerRow, day: Int) -> Unit = { _, _ -> },
) {
    val horizontalScroll = rememberScrollState()
    val colors = MaterialTheme.colorScheme
    val dayTotals = List(dayCount) { day -> rows.sumOf { it.dailyCounts.getOrElse(day) { 0 } } }

    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(12.dp))
            .background(colors.surface)
    ) {
        // Header
        TableRow(
            scrollState = horizontalScroll,
            background = colors.surfaceContainer,
            fixed = {
                HeaderCell(stringResource(R.string.report_no), NoWidth)
                HeaderCell(stringResource(R.string.report_trigger_type), TriggerTypeWidth)
            },
            scrollable = {
                HeaderCell(stringResource(R.string.report_total), TotalWidth, center = true)
                for (day in 1..dayCount) HeaderCell(dayLabel(day), DayWidth, center = true)
            },
        )
        HorizontalDivider(color = colors.outlineVariant)

        // Body
        LazyColumn(Modifier.weight(1f, fill = false)) {
            itemsIndexed(rows, key = { _, row -> row.no }) { index, row ->
                TableRow(
                    scrollState = horizontalScroll,
                    background = colors.surface,
                    fixed = {
                        BodyCell("${row.no}", NoWidth)
                        BodyCell(row.triggerType, TriggerTypeWidth)
                    },
                    scrollable = {
                        BodyCell("${row.total}", TotalWidth, center = true, bold = true)
                        for (day in 0 until dayCount) {
                            val count = row.dailyCounts.getOrElse(day) { 0 }
                            CountCell(count) { onCountClick(row, day + 1) }
                        }
                    },
                )
                if (index < rows.lastIndex) {
                    HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }

        // Footer
        HorizontalDivider(color = colors.outlineVariant)
        TableRow(
            scrollState = horizontalScroll,
            background = colors.surfaceContainer,
            fixed = {
                BodyCell("", NoWidth)
                BodyCell(stringResource(R.string.report_total), TriggerTypeWidth, bold = true)
            },
            scrollable = {
                BodyCell("${rows.sumOf { it.total }}", TotalWidth, center = true, bold = true)
                dayTotals.forEach { BodyCell("$it", DayWidth, center = true, bold = true) }
            },
        )
    }
}

/** One row = fixed part | divider | horizontally scrolling part. */
@Composable
private fun TableRow(
    scrollState: ScrollState,
    background: Color,
    fixed: @Composable () -> Unit,
    scrollable: @Composable () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(RowHeight)
            .background(background)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) { fixed() }
        VerticalDivider(Modifier.fillMaxHeight(), color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            Modifier
                .weight(1f)
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically,
        ) { scrollable() }
    }
}

@Composable
private fun HeaderCell(text: String, width: Dp, center: Boolean = false) {
    CellBox(width, center) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BodyCell(text: String, width: Dp, center: Boolean = false, bold: Boolean = false) {
    CellBox(width, center) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Non-zero counts are blue, underlined and clickable, like links. */
@Composable
private fun CountCell(count: Int, onClick: () -> Unit) {
    val clickable = count > 0
    Box(
        Modifier
            .width(DayWidth)
            .fillMaxHeight()
            .then(if (clickable) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$count",
            fontSize = 12.sp,
            color = if (clickable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textDecoration = if (clickable) TextDecoration.Underline else null,
        )
    }
}

@Composable
private fun CellBox(width: Dp, center: Boolean, content: @Composable () -> Unit) {
    Box(
        Modifier
            .width(width)
            .fillMaxHeight()
            .padding(horizontal = 8.dp),
        contentAlignment = if (center) Alignment.Center else Alignment.CenterStart,
    ) { content() }
}
