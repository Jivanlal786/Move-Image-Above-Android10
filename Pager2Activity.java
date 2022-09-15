package com.jivan.mynewgallery.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.jivan.mynewgallery.R;
import com.jivan.mynewgallery.customgallery.AlbumFile;
import com.jivan.mynewgallery.customgallery.DeleteXI;
import com.jivan.mynewgallery.splash.Glob;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Pager2Activity extends AppCompatActivity {

    ViewPager2 pager2;
    private Pager2Adapter pager2Adapter;
    private ArrayList<AlbumFile> list = new ArrayList<>();
    private int pos = 0;
    private int listpos = 0;
    private int currentPos = 0;
    private View view;
    private ImageView ivBack, ivMenu;
    private TextView tvTitle;
    private ActivityResultLauncher<Intent> resultHandler;
    private boolean isVertical = false;
    File file;
    private String TAG = "JIVAN";
    TextView tvData;
    public static File myFile;
    List<Uri> images = new ArrayList<>();
    List<Uri> grantedUris = new ArrayList<>();
    ContentResolver contentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Glob.setStatusBarColor(Pager2Activity.this);
        setContentView(R.layout.activity_pager2);

        pager2 = findViewById(R.id.mypager);
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        ivMenu = findViewById(R.id.iv_menu);
        tvData = findViewById(R.id.tv_data);

        listpos = getIntent().getIntExtra("list_pos", 0);
        pos = getIntent().getIntExtra("position", 0);

        list = MainActivity.mAlbumFiles.get(listpos).getAlbumFiles();
        registerResult();

        pager2Adapter = new Pager2Adapter(Pager2Activity.this, list);
        pager2.setAdapter(pager2Adapter);
        pager2.setCurrentItem(pos, false);
        currentPos = pos;
        pager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        PopupMenu popupMenu = new PopupMenu(Pager2Activity.this, ivMenu);
        popupMenu.inflate(R.menu.pager_menu);

        pager2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        File file = new File(list.get(pager2.getCurrentItem()).getPath());

                        switch (menuItem.getItemId()) {
                            case R.id.item1:
                                Toast.makeText(Pager2Activity.this, "Share", Toast.LENGTH_SHORT).show();
                                return true;

                            case R.id.item2:
                                Uri contentUri = FileProvider.getUriForFile(Pager2Activity.this, getPackageName() + ".provider", file);
                                Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                intent.setDataAndType(contentUri, "image/*");
                                resultHandler.launch(intent);
                                return true;

                            case R.id.item3:
                                if (isVertical) {
                                    isVertical = false;
                                    pager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
                                } else {
                                    isVertical = true;
                                    pager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
                                }
                                Toast.makeText(Pager2Activity.this, "Change Orientation", Toast.LENGTH_SHORT).show();
                                return true;

                            case R.id.item4:
///storage/emulated/0/miui/gallery/cloud/owner/new collection/IMG_20211231_193030.jpg
                                Toast.makeText(Pager2Activity.this, "Details\n" + list.get(currentPos).getPath(), Toast.LENGTH_SHORT).show();
                                return true;

                            case R.id.item5:
                                Toast.makeText(Pager2Activity.this, "Settings", Toast.LENGTH_SHORT).show();
                                return true;

                            case R.id.item6:

//                                deleteFileFromMediaStore(getContentResolver(), file);
                                deleteFile(file);
                                pager2Adapter.notifyDataSetChanged();

                                return true;

                            case R.id.item7:
                                int rotation = list.get(pager2.getCurrentItem()).getRotation();
                                if (rotation < 361) {
                                    list.get(pager2.getCurrentItem()).setRotation(rotation + 90);
                                } else {
                                    list.get(pager2.getCurrentItem()).setRotation(90);
                                }
                                pager2Adapter.notifyItemChanged(pager2.getCurrentItem());
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });

        pager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPos = position;
                view = pager2.findViewWithTag("touchview");
//                View view = pager2Adapter.getCurrentView();

