package com.emoneychecker.di

import com.emoneychecker.domain.reader.CardReader
import com.emoneychecker.domain.usecase.ReadCardUseCase
import com.emoneychecker.nfc.CardReaderImpl
import com.emoneychecker.ui.screen.ReaderViewModel
import org.koin.dsl.module

val appModule = module {
    single<CardReader> { CardReaderImpl() }
    factory { ReadCardUseCase(get()) }
    factory { ReaderViewModel(get()) }
}
