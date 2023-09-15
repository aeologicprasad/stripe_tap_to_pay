package com.example.stripe_tap_to_pay.provider

import com.stripe.stripeterminal.external.callable.ConnectionTokenCallback
import com.stripe.stripeterminal.external.callable.ConnectionTokenProvider
import com.stripe.stripeterminal.external.models.ConnectionTokenException

/**
 * A simple implementation of the [ConnectionTokenProvider] interface. We just request a
 * new token from our backend simulator and forward any exceptions along to the SDK.
 */
class TokenProvider(private val token: String) : ConnectionTokenProvider {

    override fun fetchConnectionToken(callback: ConnectionTokenCallback) {
        try {
//            val token = ApiClient.createConnectionToken()
            callback.onSuccess(token)
        } catch (e: ConnectionTokenException) {
            callback.onFailure(e)
        }
    }
}
