package br.com.lapada.ble_test

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.*
import android.os.BatteryManager
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log

import androidx.annotation.NonNull

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.MethodChannel

import kotlin.collections.ArrayList

class MainActivity: FlutterActivity() {
    private val METHOD_CHANNEL = "br.com.iracema/ble_method"
    private val EVENT_CHANNEL = "br.com.iracema/ble_event"

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    private var scanning = false
    private val handler = Handler()
    private val mLeDevices: ArrayList<BluetoothDevice> = ArrayList()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    private var mScanSettings: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()
    private var mScanFilter: ScanFilter = ScanFilter.Builder()
        .setServiceUuid(ParcelUuid.fromString("a41bc296-d17a-4e14-9762-31dc0050c860"))
        .build()
    private var filters: List<ScanFilter> = arrayListOf(mScanFilter)

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        EventChannel(flutterEngine.dartExecutor, EVENT_CHANNEL).setStreamHandler(
            object : EventChannel.StreamHandler {
                private var chargingStateChangeReceiver: BroadcastReceiver? = null
                override fun onListen(arguments: Any?, events: EventSink) {
                    chargingStateChangeReceiver = createChargingStateChangeReceiver(events)
                    registerReceiver(
                        chargingStateChangeReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                    )
                }

                override fun onCancel(arguments: Any?) {
                    unregisterReceiver(chargingStateChangeReceiver)
                    chargingStateChangeReceiver = null
                }
            }
        )
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, METHOD_CHANNEL).setMethodCallHandler {
            // Note: this method is invoked on the main thread.
                call, result ->
            when (call.method) {
                "enableBluetooth" -> {
                    enableBluetooth()
                    result.success("Bluetooth Enabled")
                }
                "scanLeDevice" -> {
                    result.success(scanLeDevice())
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    /*                 Bluetooth Low Energy                 */

    private fun enableBluetooth() {
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 0)
        }
    }

    private fun scanLeDevice(): ArrayList<BluetoothDevice> {
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

        return mLeDevices
    }

    private val mScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            println("BLE// onScanResult")
            Log.d("callbackType", callbackType.toString())
            Log.d("result", result.toString())
            mLeDevices.add(result.device)
            Log.d("Device Added", "New Device Added: " + result.device)
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

    /*                 EventChannel                 */

    private fun createChargingStateChangeReceiver(events: EventSink): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                if (status == BatteryManager.BATTERY_STATUS_UNKNOWN) {
                    events.error("UNAVAILABLE", "Charging status unavailable", null)
                } else {
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL
                    events.success(if (isCharging) "charging" else "discharging")
                }
            }
        }
    }
}
