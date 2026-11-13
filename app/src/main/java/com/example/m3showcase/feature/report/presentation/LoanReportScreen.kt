package com.example.m3showcase.feature.report.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.R
import com.example.m3showcase.core.designsystem.component.BankScreenScaffold
import com.example.m3showcase.core.designsystem.component.LoadableContent
import com.example.m3showcase.core.designsystem.component.SummaryTile
import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.core.domain.model.plusDays
import com.example.m3showcase.core.ui.dateText
import com.example.m3showcase.feature.report.domain.model.LoanReport
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoanReportRoute(onBack: () -> Unit, viewModel: LoanReportViewModel = koinViewModel()) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LoanReportEvent.ShowCount -> snackbarHostState.showSnackbar(
                    context.getString(R.string.loan_report_count_on_day, event.count, event.triggerType, event.dayIndex + 1),
                )
                LoanReportEvent.NavigateBack -> onBack()
            }
        }
    }
    LoanReportScreen(state, snackbarHostState, viewModel::onAction)
}

@Composable
fun LoanReportScreen(state: LoanReportViewState, snackbarHostState: SnackbarHostState, onAction: (LoanReportViewAction) -> Unit) {
    BankScreenScaffold(
        title = stringResource(R.string.loan_report_title),
        onBack = { onAction(LoanReportViewAction.OnBackClicked) },
        snackbarHostState = snackbarHostState,
    ) { padding ->
        LoadableContent(state.content, onRetry = { onAction(LoanReportViewAction.OnRetry) }, modifier = Modifier.padding(padding)) { report ->
            LoanReportContent(report, state.selectedDay, onAction)
        }
    }
}

@Composable
private fun LoanReportContent(data: LoanReport, selectedDay: Int?, onAction: (LoanReportViewAction) -> Unit) {
    val summary = data.summary
    val start = data.report.periodStart
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.loan_report_period, dateText(start), dateText(start.plusDays(data.report.dayCount - 1L))),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryTile(stringResource(R.string.loan_report_total_alerts), summary.totalAlerts.toString(), Modifier.weight(1f))
            SummaryTile(stringResource(R.string.loan_report_trigger_types), summary.activeTriggerTypes.toString(), Modifier.weight(1f))
        }
        summary.topTrigger?.let {
            SummaryTile(stringResource(R.string.loan_report_top_trigger), "${it.triggerType} · ${it.total}", Modifier.fillMaxWidth())
        }
        DailyChart(summary.dailyTotals, start, selectedDay) { onAction(LoanReportViewAction.OnDaySelected(it)) }
        TriggerReportTable(
            rows = data.report.rows,
            dayLabel = { day -> start.plusDays(day - 1L).day.toString() },
            onCountClick = { row, day -> onAction(LoanReportViewAction.OnCountClicked(row, day)) },
            modifier = Modifier.weight(1f).padding(bottom = 12.dp),
        )
    }
}

/**
 * Alerts per day as rounded bars that grow in on first show. The busiest day starts selected;
 * tapping a bar selects it and the caption shows that day's total.
 */
@Composable
private fun DailyChart(totals: List<Int>, start: CalendarDate, selected: Int?, onSelect: (Int) -> Unit) {
    val grow = remember { Animatable(0f) }
    LaunchedEffect(Unit) { grow.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }
    val max = (totals.maxOrNull() ?: 0).coerceAtLeast(1)
    val bar = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val highlight = MaterialTheme.colorScheme.primary
    val chartDescription = stringResource(R.string.loan_report_chart_description)

    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.94f), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row {
                Text(stringResource(R.string.loan_report_daily_alerts), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                selected?.let {
                    Text(
                        stringResource(R.string.loan_report_day_total, dateText(start.plusDays(it.toLong())), totals[it]),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = highlight,
                    )
                }
            }
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .padding(top = 12.dp)
                    .semantics { contentDescription = chartDescription }
                    .pointerInput(totals.size) {
                        detectTapGestures { offset ->
                            val index = (offset.x / (size.width / totals.size.toFloat())).toInt().coerceIn(totals.indices)
                            onSelect(index)
                        }
                    },
            ) {
                if (totals.isEmpty()) return@Canvas
                val slot = size.width / totals.size
                val barWidth = slot * 0.62f
                totals.forEachIndexed { i, value ->
                    val h = size.height * (value / max.toFloat()) * grow.value
                    drawRoundRect(
                        color = if (i == selected) highlight else bar,
                        topLeft = Offset(i * slot + (slot - barWidth) / 2, size.height - h),
                        size = Size(barWidth, h.coerceAtLeast(2f)),
                        cornerRadius = CornerRadius(barWidth / 2),
                    )
                }
            }
        }
    }
}
