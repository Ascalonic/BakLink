package com.ascalonic.baklink.downloader;

import android.os.AsyncTask;
import android.util.Log;

import com.ascalonic.baklink.ClientData;
import com.ascalonic.baklink.Video;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;

public class PixtureServerClient extends AsyncTask<String, Void, String> {

    public AsyncResponse delegate = null;

    private String clientId;
    private String videoUrl;

    public PixtureServerClient(AsyncResponse asyncResponse, String clientId, String videoUrl) {
        delegate = asyncResponse;
        this.clientId = clientId;
        this.videoUrl = videoUrl;
    }

    protected String doInBackground(String... urls) {
        try {
            String url = String.format("https://beta.backbinge.com/api/baklink/video/%s/%s",
                    clientId, URLEncoder.encode(videoUrl, "utf-8"));
            Jsoup.connect(url).get();
        } catch (IOException e) {

        }
        return "";
    }

    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }
}
