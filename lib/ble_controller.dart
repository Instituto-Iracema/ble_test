import 'dart:developer';

import 'package:flutter/services.dart';
import 'package:mobx/mobx.dart';

part 'ble_controller.g.dart';

class BLEController = _BLEController with _$BLEController;

abstract class _BLEController with Store {
  static const platform = MethodChannel('br.com.lapada/ble');

  Future<void> enableBluetooth() async {
    try {
      await platform.invokeMethod('enableBluetooth');
    } on PlatformException catch (e) {
      log('Failed to enable bluetooth: ${e.message}.');
    }
  }

  Future<void> scanLeDevice() async {
    try {
      await platform.invokeMethod('scanLeDevice');
    } on PlatformException catch (e) {
      log('Failed to scan ble device: ${e.message}.');
    }
  }
}
