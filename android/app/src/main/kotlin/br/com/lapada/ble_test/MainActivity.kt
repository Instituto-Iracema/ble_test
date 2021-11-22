package br.com.lapada.ble_test

import android.bluetooth.*
import android.bluetooth.BluetoothAdapter.STATE_CONNECTED
import android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED
import android.bluetooth.le.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.util.*
import kotlin.collections.ArrayList
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattService

class MainActivity: FlutterActivity() {
    private val CHANNEL = "br.com.lapada/ble"

    private val BluetoothService: BluetoothLeService? = null
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    private var scanning = false
    private var connectionState = STATE_DISCONNECTED
    private val handler = Handler()
    var bluetoothGatt: BluetoothGatt? = null
    val mGattCharacteristics: MutableList<BluetoothGattCharacteristic> = mutableListOf()
    val charas: MutableList<BluetoothGattCharacteristic> = mutableListOf()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    private var mScanSettings: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .build()
    private var mScanFilter: ScanFilter = ScanFilter.Builder()
        .setServiceUuid(ParcelUuid.fromString("a41bc296-d17a-4e14-9762-31dc0050c860"))
        .build()
    private var filters: List<ScanFilter> = arrayListOf(mScanFilter)

    private var connected = false
    private val LIST_NAME = "NAME"
    private val LIST_UUID = "UUID"


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
        writeData(bluetoothGatt)
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                broadcastUpdate(ACTION_GATT_CONNECTED)
                connectionState = STATE_CONNECTED
                Log.i("BluetoothProfile", newState.toString())
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                broadcastUpdate(ACTION_GATT_DISCONNECTED)
                connectionState = STATE_DISCONNECTED
                Log.i("BluetoothProfile", newState.toString())
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
                Log.w(BluetoothLeService.TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            broadcastUpdate(BluetoothLeService.ACTION_DATA_AVAILABLE)
        }
    }

    private fun enableBluetooth() {
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 0)
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }



    /*                Discover services              */

    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothLeService.ACTION_GATT_CONNECTED -> {
                    connected = true
                }
                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    connected = false
                }
                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    // Show all the supported services and characteristics on the user interface.
                    displayGattServices(BluetoothService?.supportedGattServices)
                }
            }
        }
    }

    /*                Read BLE characteristics                */

    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) return
        var uuid: String
        val unknownServiceString: String = resources.getString(R.string.unknown_service)
        val unknownCharaString: String = resources.getString(R.string.unknown_characteristic)
        val gattServiceData: MutableList<HashMap<String, String>> = mutableListOf()
        val gattCharacteristicData: MutableList<ArrayList<HashMap<String, String>>> =
            mutableListOf()

        // Loops through available GATT Services.
        gattServices.forEach { gattService ->
            val currentServiceData = HashMap<String, String>()
            uuid = gattService.uuid.toString()
            currentServiceData[LIST_NAME] = SampleGattAttributes.lookup(uuid, unknownServiceString)
            currentServiceData[LIST_UUID] = uuid
            gattServiceData += currentServiceData

            val gattCharacteristicGroupData: ArrayList<HashMap<String, String>> = arrayListOf()
            val gattCharacteristics = gattService.characteristics

            // Loops through available Characteristics.
            gattCharacteristics.forEach { gattCharacteristic ->
                charas += gattCharacteristic
                val currentCharaData: HashMap<String, String> = hashMapOf()
                uuid = gattCharacteristic.uuid.toString()
                currentCharaData[LIST_NAME] = SampleGattAttributes.lookup(uuid, unknownCharaString)
                currentCharaData[LIST_UUID] = uuid
                gattCharacteristicGroupData += currentCharaData
            }
            mGattCharacteristics += charas
            gattCharacteristicData += gattCharacteristicGroupData
        }
    }

    /*                Write BLE characteristics                */

    private fun writeData(bluetoothGatt: BluetoothGatt?){
        val serviceUuid = UUID.fromString("a41bc296-d17a-4e14-9762-31dc0050c850")
        val characteristicUuid = UUID.fromString("a41bc296-d17a-4e14-9762-31dc0050c851")

        val mSVC: BluetoothGattService = bluetoothGatt!!.getService(serviceUuid)
        val mCH = mSVC.getCharacteristic(characteristicUuid)
        mCH.setValue("<ACK PUT>")
        bluetoothGatt.writeCharacteristic(mCH)
    }

    companion object {
        const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"

        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTED = 2
    }
}
