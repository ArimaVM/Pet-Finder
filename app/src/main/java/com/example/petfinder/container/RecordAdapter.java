package com.example.petfinder.container;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petfinder.R;
import com.example.petfinder.DATABASE.DatabaseHelper;
import com.example.petfinder.pages.pet.DisplayPetDetails;

import java.util.ArrayList;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordHolder>{

    private Context context;
    private ArrayList<RecordModel> recordsList;
    private int selectedItemPosition = RecyclerView.NO_POSITION;
    DatabaseHelper databaseHelper;

    public RecordAdapter(Context context, ArrayList<RecordModel> recordsList) {
        this.context = context;
        this.recordsList = recordsList;
        databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public RecordHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_row, parent, false);
        return new RecordHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull RecordHolder holder, int position) {
        RecordModel model = recordsList.get(position);
        String id = model.getId();
        String petName = model.getName();
        String breed = model.getBreed();
        String sex = model.getSex();
        String age = model.getAge();
        String weight = model.getWeight();
        String image = model.getImage();
        String addedTime = model.getAddedtime();
        String updatedTime = model.getUpdatedtime();

        holder.petName.setText(petName);
        holder.petBreed.setText(breed);

        if (sex.equalsIgnoreCase("female")) {
            holder.petSex.setText("F");
        } else if (sex.equalsIgnoreCase("male")) {
            holder.petSex.setText("M");
        } else {
            holder.petSex.setText(sex);
        }

        if (image.equals("null")) {
            holder.petPic.setImageResource(R.drawable.profile);
        } else {
            holder.petPic.setImageURI(Uri.parse(image));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DisplayPetDetails.class);
                intent.putExtra("ID", id);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recordsList.size();
    }

    class RecordHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        private static final String TAG = "RecordHolder";
        ImageView imageButton;
        ImageView petPic;
        TextView petName, petBreed, petSex;

        public RecordHolder(@NonNull View itemView) {
            super(itemView);

            petPic = itemView.findViewById(R.id.petProfile);
            petName = itemView.findViewById(R.id.namePet);
            petBreed = itemView.findViewById(R.id.petbreed);
            petSex = itemView.findViewById(R.id.sexPet);
            imageButton = itemView.findViewById(R.id.imageButton);
            imageButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            showPopupMenu(v);
        }

        private void showPopupMenu(View view) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
             popupMenu.inflate(R.menu.popup_menu);
             popupMenu.setOnMenuItemClickListener(this);
             popupMenu.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_popup_delete:
                    Log.d(TAG, "onMenuItemClick: action_popup_delete");
                    deleteSelectedItem();
                    return true;

                default:
                    return false;
            }
        }

        private void deleteSelectedItem() {
            int position = getAdapterPosition();

            if (position != RecyclerView.NO_POSITION) {
                recordsList.remove(position);
                notifyItemRemoved(position);
            }
        }
    }
}
