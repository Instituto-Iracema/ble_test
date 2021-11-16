package br.com.lapada.ble_test

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Intent
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import kotlin.collections.ArrayList

class MainActivity: FlutterActivity() {
    private val CHANNEL = "br.com.lapada/ble"

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    private var scanning = false
    private val handler = Handler()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    private var mScanSettings: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .build()
    private var mScanFilter: ScanFilter = ScanFilter.Builder()
        .setServiceUuid(ParcelUuid.fromString("a41bc296-d17a-4e14-9762-31dc0050c860"))
        .build()
    private var filters: List<ScanFilter> = ArrayList()

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            // Note: this method is invoked on the main thread.
                call, result ->
            if (call.method == "enableBluetooth") {
                enableBluetooth()
                result.success("Bluetooth Enabled")
            } else if (call.method == "scanLeDevice") {
                scanLeDevice()
                result.success("Scanned LE Devices")
            } else {
                result.notImplemented()
            }
        }
    }

    /*                 Bluetooth Low Energy                 */

    private fun scanLeDevice() {
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner?.stopScan(mScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner?.startScan(filters, mScanSettings, mScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner?.stopScan(mScanCallback)
        }
    }

    private val mScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            println("BLE// onScanResult")
            Log.d("callbackType", callbackType.toString())
            Log.d("result", result.toString())
            val btDevice: BluetoothDevice = result.device
            connectToDevice(btDevice)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            println("BLE// onBatchScanResults")
            for (sr in results) {
                Log.i("ScanResult - Results", sr.toString())
            }
        }

        override fun onScanFailed(errorCode: Int) {
            println("BLE// onScanFailed")
            Log.e("Scan Failed", "Error Code: $errorCode")
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        var bluetoothGatt: BluetoothGatt? = null

        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                Log.i("BluetoothProfile", newState.toString())
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                Log.i("BluetoothProfile", newState.toString())
            }
        }
    }

    private fun enableBluetooth() {
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 0)
        }
    }

    /*                 GATT Service                 */

    private fun connectToDeviceGATT(device: BluetoothDevice): BluetoothGatt? {
        return device.connectGatt(this, false, bluetoothGattCallback)
    }
}
