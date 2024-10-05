/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.math.BigDecimal;
import java.util.Map;
import org.jgrapht.ext.EdgeProvider;
import pagerank.EntityNode;
import pagerank.EventEdge;

public class EventEdgeProvider2
implements EdgeProvider<EntityNode, EventEdge> {
    private BigDecimal val = new BigDecimal(44444);
    private long edgeID = 10L;

    @Override
    public EventEdge buildEdge(EntityNode from, EntityNode to, String lable, Map<String, String> attributes) {
        BigDecimal starttime = new BigDecimal(attributes.get("starttime"));
        starttime = this.val.subtract(starttime);
        BigDecimal endtime = new BigDecimal(attributes.get("endtime"));
        endtime = this.val.subtract(endtime);
        long id = attributes.containsKey("id") ? Long.parseLong(attributes.get("id")) : (this.edgeID = this.edgeID + 1L);
        long size2 = Long.parseLong(attributes.get("amount"));
        assert (size2 >= 0L);
        String type = attributes.get("type");
        EventEdge edge = new EventEdge(type, starttime, endtime, size2, from, to, id);
        return edge;
    }
}

