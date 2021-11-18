import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:provider/provider.dart';

import 'ble_controller.dart';
import 'bottom_bar.dart';

class MyHomePage extends StatefulWidget {
  const MyHomePage({Key? key}) : super(key: key);

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
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
    final bleController = Provider.of<BLEController>(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('BLE Test'),
        centerTitle: true,
        actions: [
          IconButton(
            onPressed: () async {
              if (await Permission.bluetooth.isGranted) {
                await bleController.enableBluetooth();
              } else {
                await requestPermissions();
              }
            },
            icon: const Icon(Icons.bluetooth),
          ),
          IconButton(
            onPressed: () async => await bleController.scanLeDevice(),
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
            child: Container(),
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
