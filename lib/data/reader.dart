class Reader {
  // final DeviceType deviceType;
  // final LocationStatus locationStatus;
  final String? id;
  final bool? isSimulated;
  // final NetworkStatus? networkStatus;
  final String? rawSerialNumber;
  final String? label;
  final String? deviceSwVersion;
  final String? baseUrl;
  final String? ipAddress;
  final bool? livemode;
  // final Location? location;

  Reader({
    // required this.deviceType,
    // required this.locationStatus,
    this.id,
    required this.isSimulated,
    // this.networkStatus,
    this.rawSerialNumber,
    this.label,
    this.deviceSwVersion,
    this.baseUrl,
    this.ipAddress,
    this.livemode,
    // this.location,
  });

  factory Reader.fromJson(Map<String, dynamic> json) {
    return Reader(
      // deviceType: json['deviceType'],
      // locationStatus: json['locationStatus'],
      id: json['id'],
      isSimulated: json['isSimulated'],
      // networkStatus: json['networkStatus'],
      rawSerialNumber: json['rawSerialNumber'],
      label: json['label'],
      deviceSwVersion: json['deviceSwVersion'],
      baseUrl: json['baseUrl'],
      ipAddress: json['ipAddress'],
      livemode: json['livemode'],
      // location: json['location'],
    );
  }
}
