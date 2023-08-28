import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:stripe_tap_to_pay/data/payment_intent.dart';
import 'package:stripe_tap_to_pay/stripe_tap_to_pay.dart';

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
  bool isTerminalInitialized = false;
  String? readerData;
  Future<void> initializeTerminal() async {
    try {
      final result = await _plugin.initializeStripeTerminal(
          backendUrl: 'https://example-terminal-backend-g42p.onrender.com/');
      debugPrint("Terminal Result: $result");
      isTerminalInitialized = result;
      setState(() {});
    } on PlatformException catch (e) {
      // debugPrint('$e');
      Fluttertoast.showToast(msg: e.message ?? 'Error occurred');
    }
  }

  Future<void> connectReader() async {
    try {
      final result = await _plugin.connectReader();
      readerData = result.id ?? 'connected';
      debugPrint('Reader data: ${result.id}');
      setState(() {});
    } on PlatformException catch (e) {
      // debugPrint('Error: $e');
      Fluttertoast.showToast(msg: e.message ?? 'Error occurred');
    }
  }

  Future<void> createPayment() async {
    try {
      await _plugin.createPayment(
        100,
        onPaymentSuccess: (PaymentIntent intent) {
          debugPrint('Payment success callback: ${intent.stripeAccountId}');
        },
        onPaymentError: () {
          debugPrint('Payment failed callback');
        },
      );
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
                  createPayment();
                },
                child: const Text('Create Payment')),
            const SizedBox(height: 30),
            Text('Reader: $readerData'),
          ],
        ),
      ),
    );
  }
}
