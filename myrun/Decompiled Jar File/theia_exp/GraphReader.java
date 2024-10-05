/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package theia_exp;

import java.io.File;
import java.math.BigDecimal;
import java.util.Set;
import org.jgrapht.ext.DOTImporter;
import org.jgrapht.graph.DirectedPseudograph;
import org.json.simple.JSONObject;
import pagerank.EntityNode;
import pagerank.EventEdge;
import theia_exp.EntityNodeProvider;
import theia_exp.SimpleEdgeProvider;

public class GraphReader {
    DirectedPseudograph<EntityNode, EventEdge> graph;

    public DirectedPseudograph<EntityNode, EventEdge> readGraph(String file) {
        DOTImporter<EntityNode, EventEdge> importer = new DOTImporter<EntityNode, EventEdge>(new EntityNodeProvider(), new SimpleEdgeProvider());
        this.graph = new DirectedPseudograph(EventEdge.class);
        try {
            importer.importGraph(this.graph, new File(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.graph;
    }

    public static BigDecimal getearliestStarTime(DirectedPseudograph<EntityNode, EventEdge> graph) {
        Set edgeSet = graph.edgeSet();
        BigDecimal res = null;
        for (EventEdge edge : edgeSet) {
            if (res == null) {
                res = edge.startTime;
                continue;
            }
            if (res.compareTo(edge.startTime) <= 0) continue;
            res = edge.startTime;
        }
        return res;
    }

    public static BigDecimal getearliestLatestTime(DirectedPseudograph<EntityNode, EventEdge> graph) {
        Set edgeSet = graph.edgeSet();
        BigDecimal res = null;
        for (EventEdge edge : edgeSet) {
            if (res == null) {
                res = edge.endTime;
                continue;
            }
            if (res.compareTo(edge.endTime) >= 0) continue;
            res = edge.endTime;
        }
        return res;
    }

    public static void main(String[] args) {
        String[] dotPaths = new String[]{"/home/pxf109/TheiaQuery/fivedirections-case1/fivedirections-case1.dot", "/home/pxf109/TheiaQuery/fivedirections-case3/fivedirections-case3.dot", "/home/pxf109/TheiaQuery/theia-case1/theia-case1.dot", "/home/pxf109/TheiaQuery/theia-case3/theia_3_test.dot", "/home/pxf109/TheiaQuery/trace-case5/trace-case5.dot"};
        GraphReader graphReader = new GraphReader();
        JSONObject totalRes = new JSONObject();
        for (String s2 : dotPaths) {
            DirectedPseudograph<EntityNode, EventEdge> graph = graphReader.readGraph(s2);
            BigDecimal earlierTime = GraphReader.getearliestStarTime(graph);
            BigDecimal latestTime = GraphReader.getearliestLatestTime(graph);
            System.out.println(String.format("%s:%s", s2, earlierTime.toString()));
            System.out.println(String.format("%s:%s", s2, latestTime.toString()));
        }
    }
}

