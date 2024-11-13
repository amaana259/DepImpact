/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package theia_exp;

import java.util.Map;
import org.jgrapht.ext.VertexProvider;
import pagerank.EntityNode;
import pagerank.FileEntity;
import pagerank.NetworkEntity;
import pagerank.Process;

public class EntityNodeProvider
implements VertexProvider<EntityNode> {
    @Override
    public EntityNode buildVertex(String label, Map<String, String> attributes) {
        long id = Long.parseLong(label);
        String type = attributes.get("type");
        if (type.equals("process")) {
            String pid = attributes.get("pid");
            String exename = attributes.get("exename");
            String hostname = attributes.get("hostname");
            Process p = new Process(0.0, id, 0, pid, "", "", hostname, "0", "0", exename, id);
            EntityNode node = new EntityNode(p);
            return node;
        }
        if (type.equals("file")) {
            String hostname = attributes.get("hostname");
            String name = attributes.get("name");
            FileEntity file = new FileEntity(0.0, id, 0, "0", "0", "", "", name, id);
            EntityNode node = new EntityNode(file);
            file.setLocation(hostname);
            return node;
        }
        String hostname = attributes.get("hostname");
        String src = attributes.get("src");
        String srcport = attributes.get("srcport");
        String dst = attributes.get("dst");
        String dstport = attributes.get("dstport");
        NetworkEntity network = new NetworkEntity(0.0, id, 0, "0", "0", src, dst, srcport, dstport, id);
        network.setLocation(hostname);
        EntityNode node = new EntityNode(network);
        return node;
    }
}

