package com.daniil.calculator.settingsscreen

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsScreenModel : ViewModel() {
    private val _customScreenStack =
        MutableStateFlow<SnapshotStateList<String>>(mutableStateListOf())
    var lastActive = false // true - new stack / false - back stack
        private set

    val customScreenStack: StateFlow<List<String>> = _customScreenStack

    val lazyListState = LazyListState()

    fun goToCustomScreen(screenId: String) {
        lastActive = true
        _customScreenStack.value.add(screenId)
    }

    fun backStack() {
        lastActive = false
        if (_customScreenStack.value.size > 1) {
            _customScreenStack.value.removeAt(_customScreenStack.value.lastIndex)
        } else {
            goToHome()
        }
    }
    fun goToHome() {
        _customScreenStack.value.clear()
    }
}