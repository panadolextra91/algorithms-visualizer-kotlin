package com.example.algorithms_visulize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class BarState {
    Default, Comparing, Swapping, Pivot, Sorted
}

data class Bar(val value: Int, val state: BarState = BarState.Default)

class SortingViewModel : ViewModel() {
    private val _bars = MutableStateFlow<List<Bar>>(emptyList())
    val bars: StateFlow<List<Bar>> = _bars.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    fun setInput(values: List<Int>) {
        if (_isRunning.value) return
        _bars.value = values.map { Bar(it) }
        _logs.value = emptyList()
    }

    fun startBubbleSort(delayMs: Long = 500L) {
        if (_isRunning.value || _bars.value.isEmpty()) return
        _isRunning.value = true
        viewModelScope.launch {
            bubbleSort(delayMs)
            _isRunning.value = false
        }
    }

    fun startSelectionSort(delayMs: Long = 500L) {
        if (_isRunning.value || _bars.value.isEmpty()) return
        _isRunning.value = true
        viewModelScope.launch {
            selectionSort(delayMs)
            _isRunning.value = false
        }
    }

    fun startInsertionSort(delayMs: Long = 500L) {
        if (_isRunning.value || _bars.value.isEmpty()) return
        _isRunning.value = true
        viewModelScope.launch {
            insertionSort(delayMs)
            _isRunning.value = false
        }
    }

    fun startMergeSort(delayMs: Long = 500L) {
        if (_isRunning.value || _bars.value.isEmpty()) return
        _isRunning.value = true
        viewModelScope.launch {
            mergeSort(delayMs)
            _isRunning.value = false
        }
    }

    fun startQuickSort(delayMs: Long = 500L) {
        if (_isRunning.value || _bars.value.isEmpty()) return
        _isRunning.value = true
        viewModelScope.launch {
            quickSort(delayMs)
            _isRunning.value = false
        }
    }

    private suspend fun bubbleSort(stepDelay: Long) {
        val list = _bars.value.toMutableList()
        val n = list.size
        var swapped: Boolean
        for (i in 0 until n) {
            swapped = false
            for (j in 0 until n - i - 1) {
                // mark comparing
                updateStates(list, indices = listOf(j, j + 1), state = BarState.Comparing)
                appendLog("Comparing array[" + j + "]=" + list[j].value + " with array[" + (j + 1) + "]=" + list[j + 1].value)
                delay(stepDelay)
                if (list[j].value > list[j + 1].value) {
                    // mark swapping
                    updateStates(list, indices = listOf(j, j + 1), state = BarState.Swapping)
                    appendLog("Swapping array[" + j + "] and array[" + (j + 1) + "]")
                    delay(stepDelay)
                    val tmp = list[j]
                    list[j] = list[j + 1]
                    list[j + 1] = tmp
                    _bars.value = list.toList()
                    swapped = true
                    delay(stepDelay)
                }
                // reset to default after comparison
                updateStates(list, indices = listOf(j, j + 1), state = BarState.Default)
            }
            // mark last element as sorted
            if (n - i - 1 in list.indices) {
                updateStates(list, indices = listOf(n - i - 1), state = BarState.Sorted)
                appendLog("array[" + (n - i - 1) + "] placed (sorted)")
            }
            if (!swapped) break
        }
        // mark remaining as sorted
        updateStates(list, indices = list.indices.toList(), state = BarState.Sorted)
        appendLog("All elements sorted")
    }

    private suspend fun selectionSort(stepDelay: Long) {
        val list = _bars.value.toMutableList()
        val n = list.size
        for (i in 0 until n) {
            var minIdx = i
            // mark current position
            updateStates(list, indices = listOf(i), state = BarState.Pivot)
            delay(stepDelay)
            for (j in i + 1 until n) {
                updateStates(list, indices = listOf(j), state = BarState.Comparing)
                appendLog("Comparing array[" + j + "]=" + list[j].value + " with current min array[" + minIdx + "]=" + list[minIdx].value)
                delay(stepDelay)
                if (list[j].value < list[minIdx].value) {
                    // reset old min if not i
                    if (minIdx != i) updateStates(list, indices = listOf(minIdx), state = BarState.Default)
                    minIdx = j
                    updateStates(list, indices = listOf(minIdx), state = BarState.Pivot)
                    appendLog("New min at array[" + minIdx + "]=" + list[minIdx].value)
                    delay(stepDelay)
                } else {
                    updateStates(list, indices = listOf(j), state = BarState.Default)
                }
            }
            if (minIdx != i) {
                updateStates(list, indices = listOf(i, minIdx), state = BarState.Swapping)
                appendLog("Swapping array[" + i + "] and array[" + minIdx + "]")
                delay(stepDelay)
                val tmp = list[i]
                list[i] = list[minIdx]
                list[minIdx] = tmp
                _bars.value = list.toList()
                delay(stepDelay)
            }
            updateStates(list, indices = listOf(i), state = BarState.Sorted)
            if (minIdx != i) updateStates(list, indices = listOf(minIdx), state = BarState.Default)
        }
        updateStates(list, indices = list.indices.toList(), state = BarState.Sorted)
        appendLog("All elements sorted")
    }