//                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }

    public class Pager2Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        Context context;
        ArrayList<AlbumFile> list;
        private MediaController mediaController;

        public Pager2Adapter(Context context, ArrayList<AlbumFile> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            if (viewType == AlbumFile.TYPE_IMAGE) {

                View view = LayoutInflater.from(context).inflate(R.layout.image_holder, parent, false);
                return new Pager2Adapter.MyImageHolder(view);

            } else {

                View view = LayoutInflater.from(context).inflate(R.layout.video_container, parent, false);
                return new Pager2Adapter.MyVideoHolder(view);
            }

        }

        @Override
        public int findRelativeAdapterPositionIn(@NonNull RecyclerView.Adapter<? extends RecyclerView.ViewHolder> adapter, @NonNull RecyclerView.ViewHolder viewHolder, int localPosition) {
            return super.findRelativeAdapterPositionIn(adapter, viewHolder, localPosition);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            if (list.get(position).getMediaType() == AlbumFile.TYPE_IMAGE) {

                Pager2Adapter.MyImageHolder holder1 = (Pager2Adapter.MyImageHolder) holder;
                Glide.with(context)
                        .load(list.get(position).getPath())
                        .into(holder1.ivImage);
                holder1.ivImage.setRotation(list.get(position).getRotation());

            } else {
                Pager2Adapter.MyVideoHolder myVideoHolder = (Pager2Adapter.MyVideoHolder) holder;
                myVideoHolder.videoView.setVideoPath(list.get(position).getPath());

                mediaController = new MediaController(context);
                myVideoHolder.videoView.setMediaController(mediaController);
                mediaController.setAnchorView(myVideoHolder.videoView);
                myVideoHolder.videoView.setMediaController(mediaController);

                myVideoHolder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.setLooping(true);
                        mediaPlayer.start();
                    }
                });

                myVideoHolder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
