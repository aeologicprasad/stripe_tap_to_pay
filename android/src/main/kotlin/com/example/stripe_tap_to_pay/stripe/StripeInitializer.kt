package com.example.stripe_tap_to_pay.stripe

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.stripe_tap_to_pay.service.TokenProvider
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.models.TerminalException
import com.stripe.stripeterminal.log.LogLevel
import io.flutter.plugin.common.MethodChannel

@Suppress("DEPRECATION")
class StripeInitializer {
    private val PERMISSION_REQUEST_CODE = 101;
    private val TAG = "StripeTapToPayPlugin"
    var isPermissionAvailable = false

    @RequiresApi(Build.VERSION_CODES.S)
    fun initializeStripeTerminal(activity: Activity, result: MethodChannel.Result) {
        return startPermissionFlow(activity, result)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startPermissionFlow(activity: Activity, result: MethodChannel.Result) {
        isPermissionAvailable = false;
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
            isPermissionAvailable = true;
            enableBluetooth(activity, result)
        }
    }

    private fun enableBluetooth(activity: Activity, result: MethodChannel.Result) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled) {
                bluetoothAdapter.enable()
            }
            checkGpsAndInitialize(activity, result)
        } else {
            Log.e(TAG, "Bluetooth not supported")
            result.error("bluetooth_error", "Bluetooth not supported", null);
        }
    }

    private fun checkGpsAndInitialize(activity: Activity, result: MethodChannel.Result) {
        val isTerminalInitialized = Terminal.isInitialized()
        val isGpsEnabled = verifyGpsEnabled(activity, result)
        if (!isTerminalInitialized && isGpsEnabled) {
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
        } else if(!isGpsEnabled) {
            Log.d(TAG, "GPS is not enabled")
            result.error("gps_error", "GPS is not enabled", false)
        } else{
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

    private fun verifyGpsEnabled(activity: Activity, result: MethodChannel.Result): Boolean {
        val locationManager: LocationManager? =
            activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        var gpsEnabled = false

        try {
            gpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error, while checking for GPS status")
            result.error("gps_error", e.message, null)
        }
        if (!gpsEnabled) {
            openGpsSettings(activity);
        }
        return gpsEnabled
    }

    private fun openGpsSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        activity.startActivity(intent)
    }

}