package com.emoneychecker.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emoneychecker.domain.model.NfcError
import com.emoneychecker.domain.usecase.ReadCardUseCase
import com.emoneychecker.ui.state.ReaderIntent
import com.emoneychecker.ui.state.ReaderUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val readCardUseCase: ReadCardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Idle)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var autoResetJob: Job? = null

    fun onIntent(intent: ReaderIntent) {
        when (intent) {
            is ReaderIntent.TagDetected -> handleTagDetected(intent.tag)
            is ReaderIntent.NfcStatusChanged -> handleNfcStatusChanged(intent.isEnabled)
            is ReaderIntent.DismissError -> dismissError()
            is ReaderIntent.Reset -> resetToIdle()
            is ReaderIntent.CopyBalance -> {
                // Handled in UI layer via clipboard manager
            }
        }
    }

    private fun handleTagDetected(tag: Any) {
        autoResetJob?.cancel()
        _uiState.update { ReaderUiState.Reading }

        viewModelScope.launch {
            val result = readCardUseCase(tag)
            result.fold(
                onSuccess = { cardInfo ->
                    _uiState.update { ReaderUiState.Success(cardInfo) }
                },
                onFailure = { throwable ->
                    val error = when (throwable) {
                        is NfcError -> throwable
                        else -> NfcError.Unknown(throwable)
                    }
                    _uiState.update { ReaderUiState.Error(error) }
                    scheduleAutoReset()
                }
            )
        }
    }

    private fun handleNfcStatusChanged(isEnabled: Boolean) {
        if (!isEnabled) {
            autoResetJob?.cancel()
            _uiState.update { ReaderUiState.NfcDisabled }
        } else if (_uiState.value is ReaderUiState.NfcDisabled) {
            _uiState.update { ReaderUiState.Idle }
        }
    }

    private fun dismissError() {
        autoResetJob?.cancel()
        if (_uiState.value is ReaderUiState.Error) {
            _uiState.update { ReaderUiState.Idle }
        }
    }

    private fun resetToIdle() {
        autoResetJob?.cancel()
        _uiState.update { ReaderUiState.Idle }
    }

    private fun scheduleAutoReset() {
        autoResetJob?.cancel()
        autoResetJob = viewModelScope.launch {
            delay(2500)
            if (_uiState.value is ReaderUiState.Error) {
                _uiState.update { ReaderUiState.Idle }
            }
        }
    }
}