//                    mediaPlayer.start();
                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {

            int type = 1;
            if (list.get(position).getMediaType() == AlbumFile.TYPE_IMAGE) {
                type = AlbumFile.TYPE_IMAGE;
            } else {
                type = AlbumFile.TYPE_VIDEO;
            }
            return type;
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class MyImageHolder extends RecyclerView.ViewHolder {

            PhotoView ivImage;

            public MyImageHolder(@NonNull View itemView) {
                super(itemView);

                ivImage = itemView.findViewById(R.id.photo_view);
            }
        }

        public class MyVideoHolder extends RecyclerView.ViewHolder {

            VideoView videoView;

            public MyVideoHolder(@NonNull View itemView) {
                super(itemView);

                videoView = itemView.findViewById(R.id.videoview);
            }
        }
    }

    public void deleteFile(File file) {

        Log.e(TAG, "deleteFile00 : " + currentPos);
        if (file.delete()) {
            MainActivity.mAlbumFiles.get(listpos).getAlbumFiles().remove(list.get(currentPos));
            if (MainActivity.mAlbumFiles.get(listpos).getAlbumFiles().isEmpty()) {
                Log.e(TAG, "onScanCompleted: empty list");
//                        finish();
                pager2.setVisibility(View.GONE);
                tvData.setVisibility(View.VISIBLE);
            } else {
                pager2.setVisibility(View.VISIBLE);
                tvData.setVisibility(View.GONE);
            }
            Log.e(TAG, "deleteFile11 : " + currentPos);
            MediaScannerConnection.scanFile(Pager2Activity.this, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String s, Uri uri) {
                    if (MainActivity.mAlbumFiles.get(listpos).getAlbumFiles().isEmpty()) {
                        Log.e(TAG, "onScanCompleted: empty list");
//                        finish();
                        pager2.setVisibility(View.GONE);
                        tvData.setVisibility(View.VISIBLE);
                    } else {
                        pager2.setVisibility(View.VISIBLE);
                        tvData.setVisibility(View.GONE);
                    }
                    pager2Adapter.notifyDataSetChanged();
                }
            });

        } else {
            this.file = file;
            Uri fileuri = getImageContentUri(Pager2Activity.this, file);
            if (fileuri != null) {
//                DeleteXI.getInstance().with(Pager2Activity.this).delete(launcher, fileuri, pager2Adapter);
                myFile = file;
//                moveImage(fileuri);
                IntentSender intentSender = moveFileDirect();
                if (intentSender != null) {
                    try {
                        startIntentSenderForResult(intentSender, 786, null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void deleteFileFromMediaStore(final ContentResolver contentResolver, final File file) {
        String canonicalPath;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            canonicalPath = file.getAbsolutePath();
        }
        final Uri uri = MediaStore.Files.getContentUri("external");
        final int result = contentResolver.delete(uri,
                MediaStore.Files.FileColumns.DATA + "=?", new String[]{canonicalPath});
        if (result == 0) {
            final String absolutePath = file.getAbsolutePath();
            if (!absolutePath.equals(canonicalPath)) {
                contentResolver.delete(uri,
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{absolutePath});
            }
        }

    }

    public void registerResult() {
        resultHandler = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        if (result.getResultCode() != RESULT_OK) {
                            Toast.makeText(Pager2Activity.this, "Some Problem Occured", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private final ActivityResultLauncher<IntentSenderRequest> launcher1 = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {

                }
            });


    private final ActivityResultLauncher<IntentSenderRequest> launcher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (file != null) {
                        if (!file.exists()) {
                            MainActivity.mAlbumFiles.get(listpos).getAlbumFiles().remove(list.get(currentPos));
                            pager2Adapter.notifyDataSetChanged();
                            MediaScannerConnection.scanFile(Pager2Activity.this, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String s, Uri uri) {
                                    if (MainActivity.mAlbumFiles.get(listpos).getAlbumFiles().isEmpty()) {
                                        Log.e(TAG, "onScanCompleted: empty list");
                                        pager2.setVisibility(View.GONE);
                                        tvData.setVisibility(View.VISIBLE);
                                    } else {
                                        pager2.setVisibility(View.VISIBLE);
                                        tvData.setVisibility(View.GONE);
                                    }
                                    pager2Adapter.notifyDataSetChanged();
                                }
                            });
                            Toast.makeText(this, "deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "file not deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    /*public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }*/

    public static Uri getImageContentUri(Context context, File imageFile) {
        try {
            String filePath = imageFile.getAbsolutePath();
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID},
                    MediaStore.Images.Media.DATA + "=? ",
                    new String[]{filePath}, null);
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                cursor.close();
                return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    //    public void moveImage(ActivityResultLauncher<IntentSenderRequest> launcher, Uri uri, Pager2Activity.Pager2Adapter adapter) {
    public void moveImage(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        PendingIntent pendingIntent = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            ArrayList<Uri> collection = new ArrayList<>();
            collection.add(uri);
            pendingIntent = MediaStore.createWriteRequest(contentResolver, collection);
        }

        if (pendingIntent != null) {
//            IntentSender sender = pendingIntent.getIntentSender();
//            IntentSenderRequest request = new IntentSenderRequest.Builder(sender).build();
//            launcher.launch(request);
//        }

            try {
                startIntentSenderForResult(pendingIntent.getIntentSender(), 786, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }

    }

    public IntentSender moveFileDirect() {
        images.clear();
        grantedUris.clear();
        contentResolver = getContentResolver();
//        List<Uri> images = new ArrayList<>();
        IntentSender result = null;
        PendingIntent pendingIntent = null;
        File dirSrc = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "MIUI/Gallery/Testing App");
        if (!dirSrc.isDirectory()) {
            if (dirSrc.listFiles() == null) {
                Toast.makeText(this, "No Images to move", Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        File[] files = dirSrc.listFiles();
        for (int i = 0; i < files.length; i++) {
            images.add(getImageContentUri(Pager2Activity.this, files[i]));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            pendingIntent = MediaStore.createWriteRequest(contentResolver, images);
            result = pendingIntent.getIntentSender();
        }

//        val uris = images.filter {



        return result;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 786) {
//            if (myFile.exists()) {
//                Log.e(TAG, "onActivityResult: " + myFile.getAbsolutePath());
//            }
            for (int i = 0; i < images.size(); i++) {
                if (checkUriPermission(images.get(i), Binder.getCallingPid(), Binder
                                .getCallingUid(),
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != PackageManager
                        .PERMISSION_GRANTED) {
                    grantedUris.add(images.get(i));
                }
            }

//        }
            File destination = new File(Environment.getExternalStorageDirectory(), "MIUI/Gallery/Test1");
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, destination.getPath() + "/");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                for (Uri image : grantedUris) {
                    contentResolver.update(image, values, null);
                }
            }
            Toast.makeText(this, "result code is " + resultCode + " ---- -1 ---- it means RESULT_OK", Toast.LENGTH_SHORT).show();
        }
    }
}