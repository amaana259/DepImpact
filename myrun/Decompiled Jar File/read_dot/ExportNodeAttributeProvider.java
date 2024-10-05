/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package read_dot;

import java.util.HashMap;
import java.util.Map;
import org.jgrapht.ext.ComponentAttributeProvider;
import read_dot.SimpleNode;

public class ExportNodeAttributeProvider
implements ComponentAttributeProvider<SimpleNode> {
    @Override
    public Map<String, String> getComponentAttributes(SimpleNode e) {
        HashMap<String, String> map2 = new HashMap<String, String>();
        map2.put("shape", e.shape);
        return map2;
    }
}

