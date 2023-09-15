@file:Suppress("DEPRECATION")

package com.example.stripe_tap_to_pay.logic

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import io.flutter.plugin.common.MethodChannel

@SuppressLint("StaticFieldLeak")
object PermissionHandler {
    private const val PERMISSION_REQUEST_CODE = 101
    private const val LOCATION_REQUEST_CODE = 102
    private const val TAG = "StripeTapToPayPlugin"

    // Class members for activity and result
    var activity: Activity? = null
    var result: MethodChannel.Result? = null

    @RequiresApi(Build.VERSION_CODES.S)
    fun startPermissionFlow() {

        val requiredPermissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )

        // Filter permissions that are not granted
        val deniedPermissions = requiredPermissions.filter {
            !isGranted(it)
        }

        if (deniedPermissions.isNotEmpty()) {
            Log.d(TAG, "Total permissions required: ${deniedPermissions.size}")
            requestPermissions(deniedPermissions)
        } else {
            enableBluetooth()
        }
    }

    private fun isGranted(permission: String): Boolean {
        return try {
            ContextCompat.checkSelfPermission(activity!!, permission) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permission: ${e.message}")
            result?.error("permission_check_error", e.message, null)
            false
        }
    }

    private fun requestPermissions(permissions: List<String>) {
        try {
            ActivityCompat.requestPermissions(activity!!, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting permissions: ${e.message}")
            result?.error("permission_request_error", e.message, null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun enableBluetooth() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled) {
                bluetoothAdapter.enable()
            }
            verifyGpsEnabled()
        } else {
            Log.e(TAG, "Bluetooth not supported")
            result?.error("bluetooth_error", "Bluetooth not supported", null)
        }
    }

    @SuppressLint("ServiceCast")
    @RequiresApi(Build.VERSION_CODES.S)
    private fun verifyGpsEnabled() {
        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        var gpsEnabled = false

        try {
            gpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking GPS status: ${e.message}")
            result?.error("gps_error", e.message, null)
        }

        if (!gpsEnabled) {
            showGpsSettingsDialog()
        } else {
            StripeTerminal.initializeTerminal()
        }
    }

    @SuppressLint("VisibleForTests")
    @RequiresApi(Build.VERSION_CODES.S)
    private fun showGpsSettingsDialog() {
        val locationRequest = com.google.android.gms.location.LocationRequest.create()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val settingsClient = LocationServices.getSettingsClient(activity!!)
        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnCompleteListener { _ ->
            try {
                task.getResult(ApiException::class.java)
            } catch (exception: ApiException) {
                if (exception is ResolvableApiException) {
                    // Location settings are not satisfied, show dialog
                    try {
                        exception.startResolutionForResult(activity!!, LOCATION_REQUEST_CODE)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
        }
    }
}
