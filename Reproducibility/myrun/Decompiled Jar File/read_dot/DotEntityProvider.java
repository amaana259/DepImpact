/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package read_dot;

import java.util.Map;
import org.jgrapht.ext.VertexProvider;
import read_dot.SimpleNode;

public class DotEntityProvider
implements VertexProvider<SimpleNode> {
    @Override
    public SimpleNode buildVertex(String label, Map<String, String> attributes) {
        long id = Long.parseLong(label);
        String[] nameAndRep = attributes.get("label").split(" \\[");
        String name = nameAndRep[0];
        double reputation = Double.parseDouble(nameAndRep[1].substring(0, nameAndRep[1].length() - 1));
        String type = attributes.get("shape");
        SimpleNode node = new SimpleNode(id, reputation, name, type);
        return node;
    }
}

