import 'package:stripe_tap_to_pay/data/payment_intent.dart';
import 'package:stripe_tap_to_pay/data/payment_status.dart';

class PaymentResult {
  PaymentStatus? status;
  String? message;
  PaymentIntent? data;

  PaymentResult({
    required this.status,
    required this.message,
    required this.data,
  });

  factory PaymentResult.fromJson(Map<String, dynamic> json) => PaymentResult(
        status: json["status"]?.toString().paymentStatus(),
        message: json["message"],
        data:
            json["data"] != null ? PaymentIntent.fromJson(json['data']) : null,
      );

  Map<String, dynamic> toJson() => {
        "status": status,
        "message": message,
        "data": data?.toJson(),
      };
}
