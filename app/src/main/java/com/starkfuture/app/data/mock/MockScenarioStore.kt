package com.starkfuture.app.data.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockScenarioStore {
    private val _currentScenario = MutableStateFlow(MockScenario.SUCCESS)
    val currentScenario: StateFlow<MockScenario> = _currentScenario.asStateFlow()

    fun setScenario(scenario: MockScenario) {
        _currentScenario.value = scenario
    }

    fun currentScenarioForRequest(): MockScenario = _currentScenario.value
}
