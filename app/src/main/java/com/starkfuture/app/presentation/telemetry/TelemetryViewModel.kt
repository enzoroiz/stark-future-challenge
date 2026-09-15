package com.starkfuture.app.presentation.telemetry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.starkfuture.app.data.mock.MockScenario
import com.starkfuture.app.data.mock.MockScenarioStore
import com.starkfuture.app.domain.repository.TelemetryRepository
import com.starkfuture.app.domain.repository.TelemetryResult
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TelemetryViewModel @Inject constructor(
    private val repository: TelemetryRepository,
    private val scenarioStore: MockScenarioStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<TelemetryUiState>(TelemetryUiState.Loading)
    val uiState: StateFlow<TelemetryUiState> = _uiState.asStateFlow()

    private val _selectedScenario = MutableStateFlow(scenarioStore.currentScenario.value)
    val selectedScenario: StateFlow<MockScenario> = _selectedScenario.asStateFlow()

    init {
        loadTelemetry()
    }

    fun onScenarioSelected(scenario: MockScenario) {
        _selectedScenario.value = scenario
        scenarioStore.setScenario(scenario)
        loadTelemetry()
    }

    fun retry() = loadTelemetry()

    fun loadTelemetry() {
        viewModelScope.launch {
            _uiState.value = TelemetryUiState.Loading
            when (val result = repository.getTelemetry()) {
                is TelemetryResult.Success -> _uiState.value = TelemetryUiState.Success(result.telemetry)
                TelemetryResult.Empty -> _uiState.value = TelemetryUiState.Empty
                is TelemetryResult.Error -> _uiState.value = TelemetryUiState.Error(result.message)
            }
        }
    }
}
