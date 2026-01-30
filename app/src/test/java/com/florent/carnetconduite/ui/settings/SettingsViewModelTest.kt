package com.florent.carnetconduite.ui.settings

import com.florent.carnetconduite.domain.models.UserSettings
import com.florent.carnetconduite.repository.SettingsRepository
import com.florent.carnetconduite.testutils.MainDispatcherRule
import com.florent.carnetconduite.ui.theme.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mock<SettingsRepository>()

    @Test
    fun `userSettings exposes repository state`() = runTest(mainDispatcherRule.dispatcher) {
        val settingsFlow = MutableStateFlow(UserSettings.DEFAULT.copy(themeMode = ThemeMode.DARK))
        whenever(repository.userSettings).thenReturn(settingsFlow)

        val viewModel = SettingsViewModel(repository)

        advanceUntilIdle()
        val settings = viewModel.userSettings.drop(1).first()
        assertThat(settings.themeMode).isEqualTo(ThemeMode.DARK)
    }

    @Test
    fun `updateThemeMode delegates to repository`() = runTest(mainDispatcherRule.dispatcher) {
        whenever(repository.userSettings).thenReturn(MutableStateFlow(UserSettings.DEFAULT))
        val viewModel = SettingsViewModel(repository)

        viewModel.updateThemeMode(ThemeMode.LIGHT)
        advanceUntilIdle()

        verify(repository).updateThemeMode(ThemeMode.LIGHT)
    }
}
