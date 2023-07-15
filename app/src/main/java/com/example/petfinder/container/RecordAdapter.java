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
import com.example.petfinder.components.Dashboard;
import com.example.petfinder.components.DeleteAlertDialogue;
import com.example.petfinder.pages.pet.DisplayPetDetails;
import com.example.petfinder.pages.pet.EditPet;
import com.example.petfinder.pages.pet.ScanBluetooth;

import java.util.ArrayList;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordHolder>{

    private final Context context;
    private final ArrayList<RecordModel> recordsList;
    private final Boolean isListed;
    private OnRefresh onRefresh;
    DatabaseHelper databaseHelper;

    public interface OnRefresh {
        void onRefresh();
    }
    public void setOnRefresh(OnRefresh onRefresh){
        this.onRefresh = onRefresh;
    }

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

    @Override
    public void onBindViewHolder(@NonNull RecordHolder holder, int position) {
        RecordModel model = recordsList.get(position);
        String id = model.getId();
        String petName = model.getName();
        String breed = model.getBreed();
        String sex = model.getSex();
        String image = model.getImage();

        holder.id = id;
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

        if (!isListed) holder.imageButton.setVisibility(View.GONE);

        View.OnClickListener listedClick = view -> {
            PetFinder.getInstance().setCurrentMacAddress(id);
            PetFinder.getInstance().setCurrentPetModel(databaseHelper.getRecordDetails(id));
            context.startActivity(new Intent(context, DisplayPetDetails.class));
        };
        View.OnClickListener unlistedClick = view -> {
            Intent intent = new Intent(context, ScanBluetooth.class);
            intent.putExtra("UNLISTED", model.getPetFeederID());
            context.startActivity(intent);
        };
        holder.itemView.setOnClickListener(isListed?listedClick:unlistedClick);
        holder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteAlertDialogue.ShowDeleteAlertDialogue showDeleteAlertDialogue =
                        new DeleteAlertDialogue().setDatabaseHelper(view.getContext()).setToSingle(holder.id);

                showDeleteAlertDialogue.onActionFinished(new DeleteAlertDialogue.ShowDeleteAlertDialogue.ActionFinished() {
                    @Override
                    public void onActionFinished(Boolean value) {
                        if (onRefresh!=null) onRefresh.onRefresh();
                    }
                });
                showDeleteAlertDialogue.DeleteShow();
            }
        });
    }

    @Override
    public int getItemCount() {
        return recordsList.size();
    }

    class RecordHolder extends RecyclerView.ViewHolder {

        private static final String TAG = "RecordHolder";
        ImageView imageButton;
        ImageView petPic;
        TextView petName, petBreed, petSex;
        String id;

        public RecordHolder(@NonNull View itemView) {
            super(itemView);

            petPic = itemView.findViewById(R.id.petProfile);
            petName = itemView.findViewById(R.id.namePet);
            petBreed = itemView.findViewById(R.id.petbreed);
            petSex = itemView.findViewById(R.id.sexPet);
            imageButton = itemView.findViewById(R.id.imageButton);
        }
    }
}
