import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:permission_handler/permission_handler.dart';

import 'ble_controller.dart';
import 'bottom_bar.dart';

class MyHomePage extends StatefulWidget {
  const MyHomePage({Key? key}) : super(key: key);

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  @override
  Widget build(BuildContext context) {
    Size mediaSize = MediaQuery.of(context).size;
    var bleController = Provider.of<BLEController>(context);
    bleController.enableBluetooth();
    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
            onPressed: () async {
              PermissionStatus permissionLocationStatus =
                  await Permission.location.status;

              if (permissionLocationStatus.isDenied) {
                await Permission.location.request();
              }
            },
            icon: const Icon(Icons.perm_device_info)),
        title: const Text('BLE Test'),
        centerTitle: true,
        actions: [
          IconButton(
            onPressed: () {
              bleController.enableBluetooth();
            },
            icon: const Icon(Icons.bluetooth),
          ),
          IconButton(
            onPressed: () async {
              await bleController.scanLeDevice();
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
