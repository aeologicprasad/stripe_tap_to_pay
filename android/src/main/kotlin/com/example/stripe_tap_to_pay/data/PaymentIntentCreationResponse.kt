package com.example.stripe_tap_to_pay.data

/**
 * PaymentIntentCreationResponse data model from example backend
 */
data class PaymentIntentCreationResponse(val intent: String, val secret: String)
