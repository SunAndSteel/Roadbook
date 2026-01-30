package com.florent.carnetconduite.repository

import android.content.Context
import com.florent.carnetconduite.data.TripDao
import com.florent.carnetconduite.domain.utils.Result
import com.florent.carnetconduite.testutils.TripFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TripRepositoryTest {
    private val tripDao = mock<TripDao>()
    private val context = mock<Context>()

    private class FakeSessionStorage : SessionStorage {
        val saved = mutableListOf<Long>()
        var cleared = 0
        private val flow = MutableStateFlow<Long?>(null)

        override fun getOngoingSessionId(context: Context): Flow<Long?> = flow

        override suspend fun saveOngoingSessionId(context: Context, sessionId: Long) {
            saved.add(sessionId)
            flow.value = sessionId
        }

        override suspend fun clearOngoingSessionId(context: Context) {
            cleared += 1
            flow.value = null
        }
    }

    @Test
    fun `updateEndKm delegates to dao`() = runTest {
        val sessionStorage = FakeSessionStorage()
        val repository = TripRepository(tripDao, context, sessionStorage)
        whenever(tripDao.updateEndKm(1, 120)).thenReturn(Unit)

        val result = repository.updateEndKm(1, 120)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        verify(tripDao).updateEndKm(1, 120)
    }

    @Test
    fun `finishTrip clears session`() = runTest {
        val sessionStorage = FakeSessionStorage()
        val repository = TripRepository(tripDao, context, sessionStorage)
        whenever(tripDao.finishTrip(1, 120, "Lyon", 1000L)).thenReturn(Unit)

        val result = repository.finishTrip(1, 120, "Lyon", 1000L)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat(sessionStorage.cleared).isEqualTo(1)
    }

    @Test
    fun `finishAndPrepareReturn saves new session id`() = runTest {
        val sessionStorage = FakeSessionStorage()
        val repository = TripRepository(tripDao, context, sessionStorage)
        whenever(tripDao.finishAndPrepareReturn(1, 120, "Lyon", 1000L)).thenReturn(10L)

        val result = repository.finishAndPrepareReturn(1, 120, "Lyon", 1000L)

        assertThat(result.getOrNull()).isEqualTo(10L)
        assertThat(sessionStorage.saved).contains(10L)
    }

    @Test
    fun `startReturn saves session id`() = runTest {
        val sessionStorage = FakeSessionStorage()
        val repository = TripRepository(tripDao, context, sessionStorage)
        whenever(tripDao.startReturn(any(), any(), any())).thenReturn(Unit)

        val result = repository.startReturn(12, actualStartKm = 120, startTime = 2000L)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat(sessionStorage.saved).contains(12L)
    }

    @Test
    fun `createSkippedReturn clears session`() = runTest {
        val sessionStorage = FakeSessionStorage()
        val repository = TripRepository(tripDao, context, sessionStorage)
        whenever(tripDao.createSkippedReturn(1)).thenReturn(99L)

        val result = repository.createSkippedReturn(1)

        assertThat(result.getOrNull()).isEqualTo(99L)
        assertThat(sessionStorage.cleared).isEqualTo(1)
    }

    @Test
    fun `insert forwards trip to dao`() = runTest {
        val sessionStorage = FakeSessionStorage()
        val repository = TripRepository(tripDao, context, sessionStorage)
        val trip = TripFactory.create(id = 0)
        whenever(tripDao.insertTrip(trip)).thenReturn(5L)

        val id = repository.insert(trip)

        assertThat(id).isEqualTo(5L)
        verify(tripDao).insertTrip(trip)
    }
}
