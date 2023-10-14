package com.example.stripe_tap_to_pay.logic

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.stripe_tap_to_pay.enum.PaymentStatus
import com.example.stripe_tap_to_pay.model.LocationListState
import com.example.stripe_tap_to_pay.provider.TerminalEventListener
import com.example.stripe_tap_to_pay.provider.TokenProvider
import com.google.gson.Gson
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.callable.DiscoveryListener
import com.stripe.stripeterminal.external.callable.LocationListCallback
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback
import com.stripe.stripeterminal.external.callable.ReaderCallback
import com.stripe.stripeterminal.external.models.CollectConfiguration
import com.stripe.stripeterminal.external.models.ConnectionConfiguration
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration
import com.stripe.stripeterminal.external.models.DiscoveryMethod
import com.stripe.stripeterminal.external.models.ListLocationsParameters
import com.stripe.stripeterminal.external.models.Location
import com.stripe.stripeterminal.external.models.PaymentIntent
import com.stripe.stripeterminal.external.models.Reader

import com.stripe.stripeterminal.external.models.TerminalException
import com.stripe.stripeterminal.log.LogLevel
import kotlinx.coroutines.flow.MutableStateFlow
import io.flutter.plugin.common.MethodChannel

@SuppressLint("StaticFieldLeak")
object StripeTerminal {
    private const val TAG = "StripeTapToPayPlugin"

    // Class members for secret, activity, and result
    var token: String? = null
    var activity: Activity? = null
    var result: MethodChannel.Result? = null

    private var SKIP_TIPPING = false
    private val gson = Gson()
    private val locationsList = MutableStateFlow(LocationListState())
    private var isSimulated = false

    @RequiresApi(Build.VERSION_CODES.S)
    fun setupTapToPay() {
        PermissionHandler.startPermissionFlow()
    }

    fun initializeTerminal() {
        if (!Terminal.isInitialized()) {
            try {
                Log.d(TAG, "Initializing Stripe Terminal...")
                Terminal.initTerminal(
                    activity!!, LogLevel.VERBOSE, TokenProvider(token.orEmpty()),
                    TerminalEventListener()
                )
                Log.d(TAG, "Stripe Terminal Initialized Successfully")
                result?.success(true)
            } catch (e: TerminalException) {
                Log.e(TAG, "Stripe Terminal Initialization Error: ${e.errorMessage}")
                result?.error("stripe_terminal_error", e.errorMessage, null)
            }
        } else {
            Log.d(TAG, "Stripe Terminal is already Initialized")
            result?.success(true)
        }
    }


    fun connectReader(isSimulated: Boolean) {
        this.isSimulated = isSimulated
        if (locationsList.value.locations.isEmpty()) {
            loadLocations()
        } else {
            discoverReaders()
        }
    }


    private fun loadLocations() {
        Log.d(TAG, "Loading Reader locations...")

        Terminal.getInstance().listLocations(
            ListLocationsParameters.Builder().apply {
                limit = 100
            }.build(),
            callback = object : LocationListCallback {
                override fun onFailure(e: TerminalException) {
                    Log.e(TAG, "Failed to load reader locations: ${e.errorMessage}")
                    result?.error("fetch_reader_error", e.errorMessage, null)
                }

                override fun onSuccess(locations: List<Location>, hasMore: Boolean) {
                    locationsList.value = locationsList.value.let {
                        it.copy(
                            locations = it.locations + locations,
                            hasMore = hasMore,
                            isLoading = false,
                        )
                    }

                    discoverReaders()
                }
            }
        )
    }


    fun disconnectReader() {
        if (!Terminal.isInitialized()) {
            result?.success(false)
            return
        }

        Terminal.getInstance().disconnectReader(callback = object : Callback {
            override fun onFailure(e: TerminalException) {
                Log.e(TAG, "${e.message}")
                result?.error("reader_error", "${e.message}", null)
            }

            override fun onSuccess() {
                Log.d(TAG, "Reader Disconnected")
                result?.success(true)
            }
        })
    }


    // Check if terminal connected
    fun isTerminalInitialized() {
        result?.success(Terminal.isInitialized())
    }

