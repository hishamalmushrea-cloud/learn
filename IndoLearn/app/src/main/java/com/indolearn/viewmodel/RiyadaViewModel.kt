package com.indolearn.viewmodel

import androidx.lifecycle.ViewModel
import com.indolearn.data.local.riyada.RiyadaDataResponse
import com.indolearn.data.repository.RiyadaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class RiyadaViewModel @Inject constructor(
    private val repository: RiyadaRepository
) : ViewModel() {

    private val _riyadaData = MutableStateFlow<RiyadaDataResponse?>(null)
    val riyadaData: StateFlow<RiyadaDataResponse?> = _riyadaData

    init {
        loadData()
    }

    private fun loadData() {
        _riyadaData.value = repository.getRiyadaData()
    }
}