    private suspend fun insertionSort(stepDelay: Long) {
        val list = _bars.value.toMutableList()
        val n = list.size
        for (i in 1 until n) {
            val key = list[i]
            var j = i - 1
            updateStates(list, indices = listOf(i), state = BarState.Pivot)
            appendLog("Insert array[" + i + "]=" + key.value)
            delay(stepDelay)
            while (j >= 0 && list[j].value > key.value) {
                updateStates(list, indices = listOf(j, j + 1), state = BarState.Swapping)
                appendLog("Shift array[" + j + "]=" + list[j].value + " to array[" + (j + 1) + "]")
                delay(stepDelay)
                list[j + 1] = list[j]
                _bars.value = list.toList()
                updateStates(list, indices = listOf(j, j + 1), state = BarState.Default)
                j--
            }
            list[j + 1] = key.copy(state = BarState.Default)
            _bars.value = list.toList()
            // mark sorted prefix
            updateStates(list, indices = (0..i).toList(), state = BarState.Sorted)
            delay(stepDelay)
        }
        updateStates(list, indices = list.indices.toList(), state = BarState.Sorted)
        appendLog("All elements sorted")
    }

    private suspend fun mergeSort(stepDelay: Long) {
        val list = _bars.value.toMutableList()
        suspend fun mergeSortRange(l: Int, r: Int) {
            if (l >= r) return
            val m = (l + r) / 2
            mergeSortRange(l, m)
            mergeSortRange(m + 1, r)
            // merge step
            val temp = ArrayList<Bar>(r - l + 1)
            var i = l
            var j = m + 1
            while (i <= m && j <= r) {
                updateStates(list, indices = listOf(i, j), state = BarState.Comparing)
                appendLog("Comparing array[" + i + "]=" + list[i].value + " with array[" + j + "]=" + list[j].value)
                delay(stepDelay)
                if (list[i].value <= list[j].value) {
                    temp.add(list[i].copy(state = BarState.Default))
                    updateStates(list, indices = listOf(i), state = BarState.Default)
                    i++
                } else {
                    temp.add(list[j].copy(state = BarState.Default))
                    updateStates(list, indices = listOf(j), state = BarState.Default)
                    j++
                }
            }
            while (i <= m) {
                temp.add(list[i].copy(state = BarState.Default))
                i++
            }
            while (j <= r) {
                temp.add(list[j].copy(state = BarState.Default))
                j++
            }
            for (k in temp.indices) {
                list[l + k] = temp[k]
                _bars.value = list.toList()
                delay(stepDelay)
            }
        }
        mergeSortRange(0, list.lastIndex)
        updateStates(list, indices = list.indices.toList(), state = BarState.Sorted)
        appendLog("All elements sorted")
    }

    private suspend fun quickSort(stepDelay: Long) {
        val list = _bars.value.toMutableList()
        suspend fun partition(low: Int, high: Int): Int {
            val pivotVal = list[high].value
            updateStates(list, indices = listOf(high), state = BarState.Pivot)
            appendLog("Pivot array[" + high + "]=" + pivotVal)
            delay(stepDelay)
            var i = low - 1
            for (j in low until high) {
                updateStates(list, indices = listOf(j), state = BarState.Comparing)
                appendLog("Compare array[" + j + "]=" + list[j].value + " with pivot=" + pivotVal)
                delay(stepDelay)
                if (list[j].value <= pivotVal) {
                    i++
                    if (i != j) {
                        updateStates(list, indices = listOf(i, j), state = BarState.Swapping)
                        appendLog("Swap array[" + i + "] and array[" + j + "]")
                        delay(stepDelay)
                        val t = list[i]
                        list[i] = list[j]
                        list[j] = t
                        _bars.value = list.toList()
                        delay(stepDelay)
                    }
                }
                updateStates(list, indices = listOf(j), state = BarState.Default)
            }
            if (i + 1 != high) {
                updateStates(list, indices = listOf(i + 1, high), state = BarState.Swapping)
                appendLog("Place pivot from array[" + high + "] to array[" + (i + 1) + "]")
                delay(stepDelay)
                val t = list[i + 1]
                list[i + 1] = list[high]
                list[high] = t
                _bars.value = list.toList()
                delay(stepDelay)
            }
            updateStates(list, indices = listOf(i + 1, high), state = BarState.Default)
            return i + 1
        }
        suspend fun qs(l: Int, r: Int) {
            if (l >= r) return
            val p = partition(l, r)
            updateStates(list, indices = listOf(p), state = BarState.Sorted)
            qs(l, p - 1)
            qs(p + 1, r)
        }
        qs(0, list.lastIndex)
        updateStates(list, indices = list.indices.toList(), state = BarState.Sorted)
        appendLog("All elements sorted")
    }

    private fun updateStates(list: MutableList<Bar>, indices: List<Int>, state: BarState) {
        indices.forEach { idx ->
            if (idx in list.indices) list[idx] = list[idx].copy(state = state)
        }
        _bars.value = list.toList()
    }

    private fun appendLog(message: String) {
        val updated = _logs.value.toMutableList()
        updated.add(0, message)
        _logs.value = updated
    }

    fun postLog(message: String) {
        appendLog(message)
    }
}



