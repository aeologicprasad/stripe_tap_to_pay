import 'package:stripe_tap_to_pay/data/payment_intent.dart';

import 'data/reader.dart';
import 'stripe_tap_to_pay_platform_interface.dart';

class StripeTapToPay {
  Future<bool> initializeStripeTerminal(
      {required String token, required String locationId}) {
    return StripeTapToPayPlatform.instance
        .initializeStripeTerminal(token: token, locationId: locationId);
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

  Future<void> createPayment({
    required String secret,
    bool skipTipping = true,
    required Function(PaymentIntent? paymentIntent) onPaymentSuccess,
    required Function(String? errorMessage) onPaymentError,
    required Function() onPaymentCancelled,
  }) {
    return StripeTapToPayPlatform.instance.createPayment(
      secret: secret,
      skipTipping: skipTipping,
      onPaymentSuccess: onPaymentSuccess,
      onPaymentError: onPaymentError,
      onPaymentCancelled: onPaymentCancelled,
    );
  }
}
