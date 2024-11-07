package com.example.TNTSMobileApp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _isCardViewVisible = MutableLiveData<Boolean>().apply { value = false }
    val isCardViewVisible: LiveData<Boolean> = _isCardViewVisible

    fun setCardViewVisibility(isVisible: Boolean) {
        _isCardViewVisible.value = isVisible
    }
}