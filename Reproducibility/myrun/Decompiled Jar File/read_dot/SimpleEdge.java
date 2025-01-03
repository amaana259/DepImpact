/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package read_dot;

import read_dot.SimpleNode;

public class SimpleEdge {
    SimpleNode from;
    SimpleNode to;
    double weight;

    public SimpleEdge(SimpleNode from, SimpleNode to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    public SimpleEdge(SimpleNode from, SimpleNode to) {
        this.from = from;
        this.to = to;
        this.weight = 0.0;
    }

    public String toString() {
        return this.from.toString() + "=>" + this.to.toString();
    }
}

