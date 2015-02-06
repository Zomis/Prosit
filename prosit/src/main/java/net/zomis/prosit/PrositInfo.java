package net.zomis.prosit;

public class PrositInfo {

    private final String from;
    private final String imei;
    private final String to;

    public PrositInfo(String from, String imei, String to) {
        this.from = from;
        this.imei = imei;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public String getIMEI() {
        return imei;
    }

    public String getTo() {
        return to;
    }

}
