import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:stripe_tap_to_pay/data/PaymentResult.dart';
import 'package:stripe_tap_to_pay/data/payment_intent.dart';
import 'package:stripe_tap_to_pay/data/payment_status.dart';
import 'package:stripe_tap_to_pay/data/reader.dart';

import 'stripe_tap_to_pay_platform_interface.dart';

/// An implementation of [StripeTapToPayPlatform] that uses method channels.
class MethodChannelStripeTapToPay extends StripeTapToPayPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('stripe_tap_to_pay');

  @override
  Future<bool> initializeStripeTerminal({required String token}) async {
    final result = await methodChannel
        .invokeMethod<bool?>('initializeStripeTerminal', {'token': token});
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
  Future<bool> isTerminalInitialized() async {
    final result = await methodChannel.invokeMethod('isTerminalInitialized');
    return result ?? false;
  }

  @override
  Future<bool> isReaderConnected() async {
    final result = await methodChannel.invokeMethod('isReaderConnected');
    return result ?? false;
  }

  @override
  Future<void> createPayment({
    required String secret,
    bool skipTipping = true,
    required Function(PaymentIntent? paymentIntent) onPaymentSuccess,
    required Function(String? errorMessage) onPaymentError,
    required Function() onPaymentCancelled,
  }) async {
    final data = {
      'secret': secret,
      'skipTipping': skipTipping,
    };
    final result = await methodChannel.invokeMethod('createPayment', data);
    PaymentResult paymentResult =
        PaymentResult.fromJson(json.decode(result ?? '{}'));
    if (paymentResult.status == PaymentStatus.PAYMENT_SUCCESS) {
      onPaymentSuccess(paymentResult.data);
    } else if (paymentResult.status == PaymentStatus.PAYMENT_SUCCESS) {
      onPaymentError(paymentResult.message);
    } else {
      onPaymentCancelled();
    }
  }
}
