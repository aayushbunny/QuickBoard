package com.aayush.smartticket

import android.util.Log
import androidx.navigation.NavController

fun NavController.safeNavigate(route: String) {
    try {
        this.navigate(route)
    } catch (e: Exception) {
        Log.e("NavSafe", "safeNavigate failed for route=$route", e)
    }
}
