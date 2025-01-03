/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import org.jgrapht.ext.ComponentNameProvider;
import pagerank.EventEdge;

public class EdgeAmountTimeProvider
implements ComponentNameProvider<EventEdge> {
    @Override
    public String getName(EventEdge e) {
        return e.getSize() + " " + e.getStartTime().toString() + "," + e.getEndTime().toString();
    }
}

