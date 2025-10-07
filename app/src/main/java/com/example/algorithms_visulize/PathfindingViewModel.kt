package com.example.algorithms_visulize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.ArrayDeque

enum class NodeType { Normal, Start, End, Barrier }
enum class NodeVis { None, Visited, Path }

data class Node(val row: Int, val col: Int, val type: NodeType = NodeType.Normal, val vis: NodeVis = NodeVis.None)

enum class GridMode { SetStart, SetEnd, DrawBarrier, None }

class PathfindingViewModel : ViewModel() {
    private val rows = 10
    private val cols = 10

    private val _grid: MutableStateFlow<List<List<Node>>> = MutableStateFlow(generateGrid())
    val grid: StateFlow<List<List<Node>>> = _grid.asStateFlow()

    private val _mode: MutableStateFlow<GridMode> = MutableStateFlow(GridMode.None)
    val mode: StateFlow<GridMode> = _mode.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private fun generateGrid(): List<List<Node>> = List(rows) { r -> List(cols) { c -> Node(r, c) } }

    fun setMode(mode: GridMode) { _mode.value = mode }

    fun clear() {
        if (_isRunning.value) return
        _grid.value = generateGrid()
        _status.value = ""
        _logs.value = emptyList()
    }

    fun onCellTapped(row: Int, col: Int) {
        if (_isRunning.value) return
        val current = _grid.value.map { it.toMutableList() }.toMutableList()
        when (_mode.value) {
            GridMode.SetStart -> {
                for (r in 0 until rows) for (c in 0 until cols) if (current[r][c].type == NodeType.Start) current[r][c] = current[r][c].copy(type = NodeType.Normal)
                current[row][col] = current[row][col].copy(type = NodeType.Start, vis = NodeVis.None)
            }
            GridMode.SetEnd -> {
                for (r in 0 until rows) for (c in 0 until cols) if (current[r][c].type == NodeType.End) current[r][c] = current[r][c].copy(type = NodeType.Normal)
                current[row][col] = current[row][col].copy(type = NodeType.End, vis = NodeVis.None)
            }
            GridMode.DrawBarrier -> {
                val target = current[row][col]
                val newType = if (target.type == NodeType.Barrier) NodeType.Normal else NodeType.Barrier
                current[row][col] = target.copy(type = newType, vis = NodeVis.None)
            }
            GridMode.None -> { /* no-op */ }
        }
        _grid.value = current.map { it.toList() }
    }

    fun runAlgorithm(name: String, stepDelayMs: Long = 80L) {
        if (_isRunning.value) return
        viewModelScope.launch {
            _isRunning.value = true
            clearVisitedOnly()
            _status.value = "Starting $name..."
            appendLog(_status.value)
            val gridLocal = _grid.value
            val hasStart = gridLocal.flatten().any { it.type == NodeType.Start }
            val hasEnd = gridLocal.flatten().any { it.type == NodeType.End }
            if (!hasStart || !hasEnd) {
                _status.value = if (!hasStart && !hasEnd) "Place a Start and an End first" else if (!hasStart) "Place a Start first" else "Place an End first"
                appendLog(_status.value)
                _isRunning.value = false
                return@launch
            }
            when (name) {
                "BFS" -> bfs(stepDelayMs)
                "DFS" -> dfs(stepDelayMs)
                "Dijkstra's" -> dijkstra(stepDelayMs)
            }
            _isRunning.value = false
        }
    }

