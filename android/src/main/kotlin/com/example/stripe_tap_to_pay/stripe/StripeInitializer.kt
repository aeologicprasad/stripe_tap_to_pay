package com.example.stripe_tap_to_pay.stripe

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
import com.example.stripe_tap_to_pay.service.TokenProvider
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.models.TerminalException
import com.stripe.stripeterminal.log.LogLevel
import io.flutter.plugin.common.MethodChannel


@Suppress("DEPRECATION")
class StripeInitializer {
    private val PERMISSION_REQUEST_CODE = 101;
    private val LOCATION_REQUEST_CODE = 102;
    private val TAG = "StripeTapToPayPlugin"

    @RequiresApi(Build.VERSION_CODES.S)
    fun setupTapToPay(activity: Activity, result: MethodChannel.Result) {
        return startPermissionFlow(activity, result)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startPermissionFlow(activity: Activity, result: MethodChannel.Result) {
        val deniedPermissions = mutableListOf<String>().apply {
            if (!isGranted(activity,Manifest.permission.ACCESS_FINE_LOCATION, result)) add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (!isGranted(activity,Manifest.permission.BLUETOOTH_CONNECT, result)) add(Manifest.permission.BLUETOOTH_CONNECT)
            if (!isGranted(activity,Manifest.permission.BLUETOOTH_SCAN, result)) add(Manifest.permission.BLUETOOTH_SCAN)
        }.toTypedArray()

        if (deniedPermissions.isNotEmpty()) {
            Log.d(TAG, "Total permissions required: ${deniedPermissions.size}")
            try {
                ActivityCompat.requestPermissions(activity, deniedPermissions, PERMISSION_REQUEST_CODE)
            }catch (e: Exception){
                Log.e(TAG, "Error, while requesting permissions")
                result.error("permission_request_error", e.message, null);
            }
        } else {
            enableBluetooth(activity, result)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun enableBluetooth(activity: Activity, result: MethodChannel.Result) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled) {
                bluetoothAdapter.enable()
            }
            verifyGpsEnabled(activity, result)
        } else {
            Log.e(TAG, "Bluetooth not supported")
            result.error("bluetooth_error", "Bluetooth not supported", null);
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun initializeTerminal(activity: Activity, result: MethodChannel.Result) {
        val isTerminalInitialized = Terminal.isInitialized()
        if (!isTerminalInitialized) {
            try {
                Log.d(TAG, "Initializing Stripe Terminal...")
                Terminal.initTerminal(
                    activity, LogLevel.VERBOSE, TokenProvider(),
                    TerminalEventListener()
                )
                Log.d(TAG, "Stripe Terminal Initialized Successfully")
                result.success(true)
            } catch (e: TerminalException) {
                Log.e(
                    TAG,
                    "Stripe Terminal Initialization Error: ${e.errorMessage}"
                )
                result.error("stripe_terminal_error", e.errorMessage, null)
            }
        } else {
            Log.d(TAG, "Stripe Terminal is already Initialized")
            result.success(true)
        }
    }

    private fun isGranted(activity: Activity, permission: String, result: MethodChannel.Result): Boolean {
        return try{
            ContextCompat.checkSelfPermission(
                activity,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }catch (e: Exception){
            Log.e(TAG, "Error, while checking for Permission")
            result.error("permission_check_error", e.message, null);
            false
        }
    }




    @RequiresApi(Build.VERSION_CODES.S)
    private fun verifyGpsEnabled(activity: Activity, result: MethodChannel.Result) {
        var gpsEnabled = false
        val locationManager: LocationManager? =
            activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        try {
            gpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error, while checking for GPS status")
            result.error("gps_error", e.message, null)
        }


        if (!gpsEnabled) {
            val locationRequest = com.google.android.gms.location.LocationRequest.create()
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)

            val settingsClient = LocationServices.getSettingsClient(activity)
            val task = settingsClient.checkLocationSettings(builder.build())

            task.addOnCompleteListener { task ->
                try {
                    val res = task.getResult(ApiException::class.java)
                } catch (exception: ApiException) {
                    if (exception is ResolvableApiException) {
                        // Location settings are not satisfied, show dialog
                        try {
                            exception.startResolutionForResult(activity, LOCATION_REQUEST_CODE)
                        } catch (sendEx: IntentSender.SendIntentException) {
                            // Ignore the error.
                        }
                    }
                }
            }
        }
        else{
            initializeTerminal(activity, result)
        }
    }


}