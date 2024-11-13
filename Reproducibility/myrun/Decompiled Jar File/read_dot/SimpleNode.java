/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package read_dot;

public class SimpleNode {
    long id;
    double reputation;
    String signature;
    String shape;

    public SimpleNode(long id, double reputation, String signature, String shape) {
        this.id = id;
        this.reputation = reputation;
        this.signature = signature;
        this.shape = shape;
    }

    public SimpleNode(SimpleNode old) {
        this.reputation = old.reputation;
        this.signature = old.signature;
        this.shape = old.shape;
    }

    public void setId(long i) {
        this.id = i;
    }

    public String toString() {
        return this.signature;
    }
}

