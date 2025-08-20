package com.cesoft.rawagent.location


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import com.cesoft.rawagent.App

object GpsUtil {
    fun checkGpsOn(c: Context) {
        val locationManager = c.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if( ! isEnabled) {
            AlertDialog.Builder(c)
                .setMessage("GPS Location is disabled")
                .setPositiveButton("ok") { paramDialogInterface, paramInt ->
                    c.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

/*
Requires Google Services

    fun checkGpsOn(activity: Activity) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,60*1000).build()
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(activity)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
    //        task.addOnSuccessListener { locationSettingsResponse ->
    //            //Log.e("Main", "checkGpsOn--------------------res: ${locationSettingsResponse.locationSettingsStates}")
    //            // All location settings are satisfied. The client can initialize location requests here.
    //        }
        task.addOnFailureListener { exception ->
            Log.e("Main", "checkGpsOn--------------------e: $exception")
            if(exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(activity, MainActivity.REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }*/
}