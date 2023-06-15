package com.example.petfinder.container;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petfinder.DATABASE.DatabaseHelper;
import com.example.petfinder.R;

import java.util.ArrayList;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceHolder>{
    private Context context;
    private ArrayList<DeviceModel> deviceList;
    DatabaseHelper databaseHelper;

    public DeviceAdapter(Context context, ArrayList<DeviceModel> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
        databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public DeviceAdapter.DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.device_item_view, parent, false);
        return new DeviceAdapter.DeviceHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceAdapter.DeviceHolder holder, int position) {
        DeviceModel model = deviceList.get(position);
        String id = model.getId();
        String dName = model.getName();
        String lat = model.getLatitude();
        String longi = model.getLongitude();
        String btName = model.getBtName();
        String btAddress = model.getBtAddress();

        holder.deviceName.setText(dName);
        holder.btName.setText(btName);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    class DeviceHolder extends RecyclerView.ViewHolder {

        TextView deviceName, btName;
        public DeviceHolder(@NonNull View itemView) {
            super(itemView);

            deviceName = itemView.findViewById(R.id.deviceName);
            btName = itemView.findViewById(R.id.bluetoothName);
        }
    }
}
