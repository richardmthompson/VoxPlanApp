package com.voxplanapp.navigation

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NavigationViewModel : ViewModel() {
    private val _selectedItemIndex = MutableStateFlow(0)
    val selectedItemIndex: StateFlow<Int> = _selectedItemIndex.asStateFlow()

    fun setSelectedItemIndex(index: Int) {
        Log.d("Navigation","NavigationViewModel: Setting selected item index to $index")
        _selectedItemIndex.value = index
    }
}
