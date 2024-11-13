/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package read_dot;

import org.jgrapht.ext.ComponentNameProvider;
import read_dot.SimpleEdge;

public class ExportEdgeProvider
implements ComponentNameProvider<SimpleEdge> {
    @Override
    public String getName(SimpleEdge eventEdge) {
        return "0 " + eventEdge.weight;
    }
}

