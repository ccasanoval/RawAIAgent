package com.cesoft.rawagent.location

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.annotation.RequiresPermission

object LocationProvider {
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getLocation(context: Context): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
    }
}
