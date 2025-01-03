/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import pagerank.Entity;

public class FileEntity
extends Entity {
    private String user_ID;
    private String group_ID;
    private String path;
    private String location;

    public FileEntity(double reputation, long id, int hopCount, String time1, String time2, String user_ID, String group_ID, String path, long uniqID) {
        super(reputation, id, hopCount, time1, time2, uniqID);
        this.user_ID = user_ID;
        this.group_ID = group_ID;
        this.path = path;
    }

    public FileEntity(double reputation, long id, int hopCount, String time1, String time2, String user_ID, String group_ID, String path, long uniqID, String host) {
        super(reputation, id, hopCount, time1, time2, uniqID);
        this.user_ID = user_ID;
        this.group_ID = group_ID;
        this.path = path;
        this.location = host;
    }

    public FileEntity() {
    }

    public String getUser_ID() {
        return this.user_ID;
    }

    public String getGroup_ID() {
        return this.group_ID;
    }

    public String getPath() {
        return this.path;
    }

    public void setLocation(String s2) {
        this.location = s2;
    }

    public String getLocation() {
        return this.location;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileEntity)) {
            return false;
        }
        FileEntity that = (FileEntity)o;
        if (this.user_ID != null ? !this.user_ID.equals(that.user_ID) : that.user_ID != null) {
            return false;
        }
        if (this.group_ID != null ? !this.group_ID.equals(that.group_ID) : that.group_ID != null) {
            return false;
        }
        return this.path.equals(that.path);
    }

    public int hashCode() {
        int result = this.user_ID != null ? this.user_ID.hashCode() : 0;
        result = 31 * result + (this.group_ID != null ? this.group_ID.hashCode() : 0);
        result = 31 * result + this.path.hashCode();
        return result;
    }
}

