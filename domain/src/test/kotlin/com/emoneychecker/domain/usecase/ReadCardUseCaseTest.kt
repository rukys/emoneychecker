package com.emoneychecker.domain.usecase

import com.emoneychecker.domain.model.CardBank
import com.emoneychecker.domain.model.CardInfo
import com.emoneychecker.domain.model.NfcError
import com.emoneychecker.domain.reader.CardReader
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ReadCardUseCaseTest {

    private class FakeCardReader : CardReader {
        var result: Result<CardInfo> = Result.success(
            CardInfo(
                bank = CardBank.MANDIRI,
                maskedPan = "6032 •••• •••• 1234",
                balanceRupiah = 100_000L,
                readTimestamp = Instant.EPOCH
            )
        )
        var callCount = 0

        override suspend fun read(tag: Any): Result<CardInfo> {
            callCount++
            return result
        }
    }

    @Test
    fun `invoke delegates to CardReader successfully`() = runTest {
        val fakeReader = FakeCardReader()
        val useCase = ReadCardUseCase(fakeReader)

        val result = useCase("dummy-tag")

        assertEquals(1, fakeReader.callCount)
        assertTrue(result.isSuccess)
        assertEquals(CardBank.MANDIRI, result.getOrNull()?.bank)
        assertEquals(100_000L, result.getOrNull()?.balanceRupiah)
    }

    @Test
    fun `invoke returns failure when CardReader fails`() = runTest {
        val fakeReader = FakeCardReader()
        fakeReader.result = Result.failure(NfcError.TagLost)
        val useCase = ReadCardUseCase(fakeReader)

        val result = useCase("dummy-tag")

        assertEquals(1, fakeReader.callCount)
        assertTrue(result.isFailure)
        assertEquals(NfcError.TagLost, result.exceptionOrNull())
    }
}
