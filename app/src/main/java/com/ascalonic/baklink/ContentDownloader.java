package com.ascalonic.baklink;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ascalonic.baklink.downloader.AsyncDownloadResponse;
import com.ascalonic.baklink.downloader.AsyncResponse;
import com.ascalonic.baklink.downloader.ContentType;
import com.ascalonic.baklink.downloader.DownloadManager;
import com.ascalonic.baklink.downloader.PixtureServerClient;

import org.jsoup.Jsoup;

import java.io.File;
import java.util.UUID;

public class ContentDownloader extends AppCompatActivity {

    private static final int REQUEST = 112;
    private Context mContext=ContentDownloader.this;

    private TextView logTitle, logText, lblDownloadProgress;
    private ProgressBar prgxDownloadProgress;
    private String saveLocation;
    private File savedVideo;
    private ClientData clientData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_downloader);

        logTitle = (TextView)findViewById(R.id.logTitle);
        logText = (TextView)findViewById(R.id.logText);
        lblDownloadProgress = (TextView)findViewById(R.id.lblDownloadProgress);
        prgxDownloadProgress = (ProgressBar) findViewById(R.id.prgxDownloadProgress);

        lblDownloadProgress.setText("0%");
        prgxDownloadProgress.setProgress(0);

        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (!hasPermissions(mContext, PERMISSIONS)) {
                ActivityCompat.requestPermissions((Activity) mContext, PERMISSIONS, REQUEST );
            } else {
                //do here
                Init();
            }
        } else {
            //do here
        }
        setTitle("Share video as MP4");
    }

    private void Init()
    {
        clientData = new ClientData();
        if(!clientData.ReadFromFile(getBaseContext()))
        {
            String id = UUID.randomUUID().toString();
            clientData.Id = id;
        }

        //Get data shared from other apps
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
                    Init();
                } else {
                    Toast.makeText(mContext, "BakLink doesn't have permission to write to your storage", Toast.LENGTH_LONG).show();
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

        File file
                = getBaseContext().getExternalFilesDir(null);
        File imagePath = new File(file , "downloads");
        if (!imagePath.exists()) {
            imagePath.mkdir();
        }
        String videoId = UUID.randomUUID().toString();
        savedVideo = new File(imagePath.getPath(), videoId + ".mp4");

        DownloadManager downloadManager = new DownloadManager(new AsyncDownloadResponse() {
            @Override
            public void processFinish(Object output) {
                logTitle.setText("Done.");
                logText.setText("Ready to share");

                Video video = new Video();
                video.Path = savedVideo.getAbsolutePath();
                video.Url = url;
                video.Title = "";
                clientData.Videos.add(video);

                clientData.WriteToFile(getBaseContext());
                PixtureServerClient client = new PixtureServerClient(new AsyncResponse() {
                    @Override
                    public void processFinish(Object output) {

                    }
                }, clientData.Id, video.Url);
                client.execute();

                try{
                    String authority = getBaseContext().getApplicationContext().getPackageName() + ".fileprovider";
                    Uri uri = FileProvider.getUriForFile(getBaseContext(), authority, savedVideo);
                    Intent videoShare = new Intent(Intent.ACTION_SEND);
                    videoShare.setType("*/*");
                    videoShare.setPackage("com.whatsapp");
                    videoShare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    videoShare.putExtra(Intent.EXTRA_STREAM,uri);
                    startActivity(videoShare);
                }
                catch(ActivityNotFoundException ex)
                {
                    showAlertBox("Oops! Looks like You don't have Whatsapp installed", "No Whatsapp Found");
                }
            }

            @Override
            public void onProgressUpdate(Object output) {
                lblDownloadProgress.setText((int)output + "%");
                prgxDownloadProgress.setProgress((int)output);
            }
        } ,url, savedVideo);

        ContentType type = downloadManager.GetContentType();
        if(type == ContentType.Unsupported)
        {
            showAlertBox("Sorry! Looks like a video that we don't yet support.", "Unsupported format");
        }
        else {
            logText.setText("Extracting Download Link...");
            downloadManager.ParseDownloadUrl(type);

            logText.setText("Downloading Video...");
            downloadManager.execute();
        }
    }

    private void showAlertBox(String message, String title)
    {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(message);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }
}