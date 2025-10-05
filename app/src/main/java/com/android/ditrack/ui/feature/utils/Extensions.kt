package com.android.ditrack.ui.feature.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.ParametersHolder

fun String.showMessageWithToast(context: Context) {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedKoinViewModel(
    navController: NavController,
    noinline parameters: (() -> ParametersHolder)? = null
): T {
    val navGraphRoute = destination.parent?.route ?: error("No parent route")
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return if (parameters != null) {
        koinViewModel(viewModelStoreOwner = parentEntry, parameters = parameters)
    } else {
        koinViewModel(viewModelStoreOwner = parentEntry)
    }
}