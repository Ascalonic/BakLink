package com.ascalonic.baklink.downloader;

public interface AsyncDownloadResponse {
    void processFinish(Object output);
    void onProgressUpdate(Object output);
}