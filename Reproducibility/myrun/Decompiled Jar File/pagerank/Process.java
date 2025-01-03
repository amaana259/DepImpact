/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import pagerank.Entity;

public class Process
extends Entity {
    private String pid;
    private String uid;
    private String groupID;
    private String location;
    private String name;

    public Process() {
    }

    public Process(double reputation, long id, int hopCount, String pid, String uid, String groupID, String location, String time1, String stime, String name, long uniqID) {
        super(reputation, id, hopCount, time1, stime, uniqID);
        this.pid = pid;
        this.uid = uid;
        this.groupID = groupID;
        this.location = location;
        this.name = name;
    }

    public Process(long id, int hopCount, String pid, String uid, String location, long uniqID) {
        super(id, hopCount, uniqID);
        if (pid.startsWith("=")) {
            pid = pid.substring(1);
        }
        this.pid = pid;
        this.uid = uid;
        this.location = location;
        this.groupID = null;
    }

    public String getPid() {
        return this.pid;
    }

    public String getUid() {
        return this.uid;
    }

    public String getGroupID() {
        return this.groupID;
    }

    public String getLocation() {
        return this.location;
    }

    public String getName() {
        return this.name;
    }

    public String getPidAndName() {
        return this.pid + this.name;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Process)) {
            return false;
        }
        Process process = (Process)o;
        if (this.pid != null ? !this.pid.equals(process.pid) : process.pid != null) {
            return false;
        }
        if (this.uid != null ? !this.uid.equals(process.uid) : process.uid != null) {
            return false;
        }
        return this.name != null ? this.name.equals(process.name) : process.name == null;
    }

    public int hashCode() {
        int result = this.pid != null ? this.pid.hashCode() : 0;
        result = 31 * result + (this.uid != null ? this.uid.hashCode() : 0);
        result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
        return result;
    }

    public static void main(String[] args) {
        String uid = "fang1";
        String groupId = "fang2";
        String location = "fang3";
        long id = Long.parseLong("1234");
        Process test2 = new Process(2.0, 0L, 0, "1123", uid, groupId, location, "1123", "234", "java", 12L);
        System.out.println(test2.getPidAndName());
    }
}

