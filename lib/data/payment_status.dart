enum PaymentStatus {
  PAYMENT_SUCCESS,
  PAYMENT_ERROR,
  PAYMENT_CANCELLED,
}

extension StringExtensions on String {
  PaymentStatus? paymentStatus() {
    switch (this) {
      case 'PAYMENT_SUCCESS':
        return PaymentStatus.PAYMENT_SUCCESS;
      case 'PAYMENT_ERROR':
        return PaymentStatus.PAYMENT_ERROR;
      case 'PAYMENT_CANCELLED':
        return PaymentStatus.PAYMENT_CANCELLED;
      default:
        return null;
    }
  }
}
