package com.example.petfinder.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petfinder.R;
import com.example.petfinder.pages.pet.ScanBluetooth;

import java.util.List;

public class ScanBTListViewAdapter extends RecyclerView.Adapter<ScanBTListViewAdapter.ScanBTListViewHolder> {

    private Context context;
    private List<ScanBluetooth.ScannedDevices> DeviceScanList;
    private OnItemClickListener clickListener;

    public ScanBTListViewAdapter(Context context, List<ScanBluetooth.ScannedDevices> DeviceScanList) {
        this.context = context;
        this.DeviceScanList = DeviceScanList;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ScanBTListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.bluetooth_listview_item, parent, false);
        return new ScanBTListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanBTListViewHolder holder, int position) {
        if (DeviceScanList.get(position).getDeviceName() != null) {
            holder.BluetoothName.setText(DeviceScanList.get(position).getDeviceName());
        } else {
            holder.BluetoothName.setText("Bluetooth Device");
        }
        holder.MACAddress.setText(DeviceScanList.get(position).getMACAddress());
        // TODO: CHECK IF DEVICE MAC ADDRESS IS ALREADY IN THE DATABASE.
        holder.Indicator.setBackgroundColor(context.getResources().getColor(R.color.lightGray));
    }

    @Override
    public int getItemCount() {
        return DeviceScanList.size();
    }

    public class ScanBTListViewHolder extends RecyclerView.ViewHolder {
        TextView BluetoothName, MACAddress;
        ImageView Indicator;

        public ScanBTListViewHolder(View itemView) {
            super(itemView);
            BluetoothName = itemView.findViewById(R.id.BluetoothName);
            MACAddress = itemView.findViewById(R.id.MAC_Address);
            Indicator = itemView.findViewById(R.id.indicator);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (clickListener != null) {
                        clickListener.onItemClick(position);
                    }
                }
            });
        }
    }
}