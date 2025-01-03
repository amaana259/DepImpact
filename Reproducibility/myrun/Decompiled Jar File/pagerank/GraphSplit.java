/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.DirectedPseudograph;
import pagerank.EntityIdProvider;
import pagerank.EntityNameProvider;
import pagerank.EntityNode;
import pagerank.EventEdge;
import pagerank.IterateGraph;
import pagerank.ProcessGraph;

public class GraphSplit {
    DirectedPseudograph<EntityNode, EventEdge> inputGraph;
    long curMaxVertexID;
    long curMaxEdgeID;
    private DOTExporter<EntityNode, EventEdge> exporter;
    Set<String> originalSignature;
    IterateGraph iter;

    public GraphSplit(DirectedPseudograph<EntityNode, EventEdge> input) {
        this.inputGraph = input;
        this.curMaxEdgeID = this.getMaxEdgeID(input);
        this.curMaxVertexID = this.getMaxVertexID(input);
        this.exporter = new DOTExporter(new EntityIdProvider(), new EntityNameProvider(), null);
        this.originalSignature = new HashSet<String>();
        Set set = this.inputGraph.vertexSet();
        for (EntityNode v : set) {
            this.originalSignature.add(v.getSignature());
        }
    }

    public void splitGraph() {
        System.out.println("splitGraph invoked!");
        List<VertexPair> list = this.getVertexPairNeedToBeSplited(this.inputGraph);
        System.out.println("Original ID is correct or not :");
        this.IDissue(this.inputGraph);
        HashSet<EntityNode> vertexHaveBeenSplited = new HashSet<EntityNode>();
        DirectedPseudograph newGraph = new DirectedPseudograph(EventEdge.class);
        Set<EntityNode> vertexNeedToBeSplited = this.getVertexSet(list);
        LinkedList<VertexPair> queue = new LinkedList<VertexPair>(list);
        if (queue.size() > 0) {
            System.out.println("This graph need split");
        } else {
            System.out.println("Don't need split");
        }
        while (!queue.isEmpty()) {
            while (!queue.isEmpty()) {
                VertexPair vPair = (VertexPair)queue.poll();
                if (vertexHaveBeenSplited.contains(vPair.source) || vertexHaveBeenSplited.contains(vPair.sink)) continue;
                List<EventEdge> otherEdges = this.getOtherEdges(this.inputGraph, vPair.source, vPair.sink);
                List<EventEdge> edgesBetweenTheseTwo = vPair.edgeList;
                this.updateGraph(otherEdges, edgesBetweenTheseTwo, vPair.source, vPair.sink, vertexHaveBeenSplited);
            }
            List<VertexPair> nextRound = this.getVertexPairNeedToBeSplited(this.inputGraph);
            if (nextRound.size() == 0) continue;
            LinkedList<VertexPair> nextQueue = new LinkedList<VertexPair>(nextRound);
            queue = nextQueue;
        }
        this.rebuildTimeLogical(vertexHaveBeenSplited);
        this.IDissue(this.inputGraph);
        this.iter = new IterateGraph(this.inputGraph);
    }

    private void updateGraph(List<EventEdge> otherEdges, List<EventEdge> edgesBetweenTheseTwoVertexs, EntityNode source, EntityNode sink, Set<EntityNode> vertexHaveBeenSplited) {
        int i;
        List<EntityNode> newVertexs = this.generateVertex(edgesBetweenTheseTwoVertexs);
        assert (newVertexs.size() == edgesBetweenTheseTwoVertexs.size());
        for (i = 0; i < newVertexs.size(); ++i) {
            EventEdge newEdges = new EventEdge(edgesBetweenTheseTwoVertexs.get(i), newVertexs.get(i), sink, ++this.curMaxEdgeID);
            this.inputGraph.addVertex(newVertexs.get(i));
            this.inputGraph.addEdge(newVertexs.get(i), sink, newEdges);
        }
        for (i = 0; i < newVertexs.size(); ++i) {
            for (EventEdge e : otherEdges) {
                EventEdge newEdge = new EventEdge(e, newVertexs.get(i), e.getSink(), ++this.curMaxEdgeID);
                this.inputGraph.addEdge(newVertexs.get(i), (EntityNode)this.inputGraph.getEdgeTarget(e), newEdge);
            }
        }
        Set<EventEdge> incomingEdges = this.inputGraph.incomingEdgesOf(source);
        for (int i2 = 0; i2 < newVertexs.size(); ++i2) {
            for (EventEdge e : incomingEdges) {
                EventEdge newEdge = new EventEdge(e, e.getSource(), newVertexs.get(i2), ++this.curMaxEdgeID);
                this.inputGraph.addEdge((EntityNode)this.inputGraph.getEdgeSource(e), newVertexs.get(i2), newEdge);
            }
        }
        this.removeEdgeOfSplitedVertex(incomingEdges);
        this.removeEdgeOfSplitedVertex(this.inputGraph.outgoingEdgesOf(source));
        this.inputGraph.removeVertex(source);
        vertexHaveBeenSplited.add(source);
    }

