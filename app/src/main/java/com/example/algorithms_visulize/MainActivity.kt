package com.example.algorithms_visulize

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                AppNavHost()
            }
        }
    }
}

object Routes {
    const val HOME = "home"
    const val SORTING = "sorting"
    const val PATHFINDING = "pathfinding"
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen(
            onNavigateSorting = { navController.navigate(Routes.SORTING) },
            onNavigatePathfinding = { navController.navigate(Routes.PATHFINDING) }
        ) }
        composable(Routes.SORTING) { SortingScreen() }
        composable(Routes.PATHFINDING) { PathfindingScreen() }
    }
}

@Composable
fun HomeScreen(
    onNavigateSorting: () -> Unit,
    onNavigatePathfinding: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Algorithm Visualizer",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold
        )

        MenuCard(
            title = "Sorting Algorithms",
            subtitle = "Bubble, Selection, Insertion, Merge, Quick",
            onClick = onNavigateSorting
        )
        MenuCard(
            title = "Pathfinding Algorithms",
            subtitle = "BFS, DFS, Dijkstra",
            onClick = onNavigatePathfinding
        )
        MenuCard(title = "Data Structures", onClick = { /* placeholder */ })
    }
}

@Composable
private fun MenuCard(title: String, subtitle: String? = null, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .height(140.dp),
        content = {
            Column(
                modifier = Modifier.padding(PaddingValues(20.dp)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

@Composable
fun SortingScreen(vm: SortingViewModel = viewModel()) {
    SortingScreenContent(vm)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortingScreenContent(vm: SortingViewModel) {
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    val numberRegex = remember { Regex("^[0-9,\\s]*$") }
    val barsState by vm.bars.collectAsState()
    val logs by vm.logs.collectAsState()

    val algorithms = listOf("Bubble Sort", "Selection Sort", "Insertion Sort", "Merge Sort", "Quick Sort")
    var algoExpanded by remember { mutableStateOf(false) }
    var selectedAlgorithm by remember { mutableStateOf(algorithms.first()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(title = "Sorting Algorithms")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = inputText,
                onValueChange = { value ->
                    if (numberRegex.matches(value.text)) inputText = value
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text("e.g. 5,2,9,1,7") },
                singleLine = true
            )
            Button(onClick = {
                val vals = inputText.text.split(',')
                    .mapNotNull { it.trim().takeIf { s -> s.isNotEmpty() }?.toIntOrNull() }
                vm.setInput(vals)
            }) {
                Text("Generate Array")
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                TextField(
                    value = selectedAlgorithm,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        Button(onClick = { algoExpanded = !algoExpanded }) { Text(if (algoExpanded) "▲" else "▼") }
                    }
                )
                DropdownMenu(expanded = algoExpanded, onDismissRequest = { algoExpanded = false }) {
                    algorithms.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = {
                            selectedAlgorithm = option
                            algoExpanded = false
                        })
                    }
                }
            }
            Button(onClick = {
                if (barsState.isEmpty()) {
                    val vals = inputText.text.split(',')
                        .mapNotNull { it.trim().takeIf { s -> s.isNotEmpty() }?.toIntOrNull() }
                    if (vals.isNotEmpty()) vm.setInput(vals)
                }
                when (selectedAlgorithm) {
                    "Bubble Sort" -> vm.startBubbleSort()
                    "Selection Sort" -> vm.startSelectionSort()
                    "Insertion Sort" -> vm.startInsertionSort()
                    "Merge Sort" -> vm.startMergeSort()
                    "Quick Sort" -> vm.startQuickSort()
                    else -> vm.postLog("${'$'}selectedAlgorithm not implemented yet")
                }
            }) {
                Text("Start Visualization")
            }
        }

        VisualizationBarsBars(bars = barsState)

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item { ColorLegend() }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { Text(text = "Descriptions", style = MaterialTheme.typography.titleMedium) }
            items(logs.size) { idx ->
                val msg = logs[idx]
                Text(text = msg, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun VisualizationBarsBars(bars: List<Bar>) {
    val values = bars.map { it.value }
    val maxValue = (values.maxOrNull() ?: 0).coerceAtLeast(1)
    val minBarHeight = 8.dp
    val chartHeight = 240.dp

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Array Visualization", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        if (bars.isEmpty()) {
            Text(text = "Enter numbers and tap Generate Array", style = MaterialTheme.typography.bodyMedium)
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                bars.forEach { b ->
                    val ratio = b.value.toFloat() / maxValue.toFloat()
                    val heightDp = (chartHeight * ratio) + minBarHeight
                    val color = when (b.state) {
                        BarState.Default -> MaterialTheme.colorScheme.primary
                        BarState.Comparing -> MaterialTheme.colorScheme.tertiary
                        BarState.Swapping -> MaterialTheme.colorScheme.error
                        BarState.Pivot -> MaterialTheme.colorScheme.secondary
                        BarState.Sorted -> MaterialTheme.colorScheme.secondaryContainer
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(heightDp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(color),
                            content = {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorLegend() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = "Legend", style = MaterialTheme.typography.titleSmall)
        LegendRow(color = MaterialTheme.colorScheme.primary, label = "Default")
        LegendRow(color = MaterialTheme.colorScheme.tertiary, label = "Comparing")
        LegendRow(color = MaterialTheme.colorScheme.error, label = "Swapping")
        LegendRow(color = MaterialTheme.colorScheme.secondary, label = "Pivot")
        LegendRow(color = MaterialTheme.colorScheme.secondaryContainer, label = "Sorted")
    }
}

@Composable
private fun LegendRow(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun PathfindingScreen(vm: PathfindingViewModel = viewModel()) {
    val grid by vm.grid.collectAsState()
    val status by vm.status.collectAsState()
    val modes = listOf("Set Start", "Set End", "Draw Barrier")
    var algoExpanded by remember { mutableStateOf(false) }
    val algos = listOf("BFS", "DFS", "Dijkstra's")
    var selectedAlgo by remember { mutableStateOf(algos.first()) }
    val logs by vm.logs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader(title = "Pathfinding Algorithms")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.setMode(GridMode.SetStart) }) { Text("Set Start") }
            Button(onClick = { vm.setMode(GridMode.SetEnd) }) { Text("Set End") }
            Button(onClick = { vm.setMode(GridMode.DrawBarrier) }) { Text("Draw Barrier") }
            Button(onClick = { vm.clear() }) { Text("Clear") }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                TextField(
                    value = selectedAlgo,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = { Button(onClick = { algoExpanded = !algoExpanded }) { Text(if (algoExpanded) "▲" else "▼") } }
                )
                DropdownMenu(expanded = algoExpanded, onDismissRequest = { algoExpanded = false }) {
                    algos.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = {
                            selectedAlgo = option
                            algoExpanded = false
                        })
                    }
                }
            }
            Button(onClick = { vm.runAlgorithm(selectedAlgo) }) { Text("Find Path") }
        }

        LazyVerticalGrid(columns = GridCells.Fixed(10), modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            itemsIndexed(grid.flatten()) { index, node ->
                val row = index / 10
                val col = index % 10
                val color = when (node.type) {
                    NodeType.Start -> Color.Blue
                    NodeType.End -> Color.Red
                    NodeType.Barrier -> Color.Black
                    NodeType.Normal -> when (node.vis) {
                        NodeVis.Path -> Color.Yellow
                        NodeVis.Visited -> Color(0xFFFFC0CB) // pink
                        NodeVis.None -> MaterialTheme.colorScheme.surfaceVariant
                    }
                }
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                        .clickable { vm.onCellTapped(row, col) }
                )
            }
        }

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item { PathColorLegend() }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { Text(text = "Descriptions", style = MaterialTheme.typography.titleMedium) }
            items(logs.size) { idx ->
                val msg = logs[idx]
                Text(text = msg, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ScreenHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PathColorLegend() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = "Legend", style = MaterialTheme.typography.titleSmall)
        LegendRow(color = Color.Blue, label = "Start")
        LegendRow(color = Color.Red, label = "End")
        LegendRow(color = Color.Black, label = "Barrier")
        LegendRow(color = Color(0xFFFFC0CB), label = "Visited")
        LegendRow(color = Color.Yellow, label = "Path")
        LegendRow(color = MaterialTheme.colorScheme.surfaceVariant, label = "Unvisited")
    }
}


