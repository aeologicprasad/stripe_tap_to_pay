package com.example.stripe_tap_to_pay

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import com.example.stripe_tap_to_pay.logic.PermissionHandler
import com.example.stripe_tap_to_pay.logic.StripeTerminal
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry

@Suppress("KotlinConstantConditions")
class StripeTapToPayPlugin : FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware,
    PluginRegistry.RequestPermissionsResultListener, PluginRegistry.ActivityResultListener {

    private lateinit var channel: MethodChannel
    private var activity: Activity? = null
    private var context: Context? = null
    private var result: MethodChannel.Result? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "stripe_tap_to_pay")
        channel.setMethodCallHandler(this)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        this.activity?.let {
            StripeTerminal.activity = it
            PermissionHandler.activity = it
        }
        this.result = result
        StripeTerminal.result = result;
        PermissionHandler.result = result

        when (call.method) {
            "initializeStripeTerminal" -> {
                val token = call.argument<String>("token") ?: ""
                StripeTerminal.token = token
                StripeTerminal.setupTapToPay()
            }

            "connectReader" -> {
                val isSimulated = call.argument<Boolean>("isSimulated") ?: false
                StripeTerminal.connectReader(isSimulated)
            }

            "disconnectReader" -> {
                StripeTerminal.disconnectReader()
            }

            "isTerminalInitialized" -> {
                StripeTerminal.isTerminalInitialized()
            }

            "isReaderConnected" -> {
                StripeTerminal.isReaderConnected()
            }

            "createPayment" -> {
                val secret = call.argument<String>("secret") ?: ""
                val skipTipping = call.argument<Boolean>("skipTipping") ?: true

                StripeTerminal.createPaymentIntent(secret, skipTipping)
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addRequestPermissionsResultListener(this)
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addRequestPermissionsResultListener(this)
        binding.addActivityResultListener(this)
        StripeTerminal.setupTapToPay()
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == 102 && resultCode == Activity.RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                StripeTerminal.initializeTerminal()
            }
        } else if (requestCode == 102 && resultCode == Activity.RESULT_CANCELED) {
            result?.error("location_service_error", "Location service must be enabled for tap to pay feature", null)
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray): Boolean {
        if (requestCode == 101 && grantResults.contains(-1)) {
            result?.error("permission_request_error", "Permissions required to continue with tap to pay feature", null)
            return false
        } else if (requestCode == 101) {
            StripeTerminal.setupTapToPay()
            return true
        }
        return true
    }
}
