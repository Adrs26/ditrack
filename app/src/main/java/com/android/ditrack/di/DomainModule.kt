package com.android.ditrack.di

import com.android.ditrack.domain.usecase.SetApplicationModeUseCase
import com.android.ditrack.domain.usecase.SyncGeofenceUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::SetApplicationModeUseCase)
    factoryOf(::SyncGeofenceUseCase)
}