package com.android.ditrack.di

import com.google.android.gms.location.LocationServices
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val mapsModule = module {
    single { LocationServices.getFusedLocationProviderClient(androidContext()) }
    single { LocationServices.getGeofencingClient(androidContext()) }
}