package com.android.ditrack.di

import com.android.ditrack.data.datastore.UserSessionPreferences
import com.android.ditrack.data.manager.MapsManagerImpl
import com.android.ditrack.data.repository.MapsRepositoryImpl
import com.android.ditrack.data.repository.UserSessionRepositoryImpl
import com.android.ditrack.data.source.RemoteDataSource
import com.android.ditrack.domain.manager.MapsManager
import com.android.ditrack.domain.repository.MapsRepository
import com.android.ditrack.domain.repository.UserSessionRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    singleOf(::UserSessionPreferences)
    singleOf(::RemoteDataSource)
    singleOf(::MapsManagerImpl) { bind<MapsManager>() }
    singleOf(::UserSessionRepositoryImpl) { bind<UserSessionRepository>() }
    singleOf(::MapsRepositoryImpl) { bind<MapsRepository>() }
}