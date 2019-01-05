/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreamwalker.myapplication103.service.esp32.nfcreader;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;



import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class ESPBleService extends Service {
    private final static String TAG = ESPBleService.class.getSimpleName();

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

    public final static String ACTION_SERVICE_START_SIGNAL = "com.example.bluetooth.le.ACTION_SERVICE_START_SIGNAL";
    public final static String ACTION_SERVICE_SCAN_DONE = "com.example.bluetooth.le.ACTION_SERVICE_SCAN_DONE";
    public final static String ACTION_FIRST_PHASE_DONE = "com.example.bluetooth.le.ACTION_FIRST_PHASE_DONE";
    public final static String ACTION_SECOND_PHASE_DONE = "com.example.bluetooth.le.ACTION_SECOND_PHASE_DONE";

    public final static String ACTION_REAL_TIME_START_PHASE = "com.example.bluetooth.le.ACTION_REAL_TIME_START_PHASE"; // 인증 시작
    public final static String ACTION_REAL_TIME_FIRST_PHASE = "com.example.bluetooth.le.ACTION_REAL_TIME_FIRST_PHASE"; // 암호화 인증처리
    public final static String ACTION_REAL_TIME_SECOND_PHASE = "com.example.bluetooth.le.ACTION_REAL_TIME_SECOND_PHASE"; // 날짜 인증
    public final static String ACTION_REAL_TIME_FINAL_PHASE = "com.example.bluetooth.le.ACTION_REAL_TIME_FINAL_PHASE"; // a모든 인증 완료 처리

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;


    BluetoothGattCharacteristic mHeartRateMeasurementCharacteristic;
    BluetoothGattCharacteristic mRealtimeWriteCharacteristic;  // 쓰기 위한 특성
    BluetoothGattCharacteristic mTreadmillCharacteristic;
    BluetoothGattCharacteristic mRealtimeCharacteristic;

    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(ESP32GattAttributes.HEART_RATE_MEASUREMENT);

    boolean firstPhaseCheckerFlag = false;
    boolean secondPhaseCheckerFlag = false;
    boolean dataResultDescriptor = false;
    boolean realTimeDescriptor = false;

    private final BroadcastReceiver mySyncReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case ACTION_SERVICE_START_SIGNAL:
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mRealtimeWriteCharacteristic != null) {
                                firstPhaseCheckerFlag = startRealTimeSignal(mRealtimeWriteCharacteristic);
                                Log.e(TAG, "onReceive: ACTION_SERVICE_START_SIGNAL --> " + firstPhaseCheckerFlag);
//
//                                if (firstPhaseCheckerFlag) {
//                                    broadcastUpdate(ACTION_REAL_TIME_START_PHASE);
//                                }
                            }
                        }
                    }, 2000);

                    break;
                case ACTION_SERVICE_SCAN_DONE:
                    Log.e(TAG, "onReceive: " + "서비스 스캔 끝 : 동기화 시작 --> 장비 인증 시작");
