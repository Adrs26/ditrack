package com.android.ditrack.ui.feature.utils

import com.google.android.gms.maps.model.LatLng

object DataDummyProvider {
    fun getBusStops(): List<BusStopsDummy> {
        return listOf(
            BusStopsDummy(
                id = 1,
                name = "Halte FISIP Hukum",
                latLng = LatLng(-7.0508974, 110.4371808)
            ),
            BusStopsDummy(
                id = 2,
                name = "Halte Psikologi",
                latLng = LatLng(-7.0471924, 110.4388105)
            ),
            BusStopsDummy(
                id = 3,
                name = "Halte SA-MWA FSM",
                latLng = LatLng(-7.0487922, 110.4401869)
            ),
            BusStopsDummy(
                id = 4,
                name = "Halte Widya Puraya",
                latLng = LatLng(-7.0498080, 110.4385282)
            ),
            BusStopsDummy(
                id = 5,
                name = "Halte Vokasi",
                latLng = LatLng(-7.0504309, 110.4360653)
            ),
            BusStopsDummy(
                id = 6,
                name = "Halte Fakultas Ekonomi",
                latLng = LatLng(-7.0476788, 110.4408353)
            ),
            BusStopsDummy(
                id = 7,
                name = "Halte FPP",
                latLng = LatLng(-7.0515619, 110.4417901)
            ),
            BusStopsDummy(
                id = 8,
                name = "Halte FPIK",
                latLng = LatLng(-7.0513240, 110.4417895)
            ),
            BusStopsDummy(
                id = 9,
                name = "Halte Rusunawa",
                latLng = LatLng(-7.0545273, 110.4441937)
            ),
            BusStopsDummy(
                id = 10,
                name = "Halte Masjid Hijau LPPU",
                latLng = LatLng(-7.0562602, 110.4400286)
            ),
            BusStopsDummy(
                id = 11,
                name = "Halte Tes",
                latLng = LatLng(-7.057581, 110.440196)
            )
        )
    }
}

data class BusStopsDummy(
    val id: Int,
    val name: String,
    val latLng: LatLng
)