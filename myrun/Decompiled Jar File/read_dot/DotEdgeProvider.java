/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package read_dot;

import java.util.Map;
import org.jgrapht.ext.EdgeProvider;
import read_dot.SimpleEdge;
import read_dot.SimpleNode;

public class DotEdgeProvider
implements EdgeProvider<SimpleNode, SimpleEdge> {
    @Override
    public SimpleEdge buildEdge(SimpleNode from, SimpleNode to, String lable, Map<String, String> attributes) {
        double weight = Double.parseDouble(attributes.get("label").split(" ")[1]);
        SimpleEdge edge = new SimpleEdge(from, to, weight);
        return edge;
    }
}

