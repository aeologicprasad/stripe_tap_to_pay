import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:stripe_tap_to_pay/data/payment_intent.dart';
import 'package:stripe_tap_to_pay/stripe_tap_to_pay.dart';

import 'http/api_client.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Stripe Tap to Pay',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Tap to Pay HomePage'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  final _plugin = StripeTapToPay();
  final apiClient =
      ApiClient('https://example-terminal-backend-g42p.onrender.com');

  bool isTerminalInitialized = false;
  String? readerData;
  Future<void> initializeTerminal() async {
    apiClient.post('/connection_token').then((value) async {
      try {
        final result =
            await _plugin.initializeStripeTerminal(token: value['secret']);
        debugPrint("initializeTerminal: $result");
      } on PlatformException catch (e) {
        debugPrint('$e');
        Fluttertoast.showToast(msg: e.message ?? 'Error occurred');
      }
    }).onError((error, stackTrace) {
      debugPrint('initializeTerminal: $error');
      Fluttertoast.showToast(msg: error?.toString() ?? 'Error occurred');
    });
  }

  Future<void> connectReader() async {
    try {
      final result = await _plugin.connectReader(isSimulated: true);
      readerData = result.id ?? 'connected';
      debugPrint('Reader data: ${result.id}');
    } on PlatformException catch (e) {
      debugPrint('Error: $e');
      Fluttertoast.showToast(msg: e.message ?? 'Error occurred');
    }
  }

  Future<void> createPayment() async {
    try {
      apiClient.post('create_payment_intent?amount=100').then((value) async {
        await _plugin.createPayment(
          secret: value['secret'],
          onPaymentSuccess: (PaymentIntent? intent) {
            debugPrint('Payment success callback: ${intent?.stripeAccountId}');
          },
          onPaymentError: (String? errorMessage) {
            debugPrint('onPaymentError callback: $errorMessage');
          },
          onPaymentCancelled: () {
            debugPrint('onPaymentCancelled callback');
          },
        );
      }).onError((error, stackTrace) => null);
    } on PlatformException catch (e) {
      debugPrint('createPayment: $e');
      Fluttertoast.showToast(msg: e.message ?? 'Error occurred');
    }
  }

  void checkIsTerminalInitialized() async {
    try {
      final result = await _plugin.isTerminalInitialized();
      debugPrint('isTerminalInitialized: $result');
      setState(() {});
    } on PlatformException catch (e) {
      debugPrint('isTerminalInitialized: $e');
      Fluttertoast.showToast(msg: e.message ?? 'Error occurred');
    }
  }

  void isReaderConnected() async {
    try {
      final result = await _plugin.isReaderConnected();
      debugPrint('isReaderConnected: $result');
    } on PlatformException catch (e) {
      debugPrint('isReaderConnected: $e');
      Fluttertoast.showToast(msg: e.message ?? 'Error occurred');
    }
  }

  void disconnectReader() async {
    try {
      final result = await _plugin.disconnectReader();
      debugPrint('disconnectReader: $result');
      setState(() {});
    } on PlatformException catch (e) {
      // debugPrint('Error: $e');
      Fluttertoast.showToast(msg: e.message ?? 'Error occurred');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            ElevatedButton(
                onPressed: () {
                  initializeTerminal();
                },
                child: const Text('Initialize Stripe')),
            ElevatedButton(
                onPressed: () {
                  connectReader();
                },
                child: const Text('Connect Reader')),
            ElevatedButton(
                onPressed: () {
                  disconnectReader();
                },
                child: const Text('Disconnect Reader')),
            ElevatedButton(
                onPressed: () {
                  createPayment();
                },
                child: const Text('Create Payment')),
            const SizedBox(height: 30),
            Text('Reader: $readerData'),
            const Spacer(),
            ElevatedButton(
                onPressed: () {
                  checkIsTerminalInitialized();
                },
                child: const Text('Is Terminal Initialized ?')),
            ElevatedButton(
                onPressed: () {
                  isReaderConnected();
                },
                child: const Text('Is Reader Connected ?')),
          ],
        ),
      ),
    );
  }
}
