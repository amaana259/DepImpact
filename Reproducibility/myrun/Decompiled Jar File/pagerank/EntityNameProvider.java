/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import org.jgrapht.ext.ComponentNameProvider;
import pagerank.EntityNode;

public class EntityNameProvider
implements ComponentNameProvider<EntityNode> {
    @Override
    public String getName(EntityNode e) {
        String sig = e.getSignature();
        if (sig.startsWith("=")) {
            sig = e.getSignature().substring(1);
        }
        return sig + " [" + e.reputation + "]";
    }
}

