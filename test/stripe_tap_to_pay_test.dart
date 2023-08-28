import 'package:flutter_test/flutter_test.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:stripe_tap_to_pay/data/payment_intent.dart';
import 'package:stripe_tap_to_pay/data/reader.dart';
import 'package:stripe_tap_to_pay/stripe_tap_to_pay_platform_interface.dart';

class MockStripeTapToPayPlatform
    with MockPlatformInterfaceMixin
    implements StripeTapToPayPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future<Reader> connectReader() {
    // TODO: implement connectReader
    throw UnimplementedError();
  }

  @override
  Future<bool> initializeStripeTerminal({required String backendUrl}) {
    // TODO: implement initializeStripeTerminal
    throw UnimplementedError();
  }

  @override
  Future<PaymentIntent> createPayment(
    int amount, {
    String currency = 'USD',
    bool skipTipping = true,
    bool extendedAuth = false,
    bool incrementalAuth = false,
    required Function(PaymentIntent) onPaymentSuccess,
    required Function() onPaymentError,
  }) {
    // TODO: implement createPayment
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
