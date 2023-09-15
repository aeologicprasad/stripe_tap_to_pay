import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

class ApiClient {
  final String baseUrl;

  ApiClient(this.baseUrl);

  Future<T> post<T>(
    String path, {
    Map<String, String>? headers,
    Map<String, dynamic>? body,
  }) async {
    final Uri uri = Uri.parse('$baseUrl/$path');
    final response = await http.post(
      uri,
      headers: headers,
      body: jsonEncode(body),
    );

    if (response.statusCode == 200) {
      final jsonResponse = json.decode(response.body);
      debugPrint('API Response: $jsonResponse');
      return json.decode(response.body);
    } else {
      throw Exception('Failed to load data from the server');
    }
  }
}
