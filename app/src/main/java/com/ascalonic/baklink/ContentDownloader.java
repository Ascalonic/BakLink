package com.ascalonic.baklink;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.ascalonic.baklink.downloader.AsyncResponse;
import com.ascalonic.baklink.downloader.ContentType;
import com.ascalonic.baklink.downloader.DownloadManager;

import java.io.File;

public class ContentDownloader extends AppCompatActivity {

    private static final int REQUEST = 112;
    private Context mContext=ContentDownloader.this;

    private TextView logTitle, logText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_downloader);

        logTitle = (TextView)findViewById(R.id.logTitle);
        logText = (TextView)findViewById(R.id.logText);

        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (!hasPermissions(mContext, PERMISSIONS)) {
                ActivityCompat.requestPermissions((Activity) mContext, PERMISSIONS, REQUEST );
            } else {
                //do here
            }
        } else {
            //do here
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                verifyUrl(intent.getStringExtra(Intent.EXTRA_TEXT)); // Handle text being sent
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //do here
                } else {
                    Toast.makeText(mContext, "The app was not allowed to write in your storage", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void verifyUrl(String url) {

        logTitle.setText("Please Wait...");
        logText.setText("Resolving Video...");

        DownloadManager downloadManager = new DownloadManager(new AsyncResponse() {
            @Override
            public void processFinish(Object output) {
                logTitle.setText("Done.");
                logText.setText("Ready to share");

                Uri uri = Uri.parse(Environment.getExternalStorageDirectory().toString() + "/Download/downloadedfile.mp4");
                Intent videoShare = new Intent(Intent.ACTION_SEND);
                videoShare.setType("*/*");
                videoShare.setPackage("com.whatsapp");
                videoShare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                videoShare.putExtra(Intent.EXTRA_STREAM,uri);
                startActivity(videoShare);
            }
        } ,url);

        ContentType type = downloadManager.GetContentType();
        if(type == ContentType.Unsupported)
        {
            Log.d("download_url", "Error - unsupported format");
        }
        else {
            logText.setText("Extracting Download Link...");
            downloadManager.ParseDownloadUrl();

            logText.setText("Downloading Video...");
            downloadManager.execute();
        }
    }
}