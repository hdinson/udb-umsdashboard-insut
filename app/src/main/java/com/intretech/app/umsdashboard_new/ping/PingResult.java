package com.intretech.app.umsdashboard_new.ping;

public class PingResult {
    public String err;
    public String host;
    public String ip;
    public long latency;
    public int port;
    public int status;

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Ping tcp://");
        stringBuffer.append(this.host);
        stringBuffer.append(":");
        stringBuffer.append(this.port);
        stringBuffer.append("(");
        stringBuffer.append(this.ip);
        stringBuffer.append(":");
        stringBuffer.append(this.port);
        stringBuffer.append(")");
        if (this.status == 0) {
            stringBuffer.append(" time=");
            stringBuffer.append(this.latency);
            stringBuffer.append("ms");
        } else {
            stringBuffer.append(this.err);
        }
        return stringBuffer.toString();
    }

}
