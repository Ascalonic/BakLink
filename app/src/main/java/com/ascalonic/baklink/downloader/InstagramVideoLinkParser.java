package com.ascalonic.baklink.downloader;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class InstagramVideoLinkParser extends AsyncTask<String, Void, String> {

    public AsyncResponse delegate = null;//Call back interface

    public InstagramVideoLinkParser(AsyncResponse asyncResponse) {
        delegate = asyncResponse;//Assigning call back interfacethrough constructor
    }

    protected String doInBackground(String... urls) {
        try {
            Document document = Jsoup.connect(urls[0]).get();
            Elements ogVideo = document.select("meta[property=og:video]");
            Log.d("download_url", ogVideo.attr("content"));
            return ogVideo.attr("content");
        } catch (IOException e) {

        }
        return "";
    }

    protected void onPostExecute(String result) {
        Log.d("download_url", result);
        delegate.processFinish(result);
    }
}