package com.example.petfinder.components;

import static com.example.petfinder.components.DeleteAlertDialogue.Mode.DeleteAll;
import static com.example.petfinder.components.DeleteAlertDialogue.Mode.DeleteSingle;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.example.petfinder.DATABASE.DatabaseHelper;
import com.example.petfinder.R;

public class DeleteAlertDialogue {
    @SuppressLint("StaticFieldLeak")
    static DatabaseHelper databaseHelper;
    static String id;
    static String message;
    @SuppressLint("StaticFieldLeak")
    static Context context;
    static Mode mode;
    enum Mode{
        DeleteAll, DeleteSingle
    }

    private DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }
    private String getId() {
        return id;
    }
    private String getMessage() {
        return message;
    }
    private Context getContext() {
        return context;
    }

    public Mode getMode() {
        return mode;
    }

    public SetDeleteAlertDialogue setDatabaseHelper(Context context){
        databaseHelper = new DatabaseHelper(context);
        DeleteAlertDialogue.context = context;
        return new SetDeleteAlertDialogue();
    }
    public class SetDeleteAlertDialogue{
        public ShowDeleteAlertDialogue setToAll(){
            mode = DeleteAll;
            message = "Deleting pet records will remove all data within Pet Finder, including " +
                    "GPS, Pedometer, and Geofencing information. Once deleted, data cannot be " +
                    "recovered. You can click outside to cancel.";
            return new ShowDeleteAlertDialogue();
        }
        public ShowDeleteAlertDialogue setToSingle(String id){
            DeleteAlertDialogue.id = id;
            mode = DeleteSingle;
            message = "Deleting this pet record will remove all of its data within Pet Finder, including " +
                    "GPS, Pedometer, and Geofencing information. Once deleted, data cannot be " +
                    "recovered. You can click outside to cancel.";
            return new ShowDeleteAlertDialogue();
        }
    }
    public static class ShowDeleteAlertDialogue{

        ActionFinished callback;

        public interface ActionFinished{
            void onActionFinished(Boolean value);
        }

        public void onActionFinished(ActionFinished callback){
            this.callback = callback;
        }

        public void DeleteShow(){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Warning!")
                    .setIcon(R.mipmap.tffi_logo)
                    .setMessage(message)
                    .setPositiveButton("Delete in Pet Finder only", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PositiveButtonAction();
                            if (callback!=null) callback.onActionFinished(false);
                        }
                    })
                    .setNegativeButton("Delete in both F&F Apps", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NegativeButtonAction();
                            if (callback!=null) callback.onActionFinished(true);
                        }
                    });
            AlertDialog dialog = builder.create();

            dialog.show();
        }
        private void PositiveButtonAction(){
            switch (mode){
                case DeleteAll:
                    databaseHelper.deleteAllData(false);
                case DeleteSingle:
                    databaseHelper.deleteData(id, false);
            }
        }
        private void NegativeButtonAction(){
            switch (mode){
                case DeleteAll:
                    databaseHelper.deleteAllData(true);
                case DeleteSingle:
                    databaseHelper.deleteData(id, true);
            }
        }
    }
}
