import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'ble_controller.dart';
import 'home_page.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Provider<BLEController>(
      create: (_) => BLEController(),
      child: MaterialApp(
        title: 'Flutter BLE Demo',
        theme: ThemeData(
          primarySwatch: Colors.blue,
        ),
        home: const MyHomePage(),
      ),
    );
  }
}
