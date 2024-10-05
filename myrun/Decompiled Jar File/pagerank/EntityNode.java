/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.util.Map;
import pagerank.FileEntity;
import pagerank.NetworkEntity;
import pagerank.Process;

public class EntityNode {
    private long ID;
    private FileEntity f;
    private NetworkEntity n;
    private Process p;
    private String signature;
    double reputation;
    Map<String, String> attributes;

    public EntityNode(FileEntity f) {
        this.f = f;
        this.ID = f.getUniqID();
        this.n = null;
        this.p = null;
        this.signature = f.getPath();
        this.reputation = f.getReputation();
    }

    public EntityNode(Process p) {
        this.p = p;
        this.f = null;
        this.n = null;
        this.ID = p.getUniqID();
        this.signature = p.getPidAndName();
        this.reputation = p.getReputation();
    }

    public EntityNode(NetworkEntity n) {
        this.n = n;
        this.f = null;
        this.p = null;
        this.ID = n.getUniqID();
        this.signature = n.getSrcAndDstIP();
        this.reputation = n.getReputation();
    }

    EntityNode(EntityNode e) {
        this.f = e.getF();
        this.n = e.getN();
        this.p = e.getP();
        this.ID = e.getID();
        this.signature = e.getSignature();
        this.reputation = e.reputation;
    }

    EntityNode(EntityNode old, long id) {
        this.f = old.getF();
        this.n = old.getN();
        this.p = old.getP();
        this.ID = id;
        this.signature = old.getSignature();
        this.reputation = old.reputation;
    }

    public EntityNode(long id, double reputation, Map<String, String> attributes) {
        this.ID = id;
        this.reputation = reputation;
        this.f = null;
        this.p = null;
        this.n = null;
        this.attributes = attributes;
        this.signature = attributes.get("name");
    }

    EntityNode(long id, double reputation, String signature) {
        this.ID = id;
        this.reputation = reputation;
        this.signature = signature;
        this.f = null;
        this.n = null;
        this.p = null;
    }

    long getID() {
        return this.ID;
    }

    FileEntity getF() {
        return this.f;
    }

    NetworkEntity getN() {
        return this.n;
    }

    Process getP() {
        return this.p;
    }

    public String getSignature() {
        return this.signature;
    }

    void setReputation(double r) {
        this.reputation = r;
    }

    double getReputation() {
        return this.reputation;
    }

    public boolean isFileNode() {
        return this.f != null;
    }

    public boolean isNetworkNode() {
        return this.n != null;
    }

    public boolean isProcessNode() {
        return this.p != null;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EntityNode)) {
            return false;
        }
        EntityNode that = (EntityNode)o;
        if (this.ID != that.ID) {
            return false;
        }
        if (this.f != null ? !this.f.equals(that.f) : that.f != null) {
            return false;
        }
        if (this.n != null ? !this.n.equals(that.n) : that.n != null) {
            return false;
        }
        if (this.p != null ? !this.p.equals(that.p) : that.p != null) {
            return false;
        }
        return this.signature.equals(that.signature);
    }

    public int hashCode() {
        int result = (int)(this.ID ^ this.ID >>> 32);
        result = 31 * result + (this.f != null ? this.f.hashCode() : 0);
        result = 31 * result + (this.n != null ? this.n.hashCode() : 0);
        result = 31 * result + (this.p != null ? this.p.hashCode() : 0);
        result = 31 * result + this.signature.hashCode();
        return result;
    }

    public String toString() {
        return this.getID() + " " + this.getSignature();
    }
}

