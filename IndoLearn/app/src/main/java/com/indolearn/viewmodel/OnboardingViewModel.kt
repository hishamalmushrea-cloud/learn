package com.indolearn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indolearn.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _onboardingCompleted = MutableStateFlow<Boolean?>(null)
    val onboardingCompleted: StateFlow<Boolean?> = _onboardingCompleted.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.isOnboardingComplete.collect { completed ->
                _onboardingCompleted.value = completed
            }
        }
    }

    fun completeOnboarding(languageCode: String) {
        viewModelScope.launch {
            // احفظ اللغة قبل إنهاء التهيئة كي لا تومض بيانات لغة أخرى في الرئيسية.
            preferencesManager.setSelectedLanguage(languageCode)
            preferencesManager.setOnboardingComplete(true)
        }
    }
}