//
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mRealtimeWriteCharacteristic != null) {
                                firstPhaseCheckerFlag = deviceAuth(mRealtimeWriteCharacteristic);
                                Log.e(TAG, "onReceive: ACTION_SERVICE_SCAN_DONE --> " + firstPhaseCheckerFlag);

//                                if (firstPhaseCheckerFlag) {
//                                    broadcastUpdate(ACTION_REAL_TIME_FIRST_PHASE);
//                                }
                            }

                        }
                    }, 2000);


                    break;

                case ACTION_FIRST_PHASE_DONE:
                    Log.e(TAG, "onReceive: " + "날짜 시간 동기화 시작 ");

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mRealtimeWriteCharacteristic != null) {
                                secondPhaseCheckerFlag = syncDateTimeToDevice(mRealtimeWriteCharacteristic);
                                Log.e(TAG, "onReceive: ACTION_FIRST_PHASE_DONE --> " + secondPhaseCheckerFlag);

//                                if (secondPhaseCheckerFlag) {
//                                    broadcastUpdate(ACTION_REAL_TIME_SECOND_PHASE);
//                                }
                            }

                        }
                    }, 2000);


                    Log.e(TAG, "onReceive: " + "디바이스 인증 완료 ");
                    break;

                case ACTION_SECOND_PHASE_DONE:
                    broadcastUpdate(ACTION_REAL_TIME_FINAL_PHASE);
                    Log.e(TAG, "onReceive: " + "데이터 동기화 시작  ");
                    break;

            }
        }
    };

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mBluetoothGatt = gatt;
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.e(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.e(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.e(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for (BluetoothGattService service : gatt.getServices()) {
                    Log.e(TAG, "onServicesDiscovered: " + service.getUuid().toString());

                    if (ESP32GattAttributes.BLE_SERVICE_CUSTOM.equals(service.getUuid())) {

                        mRealtimeWriteCharacteristic = service.getCharacteristic(ESP32GattAttributes.BLE_CHAR_CUSTOM);
                        mTreadmillCharacteristic = service.getCharacteristic(ESP32GattAttributes.BLE_CHAR_CUSTOM_TRAY);
                        mRealtimeCharacteristic = service.getCharacteristic(ESP32GattAttributes.BLE_CHAR_CUSTOM_REALTIME);
                        Log.e(TAG, "onServicesDiscovered: bike" + mRealtimeWriteCharacteristic.getUuid().toString());
                        Log.e(TAG, "onServicesDiscovered: tread" + mTreadmillCharacteristic.getUuid().toString());
                        Log.e(TAG, "onServicesDiscovered: real time" + mRealtimeCharacteristic.getUuid().toString());
                        if (mRealtimeWriteCharacteristic != null) {
                            Log.e(TAG, "onServicesDiscovered: " + "mIndoorBikeCharacteristic set");
                        }
                        if (mTreadmillCharacteristic != null) {
                            dataResultDescriptor = enableResultNotification(gatt);
                            Log.e(TAG, "onServicesDiscovered: " + "mTreadmillCharacteristic set");
                        }
                    }
                }

//                broadcastUpdate(ACTION_SERVICE_SCAN_DONE);

                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

            } else {
                Log.e(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
//            super.onDescriptorWrite(gatt, descriptor, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {

                // TODO: 2018-09-05 만약 데이터 결과 디스크립터가 설정되면
                if (dataResultDescriptor) {
                    Log.e(TAG, "onDescriptorWrite: " + "------- dataResultDescriptor Descriptor Write Done");
//                    Log.e(TAG, "onDescriptorWrite: dataSyncDescriptor -> " + dataSyncDescriptor);
//                    broadcastUpdate(ACTION_SERVICE_SCAN_DONE);
//                    dataResultDescriptor = false; // TODO: 2018-09-05 콜백이 다시 발생하면 다시 시간 동기화를 진행하기때문에 플레그를 끈다.
                    dataResultDescriptor = false;
                    realTimeDescriptor = enableRealtimeNotification(gatt);

                }

                if (realTimeDescriptor) {
                    Log.e(TAG, "onDescriptorWrite: " + "------- realTimeDescriptor Descriptor Write Done");
                    broadcastUpdate(ACTION_SERVICE_START_SIGNAL);
                    realTimeDescriptor = false; // TODO: 2018-09-05 콜백이 다시 발생하면 다시 시간 동기화를 진행하기때문에 플레그를 끈다.
                }


            } else {
                Log.d(TAG, "Callback: Error writing GATT Descriptor: " + status);
            }

        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    /**
     * 데이터를 처리하는 메소드
     *
     * @param action
     * @param characteristic
     */
    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));

        } else if (ESP32GattAttributes.BLE_CHAR_CUSTOM_TRAY.equals(characteristic.getUuid())) {

            final byte[] data = characteristic.getValue();

            String values = null;
            String soups = null;
            String sideAs = null;
            String sideBs = null;
            String sideCs = null;
            String sideDs = null;
            if (data != null && data.length > 0) {

                for (byte byteChar : data) {
                    Log.e(TAG, "broadcastUpdate: --> " + byteChar);

                }

                if (data[0] == -1) {
                    values = "0 ";
                } else {
                    float floatVal;
                    final int intVal = (data[0] << 8) & 0xff00 | (data[1] & 0x00ff);
//                    floatVal = (float) intVal / 100;
//                    values = String.format("%.2f", floatVal);
                    values = String.valueOf(intVal);

                    final int soup = (data[2] << 8) & 0xff00 | (data[3] & 0x00ff);
                    soups = String.valueOf(soup);

                    final int sideA = (data[4] << 8) & 0xff00 | (data[5] & 0x00ff);
                    sideAs = String.valueOf(sideA);

                    final int sideB = (data[6] << 8) & 0xff00 | (data[7] & 0x00ff);
//                    floatVal = (float) intVal / 100;
                    sideBs = String.valueOf(sideB);

                    final int sideC = (data[8] << 8) & 0xff00 | (data[9] & 0x00ff);
//                    floatVal = (float) intVal / 100;
                    sideCs = String.valueOf(sideC);
                    final int sideD = (data[10] << 8) & 0xff00 | (data[11] & 0x00ff);
//                    floatVal = (float) intVal / 100;
                    sideDs = String.valueOf(sideD);
                }

            }

            intent.putExtra(EXTRA_DATA,
                    "Rice:" + values + "g, "
                            + "soup:" + soups + "g, "
                            + "sideA:" + sideAs + "g, "
                            + "sideB:" + sideBs + "g, "
                            + "sideC:" + sideCs + "g, "
                            + "sideD:" + sideDs + "g ");

        } else if (ESP32GattAttributes.BLE_CHAR_CUSTOM_REALTIME.equals(characteristic.getUuid())) {

            final byte[] data = characteristic.getValue();

            String values = null;

            if (data != null && data.length > 0) {
                if (data[0] == 0x02 && data[1] == 0x10 && data[2] == 0x03) {
                    Log.e(TAG, "broadcastUpdate: " + "1차 인증 성공");
//                    Toasty.success(getApplicationContext(), "1차 인증 성공", Toast.LENGTH_SHORT).show();
                    broadcastUpdate(ACTION_SERVICE_SCAN_DONE);
                }
                if (data[0] == 0x02 && data[1] == 0x11 && data[2] == 0x03) {
//                    Toasty.success(getApplicationContext(), "암호화 인증 성공", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "broadcastUpdate: " + "장비 암호화 인증 성공");
                    broadcastUpdate(ACTION_FIRST_PHASE_DONE);
                }
                if (data[0] == 0x02 && data[1] == 0x21 && data[2] == 0x03) {
                    Log.e(TAG, "broadcastUpdate: " + "장비 암호화 인증 실패");
//                    broadcastUpdate(ACTION_FIRST_PHASE_DONE);
                }
                if (data[0] == 0x02 && data[1] == 0x12 && data[2] == 0x03) {
                    Log.e(TAG, "broadcastUpdate: " + "모든 인증 완료 ");
                    broadcastUpdate(ACTION_SECOND_PHASE_DONE);
                } else {

                    for (byte byteChar : data) {
                        Log.e(TAG, "broadcastUpdate: --> " + byteChar);
                    }

                    float floatVal;

                    if (data[0] == -1) {

                        values = "0.0,";

                    } else {
                        final int intVal = (data[0] << 8) & 0xff00 | (data[1] & 0xff);
                        floatVal = (float) intVal / 100;
                        values = String.format("%.1f", floatVal) + ",";

                    }

                    if (data[2] == -1) {
                        values += "0.0,";
                    } else {
                        final int intSoup = (data[2] << 8) & 0xff00 | (data[3] & 0xff);
                        floatVal = (float) intSoup / 100;
                        values += String.format("%.1f", floatVal) + ",";
                    }

                    if (data[4] == -1) {
                        values += "0.0,";
                    } else {
                        final int intSideA = (data[4] << 8) & 0xff00 | (data[5] & 0xff);
                        floatVal = (float) intSideA / 100;
                        values += String.format("%.1f", floatVal) + ",";
                    }

                    if (data[6] == -1) {
                        values += "0.0,";
                    } else {
                        final int intSideB = (data[6] << 8) & 0xff00 | (data[7] & 0xff);
                        floatVal = (float) intSideB / 100;
                        values += String.format("%.1f", floatVal) + ",";
                    }

                    if (data[8] == -1) {
                        values += "0.0,";
                    } else {
                        final int intSideC = (data[8] << 8) & 0xff00 | (data[9] & 0xff);
                        floatVal = (float) intSideC / 100;
                        values += String.format("%.1f", floatVal) + ",";
                    }

                    if (data[10] == -1) {
                        values += "0.0,";
                    } else {
                        final int intSideD = (data[10] << 8) & 0xff00 | (data[11] & 0xff);
                        floatVal = (float) intSideD / 100;
                        values += String.format("%.1f", floatVal);
                    }


                    Log.e(TAG, "broadcastUpdate: " + values);


                }
            }
//            intent.putExtra(EXTRA_DATA, values + "g");
            intent.putExtra(EXTRA_DATA, values);
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                Log.e(TAG, "data in ");
                for (byte byteChar : data) {
                    Log.e(TAG, "broadcastUpdate: --> " + byteChar);
                    stringBuilder.append(String.format("%02X ", byteChar));
                }
                Log.e(TAG, "data out ");


                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public ESPBleService getService() {
            return ESPBleService.this;
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SERVICE_START_SIGNAL);
        intentFilter.addAction(ACTION_SERVICE_SCAN_DONE);
        intentFilter.addAction(ACTION_FIRST_PHASE_DONE);
        intentFilter.addAction(ACTION_SECOND_PHASE_DONE);
        return intentFilter;
    }

    @Override
    public IBinder onBind(Intent intent) {
        registerReceiver(mySyncReceiver, makeGattUpdateIntentFilter());
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        unregisterReceiver(mySyncReceiver);
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(ESP32GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }

    public boolean readCardInformation(){
        if(mRealtimeWriteCharacteristic == null) return false;

        byte[] temp = new byte[4];
        temp[0] = (byte) (0x02);
        temp[1] = (byte) (0x07);
        temp[2] = (byte) (0x70);
        temp[3] = (byte) (0x03);

        mRealtimeWriteCharacteristic.setValue(temp);
        return mBluetoothGatt.writeCharacteristic(mRealtimeWriteCharacteristic);

    }

    private boolean startRealTimeSignal(BluetoothGattCharacteristic characteristic) {
        byte[] temp = new byte[4];

        temp[0] = (byte) (0x02);
        temp[1] = (byte) (0x07);
        temp[2] = (byte) (0x70);
        temp[3] = (byte) (0x03);

        characteristic.setValue(temp);
        return mBluetoothGatt.writeCharacteristic(characteristic);

    }


    private boolean syncDateTimeToDevice(BluetoothGattCharacteristic characteristic) {

        byte[] temp = new byte[7];
        Calendar cal = Calendar.getInstance(Locale.KOREA);
        long times = cal.getTime().getTime() / 1000;
        Log.e(TAG, "syncDateTimeToDevice: 현재시간" + times);

        temp[0] = (byte) (0x02);
        temp[1] = (byte) (0x72);
        temp[2] = (byte) ((times >> 24) & 0xff);
        temp[3] = (byte) ((times >> 16) & 0xff);
        temp[4] = (byte) ((times >> 8) & 0xff);
        temp[5] = (byte) (times & 0xff);
        temp[6] = (byte) (0x03);

        for (byte b :
                temp) {
            Log.e(TAG, "syncDateTimeToDevice: " + b);

        }

        characteristic.setValue(temp);
        return mBluetoothGatt.writeCharacteristic(characteristic);

    }


    private boolean deviceAuth(BluetoothGattCharacteristic characteristic) {

        byte[] authBytes = sha256Bytes("smartfoodtray");
        byte[] sendBytes = new byte[20];


        sendBytes[0] = 0x02;
        sendBytes[1] = 0x71;

        System.arraycopy(authBytes, 0, sendBytes, 2, 17);

        sendBytes[19] = 0x03;


        for (byte b : sendBytes) {
            Log.e(TAG, "deviceAuth: " + b);
        }

        characteristic.setValue(sendBytes);

        return mBluetoothGatt.writeCharacteristic(characteristic);

    }

    private boolean enableResultNotification(final BluetoothGatt gatt) {
        if (mTreadmillCharacteristic == null) {
            return false;
        }
        boolean notiResult = gatt.setCharacteristicNotification(mTreadmillCharacteristic, true);
        Log.e(TAG, "enableResultNotification: " + notiResult);
        final BluetoothGattDescriptor d = mTreadmillCharacteristic.getDescriptor(ESP32GattAttributes.BLE_DESCRIPTOR_DESCRIPTOR);
        d.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        boolean result = gatt.writeDescriptor(d);
        return result;
    }

    private boolean enableRealtimeNotification(final BluetoothGatt gatt) {
        if (mRealtimeCharacteristic == null) {
            return false;
        }
        boolean notiResult = gatt.setCharacteristicNotification(mRealtimeCharacteristic, true);
        Log.e(TAG, "enableResultNotification: " + notiResult);
        final BluetoothGattDescriptor d = mRealtimeCharacteristic.getDescriptor(ESP32GattAttributes.BLE_DESCRIPTOR_DESCRIPTOR);
        d.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        boolean result = gatt.writeDescriptor(d);
        return result;
    }


    private byte[] sha256Bytes(String input) {
        return hashBytes("SHA-256", input);
    }

    private String sha256(String input) {
        return hashString("SHA-256", input);
    }

    private String hashString(String type, String input) {
        String HEX_CHARS = "0123456789ABCDEF";
        byte[] bytes = new byte[0];
        try {
            bytes = MessageDigest.getInstance(type).digest(input.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte it : bytes) {
            int i = (int) it;
            result.append(HEX_CHARS.charAt(i >> 4 & 0x0f));
            result.append(HEX_CHARS.charAt(i & 0x0f));
        }
//        bytes.forEach {
//            val i = it.toInt()
//            result.append(HEX_CHARS[i shr 4 and 0x0f])
//            result.append(HEX_CHARS[i and 0x0f])
//        }

        return result.toString();
    }

    private byte[] hashBytes(String type, String input) {
        String HEX_CHARS = "0123456789ABCDEF";
        byte[] bytes = new byte[0];
        try {
            bytes = MessageDigest.getInstance(type).digest(input.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte it : bytes) {
            int i = (int) it;
            result.append(HEX_CHARS.charAt(i >> 4 & 0x0f));
            result.append(HEX_CHARS.charAt(i & 0x0f));
        }
//        bytes.forEach {
//            val i = it.toInt()
//            result.append(HEX_CHARS[i shr 4 and 0x0f])
//            result.append(HEX_CHARS[i and 0x0f])
//        }

        return bytes;
    }
}
