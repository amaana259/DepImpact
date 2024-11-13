/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.Serializable;
import java.math.BigDecimal;

public class Event
implements Serializable {
    private String type;
    private String startS;
    private String startMs;
    private String endS;
    private String endMs;
    private long ID;

    public Event() {
    }

    public Event(String type, String startS, String startMs, long ID2) {
        this.type = type;
        this.startS = startS;
        this.startMs = startMs;
        this.ID = ID2;
    }

    public String getType() {
        return this.type;
    }

    public void setEndTime(String str) {
        String[] times = str.split("\\.");
        this.endS = times[0];
        this.endMs = times[1];
    }

    public String getInterval() {
        String s2 = String.valueOf(this.startS) + "." + String.valueOf(this.startMs);
        String e = String.valueOf(this.endS) + "." + String.valueOf(this.endMs);
        BigDecimal start = new BigDecimal(s2);
        BigDecimal end = new BigDecimal(e);
        return end.subtract(start).toString();
    }

    public String getStart() {
        return String.valueOf(this.startS) + "." + String.valueOf(this.startMs);
    }

    public String getEnd() {
        return String.valueOf(this.endS) + "." + String.valueOf(this.endMs);
    }

    public long getUniqID() {
        return this.ID;
    }

    public static void main(String[] args) {
        long e = Long.parseLong("156985663353");
        long es = Long.parseLong("123698745");
        String s2 = "1569874563";
        String ms = "987456321";
        Event test2 = new Event("1", s2, ms, 5758L);
        test2.setEndTime("156985663353.123698745");
        String res = test2.getInterval();
        BigDecimal a = new BigDecimal("156985663353.123698745");
        BigDecimal b = new BigDecimal("1569874563.987456321");
        System.out.println(res.toString());
        System.out.println("b-a = " + a.subtract(b).toString());
        System.out.println(res.equals(a.subtract(b)));
    }

    public String toString() {
        return "Event{type='" + this.type + '\'' + ", startS='" + this.startS + '\'' + ", startMs='" + this.startMs + '\'' + ", endS='" + this.endS + '\'' + ", endMs='" + this.endMs + '\'' + ", ID=" + this.ID + '}';
    }

    public Event clone() {
        return new Event(this.type, this.startS, this.startMs, this.ID);
    }
}

