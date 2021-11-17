package com.intretech.app.umsdashboard_new.ping;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.Socket;


public class PingJob {
    private String host;
    private boolean isRunning;
    TextView logTextView;
    Handler mainHandler = new Handler(Looper.getMainLooper());
    private int port;

    public PingJob(String str, int i, TextView textView) {
        this.host = str;
        this.port = i;
        this.logTextView = textView;
    }

    public PingResult Ping(String str, int i) {
        Exception e;
        String str2;
        long currentTimeMillis = System.currentTimeMillis();
        PingResult pingResult = new PingResult();
        try {
            str2 = InetAddress.getByName(str).getHostAddress();
            try {
                new Socket(str2, i).close();
                pingResult.status = 0;
            } catch (Exception e2) {
                e = e2;
                try {
                    Log.e("TCPing", e.toString());
                    pingResult.err = e.getLocalizedMessage();
                    pingResult.status = -1;
                    long currentTimeMillis2 = System.currentTimeMillis() - currentTimeMillis;
                    pingResult.host = str;
                    pingResult.port = i;
                    pingResult.ip = str2;
                    pingResult.latency = currentTimeMillis2;
                    return pingResult;
                } catch (Throwable th) {
                    System.currentTimeMillis();
                    throw th;
                }
            }
        } catch (Exception e3) {
            e = e3;
            str2 = str;
            Log.e("TCPing", e.toString());
            pingResult.err = e.getLocalizedMessage();
            pingResult.status = -1;
            long currentTimeMillis22 = System.currentTimeMillis() - currentTimeMillis;
            pingResult.host = str;
            pingResult.port = i;
            pingResult.ip = str2;
            pingResult.latency = currentTimeMillis22;
            return pingResult;
        }
        long currentTimeMillis222 = System.currentTimeMillis() - currentTimeMillis;
        pingResult.host = str;
        pingResult.port = i;
        pingResult.ip = str2;
        pingResult.latency = currentTimeMillis222;
        return pingResult;
    }

    public void Stop() {
        synchronized (this) {
            this.isRunning = false;
            this.logTextView = null;
        }
    }

    public void Start() {
        this.isRunning = true;
        new Thread(new Runnable() {
            /* class com.mioware.tcping.PingJob.AnonymousClass1 */

            public void run() {
                while (true) {
                    try {
                        synchronized (this) {
                            if (PingJob.this.isRunning) {
                                PingJob.this.doPing();
                            }
                        }
                        Thread.sleep(500);
                    } catch (Exception e) {
                        Log.e("TCPing", e.toString());
                        return;
                    }
                }
            }
        }).start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doPing() {
        final PingResult Ping = Ping(this.host, this.port);
        this.mainHandler.post(new Runnable() {
            /* class com.mioware.tcping.PingJob.AnonymousClass2 */

            public void run() {
                synchronized (this) {
                    if (PingJob.this.logTextView != null) {
                        String charSequence = PingJob.this.logTextView.getText().toString();
                        TextView textView = PingJob.this.logTextView;
                        if (textView.getLineCount()>10){
                            textView.setText( Ping.toString() + "\n");
                        }else{
                            textView.setText(charSequence + Ping.toString() + "\n");
                        }
                    }
                }
            }
        });
    }

}
