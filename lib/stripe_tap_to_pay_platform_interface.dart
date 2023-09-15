import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:stripe_tap_to_pay/data/payment_intent.dart';

import 'data/reader.dart';
import 'stripe_tap_to_pay_method_channel.dart';

abstract class StripeTapToPayPlatform extends PlatformInterface {
  /// Constructs a StripeTapToPayPlatform.
  StripeTapToPayPlatform() : super(token: _token);

  static final Object _token = Object();

  static StripeTapToPayPlatform _instance = MethodChannelStripeTapToPay();

  /// The default instance of [StripeTapToPayPlatform] to use.
  ///
  /// Defaults to [MethodChannelStripeTapToPay].
  static StripeTapToPayPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [StripeTapToPayPlatform] when
  /// they register themselves.
  static set instance(StripeTapToPayPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<bool> initializeStripeTerminal({required String token}) {
    throw UnimplementedError(
        'initializeStripeTerminal() has not been implemented.');
  }

  Future<Reader> connectReader({required bool isSimulated}) {
    throw UnimplementedError('connectReader() has not been implemented.');
  }

  Future<void> createPayment({
    required String secret,
    bool skipTipping = true,
    required Function(PaymentIntent? paymentIntent) onPaymentSuccess,
    required Function(String? errorMessage) onPaymentError,
    required Function() onPaymentCancelled,
  }) {
    throw UnimplementedError('createPayment() has not been implemented.');
  }

  Future<bool> disconnectReader() {
    throw UnimplementedError('disconnectReader() has not been implemented.');
  }

  Future<bool> isReaderConnected() {
    throw UnimplementedError('isReaderConnected() has not been implemented.');
  }

  Future<bool> isTerminalInitialized() {
    throw UnimplementedError(
        'isTerminalInitialized() has not been implemented.');
  }
}
