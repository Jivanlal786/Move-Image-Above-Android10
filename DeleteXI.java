package com.jivan.mynewgallery.customgallery;

import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;

import com.jivan.mynewgallery.activities.Pager2Activity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

public class DeleteXI {

    private Context context;

    public static DeleteXI getInstance() {
        return new DeleteXI();
    }

    public DeleteXI with(Context context) {
        this.context = context;
        return this;
    }


    public void delete(ActivityResultLauncher<IntentSenderRequest> launcher, Uri uri, Pager2Activity.Pager2Adapter adapter) {

        ContentResolver contentResolver = context.getContentResolver();

        try {

            //delete object using resolver
            contentResolver.delete(uri, null, null);
            adapter.notifyDataSetChanged();

        } catch (SecurityException e) {

            PendingIntent pendingIntent = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                ArrayList<Uri> collection = new ArrayList<>();
                collection.add(uri);
                pendingIntent = MediaStore.createDeleteRequest(contentResolver, collection);

            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                //if exception is recoverable then again send delete request using intent
                if (e instanceof RecoverableSecurityException) {
                    RecoverableSecurityException exception = (RecoverableSecurityException) e;
                    pendingIntent = exception.getUserAction().getActionIntent();
                }
            }

            if (pendingIntent != null) {
                IntentSender sender = pendingIntent.getIntentSender();
                IntentSenderRequest request = new IntentSenderRequest.Builder(sender).build();
                launcher.launch(request);
            }
        }
    }

    public void moveImage(ActivityResultLauncher<IntentSenderRequest> launcher, Uri uri, Pager2Activity.Pager2Adapter adapter) {
        ContentResolver contentResolver = context.getContentResolver();
        PendingIntent pendingIntent = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            ArrayList<Uri> collection = new ArrayList<>();
            collection.add(uri);
            pendingIntent = MediaStore.createWriteRequest(contentResolver, collection);
        }

        if (pendingIntent != null) {
            IntentSender sender = pendingIntent.getIntentSender();
            IntentSenderRequest request = new IntentSenderRequest.Builder(sender).build();
            launcher.launch(request);
        }


    }

}
