package com.starkfuture.app.presentation.telemetry

import com.starkfuture.app.data.mock.MockScenario
import com.starkfuture.app.data.mock.MockScenarioStore
import com.starkfuture.app.domain.model.Battery
import com.starkfuture.app.domain.model.Bike
import com.starkfuture.app.domain.model.Motor
import com.starkfuture.app.domain.model.RideSettings
import com.starkfuture.app.domain.model.Session
import com.starkfuture.app.domain.model.Telemetry
import com.starkfuture.app.domain.model.Warning
import com.starkfuture.app.domain.repository.TelemetryRepository
import com.starkfuture.app.domain.repository.TelemetryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TelemetryViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should load success state on init when repository returns success`() = runTest {
        val viewModel = TelemetryViewModel(
            repository = FakeTelemetryRepository(TelemetryResult.Success(sampleTelemetry())),
            scenarioStore = MockScenarioStore()
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is TelemetryUiState.Success)
    }

    @Test
    fun `should update scenario and reload telemetry when scenario is selected`() = runTest {
        val scenarioStore = MockScenarioStore()
        val repository = FakeTelemetryRepository(TelemetryResult.Empty)
        val viewModel = TelemetryViewModel(repository, scenarioStore)

        advanceUntilIdle()
        viewModel.onScenarioSelected(MockScenario.ERROR)
        advanceUntilIdle()

        assertEquals(MockScenario.ERROR, viewModel.selectedScenario.value)
        assertEquals(MockScenario.ERROR, scenarioStore.currentScenario.value)
        assertEquals(2, repository.callCount)
        assertEquals(TelemetryUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `should reload and publish error state when retry is triggered and repository returns error`() = runTest {
        val repository = FakeTelemetryRepository(TelemetryResult.Error("failed"))
        val viewModel = TelemetryViewModel(repository, MockScenarioStore())

        advanceUntilIdle()
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(2, repository.callCount)
        assertEquals(TelemetryUiState.Error("failed"), viewModel.uiState.value)
    }

    @Test
    fun `should switch to success scenario when retry is triggered from error scenario`() = runTest {
        val scenarioStore = MockScenarioStore().apply { setScenario(MockScenario.ERROR) }
        val repository = ScenarioAwareTelemetryRepository(scenarioStore)
        val viewModel = TelemetryViewModel(repository, scenarioStore)

        advanceUntilIdle()
        viewModel.retry()
        advanceUntilIdle()

        assertEquals(MockScenario.SUCCESS, viewModel.selectedScenario.value)
        assertEquals(MockScenario.SUCCESS, scenarioStore.currentScenario.value)
        assertTrue(viewModel.uiState.value is TelemetryUiState.Success)
    }

    private fun sampleTelemetry() = Telemetry(
        bike = Bike("Stark VARG MX 1.2", "Alpha", "3.4.1", "https://example.com/bike.webp"),
        timestamp = "2025-05-19T10:32:45Z",
        battery = Battery(73, 38, 34.7, "discharging"),
        motor = Motor(52.4, 61.2),
        rideSettings = RideSettings("enduro", 80, 45, 60),
        session = Session(3742, "1:02:22", 24.7, 94.1),
        warnings = listOf(Warning("W_MOT_TEMP_HIGH", "Motor temperature elevated", "warning"))
    )
}

private class FakeTelemetryRepository(
    private val result: TelemetryResult
) : TelemetryRepository {
    var callCount: Int = 0
        private set

    override suspend fun getTelemetry(): TelemetryResult {
        callCount += 1
        return result
    }
}

private class ScenarioAwareTelemetryRepository(
    private val scenarioStore: MockScenarioStore
) : TelemetryRepository {
    override suspend fun getTelemetry(): TelemetryResult =
        if (scenarioStore.currentScenario.value == MockScenario.ERROR) {
            TelemetryResult.Error("failed")
        } else {
            TelemetryResult.Success(
                Telemetry(
                    bike = Bike("Stark VARG MX 1.2", "Alpha", "3.4.1", "https://example.com/bike.webp"),
                    timestamp = "2025-05-19T10:32:45Z",
                    battery = Battery(73, 38, 34.7, "discharging"),
                    motor = Motor(52.4, 61.2),
                    rideSettings = RideSettings("enduro", 80, 45, 60),
                    session = Session(3742, "1:02:22", 24.7, 94.1),
                    warnings = listOf(Warning("W_MOT_TEMP_HIGH", "Motor temperature elevated", "warning"))
                )
            )
        }
}