    private suspend fun dfs(stepDelay: Long) {
        val gridLocal = _grid.value
        val start = gridLocal.flatten().firstOrNull { it.type == NodeType.Start } ?: return
        val end = gridLocal.flatten().firstOrNull { it.type == NodeType.End } ?: return

        val dirs = arrayOf(intArrayOf(1,0), intArrayOf(-1,0), intArrayOf(0,1), intArrayOf(0,-1))
        val visited = Array(rows) { BooleanArray(cols) }
        val parent = Array(rows) { Array(cols) { -1 to -1 } }

        fun neighbors(r: Int, c: Int): List<Pair<Int,Int>> {
            val res = ArrayList<Pair<Int,Int>>(4)
            for (d in dirs) {
                val nr = r + d[0]
                val nc = c + d[1]
                if (nr in 0 until rows && nc in 0 until cols) res.add(nr to nc)
            }
            return res
        }

        var found = false
        suspend fun dfsVisit(r: Int, c: Int) {
            if (found) return
            if (r == end.row && c == end.col) { found = true; return }
            visited[r][c] = true
            if (!((r == start.row && c == start.col) || (r == end.row && c == end.col))) {
                markVisited(r, c)
                delay(stepDelay)
            }
            for ((nr, nc) in neighbors(r, c)) {
                if (!visited[nr][nc]) {
                    val n = _grid.value[nr][nc]
                    if (n.type != NodeType.Barrier) {
                        parent[nr][nc] = r to c
                        dfsVisit(nr, nc)
                        if (found) return
                    }
                }
            }
        }

        _status.value = "DFS: exploring..."
        appendLog(_status.value)
        dfsVisit(start.row, start.col)

        if (found) {
            _status.value = "End found. Backtracking path..."
            appendLog(_status.value)
            var r = end.row
            var c = end.col
            val path = mutableListOf<Pair<Int,Int>>()
            while (!(r == start.row && c == start.col)) {
                path.add(r to c)
                val p = parent[r][c]
                if (p.first == -1) break
                r = p.first
                c = p.second
            }
            path.reverse()
            for ((pr, pc) in path) {
                if (!(pr == start.row && pc == start.col) && !(pr == end.row && pc == end.col)) {
                    markPath(pr, pc)
                    delay(stepDelay)
                    appendLog("Path cell[" + pr + "][" + pc + "]")
                }
            }
            _status.value = "Completed. Path length: " + path.size
            appendLog(_status.value)
        } else {
            _status.value = "No path found."
            appendLog(_status.value)
        }
    }

    private suspend fun dijkstra(stepDelay: Long) {
        val gridLocal = _grid.value
        val start = gridLocal.flatten().firstOrNull { it.type == NodeType.Start } ?: return
        val end = gridLocal.flatten().firstOrNull { it.type == NodeType.End } ?: return

        val dirs = arrayOf(intArrayOf(1,0), intArrayOf(-1,0), intArrayOf(0,1), intArrayOf(0,-1))
        val dist = Array(rows) { IntArray(cols) { Int.MAX_VALUE } }
        val parent = Array(rows) { Array(cols) { -1 to -1 } }
        val visited = Array(rows) { BooleanArray(cols) }

        data class Cell(val r: Int, val c: Int, val d: Int)
        val cmp = Comparator<Cell> { a, b -> a.d - b.d }
        val pq = java.util.PriorityQueue(cmp)

        dist[start.row][start.col] = 0
        pq.add(Cell(start.row, start.col, 0))
        _status.value = "Dijkstra: starting..."
        appendLog(_status.value)

        while (pq.isNotEmpty()) {
            val cur = pq.poll()
            if (visited[cur.r][cur.c]) continue
            visited[cur.r][cur.c] = true
            if (!((cur.r == start.row && cur.c == start.col) || (cur.r == end.row && cur.c == end.col))) {
                markVisited(cur.r, cur.c)
                delay(stepDelay)
            }
            if (cur.r == end.row && cur.c == end.col) break
            for (d in dirs) {
                val nr = cur.r + d[0]
                val nc = cur.c + d[1]
                if (nr in 0 until rows && nc in 0 until cols) {
                    val n = _grid.value[nr][nc]
                    if (n.type != NodeType.Barrier && !visited[nr][nc]) {
                        val nd = dist[cur.r][cur.c] + 1 // uniform cost 1 per move
                        if (nd < dist[nr][nc]) {
                            dist[nr][nc] = nd
                            parent[nr][nc] = cur.r to cur.c
                            pq.add(Cell(nr, nc, nd))
                            appendLog("Relaxed cell[" + nr + "][" + nc + "] with distance " + nd)
                        }
                    }
                }
            }
        }

        if (visited[end.row][end.col]) {
            _status.value = "End found. Backtracking path..."
            appendLog(_status.value)
            var r = end.row
            var c = end.col
            val path = mutableListOf<Pair<Int,Int>>()
            while (!(r == start.row && c == start.col)) {
                path.add(r to c)
                val p = parent[r][c]
                if (p.first == -1) break
                r = p.first
                c = p.second
            }
            path.reverse()
            for ((pr, pc) in path) {
                if (!(pr == start.row && pc == start.col) && !(pr == end.row && pc == end.col)) {
                    markPath(pr, pc)
                    delay(stepDelay)
                    appendLog("Path cell[" + pr + "][" + pc + "]")
                }
            }
            _status.value = "Completed. Path length: " + path.size
            appendLog(_status.value)
        } else {
            _status.value = "No path found."
            appendLog(_status.value)
        }
    }

