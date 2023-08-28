import 'package:flutter/foundation.dart';

class PaymentIntent {
  final bool allowOffline;
  final int amount;
  final int amountCapturable;
  final int amountReceived;
  final String application;
  final int applicationFeeAmount;
  final int canceledAt;
  final String cancellationReason;
  final String captureMethod;
  final String? clientSecret;
  final String? confirmationMethod;
  final int created;
  final String? currency;
  final String? customer;
  final String? description;
  final String? invoice;
  final bool livemode;
  final Map<String, String>? metadata;
  final String? onBehalfOf;
  final String? receiptEmail;
  final String? review;
  final String? setupFutureUsage;
  final String? statementDescriptor;
  final String? transferGroup;
  final int? amountTip;
  final String? statementDescriptorSuffix;
  final String? stripeAccountId;

  PaymentIntent({
    required this.allowOffline,
    required this.amount,
    required this.amountCapturable,
    required this.amountReceived,
    required this.application,
    required this.applicationFeeAmount,
    required this.canceledAt,
    required this.cancellationReason,
    required this.captureMethod,
    required this.clientSecret,
    required this.confirmationMethod,
    required this.created,
    required this.currency,
    required this.customer,
    required this.description,
    required this.invoice,
    required this.livemode,
    required this.metadata,
    required this.onBehalfOf,
    required this.receiptEmail,
    required this.review,
    required this.setupFutureUsage,
    required this.statementDescriptor,
    required this.transferGroup,
    required this.amountTip,
    required this.statementDescriptorSuffix,
    required this.stripeAccountId,
  });

  factory PaymentIntent.fromJson(Map<String, dynamic> json) =>
      _$PaymentIntentFromJson(json);

  Map<String, dynamic> toJson() => _$PaymentIntentToJson(this);
}

PaymentIntent _$PaymentIntentFromJson(Map<String, dynamic> json) {
  return PaymentIntent(
    allowOffline: json['allowOffline'] as bool,
    amount: json['amount'] as int,
    amountCapturable: json['amountCapturable'] as int,
    amountReceived: json['amountReceived'] as int,
    application: json['application'] as String,
    applicationFeeAmount: json['applicationFeeAmount'] as int,
    canceledAt: json['canceledAt'] as int,
    cancellationReason: json['cancellationReason'] as String,
    captureMethod: json['captureMethod'] as String,
    clientSecret: json['clientSecret'] as String?,
    confirmationMethod: json['confirmationMethod'] as String?,
    created: json['created'] as int,
    currency: json['currency'] as String?,
    customer: json['customer'] as String?,
    description: json['description'] as String?,
    invoice: json['invoice'] as String?,
    livemode: json['livemode'] as bool,
    metadata: json['metadata'] as Map<String, String>?,
    onBehalfOf: json['onBehalfOf'] as String?,
    receiptEmail: json['receiptEmail'] as String?,
    review: json['review'] as String?,
    setupFutureUsage: json['setupFutureUsage'] as String?,
    statementDescriptor: json['statementDescriptor'] as String?,
    transferGroup: json['transferGroup'] as String?,
    amountTip: json['amountTip'] as int?,
    statementDescriptorSuffix: json['statementDescriptorSuffix'] as String?,
    stripeAccountId: json['stripeAccountId'] as String?,
  );
}

Map<String, dynamic> _$PaymentIntentToJson(PaymentIntent instance) =>
    <String, dynamic>{
      'allowOffline': instance.allowOffline,
      'amount': instance.amount,
      'amountCapturable': instance.amountCapturable,
      'amountReceived': instance.amountReceived,
      'application': instance.application,
      'applicationFeeAmount': instance.applicationFeeAmount,
      'canceledAt': instance.canceledAt,
      'cancellationReason': instance.cancellationReason,
      'captureMethod': instance.captureMethod,
      'clientSecret': instance.clientSecret,
      'confirmationMethod': instance.confirmationMethod,
      'created': instance.created,
      'currency': instance.currency,
      'customer': instance.customer,
      'description': instance.description,
      'invoice': instance.invoice,
      'livemode': instance.livemode,
      'metadata': instance.metadata,
      'onBehalfOf': instance.onBehalfOf,
      'receiptEmail': instance.receiptEmail,
      'review': instance.review,
      'setupFutureUsage': instance.setupFutureUsage,
      'statementDescriptor': instance.statementDescriptor,
      'transferGroup': instance.transferGroup,
      'amountTip': instance.amountTip,
      'statementDescriptorSuffix': instance.statementDescriptorSuffix,
      'stripeAccountId': instance.stripeAccountId,
    };
