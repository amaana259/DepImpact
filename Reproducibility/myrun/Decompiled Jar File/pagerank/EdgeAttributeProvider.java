/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.jgrapht.ext.ComponentAttributeProvider;
import pagerank.EventEdge;

public class EdgeAttributeProvider
implements ComponentAttributeProvider<EventEdge> {
    @Override
    public Map<String, String> getComponentAttributes(EventEdge eventEdge) {
        HashMap<String, String> map2 = new HashMap<String, String>();
        long amount = eventEdge.getSize();
        BigDecimal endTime = eventEdge.endTime;
        BigDecimal startTime = eventEdge.startTime;
        String event = eventEdge.getEvent();
        map2.put("starttime", String.valueOf(startTime));
        map2.put("endtime", String.valueOf(endTime));
        map2.put("amount", String.valueOf(amount));
        map2.put("type", eventEdge.getType());
        map2.put("id", String.valueOf(eventEdge.id));
        map2.put("event", event);
        return map2;
    }
}

