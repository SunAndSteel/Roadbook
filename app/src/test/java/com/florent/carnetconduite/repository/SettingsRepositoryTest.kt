package com.florent.carnetconduite.repository

import com.florent.carnetconduite.domain.models.UserSettings
import com.florent.carnetconduite.ui.theme.ThemeMode
import com.florent.carnetconduite.testutils.FakePreferencesDataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryTest {
    private fun createRepository(): SettingsRepository {
        val context = mock<android.content.Context>()
        val dataStore = FakePreferencesDataStore()
        return SettingsRepository(context = context, dataStore = dataStore)
    }

    @Test
    fun `userSettings emits defaults when datastore empty`() = runTest {
        val repository = createRepository()

        val settings = repository.userSettings.first()

        assertThat(settings).isEqualTo(UserSettings.DEFAULT)
    }

    @Test
    fun `updateThemeMode updates flow`() = runTest {
        val repository = createRepository()

        repository.updateThemeMode(ThemeMode.DARK)

        val settings = repository.userSettings.first()
        assertThat(settings.themeMode).isEqualTo(ThemeMode.DARK)
    }

    @Test
    fun `updateDefaultGuide rejects invalid values`() = runTest {
        val repository = createRepository()

        val exception = kotlin.runCatching { repository.updateDefaultGuide("3") }.exceptionOrNull()
        assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
    }
}
