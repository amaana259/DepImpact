/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Set;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.KosarajuStrongConnectivityInspector;
import org.jgrapht.graph.DirectedPseudograph;
import pagerank.EntityNode;
import pagerank.EventEdge;

public class GraphConnectivity {
    private DirectedPseudograph<EntityNode, EventEdge> graph;
    FileWriter writer;

    GraphConnectivity(DirectedPseudograph<EntityNode, EventEdge> graph) {
        this.graph = graph;
    }

    public void testStrongConn(String name) {
        KosarajuStrongConnectivityInspector<EntityNode, EventEdge> scAlg = new KosarajuStrongConnectivityInspector<EntityNode, EventEdge>(this.graph);
        List stronglyConnetedSet = scAlg.stronglyConnectedSets();
        try {
            this.writer = new FileWriter(new File(name));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Strongly connected components:");
        for (int i = 0; i < stronglyConnetedSet.size(); ++i) {
            try {
                this.writer.write(i + " " + System.lineSeparator());
                for (EntityNode node : stronglyConnetedSet.get(i)) {
                    this.writer.write(node.toString() + System.lineSeparator());
                }
                this.writer.write(System.lineSeparator());
                continue;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            this.writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testWeakConnectivity(String name) {
        ConnectivityInspector<EntityNode, EventEdge> connectivityInspector = new ConnectivityInspector<EntityNode, EventEdge>(this.graph);
        List<Set<EntityNode>> weaklyConnected = connectivityInspector.connectedSets();
        try {
            File file = new File(name);
            if (!file.exists()) {
                file.createNewFile();
            }
            this.writer = new FileWriter(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Weakly connected component size: " + weaklyConnected.size());
        for (int i = 0; i < weaklyConnected.size(); ++i) {
            try {
                this.writer.write(i + " " + System.lineSeparator());
                for (EntityNode node : weaklyConnected.get(i)) {
                    this.writer.write(node.toString() + System.lineSeparator());
                }
                this.writer.write(System.lineSeparator());
                continue;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            this.writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

