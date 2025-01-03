/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import org.jgrapht.ext.ComponentNameProvider;
import pagerank.EntityNode;

public class EntityIdProvider
implements ComponentNameProvider<EntityNode> {
    @Override
    public String getName(EntityNode e) {
        return "" + e.getID();
    }
}

