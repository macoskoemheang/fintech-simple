package com.example.m3showcase.feature.report.presentation

import com.example.m3showcase.core.designsystem.bankTopAppBarColors
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Screen shown for the QR (Receive) bottom navigation tab. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerReportRoute() {
    val rows = remember { sampleTriggerRows() }
    Scaffold(
        containerColor = Color.Transparent, // AppBackground shows through.
        topBar = { CenterAlignedTopAppBar(colors = bankTopAppBarColors(), title = { Text("Trigger Report") }) }) { padding ->
        TriggerReportTable(
            rows = rows,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
        )
    }
}
