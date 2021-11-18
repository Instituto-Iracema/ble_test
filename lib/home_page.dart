import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

import 'bottom_bar.dart';

class MyHomePage extends StatefulWidget {
  const MyHomePage({Key? key}) : super(key: key);

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  static const platform = MethodChannel('br.com.iracema/ble_method');

  String textReceived = '';

  Future<void> enableBluetooth() async {
    try {
      await platform.invokeMethod('enableBluetooth');
    } on PlatformException catch (e) {
      log('Failed to enable bluetooth: ${e.message}.');
    }
  }

  Future<dynamic> scanLeDevice() async {
    var bleDevicesList;

    try {
      bleDevicesList = await platform.invokeMethod('scanLeDevice');
      log('bleDevicesList: $bleDevicesList');
    } on PlatformException catch (e) {
      bleDevicesList = 'Failed to scan ble device: ${e.message}.';
    }

    return bleDevicesList;
  }

  Future<void> requestPermissions() async => await [
        Permission.location,
        Permission.bluetooth,
      ].request().then((value) => log('permissions status: $value'));

  @override
  void initState() {
    requestPermissions();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final mediaSize = MediaQuery.of(context).size;
    return Scaffold(
      appBar: AppBar(
        title: const Text('BLE Test'),
        centerTitle: true,
        actions: [
          IconButton(
            onPressed: () async {
              if (await Permission.bluetooth.isGranted) {
                await enableBluetooth();
              } else {
                await requestPermissions();
              }
            },
            icon: const Icon(Icons.bluetooth),
          ),
          IconButton(
            onPressed: () async {
              await scanLeDevice();
            },
            icon: const Icon(Icons.send),
          ),
        ],
      ),
      body: Stack(
        children: [
          Container(
            margin: const EdgeInsets.only(left: 12, top: 5),
            child: const Text(
              'Not Connected',
              style: TextStyle(color: Colors.black),
            ),
          ),
          Container(
            margin: const EdgeInsets.only(
              left: 10,
              right: 10,
              top: 25,
              bottom: 90,
            ),
            padding: const EdgeInsets.all(10),
            width: mediaSize.width,
            height: mediaSize.height,
            decoration: BoxDecoration(
              border: Border.all(
                color: Colors.blue,
                style: BorderStyle.solid,
              ),
              borderRadius: BorderRadius.circular(5),
            ),
            child: Text(textReceived),
          ),
          const Positioned(
            left: 0,
            right: 0,
            bottom: 0,
            child: BottomBar(),
          ),
        ],
      ),
    );
  }
}
