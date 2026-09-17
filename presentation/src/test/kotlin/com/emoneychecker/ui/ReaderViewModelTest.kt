package com.emoneychecker.ui

import app.cash.turbine.test
import com.emoneychecker.domain.model.CardBank
import com.emoneychecker.domain.model.CardInfo
import com.emoneychecker.domain.model.NfcError
import com.emoneychecker.domain.reader.CardReader
import com.emoneychecker.domain.usecase.ReadCardUseCase
import com.emoneychecker.ui.screen.ReaderViewModel
import com.emoneychecker.ui.state.ReaderIntent
import com.emoneychecker.ui.state.ReaderUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeCardReader : CardReader {
        var result: Result<CardInfo> = Result.success(
            CardInfo(
                bank = CardBank.MANDIRI,
                maskedPan = "6032 •••• •••• 1234",
                balanceRupiah = 125_000L,
                readTimestamp = Instant.EPOCH
            )
        )
        var callCount = 0

        override suspend fun read(tag: Any): Result<CardInfo> {
            callCount++
            return result
        }
    }

    private val testCardInfo = CardInfo(
        bank = CardBank.MANDIRI,
        maskedPan = "6032 •••• •••• 1234",
        balanceRupiah = 125_000L,
        readTimestamp = Instant.EPOCH
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `tag detected transitions to success state`() = runTest(testDispatcher) {
        val fakeReader = FakeCardReader()
        fakeReader.result = Result.success(testCardInfo)
        val viewModel = ReaderViewModel(ReadCardUseCase(fakeReader))

        viewModel.uiState.test {
            assertEquals(ReaderUiState.Idle, awaitItem())

            viewModel.onIntent(ReaderIntent.TagDetected("fakeTag"))

            assertTrue(awaitItem() is ReaderUiState.Reading)
            val success = awaitItem()
            assertTrue(success is ReaderUiState.Success)
            assertEquals(testCardInfo, (success as ReaderUiState.Success).cardInfo)
        }
    }

    @Test
    fun `tag lost error transitions to error state then auto-reset to idle`() = runTest(testDispatcher) {
        val fakeReader = FakeCardReader()
        fakeReader.result = Result.failure(NfcError.TagLost)
        val viewModel = ReaderViewModel(ReadCardUseCase(fakeReader))

        viewModel.uiState.test {
            assertEquals(ReaderUiState.Idle, awaitItem())

            viewModel.onIntent(ReaderIntent.TagDetected("fakeTag"))

            assertTrue(awaitItem() is ReaderUiState.Reading)
            val error = awaitItem()
            assertTrue(error is ReaderUiState.Error)
            assertEquals(NfcError.TagLost, (error as ReaderUiState.Error).error)

            // Auto-reset after 2.5s
            testScheduler.advanceTimeBy(2600)
            assertEquals(ReaderUiState.Idle, awaitItem())
        }
    }

    @Test
    fun `nfc disabled intent transitions to nfc disabled state`() = runTest(testDispatcher) {
        val fakeReader = FakeCardReader()
        val viewModel = ReaderViewModel(ReadCardUseCase(fakeReader))

        viewModel.uiState.test {
            assertEquals(ReaderUiState.Idle, awaitItem())

            viewModel.onIntent(ReaderIntent.NfcStatusChanged(isEnabled = false))
            assertEquals(ReaderUiState.NfcDisabled, awaitItem())

            viewModel.onIntent(ReaderIntent.NfcStatusChanged(isEnabled = true))
            assertEquals(ReaderUiState.Idle, awaitItem())
        }
    }

    @Test
    fun `reset intent transitions from success to idle`() = runTest(testDispatcher) {
        val fakeReader = FakeCardReader()
        fakeReader.result = Result.success(testCardInfo)
        val viewModel = ReaderViewModel(ReadCardUseCase(fakeReader))

        viewModel.uiState.test {
            assertEquals(ReaderUiState.Idle, awaitItem())

            viewModel.onIntent(ReaderIntent.TagDetected("fakeTag"))
            assertTrue(awaitItem() is ReaderUiState.Reading)
            assertTrue(awaitItem() is ReaderUiState.Success)

            viewModel.onIntent(ReaderIntent.Reset)
            assertEquals(ReaderUiState.Idle, awaitItem())
        }
    }
}
