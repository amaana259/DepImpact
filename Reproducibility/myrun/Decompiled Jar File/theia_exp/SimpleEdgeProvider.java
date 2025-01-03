/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package theia_exp;

import java.math.BigDecimal;
import java.util.Map;
import org.jgrapht.ext.EdgeProvider;
import pagerank.EntityNode;
import pagerank.EventEdge;

public class SimpleEdgeProvider
implements EdgeProvider<EntityNode, EventEdge> {
    @Override
    public EventEdge buildEdge(EntityNode from, EntityNode to, String lable, Map<String, String> attributes) {
        long amount = Long.parseLong(attributes.get("amount"));
        BigDecimal time = BigDecimal.valueOf(Long.valueOf(attributes.get("time")));
        String event = attributes.get("type");
        long id = Long.parseLong(attributes.get("id"));
        EventEdge edge = new EventEdge("", time, time, amount, from, to, id);
        edge.setEdgeEvent(event);
        return edge;
    }
}