    private void removeEdgeOfSplitedVertex(Set<EventEdge> set) {
        ArrayList<EventEdge> list = new ArrayList<EventEdge>(set);
        for (int i = 0; i < list.size(); ++i) {
            this.inputGraph.removeEdge((EventEdge)list.get(i));
        }
    }

    private List<EntityNode> generateVertex(List<EventEdge> edges) {
        ArrayList<EntityNode> list = new ArrayList<EntityNode>();
        for (EventEdge e : edges) {
            EntityNode v = new EntityNode(e.getSource(), ++this.curMaxVertexID);
            list.add(v);
        }
        return list;
    }

    private List<EventEdge> getOtherEdges(DirectedPseudograph<EntityNode, EventEdge> graph, EntityNode source, EntityNode sink) {
        Set outEdges = graph.outgoingEdgesOf(source);
        ArrayList<EventEdge> list = new ArrayList<EventEdge>();
        for (EventEdge e : outEdges) {
            if (e.getSink().equals(sink)) continue;
            list.add(e);
        }
        return list;
    }

    private Set<EntityNode> getVertexSet(List<VertexPair> list) {
        HashSet<EntityNode> set = new HashSet<EntityNode>();
        for (VertexPair p : list) {
            set.add(p.sink);
            set.add(p.source);
        }
        return set;
    }

    private List<VertexPair> getVertexPairNeedToBeSplited(DirectedPseudograph<EntityNode, EventEdge> graph) {
        Set vertexSet = graph.vertexSet();
        LinkedList<VertexPair> list = new LinkedList<VertexPair>();
        for (EntityNode s2 : vertexSet) {
            Set<EventEdge> incomingEdges = graph.incomingEdgesOf(s2);
            Map<EntityNode, Integer> count2 = this.countEdgesBetweenVertexs(incomingEdges);
            for (EntityNode v : count2.keySet()) {
                if (count2.get(v) <= 1) continue;
                List<EventEdge> edges = this.getEdgesBetweenTheseTwoVertexs(incomingEdges, v);
                VertexPair vPair = new VertexPair(v, s2, edges);
                list.add(vPair);
            }
        }
        return list;
    }

    private Map<EntityNode, Integer> countEdgesBetweenVertexs(Set<EventEdge> iEdge) {
        HashMap<EntityNode, Integer> res = new HashMap<EntityNode, Integer>();
        for (EventEdge i : iEdge) {
            EntityNode source = i.getSource();
            res.put(source, res.getOrDefault(source, 0) + 1);
        }
        return res;
    }

    private List<EventEdge> getEdgesBetweenTheseTwoVertexs(Set<EventEdge> iEdges, EntityNode source) {
        ArrayList<EventEdge> list = new ArrayList<EventEdge>();
        for (EventEdge i : iEdges) {
            if (!i.getSource().equals(source)) continue;
            list.add(i);
        }
        return list;
    }

    private long getMaxEdgeID(DirectedPseudograph<EntityNode, EventEdge> afterCPR) {
        if (afterCPR == null) {
            throw new IllegalArgumentException("input parameter is null");
        }
        Set edgeSet = afterCPR.edgeSet();
        long maxID = Long.MIN_VALUE;
        for (EventEdge e : edgeSet) {
            maxID = Math.max(maxID, e.getID());
        }
        return maxID;
    }

    private long getMaxVertexID(DirectedPseudograph<EntityNode, EventEdge> afterCPR) {
        Set vertexSet = afterCPR.vertexSet();
        long maxId = Long.MIN_VALUE;
        for (EntityNode e : vertexSet) {
            maxId = Math.max(maxId, e.getID());
        }
        return maxId;
    }

