import 'package:stripe_tap_to_pay/data/payment_intent.dart';

import 'data/reader.dart';
import 'stripe_tap_to_pay_platform_interface.dart';

class StripeTapToPay {
  Future<bool> initializeStripeTerminal({required String backendUrl}) {
    return StripeTapToPayPlatform.instance
        .initializeStripeTerminal(backendUrl: backendUrl);
  }

  Future<Reader> connectReader({bool isSimulated = false}) {
    return StripeTapToPayPlatform.instance
        .connectReader(isSimulated: isSimulated);
  }

  Future<bool> disconnectReader() {
    return StripeTapToPayPlatform.instance.disconnectReader();
  }

  Future<bool> isTerminalInitialized() {
    return StripeTapToPayPlatform.instance.isTerminalInitialized();
  }

  Future<bool> isReaderConnected() {
    return StripeTapToPayPlatform.instance.isReaderConnected();
  }

  Future<void> createPayment(
    int amount, {
    String currency = 'usd',
    bool skipTipping = true,
    bool extendedAuth = false,
    bool incrementalAuth = false,
    required Function(PaymentIntent? paymentIntent) onPaymentSuccess,
    required Function(String? errorMessage) onPaymentError,
    required Function() onPaymentCancelled,
  }) {
    return StripeTapToPayPlatform.instance.createPayment(amount,
        currency: currency,
        skipTipping: skipTipping,
        extendedAuth: extendedAuth,
        incrementalAuth: incrementalAuth,
        onPaymentSuccess: onPaymentSuccess,
        onPaymentError: onPaymentError,
        onPaymentCancelled: onPaymentCancelled);
  }
}
