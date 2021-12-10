package com.intretech.app.umsdashboard_new.download.model;

/**
 * 下载状态
 */
public enum DownloadState {
    START(0),
    DOWN(1),
    PAUSE(2),
    STOP(3),
    ERROR(4),
    FINISH(5);
    private int state;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    DownloadState(int state) {
        this.state = state;
    }
}
