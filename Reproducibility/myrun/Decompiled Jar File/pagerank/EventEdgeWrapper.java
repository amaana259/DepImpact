/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import org.apache.commons.math3.ml.clustering.Clusterable;
import pagerank.EventEdge;

public class EventEdgeWrapper
implements Clusterable {
    private double[] points;
    private EventEdge edge;

    public EventEdgeWrapper(EventEdge edge) {
        this.edge = edge;
        this.points = new double[]{edge.timeWeight, edge.amountWeight, edge.structureWeight};
    }

    public EventEdge getEventEdge() {
        return this.edge;
    }

    @Override
    public double[] getPoint() {
        return this.points;
    }
}

