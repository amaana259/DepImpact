/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.Serializable;

public class Entity
implements Serializable {
    private double reputation;
    private long id;
    private int hopCount;
    private long timestamp1;
    private long timestamp2;
    private long uniqID;

    public Entity() {
    }

    public Entity(long id, int hopCount, long uniqID) {
        this.id = id;
        this.hopCount = hopCount;
        this.reputation = 0.0;
        this.uniqID = uniqID;
    }

    public Entity(double reputation, long id, int hopCount, String time1, String time2, long uniqID) {
        this.reputation = reputation;
        this.id = id;
        this.hopCount = hopCount;
        this.timestamp1 = Long.valueOf(time1);
        this.timestamp2 = Long.valueOf(time2);
        this.uniqID = uniqID;
    }

    public double getReputation() {
        return this.reputation;
    }

    public long getID() {
        return this.id;
    }

    public int getHopCount() {
        return this.hopCount;
    }

    public void setReputation(double r) {
        this.reputation = r;
    }

    public void setHopCount(int h2) {
        this.hopCount = h2;
    }

    public void setId(int i) {
        this.id = i;
    }

    public String getTimeStamp() {
        String s2 = String.valueOf(this.timestamp1) + "." + String.valueOf(this.timestamp2);
        return s2;
    }

    public long getUniqID() {
        return this.uniqID;
    }

    public static void main(String[] args) {
        Entity test2 = new Entity(2.0, 0L, 15, "1152654", "493685052", 5L);
        System.out.println("HopCount: " + test2.getHopCount());
        System.out.println("id: " + test2.getID());
        System.out.println("repuattion: " + test2.getReputation());
        System.out.println("timestap: " + test2.getTimeStamp());
    }
}

