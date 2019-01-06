package com.dreamwalker.myapplication103.activity.device.esp32

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dreamwalker.myapplication103.R
import com.dreamwalker.myapplication103.adapter.device.scan.DeviceItemClickListener
import com.dreamwalker.myapplication103.adapter.device.scan.DeviceScanAdapterV2
import com.dreamwalker.myapplication103.intent.AppConst.BLE_ADDRESS_INTENT
import com.dreamwalker.myapplication103.model.Device
import com.dreamwalker.myapplication103.service.esp32.nfcreader.DeviceConst
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_device_scan.*
import org.jetbrains.anko.toast
import java.util.*

class DeviceScanActivity : AppCompatActivity(), DeviceItemClickListener {


    private val PERMISSION_REQUEST_COARSE_LOCATION = 1000
    private val REQUEST_ENABLE_BT = 1
    private val SCAN_PERIOD: Long = 10000 // Stops scanning after 10 seconds.

    lateinit var bluetoothManager: BluetoothManager
    var bluetoothAdapter: BluetoothAdapter? = null
    var bluetoothLeScanner: BluetoothLeScanner? = null

    lateinit var bleDeviceList: ArrayList<BluetoothDevice>
    lateinit var scanResultArrayList: ArrayList<ScanResult>

    internal lateinit var handler: Handler

    internal var mScanning: Boolean = false