    public void getOutPutGraph(String i) {
        this.IDissue(this.inputGraph);
        this.checkSignature();
        try {
            this.exporter.exportGraph(this.inputGraph, new FileWriter(String.format("%s.dot", i)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean IDissue(DirectedPseudograph<EntityNode, EventEdge> graph) {
        HashSet<Long> vertexID = new HashSet<Long>();
        HashSet<Long> edgeID = new HashSet<Long>();
        Set vertexs = graph.vertexSet();
        Set edges = graph.edgeSet();
        for (EntityNode v : vertexs) {
            if (vertexID.add(v.getID())) continue;
            System.out.println(v.getSignature() + v.getID());
            System.out.println("Duplicate vertex ID");
            break;
        }
        for (EventEdge e : edges) {
            if (edgeID.add(e.getID())) continue;
            System.out.println("Source: " + e.getSource().getID() + " Target: " + e.getSink().getID());
            System.out.println("Duplicate edge id: " + e.id);
            break;
        }
        return true;
    }

    public void outputGraph(String file) {
        this.iter.exportGraph(file);
    }

    private void checkSignature() {
        Set vertex = this.inputGraph.vertexSet();
        HashSet<String> newSignature = new HashSet<String>();
        for (EntityNode v : vertex) {
            newSignature.add(v.getSignature());
        }
        for (String s2 : this.originalSignature) {
            if (newSignature.contains(s2)) continue;
            System.out.println("lose vertex");
        }
    }

    @Deprecated
    public void rebuildTimeLogical() {
        Set vertexs = this.inputGraph.vertexSet();
        for (EntityNode v : vertexs) {
            BigDecimal largestTimeOfOutEdges = this.getLargestTimeOfOutEdges(v);
            Set incomEdgesOfV = this.inputGraph.incomingEdgesOf(v);
            HashSet<EventEdge> edgeNeedRemoved = new HashSet<EventEdge>();
            for (EventEdge iEdge : incomEdgesOfV) {
                if (largestTimeOfOutEdges.equals(BigDecimal.ZERO) || iEdge.getStartTime().compareTo(largestTimeOfOutEdges) <= 0) continue;
                edgeNeedRemoved.add(iEdge);
            }
            for (EventEdge edge : edgeNeedRemoved) {
                this.inputGraph.removeEdge(edge);
            }
        }
        this.removeNecessaryVertex();
    }

    public void rebuildTimeLogical(Set<EntityNode> splited) {
        Set vertexs = this.inputGraph.vertexSet();
        for (EntityNode v : vertexs) {
            for (EntityNode v2 : splited) {
                if (!v.getSignature().equals(v2.getSignature())) continue;
                BigDecimal largestTimeOfOutEdges = this.getLargestTimeOfOutEdges(v);
                Set incomEdgesOfV = this.inputGraph.incomingEdgesOf(v);
                HashSet<EventEdge> edgeNeedRemoved = new HashSet<EventEdge>();
                for (EventEdge iEdge : incomEdgesOfV) {
                    if (largestTimeOfOutEdges.equals(BigDecimal.ZERO) || iEdge.getStartTime().compareTo(largestTimeOfOutEdges) <= 0) continue;
                    edgeNeedRemoved.add(iEdge);
                }
                for (EventEdge edge : edgeNeedRemoved) {
                    this.inputGraph.removeEdge(edge);
                }
            }
        }
        this.removeNecessaryVertex();
    }

    private BigDecimal getLargestTimeOfOutEdges(EntityNode v) {
        Set outgoingEdges = this.inputGraph.outgoingEdgesOf(v);
        if (outgoingEdges.size() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal res = BigDecimal.ZERO;
        for (EventEdge e : outgoingEdges) {
            if (e.getEndTime().compareTo(res) <= 0) continue;
            res = e.getEndTime();
        }
        return res;
    }

    private void removeNecessaryVertex() {
        Set vertex = this.inputGraph.vertexSet();
        HashSet<EntityNode> vertexNeedRemoved = new HashSet<EntityNode>();
        for (EntityNode v : vertex) {
            Set outEdges = this.inputGraph.outgoingEdgesOf(v);
            Set inEdges = this.inputGraph.incomingEdgesOf(v);
            if (outEdges.size() != 0 || inEdges.size() != 0) continue;
            vertexNeedRemoved.add(v);
        }
        for (EntityNode v : vertexNeedRemoved) {
            this.inputGraph.removeVertex(v);
        }
    }

    public static void main(String[] args) {
        String[] localIP = new String[]{"129.22.21.193"};
        ProcessGraph test2 = new ProcessGraph("DataForSplit.txt", localIP);
        test2.backTrack("/home/fang/thesis2/code_about_data/test_output.txt", "File");
        GraphSplit test22 = new GraphSplit(test2.backTrack);
        test22.splitGraph();
        IterateGraph iterGraph = new IterateGraph(test22.inputGraph);
    }

    private class VertexPair {
        EntityNode source;
        EntityNode sink;
        List<EventEdge> edgeList;

        VertexPair(EntityNode source, EntityNode sink, List<EventEdge> edges) {
            this.source = source;
            this.sink = sink;
            this.edgeList = edges;
        }
    }
}

