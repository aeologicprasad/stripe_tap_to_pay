import 'package:flutter_test/flutter_test.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:stripe_tap_to_pay/data/payment_intent.dart';
import 'package:stripe_tap_to_pay/data/reader.dart';
import 'package:stripe_tap_to_pay/stripe_tap_to_pay_platform_interface.dart';

class MockStripeTapToPayPlatform
    with MockPlatformInterfaceMixin
    implements StripeTapToPayPlatform {
  @override
  Future<Reader> connectReader({required bool isSimulated}) {
    // TODO: implement connectReader
    throw UnimplementedError();
  }

  @override
  Future<bool> disconnectReader() {
    // TODO: implement disconnectReader
    throw UnimplementedError();
  }

  @override
  Future<bool> isTerminalInitialized() {
    // TODO: implement disconnectReader
    throw UnimplementedError();
  }

  @override
  Future<bool> isReaderConnected() {
    // TODO: implement disconnectReader
    throw UnimplementedError();
  }

  @override
  Future<void> createPayment(
      {required String secret,
      bool skipTipping = true,
      required Function(PaymentIntent? paymentIntent) onPaymentSuccess,
      required Function(String? errorMessage) onPaymentError,
      required Function() onPaymentCancelled}) {
    // TODO: implement createPayment
    throw UnimplementedError();
  }

  @override
  Future<bool> initializeStripeTerminal(
      {required String token, required String locationId}) {
    // TODO: implement initializeStripeTerminal
    throw UnimplementedError();
  }
}

void main() {
  // final StripeTapToPayPlatform initialPlatform = StripeTapToPayPlatform.instance;
  //
  // test('$MethodChannelStripeTapToPay is the default instance', () {
  //   expect(initialPlatform, isInstanceOf<MethodChannelStripeTapToPay>());
  // });
  //
  // test('getPlatformVersion', () async {
  //   StripeTapToPay stripeTapToPayPlugin = StripeTapToPay();
  //   MockStripeTapToPayPlatform fakePlatform = MockStripeTapToPayPlatform();
  //   StripeTapToPayPlatform.instance = fakePlatform;
  //
  //   expect(await stripeTapToPayPlugin.getPlatformVersion(), '42');
  // });
}
