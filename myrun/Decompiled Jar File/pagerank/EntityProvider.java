/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.util.Map;
import org.jgrapht.ext.VertexProvider;
import pagerank.EntityNode;

public class EntityProvider
implements VertexProvider<EntityNode> {
    @Override
    public EntityNode buildVertex(String label, Map<String, String> attributes) {
        long id = Long.parseLong(label);
        String name = attributes.get("name");
        String type = attributes.get("type");
        double reputation = 0.0;
        if (attributes.get("reputation") != null) {
            reputation = Double.parseDouble(attributes.get("reputation"));
        }
        EntityNode node = new EntityNode(id, reputation, attributes);
        return node;
    }
}

