package com.android.ditrack.di

import com.android.ditrack.ui.feature.screen.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::MainViewModel)
}