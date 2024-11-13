/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package read_dot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jgrapht.graph.DirectedPseudograph;
import read_dot.ReadGraphFromDot;
import read_dot.SimpleEdge;
import read_dot.SimpleNode;

public class CombineTwoGraph {
    public DirectedPseudograph<SimpleNode, SimpleEdge> combineGraph(DirectedPseudograph<SimpleNode, SimpleEdge> latest, DirectedPseudograph<SimpleNode, SimpleEdge> previous) {
        Set<SimpleNode> latestCandidates = this.getCandidateNodes(latest);
        Set<SimpleNode> previousCandidates = this.getCandidateNodes(previous);
        Map<String, SimpleNode> connections = this.findConnectionPoints(latestCandidates, previousCandidates);
        long latestLargestID = this.getlargetID(latest.vertexSet());
        if (connections.keySet().size() == 0) {
            return latest;
        }
        for (SimpleEdge e : previous.edgeSet()) {
            SimpleEdge edge;
            SimpleNode newSrc;
            if (connections.containsKey(e.to.signature)) {
                SimpleNode target = connections.get(e.to.signature);
                newSrc = new SimpleNode(e.from);
                newSrc.setId(latestLargestID++);
                latest.addVertex(newSrc);
                edge = new SimpleEdge(newSrc, target);
                latest.addEdge(newSrc, target, edge);
                continue;
            }
            SimpleNode newTarget = new SimpleNode(e.to);
            newTarget.setId(latestLargestID++);
            newSrc = new SimpleNode(e.from);
            newSrc.setId(latestLargestID++);
            edge = new SimpleEdge(newSrc, newTarget);
            latest.addVertex(newTarget);
            latest.addVertex(newSrc);
            latest.addEdge(newSrc, newTarget, edge);
        }
        return latest;
    }

    private Map<String, SimpleNode> findConnectionPoints(Set<SimpleNode> latest, Set<SimpleNode> previous) {
        HashMap<String, SimpleNode> targets = new HashMap<String, SimpleNode>();
        for (SimpleNode v : latest) {
            targets.put(v.signature, v);
        }
        HashMap<String, SimpleNode> previousToLatest = new HashMap<String, SimpleNode>();
        for (SimpleNode v : previous) {
            String sig = v.signature;
            if (targets.containsKey(sig)) {
                previousToLatest.put(sig, (SimpleNode)targets.get(sig));
            }
            String[] tokens = sig.split("->");
            String reverse = String.format("%s->%s", tokens[1], tokens[0]);
            if (!targets.containsKey(reverse)) continue;
            previousToLatest.put(sig, (SimpleNode)targets.get(reverse));
        }
        return previousToLatest;
    }

    private Set<SimpleNode> getCandidateNodes(DirectedPseudograph<SimpleNode, SimpleEdge> graph) {
        HashSet<SimpleNode> res = new HashSet<SimpleNode>();
        Set candidates = graph.vertexSet();
        for (SimpleNode v : candidates) {
            if (!v.signature.contains("->")) continue;
            res.add(v);
        }
        return res;
    }

    private long getlargetID(Set<SimpleNode> nodes) {
        long id = 0L;
        for (SimpleNode v : nodes) {
            id = Math.max(id, v.id);
        }
        return id + 1L;
    }

    public static void main(String[] args) {
        String path1 = "C:\\Users\\fang2\\OneDrive\\Desktop\\reptracker\\reptracker\\input_11\\dataleak_test_host2\\res\\dataleak_test_host2-case\\sysrep\\1\\filtered_by_forward_0_dataleakhost2_sysrep.dot";
        ReadGraphFromDot reader = new ReadGraphFromDot();
        DirectedPseudograph<SimpleNode, SimpleEdge> latest = reader.readGraph(path1);
        int previousSize = latest.edgeSet().size();
        String path2 = "C:\\Users\\fang2\\OneDrive\\Desktop\\reptracker\\reptracker\\input_11\\dataleak_test_host1\\res\\dataleak_test_host1-case\\sysrep\\1\\filtered_by_forward_0_dataleakhost1_sysrep.dot";
        DirectedPseudograph<SimpleNode, SimpleEdge> previous = reader.readGraph(path2);
        for (SimpleEdge e : previous.edgeSet()) {
            System.out.println(e.toString());
        }
        CombineTwoGraph test2 = new CombineTwoGraph();
        DirectedPseudograph<SimpleNode, SimpleEdge> res = test2.combineGraph(latest, previous);
        System.out.println("Merged----------------------");
        for (SimpleEdge e : res.edgeSet()) {
            System.out.println(e.toString());
        }
        assert (res.edgeSet().size() == previousSize || res.edgeSet().size() == previousSize + previous.edgeSet().size());
    }
}

