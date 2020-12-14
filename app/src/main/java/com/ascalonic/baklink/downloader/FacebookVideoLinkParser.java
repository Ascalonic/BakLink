package com.ascalonic.baklink.downloader;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class FacebookVideoLinkParser extends AsyncTask<String, Void, String> {

    public AsyncResponse delegate = null;//Call back interface

    public FacebookVideoLinkParser(AsyncResponse asyncResponse) {
        delegate = asyncResponse;//Assigning call back interfacethrough constructor
    }

    protected String doInBackground(String... urls) {
        try {
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36";
            Document document = Jsoup.connect(urls[0]).userAgent(userAgent).get();
            Elements ogVideo = document.select("meta[property=og:video]");
            //replace ampersands
            String url = ogVideo.attr("content");
            url = url.replace("&amp;", "&");
            return url;
        } catch (IOException e) {

        }
        return "";
    }

    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }
}

