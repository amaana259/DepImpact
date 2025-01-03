/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package read_dot;

import org.jgrapht.ext.ComponentNameProvider;
import read_dot.SimpleNode;

public class NodeIdProvider
implements ComponentNameProvider<SimpleNode> {
    @Override
    public String getName(SimpleNode simpleNode) {
        return simpleNode.id + "";
    }
}

