package com.android.ditrack.di

import com.android.ditrack.data.datastore.UserSessionPreferences
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    singleOf(::UserSessionPreferences)
}