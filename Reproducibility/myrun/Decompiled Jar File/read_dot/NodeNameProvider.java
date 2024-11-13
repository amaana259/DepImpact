/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package read_dot;

import org.jgrapht.ext.ComponentNameProvider;
import read_dot.SimpleNode;

public class NodeNameProvider
implements ComponentNameProvider<SimpleNode> {
    @Override
    public String getName(SimpleNode e) {
        String sig = e.signature;
        if (sig.startsWith("=")) {
            sig = e.signature.substring(1);
        }
        return sig + " [" + e.reputation + "]";
    }
}

