package com.example.m3showcase.feature.showcase.presentation.sections

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.VerticalFloatingToolbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** M3 Expressive Components Showcase. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ComponentsSection(onDemoClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader("NEW & EXPRESSIVE COMPONENTS", "Interactive gallery of M3 component specs")

        DemoCard("1. App bars (Top & Bottom)") {
            AppBarsDemo(onDemoClick)
        }

        DemoCard("2. Button groups (Connected & Single/Multi-select)") {
            ConnectedGroupDemo()
        }

        DemoCard("3. Common buttons (All shapes & sizes)") {
            CommonButtonsDemo(onDemoClick)
        }

        DemoCard("4. Extended FAB & FAB Menu") {
            FabMenuDemo(onDemoClick)
        }

        DemoCard("5. Floating Action Buttons (FAB, Small, Large)") {
            FabsDemo(onDemoClick)
        }

        DemoCard("6. Icon buttons (Filled, Tonal, Outlined, Standard)") {
            IconButtonsDemo(onDemoClick)
        }

        DemoCard("7. Loading indicators (Morphing & Contained)") {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LoadingIndicator(Modifier.size(56.dp))
                    Text("Morphing Loader", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ContainedLoadingIndicator(Modifier.size(56.dp))
                    Text("Contained Loader", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        DemoCard("8. Navigation bar & Navigation rail") {
            NavBarsDemo()
        }

        DemoCard("9. Progress indicators (Wavy & Standard)") {
            ProgressIndicatorsDemo()
        }

        DemoCard("10. Sliders (Standard & Range)") {
            SlidersDemo()
        }

        DemoCard("11. Split buttons (Leading + Trailing)") {
            SplitButtonDemo(onDemoClick)
        }

        DemoCard("12. Floating Toolbars (Horizontal & Vertical)") {
            FloatingToolbarsDemo(onDemoClick)
        }
    }
}

@Composable
private fun DemoCard(title: String, content: @Composable () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBarsDemo(onDemoClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CenterAlignedTopAppBar(
            title = { Text("Center Aligned App Bar") },
            navigationIcon = {
                IconButton(onClick = { onDemoClick("App Bar Menu") }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu")
                }
            },
            actions = {
                IconButton(onClick = { onDemoClick("App Bar Action") }) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
            }
        )

        BottomAppBar(
            actions = {
                IconButton(onClick = { onDemoClick("Bottom Bar Home") }) { Icon(Icons.Filled.Home, "Home") }
                IconButton(onClick = { onDemoClick("Bottom Bar Search") }) { Icon(Icons.Filled.Search, "Search") }
                IconButton(onClick = { onDemoClick("Bottom Bar Person") }) { Icon(Icons.Filled.Person, "Person") }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { onDemoClick("Bottom Bar FAB") }) {
                    Icon(Icons.Filled.Add, "Add")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ConnectedGroupDemo() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Connected Single Select:", style = MaterialTheme.typography.labelMedium)
        val options = listOf("Day", "Week", "Month", "Year")
        var selected by rememberSaveable { mutableIntStateOf(0) }
        Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
            options.forEachIndexed { index, label ->
                ToggleButton(
                    checked = selected == index,
                    onCheckedChange = { selected = index },
                    modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    }
                ) { Text(label) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
private fun CommonButtonsDemo(onDemoClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Button Variants:", style = MaterialTheme.typography.labelMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { onDemoClick("Filled Button") }, shapes = ButtonDefaults.shapes()) { Text("Filled") }
            ElevatedButton(onClick = { onDemoClick("Elevated Button") }, shapes = ButtonDefaults.shapes()) { Text("Elevated") }
            FilledTonalButton(onClick = { onDemoClick("Tonal Button") }, shapes = ButtonDefaults.shapes()) { Text("Tonal") }
            OutlinedButton(onClick = { onDemoClick("Outlined Button") }, shapes = ButtonDefaults.shapes()) { Text("Outlined") }
            TextButton(onClick = { onDemoClick("Text Button") }, shapes = ButtonDefaults.shapes()) { Text("Text") }
        }

        Text("Button Sizes (XS, S, M, L, XL):", style = MaterialTheme.typography.labelMedium)
        val sizes: List<Pair<String, Dp>> = listOf(
            "XS" to ButtonDefaults.ExtraSmallContainerHeight,
            "Small" to ButtonDefaults.MinHeight,
            "Medium" to ButtonDefaults.MediumContainerHeight,
            "Large" to ButtonDefaults.LargeContainerHeight,
            "XL" to ButtonDefaults.ExtraLargeContainerHeight
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sizes.forEach { (label, height) ->
                Button(
                    onClick = { onDemoClick("$label Size Button") },
                    modifier = Modifier.heightIn(height),
                    contentPadding = ButtonDefaults.contentPaddingFor(height),
                    shapes = ButtonDefaults.shapes()
                ) { Text(label, style = ButtonDefaults.textStyleFor(height)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FabMenuDemo(onDemoClick: (String) -> Unit) {
    var fabExpanded by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ExtendedFloatingActionButton(
            onClick = { onDemoClick("Extended FAB Clicked") },
            icon = { Icon(Icons.Filled.Navigation, contentDescription = null) },
            text = { Text("Extended FAB") }
        )

        Text("Floating Action Button Menu:", style = MaterialTheme.typography.labelMedium)
        FloatingActionButtonMenu(
            expanded = fabExpanded,
            button = {
                ToggleFloatingActionButton(
                    checked = fabExpanded,
                    onCheckedChange = { fabExpanded = it }
                ) {
                    Icon(if (fabExpanded) Icons.Filled.Add else Icons.Filled.Menu, contentDescription = "Menu")
                }
            }
        ) {
            FloatingActionButtonMenuItem(
                onClick = { fabExpanded = false; onDemoClick("FAB Item 1") },
                icon = { Icon(Icons.Filled.Star, contentDescription = null) },
                text = { Text("Favorite") }
            )
            FloatingActionButtonMenuItem(
                onClick = { fabExpanded = false; onDemoClick("FAB Item 2") },
                icon = { Icon(Icons.Filled.Share, contentDescription = null) },
                text = { Text("Share") }
            )
        }
    }
}

@Composable
private fun FabsDemo(onDemoClick: (String) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SmallFloatingActionButton(onClick = { onDemoClick("Small FAB") }) { Icon(Icons.Filled.Add, "Small FAB") }
        FloatingActionButton(onClick = { onDemoClick("Standard FAB") }) { Icon(Icons.Filled.Add, "Standard FAB") }
        LargeFloatingActionButton(onClick = { onDemoClick("Large FAB") }) { Icon(Icons.Filled.Add, "Large FAB") }
    }
}

@Composable
private fun IconButtonsDemo(onDemoClick: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconButton(onClick = { onDemoClick("Standard Icon Button") }) { Icon(Icons.Filled.Favorite, "Favorite") }
        FilledIconButton(onClick = { onDemoClick("Filled Icon Button") }) { Icon(Icons.Filled.Favorite, "Favorite") }
        FilledTonalIconButton(onClick = { onDemoClick("Tonal Icon Button") }) { Icon(Icons.Filled.Favorite, "Favorite") }
        OutlinedIconButton(onClick = { onDemoClick("Outlined Icon Button") }) { Icon(Icons.Filled.Favorite, "Favorite") }
    }
}

@Composable
private fun NavBarsDemo() {
    var selectedTab by remember { mutableIntStateOf(0) }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Navigation Bar:", style = MaterialTheme.typography.labelMedium)
        NavigationBar {
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                label = { Text("Home") }
            )
            NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Icon(Icons.Filled.Search, contentDescription = null) },
                label = { Text("Search") }
            )
            NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                label = { Text("Profile") }
            )
        }

        Text("Navigation Rail:", style = MaterialTheme.typography.labelMedium)
        Row {
            NavigationRail {
                NavigationRailItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Home") }
                )
                NavigationRailItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    label = { Text("Search") }
                )
            }
            Spacer(Modifier.width(16.dp))
            Text("Content beside rail", modifier = Modifier.align(Alignment.CenterVertically))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProgressIndicatorsDemo() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Wavy Progress Indicators:", style = MaterialTheme.typography.labelMedium)
        LinearWavyProgressIndicator(progress = { 0.7f }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularWavyProgressIndicator(progress = { 0.75f })
            CircularProgressIndicator(progress = { 0.5f })
        }
        Text("Standard Progress Indicator:", style = MaterialTheme.typography.labelMedium)
        LinearProgressIndicator(progress = { 0.4f }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun SlidersDemo() {
    var sliderVal by remember { mutableFloatStateOf(0.5f) }
    var rangeVal by remember { mutableStateOf(0.2f..0.8f) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Standard Slider: ${(sliderVal * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
        Slider(value = sliderVal, onValueChange = { sliderVal = it })

        Text("Range Slider: ${(rangeVal.start * 100).toInt()}% - ${(rangeVal.endInclusive * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
        RangeSlider(value = rangeVal, onValueChange = { rangeVal = it })
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SplitButtonDemo(onDemoClick: (String) -> Unit) {
    var checked by remember { mutableStateOf(false) }
    SplitButtonLayout(
        leadingButton = {
            SplitButtonDefaults.LeadingButton(onClick = { onDemoClick("Primary Action") }) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(SplitButtonDefaults.LeadingIconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Edit")
            }
        },
        trailingButton = {
            SplitButtonDefaults.TrailingButton(
                checked = checked,
                onCheckedChange = { checked = it },
                modifier = Modifier.semantics { contentDescription = "More options" }
            ) { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FloatingToolbarsDemo(onDemoClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Horizontal Floating Toolbar:", style = MaterialTheme.typography.labelMedium)
        HorizontalFloatingToolbar(
            expanded = true,
            leadingContent = {
                IconButton(onClick = { onDemoClick("Edit Action") }) { Icon(Icons.Filled.Edit, "Edit") }
            },
            trailingContent = {
                IconButton(onClick = { onDemoClick("More Action") }) { Icon(Icons.Filled.MoreVert, "More") }
            },
            content = {
                IconButton(onClick = { onDemoClick("Favorite Action") }) { Icon(Icons.Filled.Favorite, "Favorite") }
                IconButton(onClick = { onDemoClick("Share Action") }) { Icon(Icons.Filled.Share, "Share") }
            }
        )

        Spacer(Modifier.height(8.dp))
        Text("Vertical Floating Toolbar:", style = MaterialTheme.typography.labelMedium)
        VerticalFloatingToolbar(
            expanded = true,
            content = {
                IconButton(onClick = { onDemoClick("Tool 1") }) { Icon(Icons.Filled.Star, "Tool 1") }
                IconButton(onClick = { onDemoClick("Tool 2") }) { Icon(Icons.Filled.Check, "Tool 2") }
            }
        )
    }
}
