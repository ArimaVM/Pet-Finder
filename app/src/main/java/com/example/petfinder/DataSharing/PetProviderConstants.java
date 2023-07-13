package com.example.petfinder.DataSharing;


import android.net.Uri;

import com.example.petfinder.DATABASE.Constants;

public interface PetProviderConstants {
    Uri CONTENT_URI_PETS = Uri.parse("content://com.example.petfeeder/"+ Constants.TABLE_NAME);
    Uri CONTENT_URI_STEP = Uri.parse("content://com.example.petfeeder/"+ Constants.TABLE_NAME3);
}