package com.example.stripe_tap_to_pay.stripe

import android.util.Log
import com.example.stripe_tap_to_pay.data.LocationListState
import com.example.stripe_tap_to_pay.data.PaymentIntentCreationResponse
import com.example.stripe_tap_to_pay.service.ApiClient
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
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.Call
import retrofit2.Response
import io.flutter.plugin.common.MethodChannel

class StripePaymentHandler {
    private val TAG = "StripeTapToPayPlugin"
    private var SKIP_TIPPING = false
    private var result: MethodChannel.Result? = null
    private val gson = Gson()
    private val locationsList = MutableStateFlow(LocationListState())
    private var isSimulated = false

    fun connectReader(isSimulated: Boolean, result: MethodChannel.Result) {
        this.result = result
        this.isSimulated = isSimulated
        if (locationsList.value.locations.isEmpty()) {
            loadLocations()
        } else {
            discoverReaders()
        }
    }

    // Location callback : for loading locations using stripe terminal
    private val locationCallback = object : LocationListCallback {
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

    private fun loadLocations() {
        Log.d(TAG, "Loading Reader locations...")

        Terminal.getInstance().listLocations(
            ListLocationsParameters.Builder().apply {
                limit = 100
            }.build(),
            locationCallback
        )
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
                            result?.error("102", e.errorMessage, null)
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
                result?.error("102", e.errorMessage, e)
            }
        })
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

    fun createPaymentIntent(
        amount: Long,
        currency: String,
        skipTipping: Boolean,
        extendedAuth: Boolean,
        incrementalAuth: Boolean,
        result: MethodChannel.Result
    ) {
        Log.d(TAG, "Creating Stripe Payment Intent...")
        this.result = result
        SKIP_TIPPING = skipTipping

        ApiClient.createPaymentIntent(
            amount,
            currency,
            extendedAuth,
            incrementalAuth,
            callback = object : retrofit2.Callback<PaymentIntentCreationResponse> {
                override fun onResponse(
                    call: Call<PaymentIntentCreationResponse>,
                    response: Response<PaymentIntentCreationResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        Terminal.getInstance().retrievePaymentIntent(
                            response.body()?.secret!!,
                            createPaymentIntentCallback
                        )
                    } else {
                        Log.e(TAG, "Failed to get secrete key from server")
                        result?.error(
                            "secret_key_error",
                            "Failed to get secrete key from server",
                            null
                        )
                    }
                }

                override fun onFailure(
                    call: Call<PaymentIntentCreationResponse>,
                    t: Throwable
                ) {
                    Log.e(TAG, "${t.message}")
                    result?.error("secret_key_error", t.message, null)
                }
            }
        )
    }

    private val collectPaymentMethodCallback by lazy {
        object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                Log.d(TAG, "Processing Payment...")
                Terminal.getInstance().processPayment(paymentIntent, processPaymentCallback)
            }

            override fun onFailure(e: TerminalException) {
                Log.e(TAG, e.errorMessage)
                result?.success(null)
            }
        }
    }

    private val processPaymentCallback by lazy {
        object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                ApiClient.capturePaymentIntent(paymentIntent.id)
                Log.d(TAG, "Payment Successful: ${paymentIntent.id}")
                result?.success(gson.toJson(paymentIntent))
            }

            override fun onFailure(e: TerminalException) {
                Log.e(TAG, e.errorMessage)
                result?.success(null)
            }
        }
    }
}