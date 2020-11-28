package com.ascalonic.baklink.downloader;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadManager extends AsyncTask<String, Void, String> {

    private String url;
    private String downloadUrl;

    public AsyncResponse delegate = null;//Call back interface

    public DownloadManager(AsyncResponse asyncResponse, String url) {
        delegate = asyncResponse;//Assigning call back interfacethrough constructor
        this.url = url;
    }

    public ContentType GetContentType() {
        if (url.startsWith("https://www.instagram.com/reel")) {
            return ContentType.InstagramReel;
        } else if (url.startsWith("https://youtu.be/")) {
            return ContentType.YoutubeVideo;
        } else if (url.startsWith("https://www.instagram.com")) {
            return ContentType.InstagramVideo;
        } else if (url.startsWith("https://www.instagram.com/tv/")) {
            return ContentType.IgtvVideo;
        } else if (url.startsWith("https://www.facebook.com/113383062038446/posts/")) {
            return ContentType.YoutubeVideo;
        } else
            return ContentType.Unsupported;
    }

    public void ParseDownloadUrl()
    {
        new InstagramVideoLinkParser(new AsyncResponse() {
            @Override
            public void processFinish(Object output) {
                downloadUrl = (String)output;
                Log.d("download_url_2", downloadUrl);
            }
        }).execute(url);
    }

    protected String doInBackground(String... urls) {
        int count;
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            URL url = new URL(downloadUrl);

            URLConnection conection = url.openConnection();
            conection.connect();
            // getting file length
            int lenghtOfFile = conection.getContentLength();

            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            // Output stream to write file
            OutputStream output = new FileOutputStream(root + "/Download/downloadedfile.mp4");
            byte data[] = new byte[1024];

            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("download_url", e.getMessage());
        }

        return null;
    }

    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }
}
