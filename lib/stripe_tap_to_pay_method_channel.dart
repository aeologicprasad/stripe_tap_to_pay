import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:stripe_tap_to_pay/data/payment_intent.dart';
import 'package:stripe_tap_to_pay/data/reader.dart';

import 'stripe_tap_to_pay_platform_interface.dart';

/// An implementation of [StripeTapToPayPlatform] that uses method channels.
class MethodChannelStripeTapToPay extends StripeTapToPayPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('stripe_tap_to_pay');

  @override
  Future<bool> initializeStripeTerminal({required String backendUrl}) async {
    final result = await methodChannel.invokeMethod<bool?>(
        'initializeStripeTerminal', {'backendUrl': backendUrl});
    return result ?? false;
  }

  @override
  Future<Reader> connectReader({required bool isSimulated}) async {
    final result = await methodChannel
        .invokeMethod('connectReader', {'isSimulated': isSimulated});
    return Reader.fromJson(json.decode(result ?? '{}'));
  }

  @override
  Future<bool> disconnectReader() async {
    final result = await methodChannel.invokeMethod('disconnectReader');
    return result ?? false;
  }

  @override
  Future<void> createPayment(
    int amount, {
    String currency = 'usd',
    bool skipTipping = true,
    bool extendedAuth = false,
    bool incrementalAuth = false,
    required Function(PaymentIntent) onPaymentSuccess,
    required Function() onPaymentError,
  }) async {
    final data = {
      'amount': amount,
      'currency': currency,
      'skipTipping': skipTipping,
      'extendedAuth': extendedAuth,
      'incrementalAuth': incrementalAuth,
    };
    final result = await methodChannel.invokeMethod('createPayment', data);
    PaymentIntent? intent;
    if (result != null) {
      intent = PaymentIntent.fromJson(json.decode(result ?? '{}'));
      onPaymentSuccess(intent);
    } else {
      onPaymentError();
    }
  }
}