    lateinit var adapter: DeviceScanAdapterV2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_scan)

        setSupportActionBar(tool_bar)
        tool_bar.setNavigationOnClickListener { v ->
            val builder = AlertDialog.Builder(this@DeviceScanActivity)
            builder.setTitle("알림")
            builder.setMessage("장비(RFID 리더기) 검색을 종료하시겠어요?")
            builder.setPositiveButton(android.R.string.yes) {
                dialog, which ->
                dialog.dismiss()
                finish()
            }
            builder.setNegativeButton(android.R.string.no) { dialog, which -> dialog.dismiss() }
            builder.show()
        }

        bleDeviceList = ArrayList()
        scanResultArrayList = ArrayList()
        handler = Handler()


        with(recyclerView){
            layoutManager = LinearLayoutManager(this@DeviceScanActivity)
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(this@DeviceScanActivity, DividerItemDecoration.VERTICAL))
        }


        checkScanPermission()
        getBluetoothAdapter()
        checkBluetoothSupport()
        checkBleSupport()


        if (!(bluetoothAdapter?.isEnabled!!)) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            //            if (audioManager.getRingerMode() != AudioManager.ADJUST_MUTE){
            //                mediaPlayer.start();
            //            }
            adapter = DeviceScanAdapterV2(bleDeviceList, scanResultArrayList, this)
            adapter.setDeviceItemClickListener(this)
            recyclerView.adapter = adapter
            scanLeDevice(true)

            //            StateButton.setText("STOP");
        }




    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun checkScanPermission() {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.permission_alert_title))
            builder.setMessage(getString(R.string.permission_alert_message))
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setOnDismissListener { dialog ->
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_COARSE_LOCATION)
                dialog.dismiss()
            }
            builder.show()
        }
    }

    private fun checkBleSupport() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            toast("BLE를 지원하지 않는 기종입니다.")
            finish()
        }
    }

    private fun getBluetoothAdapter() {
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

    }

    private fun checkBluetoothSupport() {

        if (bluetoothAdapter == null) {
            toast("블루투스를 지원하지 않습니다.")
            finish()
        }

    }


    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            //            super.onScanResult(callbackType, result);
            val device = result.device


            // TODO: 2018-10-12 중복 방지
            if (bleDeviceList.size < 1) {
                bleDeviceList.add(device)
                scanResultArrayList.add(result)
                adapter.notifyDataSetChanged()
            } else {
                var flag = true
                for (i in bleDeviceList.indices) {
                    if (device.address == bleDeviceList[i].address) {
                        flag = false
                    }
                }
                if (flag) {
                    bleDeviceList.add(device)
                    scanResultArrayList.add(result)
                    adapter.notifyDataSetChanged()
                }
            }

            //String address = device.getAddress();
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
        }
    }


    private fun scanLeDevice(enable: Boolean) {
        // 만약 블루투스가 활성화 되어잇지 않다면
        if (!(bluetoothAdapter?.isEnabled!!)) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {

            if (enable) {
                handler.postDelayed({
                    //                    Log.e(TAG, "scanLeDevice: ---> " + "Scan Handler Done!!")
                    mScanning = false
                    bluetoothLeScanner?.stopScan(leScanCallback)
                    invalidateOptionsMenu()
                    //                        StateButton.setText("SCAN");
                    //                        animationView.cancelAnimation();
                    //                        animationView.setFrame(0);
                }, SCAN_PERIOD)
                mScanning = true
                startNEWBTLEDiscovery()
                invalidateOptionsMenu()
            } else {
                mScanning = false
                bluetoothLeScanner?.stopScan(leScanCallback)
                invalidateOptionsMenu()
            }

        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startNEWBTLEDiscovery() {
        // Only use new API when user uses Lollipop+ device
        if (bluetoothLeScanner == null) {
            getBluetoothAdapter()
        } else {
            bluetoothLeScanner?.startScan(getScanFilters(), getScanSettings(), leScanCallback)
        }

    }

    private fun getScanFilters(): List<ScanFilter> {
        val allFilters = ArrayList<ScanFilter>()
        val scanFilter0 = ScanFilter.Builder().setDeviceName(DeviceConst.DEVICE_NAME).build()
        allFilters.add(scanFilter0)
        //        ScanFilter scanFilter1 = new ScanFilter.Builder().setDeviceName("").build();
        //        for (DeviceCoordinator coordinator : DeviceHelper.getInstance().getAllCoordinators()) {
        //            allFilters.addAll(coordinator.createBLEScanFilters());
        //        }
        return allFilters
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getScanSettings(): ScanSettings {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ScanSettings.Builder()
                    .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setMatchMode(android.bluetooth.le.ScanSettings.MATCH_MODE_STICKY)
                    .build()
        } else {
            ScanSettings.Builder()
                    .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_device_scan, menu)

        if (!mScanning) {
            menu?.findItem(R.id.cancel)?.isVisible = false
            menu?.findItem(R.id.scan)?.isVisible = true
        } else {
            menu?.findItem(R.id.cancel)?.isVisible = true
            menu?.findItem(R.id.scan)?.isVisible = false
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.scan -> {
                bleDeviceList.clear()
                scanResultArrayList.clear()
                adapter.notifyDataSetChanged()
                scanLeDevice(true)
            }

            R.id.cancel -> scanLeDevice(false)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO: 2018-11-02 권한이 허가되면 스캔을 시작합니다.
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since location access has not been granted, " + "this app will not be able to discover BLE Device when in the background.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener { dialog ->
                        dialog.dismiss()
                        finish()
                    }
                    builder.show()
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish()
        }
    }


    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter?.isEnabled!!) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            //            adapter = new DeviceScanAdapter(bleDeviceList, this);
            //            recyclerView.setAdapter(adapter);
            //            scanLeDevice(true);
            //            StateButton.setText("STOP");
        }
    }

    override fun onPause() {
        scanLeDevice(false)
        bleDeviceList.clear()
        adapter.notifyDataSetChanged()
        super.onPause()
    }

    override fun onItemClick(v: View?, position: Int) {


        val deviceAddress = bleDeviceList[position].address
        val deviceName = bleDeviceList[position].name
        val userDeviceList = ArrayList<Device>()
        userDeviceList.add(Device(deviceName, deviceAddress))

        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("장치 연결 알림")
        alertDialog.setMessage(deviceName + "장치와 연결하여 RFID 정보를 확인합니다.")
        alertDialog.setPositiveButton(android.R.string.ok) {
            dialog, which ->

            Paper.book("user").write("device", userDeviceList)
            dialog.dismiss()

            val intent = Intent(this@DeviceScanActivity, DeviceControlActivity::class.java)
            intent.putExtra(BLE_ADDRESS_INTENT, deviceAddress)
            startActivity(intent)
            finish()
        }

        alertDialog.show()


    }

    override fun onItemLongClick(v: View?, position: Int) {

    }


}