    private fun clearVisitedOnly() {
        val current = _grid.value.map { it.toMutableList() }.toMutableList()
        for (r in 0 until rows) for (c in 0 until cols) {
            val n = current[r][c]
            if (n.vis != NodeVis.None && n.type == NodeType.Normal) current[r][c] = n.copy(vis = NodeVis.None)
            if (n.type != NodeType.Normal && n.vis != NodeVis.None) current[r][c] = n.copy(vis = NodeVis.None)
        }
        _grid.value = current.map { it.toList() }
    }

    private suspend fun bfs(stepDelay: Long) {
        val gridLocal = _grid.value
        val start = gridLocal.flatten().firstOrNull { it.type == NodeType.Start } ?: return
        val end = gridLocal.flatten().firstOrNull { it.type == NodeType.End } ?: return

        val dirs = arrayOf(intArrayOf(1,0), intArrayOf(-1,0), intArrayOf(0,1), intArrayOf(0,-1))
        val visited = Array(rows) { BooleanArray(cols) }
        val parent = Array(rows) { Array(cols) { -1 to -1 } }
        val queue: ArrayDeque<Node> = ArrayDeque()

        queue.add(start)
        visited[start.row][start.col] = true
        _status.value = "BFS: enqueued start cell[" + start.row + "][" + start.col + "]"
        appendLog(_status.value)

        var found = false
        while (queue.isNotEmpty()) {
            val cur = queue.removeFirst()
            _status.value = "Visiting cell[" + cur.row + "][" + cur.col + "]"
            appendLog(_status.value)
            if (cur.type != NodeType.Start && cur.type != NodeType.End) markVisited(cur.row, cur.col)
            delay(stepDelay)
            if (cur.row == end.row && cur.col == end.col) { found = true; break }

            for (d in dirs) {
                val nr = cur.row + d[0]
                val nc = cur.col + d[1]
                if (nr in 0 until rows && nc in 0 until cols && !visited[nr][nc]) {
                    val n = _grid.value[nr][nc]
                    if (n.type != NodeType.Barrier) {
                        visited[nr][nc] = true
                        parent[nr][nc] = cur.row to cur.col
                        queue.add(n)
                        _status.value = "Queued cell[" + n.row + "][" + n.col + "]"
                        appendLog(_status.value)
                    }
                }
            }
        }

        if (found) {
            _status.value = "End found. Backtracking path..."
            appendLog(_status.value)
            // backtrack
            var r = end.row
            var c = end.col
            val path = mutableListOf<Pair<Int,Int>>()
            while (!(r == start.row && c == start.col)) {
                path.add(r to c)
                val p = parent[r][c]
                if (p.first == -1) break
                r = p.first
                c = p.second
            }
            path.reverse()
            for ((pr, pc) in path) {
                if (!(pr == start.row && pc == start.col) && !(pr == end.row && pc == end.col)) {
                    markPath(pr, pc)
                    delay(stepDelay)
                    _status.value = "Path cell[" + pr + "][" + pc + "]"
                    appendLog(_status.value)
                }
            }
            _status.value = "Completed. Path length: ${path.size}"
            appendLog(_status.value)
        } else {
            _status.value = "No path found."
            appendLog(_status.value)
        }
    }

    private fun markVisited(row: Int, col: Int) {
        val current = _grid.value.map { it.toMutableList() }.toMutableList()
        val n = current[row][col]
        current[row][col] = n.copy(vis = NodeVis.Visited)
        _grid.value = current.map { it.toList() }
    }

    private fun markPath(row: Int, col: Int) {
        val current = _grid.value.map { it.toMutableList() }.toMutableList()
        val n = current[row][col]
        current[row][col] = n.copy(vis = NodeVis.Path)
        _grid.value = current.map { it.toList() }
    }

    private fun appendLog(message: String) {
        val updated = _logs.value.toMutableList()
        updated.add(0, message)
        _logs.value = updated
    }
}


