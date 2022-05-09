package com.intretech.app.umsdashboard_new.download.callback;

import okhttp3.ResponseBody;

public interface DownloadListener {
    void onStart(ResponseBody responseBody);
}
