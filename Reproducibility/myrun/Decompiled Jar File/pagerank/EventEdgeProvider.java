/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import org.jgrapht.ext.ComponentNameProvider;
import pagerank.EventEdge;

public class EventEdgeProvider
implements ComponentNameProvider<EventEdge> {
    @Override
    public String getName(EventEdge eventEdge) {
        return eventEdge.id + " " + eventEdge.weight;
    }
}

