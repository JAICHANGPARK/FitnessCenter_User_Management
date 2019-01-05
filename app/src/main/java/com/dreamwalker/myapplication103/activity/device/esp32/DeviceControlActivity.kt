package com.dreamwalker.myapplication103.activity.device.esp32

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dreamwalker.myapplication103.R
import com.dreamwalker.myapplication103.activity.UserRegisterActivity
import com.dreamwalker.myapplication103.intent.AppConst.BLE_ADDRESS_INTENT
import com.dreamwalker.myapplication103.intent.AppConst.NFC_TAG_ID_INTENT
import com.dreamwalker.myapplication103.service.esp32.nfcreader.ESPBleService
import kotlinx.android.synthetic.main.activity_device_control.*
import org.jetbrains.anko.toast

class DeviceControlActivity : AppCompatActivity() {

    private val REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1000


    lateinit var mDeviceAddress: String
    private var mBluetoothLeService: ESPBleService? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private lateinit var bluetoothManager: BluetoothManager

    private var mConnected = false

    var count = 0
    var tagID : StringBuilder? = null
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            val binder = service as ESPBleService.LocalBinder
            mBluetoothLeService = binder.service
            if (!mBluetoothLeService!!.initialize()) {
                finish()
            }
            mBluetoothLeService!!.connect(mDeviceAddress)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBluetoothLeService = null
        }
    }


    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (ESPBleService.ACTION_GATT_CONNECTED == action) {
                mConnected = true
                ble_state_textview.text = "장치와 연결됨"
//                textView.append("연결됨" + "\n")
//                updateConnectionState(R.string.connected)
                invalidateOptionsMenu()
            } else if (ESPBleService.ACTION_GATT_DISCONNECTED == action) {
                ble_state_textview.text = "연결 해제"
                mConnected = false
//                textView.append("연결 해제"+ "\n")
//                updateConnectionState(R.string.disconnected)
                invalidateOptionsMenu()
//                clearUI()
            } else if (ESPBleService.ACTION_GATT_SERVICES_DISCOVERED == action) {
                // Show all the supported services and characteristics on the user interface.
//                displayGattServices(mBluetoothLeService.getSupportedGattServices())
//                textView.append("서비스 특성 탐색 완료" + "\n")
            } else if (ESPBleService.ACTION_DATA_AVAILABLE == action) {
                val values = intent.getStringExtra(ESPBleService.EXTRA_DATA)

                if (values != null) {
                    tag_textview.text = values
                    tagID?.append(values)
                    val trimsValue = values.split(",")
                    count++
                }

//                textView.append(intent.getStringExtra(RealTimeBluetoothLeService.EXTRA_DATA) + "\n")
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA))
            } else if (ESPBleService.ACTION_REAL_TIME_START_PHASE == action) {
//                Toast.makeText(applicationContext, "인증 시작 ", Toast.LENGTH_SHORT).show()
            } else if (ESPBleService.ACTION_REAL_TIME_FIRST_PHASE == action) {
//                Toast.makeText(applicationContext, "암호화 인증 완료 ", Toast.LENGTH_SHORT).show()
            } else if (ESPBleService.ACTION_REAL_TIME_SECOND_PHASE == action) {
//                Toast.makeText(applicationContext, "시간 동기화 완료 ", Toast.LENGTH_SHORT).show()
            } else if (ESPBleService.ACTION_REAL_TIME_FINAL_PHASE == action) {
                toast("모든 인증 처리 완료 ")

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_control)

        mDeviceAddress = intent.getStringExtra(BLE_ADDRESS_INTENT)
        tagID = StringBuilder()

        checkPermission()
        checkBLESupport()
        val checkBluetoothEnableFlag = checkBluetoothEnable()

        if (checkBluetoothEnableFlag) {
            val gattServiceIntent = Intent(this, ESPBleService::class.java)
            bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
        }

        read_button.setOnClickListener {
            tagID?.setLength(0)

            if (mBluetoothLeService != null) {
                val writeCheck = mBluetoothLeService!!.readCardInformation()

                if (writeCheck) {
                    toast("프로토콜 전송 성공")
                } else {
                    toast("프로토콜 전송 실패")
                }
            }

        }

        delete_button.setOnClickListener {
            if (mBluetoothLeService != null) {
                val writeCheck = mBluetoothLeService!!.deleteCardInformation()

                if (writeCheck) {
                    toast("프로토콜 전송 성공")
                } else {
                    toast("프로토콜 전송 실패")
                }
            }
        }
        user_register_button.setOnClickListener {
            val registrationIntent = Intent(this, UserRegisterActivity::class.java)
            registrationIntent.putExtra(NFC_TAG_ID_INTENT, tagID.toString())
            startActivity(registrationIntent)
//            finish()
        }

        user_search_button.setOnClickListener {

        }


    }

    override fun onResume() {
        super.onResume()

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())

        if (mBluetoothLeService != null) {
            val result = mBluetoothLeService!!.connect(mDeviceAddress)
            Log.d("Mains", "Connect request result=$result")
        }

    }

    override fun onPause() {
        super.onPause()
        // TODO: 2018-07-06 브로드케스트 리시버는 꼭 해제해준다. 하지 않게되면 메모리 점유가 진행되어 메모리 예외가 발생한다. - 박제창
        unregisterReceiver(mGattUpdateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()

        // TODO: 2018-07-06 서비스는 꼭 unbind 해준다. - 박제창
        unbindService(mServiceConnection)
        mBluetoothLeService = null
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ESPBleService.ACTION_GATT_CONNECTED)
        intentFilter.addAction(ESPBleService.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(ESPBleService.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(ESPBleService.ACTION_DATA_AVAILABLE)

        intentFilter.addAction(ESPBleService.ACTION_REAL_TIME_START_PHASE)
        intentFilter.addAction(ESPBleService.ACTION_REAL_TIME_FIRST_PHASE)
        intentFilter.addAction(ESPBleService.ACTION_REAL_TIME_SECOND_PHASE)
        intentFilter.addAction(ESPBleService.ACTION_REAL_TIME_FINAL_PHASE)
        return intentFilter
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_PERMISSION_ACCESS_COARSE_LOCATION)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_ACCESS_COARSE_LOCATION ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "스캔 권한을 얻었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "권한이 거부됬습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }

    private fun checkBLESupport() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun checkBluetoothEnable(): Boolean {
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
        // Checks if Bluetooth is supported on the device.
        return if (mBluetoothAdapter == null) {
//            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show()
            finish()
            false
        } else {
            true
        }

    }

}