    // Check if reader connected or not
    fun isReaderConnected() {
        try {
            if (!Terminal.isInitialized()) {
                result?.success(false)
                return
            }
            val isConnected = Terminal.getInstance().connectedReader != null
            result?.success(isConnected)
        } catch (e: Exception) {
            result?.error("terminal_error", "${e.message}", null)
        }
    }


    // Discover available readers: User mobile phone as a Reader
    private fun discoverReaders() {
        Log.d(TAG, "Discovering Readers...")
        val config = DiscoveryConfiguration(
            timeout = 0,
            discoveryMethod = DiscoveryMethod.LOCAL_MOBILE,
            isSimulated = isSimulated,
            location = locationsList.value.locations[0].id
        )

        Terminal.getInstance().discoverReaders(config, discoveryListener = object :
            DiscoveryListener {
            override fun onUpdateDiscoveredReaders(readers: List<Reader>) {
                readers.filter { it.networkStatus != Reader.NetworkStatus.OFFLINE }
                val reader = readers[0]

                val configs =
                    ConnectionConfiguration.LocalMobileConnectionConfiguration("${locationsList.value.locations[0].id}")

                Terminal.getInstance().connectLocalMobileReader(
                    reader,
                    configs,
                    object : ReaderCallback {
                        override fun onFailure(e: TerminalException) {
                            Log.e(TAG, e.errorMessage)
                            result?.error("reader_error", e.errorMessage, null)
                        }

                        override fun onSuccess(reader: Reader) {
                            Log.d(TAG, "Reader connected Successfully....")
                            result?.success(gson.toJson(reader))
                        }
                    }
                )
            }
        }, object : Callback {
            override fun onSuccess() {
                Log.d(TAG, "Finished discovering readers")
            }

            override fun onFailure(e: TerminalException) {
                Log.e(TAG, e.errorMessage)
                result?.error("reader_error", e.errorMessage, e)
            }
        })
    }

    fun createPaymentIntent(
        secret: String,
        skipTipping: Boolean,
    ) {
        Log.d(TAG, "Creating Stripe Payment Intent...")
        SKIP_TIPPING = skipTipping

        Terminal.getInstance().retrievePaymentIntent(
            secret,
            createPaymentIntentCallback
        )
    }


    private val createPaymentIntentCallback by lazy {
        object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                Log.d(TAG, "Payment Intent created Successfully...")

                val skipTipping = SKIP_TIPPING

                val collectConfig = CollectConfiguration.Builder()
                    .skipTipping(skipTipping)
                    .build()

                Log.d(TAG, "Creating Collecting Payment method...")
                Terminal.getInstance().collectPaymentMethod(
                    paymentIntent, collectPaymentMethodCallback, collectConfig
                )
            }

            override fun onFailure(e: TerminalException) {
                Log.e(TAG, e.errorMessage)
                result?.error("payment_intent_error", e.message, null)
            }
        }
    }

    private val collectPaymentMethodCallback by lazy {
        object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                Log.d(TAG, "Processing Payment...")
                Terminal.getInstance().processPayment(paymentIntent, processPaymentCallback)
            }

            override fun onFailure(e: TerminalException) {
                Log.e(TAG, e.errorMessage)
                val paymentResult = mapOf<String, Any?>(
                    "status" to PaymentStatus.PAYMENT_ERROR,
                    "message" to e.errorMessage,
                    "data" to null,
                )
                result?.success(gson.toJson(paymentResult))
            }
        }
    }

    private val processPaymentCallback by lazy {
        object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
//                ApiClient.capturePaymentIntent(paymentIntent.id)
                Log.d(TAG, "Payment Successful: ${paymentIntent.id}")
                val paymentResult = mapOf<String, Any?>(
                    "status" to PaymentStatus.PAYMENT_SUCCESS,
                    "message" to "Payment Successful",
                    "data" to paymentIntent,
                )
                result?.success(gson.toJson(paymentResult))
            }

            override fun onFailure(e: TerminalException) {
                Log.e(TAG, e.errorMessage)
                val paymentResult = mapOf<String, Any?>(
                    "status" to PaymentStatus.PAYMENT_ERROR,
                    "message" to e.errorMessage,
                    "data" to null,
                )
                result?.success(gson.toJson(paymentResult))
            }
        }
    }


}
