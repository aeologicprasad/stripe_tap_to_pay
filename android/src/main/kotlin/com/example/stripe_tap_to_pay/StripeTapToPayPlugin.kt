package com.example.stripe_tap_to_pay

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.stripe_tap_to_pay.service.BASE_URL
import com.example.stripe_tap_to_pay.stripe.StripeInitializer
import com.example.stripe_tap_to_pay.stripe.StripePaymentHandler
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.embedding.engine.plugins.lifecycle.HiddenLifecycleReference

/** StripeTapToPayPlugin */
class StripeTapToPayPlugin : FlutterPlugin, MethodCallHandler, ActivityAware,
    PluginRegistry.RequestPermissionsResultListener, PluginRegistry.ActivityResultListener {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var activity: Activity? = null
    private var context: Context? = null
    private val TAG = "StripeTapToPayPlugin"
    private val stripeInitializer = StripeInitializer();
    private val paymentHandler = StripePaymentHandler()
    private var previousEvent: Lifecycle.Event? = null
    private var result: MethodChannel.Result? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext  // Store the application context
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "stripe_tap_to_pay")
        channel.setMethodCallHandler(this)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        this.result = result
        when (call.method) {
            "initializeStripeTerminal" -> {
                BASE_URL = call.argument<String>("backendUrl")?:""
                stripeInitializer.setupTapToPay(activity!!, result)
            }

            "connectReader" -> {
                paymentHandler.connectReader(result)
            }

            "createPayment" -> {
                val amount: Long = (call.argument<Int>("amount") ?: 1).toLong()
                val currency = call.argument<String>("currency")?:"usd"
                val skipTipping = call.argument<Boolean>("skipTipping")?:true
                val extendedAuth = call.argument<Boolean>("extendedAuth")?:false
                val incrementalAuth = call.argument<Boolean>("incrementalAuth")?:false
                paymentHandler.createPaymentIntent(amount, currency, skipTipping, extendedAuth, incrementalAuth, result)
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addRequestPermissionsResultListener(this)
        binding.addActivityResultListener(this)
//        (binding.lifecycle as HiddenLifecycleReference)
//            .lifecycle
//            .addObserver(LifecycleEventObserver { _, event ->
//                Log.d(TAG, "$event")
//                if (!stripeInitializer.gpsDialogActive && previousEvent == Lifecycle.Event.ON_PAUSE && event == Lifecycle.Event.ON_RESUME) {
//                    Log.d(TAG, "Re-initializing Stripe Terminal...")
//                    CoroutineScope(Dispatchers.Main).launch {
//                        stripeInitializer.initializeTerminal(activity!!, result!!)
//                        stripeInitializer.gpsDialogActive = false
//                    }
//                }
//                previousEvent = event
//            })
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        Log.d(TAG, "onReattachedToActivityForConfigChanges: $binding");
        activity = binding.activity
        binding.addRequestPermissionsResultListener(this)
        binding.addActivityResultListener(this)
        stripeInitializer.setupTapToPay(activity!!, result!!)
    }

    override fun onDetachedFromActivity() {
        activity = null
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        Log.d(TAG, "onActivityResult: $requestCode, $resultCode, $data");
        if(requestCode==102 && resultCode==-1){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                stripeInitializer.initializeTerminal(activity!!, result!!)
            }
        }else if (requestCode==102 && resultCode==0){
            result?.success(false)
        }
        return true
    }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ): Boolean {
        Log.d(TAG, "onRequestPermissionsResult: $requestCode, $permissions, $grantResults");
        stripeInitializer.setupTapToPay(activity!!, result!!)
        return true
    }


}
