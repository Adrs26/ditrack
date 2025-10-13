package com.android.ditrack.di

import com.android.ditrack.domain.usecase.GetBusStopsUseCase
import com.android.ditrack.domain.usecase.GetRouteInfoUseCase
import com.android.ditrack.domain.usecase.SyncGeofenceUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::GetBusStopsUseCase)
    factoryOf(::GetRouteInfoUseCase)
    factoryOf(::SyncGeofenceUseCase)
}