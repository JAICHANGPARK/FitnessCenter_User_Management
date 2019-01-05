package com.dreamwalker.myapplication103.adapter.device.scan;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dreamwalker.myapplication103.R;
import com.dreamwalker.myapplication103.model.Device;

import java.util.ArrayList;
import java.util.HashSet;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.paperdb.Paper;


public class DeviceScanAdapterV2 extends RecyclerView.Adapter<DeviceScanAdapterV2.DeviceScanViewHolder> {

    ArrayList<BluetoothDevice> deviceArrayList;
    ArrayList<ScanResult> scanResultArrayList;
    Context context;
    SharedPreferences preferences;

    HashSet<Device> deviceDatabase = new HashSet<>();

    DeviceItemClickListener deviceItemClickListener;


    public void setDeviceItemClickListener(DeviceItemClickListener deviceItemClickListener) {
        this.deviceItemClickListener = deviceItemClickListener;
    }

    public DeviceScanAdapterV2(ArrayList<BluetoothDevice> deviceArrayList, ArrayList<ScanResult> scanResults, Context context) {
        this.deviceArrayList = deviceArrayList;
        this.scanResultArrayList = scanResults;
        this.context = context;

        preferences = this.context.getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        Paper.init(this.context);
    }

    @NonNull
    @Override
    public DeviceScanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_device_v2, parent, false);
        return new DeviceScanViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceScanViewHolder holder, int position) {

        final String deviceName = deviceArrayList.get(position).getName();
        final String deviceAddress = deviceArrayList.get(position).getAddress();
        int deviceRssi = scanResultArrayList.get(position).getRssi();
        double distance = getDistance(deviceRssi, 3, 2);
        double distanceRound = Math.round(distance * 100) / 100.0;
        float finalDistance = (float) distanceRound;
        if (deviceName != null && deviceName.length() > 0) {
            holder.deviceName.setText(deviceName);
        } else {
            holder.deviceName.setText(R.string.unknown_device);
        }


        holder.deviceAddress.setText(deviceArrayList.get(position).getAddress());
        holder.deviceRssi.setText(String.valueOf(deviceRssi) + "(" + finalDistance + "cm)");

    }

    @Override
    public int getItemCount() {
        return deviceArrayList.size();
    }


    class DeviceScanViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRssi;
        LinearLayout container;

        DeviceScanViewHolder(View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            deviceAddress = itemView.findViewById(R.id.device_address);
            deviceRssi = itemView.findViewById(R.id.device_rssi);
            container = itemView.findViewById(R.id.container);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (deviceItemClickListener != null) {
                deviceItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }

    }

    protected static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }

    double getDistance(int rssi, int txPower) {
        /*
         * RSSI = TxPower - 10 * n * lg(d)
         * n = 2 (in free space)
         *
         * d = 10 ^ ((TxPower - RSSI) / (10 * n))
         */

        return Math.pow(10d, ((double) txPower - rssi) / (10 * 2));
    }

    double getDistance(int rssi, int txPower, int n) {
        /*
         * RSSI = TxPower - 10 * n * lg(d)
         * n = 2 (in free space)
         *
         * d = 10 ^ ((TxPower - RSSI) / (10 * n))
         */

        return Math.sqrt(Math.pow(10d, ((double) txPower - rssi) / (10 * n)));
    }

}
