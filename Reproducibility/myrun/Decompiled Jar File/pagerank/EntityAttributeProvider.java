/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.util.HashMap;
import java.util.Map;
import org.jgrapht.ext.ComponentAttributeProvider;
import pagerank.EntityNode;

public class EntityAttributeProvider
implements ComponentAttributeProvider<EntityNode> {
    @Override
    public Map<String, String> getComponentAttributes(EntityNode e) {
        HashMap<String, String> map2 = new HashMap<String, String>();
        if (e.attributes == null) {
            if (e.getP() != null) {
                map2.put("shape", "box");
            }
            if (e.getF() != null) {
                map2.put("shape", "ellipse");
            }
            if (e.getN() != null) {
                map2.put("shape", "parallelogram");
            }
        } else {
            String type = e.attributes.get("type");
            if (type.equals("Process")) {
                map2.put("shape", "box");
            } else if (type.equals("File")) {
                map2.put("shape", "ellipse");
            } else {
                map2.put("shape", "parallelogram");
            }
        }
        return map2;
    }
}

