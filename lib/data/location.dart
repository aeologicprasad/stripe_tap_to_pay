class Location {
  final String? id;
  final AddressDataModel? address;
  final String? displayName;
  final bool? livemode;
  final Map<String, String>? metadata;

  Location({
    this.id,
    this.address,
    this.displayName,
    this.livemode,
    this.metadata,
  });

  factory Location.fromJson(Map<String, dynamic> json) {
    return Location(
      id: json['id'],
      address: json['address'] != null ? AddressDataModel.fromJson(json['address']) : null,
      displayName: json['displayName'],
      livemode: json['livemode'],
      metadata: Map<String, String>.from(json['metadata']),
    );
  }
}

class AddressDataModel {
  final String? city;
  final String? country;
  final String? line1;
  final String? line2;
  final String? postalCode;
  final String? state;

  AddressDataModel({
    this.city,
    this.country,
    this.line1,
    this.line2,
    this.postalCode,
    this.state,
  });

  factory AddressDataModel.fromJson(Map<String, dynamic> json) {
    return AddressDataModel(
      city: json['city'],
      country: json['country'],
      line1: json['line1'],
      line2: json['line2'],
      postalCode: json['postalCode'],
      state: json['state'],
    );
  }
}
