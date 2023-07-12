package com.example.petfinder.container;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petfinder.R;
import com.example.petfinder.DATABASE.DatabaseHelper;
import com.example.petfinder.application.PetFinder;
import com.example.petfinder.pages.pet.DisplayPetDetails;

import java.util.ArrayList;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordHolder>{

    private Context context;
    private ArrayList<RecordModel> recordsList;
    private int selectedItemPosition = RecyclerView.NO_POSITION;
    private Boolean isListed;
    DatabaseHelper databaseHelper;

    public RecordAdapter(Context context, ArrayList<RecordModel> recordsList, Boolean isListed) {
        this.context = context;
        this.recordsList = recordsList;
        this.isListed = isListed;
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
            holder.petSex.setText("Female");
        } else if (sex.equalsIgnoreCase("male")) {
            holder.petSex.setText("Male");
        } else {
            holder.petSex.setText(sex);
        }

        if (image.equals("null")) {
            holder.petPic.setImageResource(R.drawable.profile);
        } else {
            holder.petPic.setImageURI(Uri.parse(image));
        }

        View.OnClickListener listedClick = view -> {
            PetFinder.getInstance().setCurrentMacAddress(id);
            PetFinder.getInstance().setCurrentPetModel(databaseHelper.getRecordDetails(id));
            context.startActivity(new Intent(context, DisplayPetDetails.class));
        };
        View.OnClickListener unlistedClick = view -> {
            //TODO: WHAT HAPPENS TO THIS NEW PET.
        };
        holder.itemView.setOnClickListener(isListed?listedClick:unlistedClick);
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

                    AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                    builder.setTitle("Delete Confirmation")
                            .setMessage("Choose an option:")
                            .setPositiveButton("Delete in this app", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteSelectedItem();
                                }
                            })
                            .setNegativeButton("Delete in both apps", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showToast("Deleted in Petfinder and Petfeeder");
                                }
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();


                    return true;

                default:
                    return false;
            }
        }

        private void showToast(String message) {
            Toast.makeText(itemView.getContext(), message, Toast.LENGTH_SHORT).show();
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
