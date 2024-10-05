/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.DirectedPseudograph;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import pagerank.EdgeAmountTimeProvider;
import pagerank.EdgeAttributeProvider;
import pagerank.EntityAttributeProvider;
import pagerank.EntityIdProvider;
import pagerank.EntityNameProvider;
import pagerank.EntityNode;
import pagerank.EventEdge;
import pagerank.EventEdgeProvider;
import pagerank.MetaConfig;

public class IterateGraph {
    DirectedPseudograph<EntityNode, EventEdge> inputgraph;
    DOTExporter<EntityNode, EventEdge> exporter;
    Map<String, EntityNode> indexOfNode;
    DOTExporter<EntityNode, EventEdge> exporterWithTimeAndData;

    public IterateGraph(DirectedPseudograph<EntityNode, EventEdge> graph) {
        this.inputgraph = graph;
        this.exporter = new DOTExporter<EntityNode, EventEdge>(new EntityIdProvider(), new EntityNameProvider(), new EventEdgeProvider(), new EntityAttributeProvider(), null);
        this.exporterWithTimeAndData = new DOTExporter<EntityNode, EventEdge>(new EntityIdProvider(), new EntityNameProvider(), new EventEdgeProvider(), new EntityAttributeProvider(), new EdgeAttributeProvider());
        this.indexOfNode = new HashMap<String, EntityNode>();
        for (EntityNode n : graph.vertexSet()) {
            this.indexOfNode.put(n.getSignature(), n);
        }
    }

    public DirectedPseudograph<EntityNode, EventEdge> bfs(String input) {
        EntityNode start = this.getGraphVertex(input);
        LinkedList queue = new LinkedList();
        if (start != null) {
            return this.bfs(start);
        }
        System.out.println("Your input doesn't exist in the graph");
        return null;
    }

    private DirectedPseudograph<EntityNode, EventEdge> bfs(EntityNode start) {
        LinkedList<EntityNode> queue = new LinkedList<EntityNode>();
        DirectedPseudograph<EntityNode, EventEdge> newgraph = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        queue.offer(start);
        HashSet<EntityNode> nodeInTheQueue = new HashSet<EntityNode>();
        nodeInTheQueue.add(start);
        while (!queue.isEmpty()) {
            EntityNode cur = (EntityNode)queue.poll();
            newgraph.addVertex(cur);
            Set inEdges = this.inputgraph.incomingEdgesOf(cur);
            for (EventEdge edge : inEdges) {
                EntityNode source = edge.getSource();
                newgraph.addVertex(source);
                newgraph.addEdge(source, cur, edge);
                if (nodeInTheQueue.contains(source)) continue;
                nodeInTheQueue.add(source);
                queue.offer(source);
            }
            Set outEdges = this.inputgraph.outgoingEdgesOf(cur);
            for (EventEdge edge : outEdges) {
                EntityNode target = edge.getSink();
                newgraph.addVertex(target);
                newgraph.addEdge(cur, target, edge);
                if (nodeInTheQueue.contains(target)) continue;
                nodeInTheQueue.add(target);
                queue.offer(target);
            }
        }
        return newgraph;
    }

    public void exportGraph(String fileName) {
        try {
            String dotName = String.format("%s.dot", fileName);
            this.exporter.exportGraph(this.inputgraph, new FileWriter(dotName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportGraphWithEdgeInfo(String fileName) {
        try {
            String dotName = String.format("%s.dot", fileName);
            this.exporterWithTimeAndData.exportGraph(this.inputgraph, new FileWriter(dotName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportGraph(DirectedPseudograph<EntityNode, EventEdge> graph, String fileName) {
        try {
            this.exporter.exportGraph(graph, new FileWriter(String.format("%s.dot", fileName)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportGraphBasedOnThreshold(DirectedPseudograph<EntityNode, EventEdge> graph, String POI, double change, boolean forward, String fileName) {
        DirectedPseudograph<EntityNode, EventEdge> res = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        EntityNode start = this.getGraphVertex(POI);
        assert (start != null);
        HashSet<EntityNode> processedVertex = new HashSet<EntityNode>();
        LinkedList<EntityNode> queue = new LinkedList<EntityNode>();
        queue.offer(start);
        processedVertex.add(start);
        while (!queue.isEmpty()) {
            EntityNode target;
            EntityNode source;
            Set edgeSet;
            EntityNode cur = (EntityNode)queue.poll();
            res.addVertex(cur);
            if (forward) {
                edgeSet = graph.outgoingEdgesOf(cur);
                for (EventEdge edge : edgeSet) {
                    source = edge.getSource();
                    if (!this.satisfyChangeRate(source, target = edge.getSink(), change)) continue;
                    res.addVertex(target);
                    res.addEdge(source, target, edge);
                    if (processedVertex.contains(target)) continue;
                    processedVertex.add(target);
                    queue.offer(target);
                }
                continue;
            }
            edgeSet = graph.incomingEdgesOf(cur);
            for (EventEdge edge : edgeSet) {
                source = edge.getSource();
                target = edge.getSink();
                if (!this.satisfyChangeRate(target, source, change)) continue;
                res.addVertex(source);
                res.addEdge(source, target, edge);
                if (processedVertex.contains(source)) continue;
                processedVertex.add(source);
                queue.offer(source);
            }
        }
        this.exportGraph(res, "limited_reputation_change" + fileName);
    }

    private boolean satisfyChangeRate(EntityNode node1, EntityNode node2, double change) {
        double reputationDiff = node1.reputation - node2.reputation;
        if (node1.reputation == 0.0) {
            return false;
        }
        return reputationDiff / node1.reputation <= change;
    }

    public EntityNode getGraphVertex(String input) {
        if (this.indexOfNode.containsKey(input)) {
            return this.indexOfNode.get(input);
        }
        System.out.println("Can't find the vertex");
        return null;
    }

    public void printVertexReputation() {
        Set vertex = this.inputgraph.vertexSet();
        int count2 = 0;
        for (EntityNode v : vertex) {
            System.out.print(String.valueOf(v.getReputation()) + " ");
            if ((++count2 + 1) % 20 != 0) continue;
            System.out.println();
        }
    }

    public boolean findProcessNode(DirectedPseudograph<EntityNode, EventEdge> graph, String pname) {
        Set vertex = graph.vertexSet();
        for (EntityNode v : vertex) {
            if (v.getP() == null || !v.getP().getName().equals(pname)) continue;
            return true;
        }
        return false;
    }

    public void exportGraphAmountAndTime(String file) {
        DOTExporter<EntityNode, EventEdge> export = new DOTExporter<EntityNode, EventEdge>(new EntityIdProvider(), new EntityNameProvider(), new EdgeAmountTimeProvider(), new EntityAttributeProvider(), null);
        try {
            export.exportGraph(this.inputgraph, new FileWriter(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printPathsOfSpecialVertex(String vertex) {
        EntityNode v1 = this.getGraphVertex(vertex);
        assert (v1 != null);
        Set outgoing = this.inputgraph.outgoingEdgesOf(v1);
        Set incoming = this.inputgraph.incomingEdgesOf(v1);
        ArrayList<EventEdge> list = new ArrayList<EventEdge>(outgoing);
        this.sortEdgesBasedOnWeight(list);
        ArrayList<EventEdge> list2 = new ArrayList<EventEdge>(incoming);
        this.sortEdgesBasedOnWeight(list2);
        try {
            EventEdge edge;
            int i;
            FileWriter w = new FileWriter(new File(String.format("%s.txt", vertex)));
            for (i = 0; i < list.size(); ++i) {
                edge = (EventEdge)list.get(i);
                w.write("Target: " + edge.getSink().getSignature() + " Data: " + edge.getSize() + " Weight: " + edge.weight + System.lineSeparator());
            }
            for (i = 0; i < list2.size(); ++i) {
                edge = (EventEdge)list2.get(i);
                w.write("Source: " + edge.getSource().getSignature() + " Data: " + edge.getSize() + " Weiget: " + edge.weight + System.lineSeparator());
            }
            w.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BigDecimal getLatestOperationTime(String str) {
        EntityNode vertex = this.getGraphVertex(str);
        assert (vertex != null);
        Set incoming = this.inputgraph.incomingEdgesOf(vertex);
        BigDecimal res = BigDecimal.ZERO;
        for (EventEdge e : incoming) {
            if (res.compareTo(e.getStartTime()) >= 0) continue;
            res = e.getStartTime();
        }
        return res;
    }

    public BigDecimal getLatestOperationTimeByOutgoing(String str) {
        EntityNode vertex = this.getGraphVertex(str);
        assert (vertex != null);
        Set outgoing = this.inputgraph.outgoingEdgesOf(vertex);
        BigDecimal res = BigDecimal.ZERO;
        for (EventEdge e : outgoing) {
            if (res.compareTo(e.getStartTime()) >= 0) continue;
            res = e.getStartTime();
        }
        return res;
    }

    public void OutputPaths(List<GraphPath<EntityNode, EventEdge>> paths) {
        System.out.println("Paths size:" + paths.size());
        for (int i = 0; i < paths.size(); ++i) {
            Graph<EntityNode, EventEdge> g2 = paths.get(i).getGraph();
            String fileName = String.format("Path %d.dot", i);
            try {
                this.exporter.exportGraph(g2, new FileWriter(new File(fileName)));
                continue;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sortEdgesBasedOnWeight(List<EventEdge> edges) {
        Comparator<EventEdge> cp = new Comparator<EventEdge>(){

            @Override
            public int compare(EventEdge a, EventEdge b) {
                if (a.weight >= b.weight) {
                    return 1;
                }
                return 0;
            }
        };
        Collections.sort(edges, cp);
    }

    public BigDecimal getLatestOperationTime(EntityNode node) {
        assert (node != null);
        Set edges = this.inputgraph.incomingEdgesOf(node);
        BigDecimal res = BigDecimal.ZERO;
        for (EventEdge e : edges) {
            if (res.compareTo(e.getEndTime()) >= 0) continue;
            res = e.getEndTime();
        }
        return res;
    }

    public List<DirectedPseudograph<EntityNode, EventEdge>> getHighWeightPaths(String s2) {
        EntityNode start = this.getGraphVertex(s2);
        assert (start != null);
        ArrayList<DirectedPseudograph<EntityNode, EventEdge>> paths = new ArrayList<DirectedPseudograph<EntityNode, EventEdge>>();
        for (int i = 0; i < 5; ++i) {
            DirectedPseudograph<EntityNode, EventEdge> path = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
            LinkedList<EntityNode> queue = new LinkedList<EntityNode>();
            queue.offer(start);
            HashSet<EntityNode> visited = new HashSet<EntityNode>();
            while (!queue.isEmpty()) {
                EventEdge out;
                Set<EventEdge> outgoing;
                EntityNode cur = (EntityNode)queue.poll();
                visited.add(cur);
                path.addVertex(cur);
                Set<EventEdge> incoming = this.inputgraph.incomingEdgesOf(cur);
                if (incoming.size() > 0) {
                    EventEdge inc;
                    List<EventEdge> listOfIncoming = this.sortBasedOnWeight(incoming);
                    if (listOfIncoming.size() == 1) {
                        inc = listOfIncoming.get(0);
                        path.addVertex(inc.getSource());
                        path.addEdge(inc.getSource(), cur, inc);
                        if (!visited.contains(inc.getSource())) {
                            queue.offer(inc.getSource());
                        }
                    } else {
                        inc = listOfIncoming.get(0);
                        if (!visited.contains(inc.getSource())) {
                            queue.offer(inc.getSource());
                        }
                        path.addVertex(inc.getSource());
                        path.addEdge(inc.getSource(), cur, inc);
                        this.inputgraph.removeEdge(inc);
                    }
                }
                if ((outgoing = this.inputgraph.outgoingEdgesOf(cur)).size() <= 0) continue;
                List<EventEdge> listOfOutgoing = this.sortBasedOnWeight(outgoing);
                if (listOfOutgoing.size() == 1) {
                    out = listOfOutgoing.get(0);
                    path.addVertex(out.getSink());
                    path.addEdge(cur, out.getSink(), out);
                    if (visited.contains(out.getSink())) continue;
                    queue.offer(out.getSink());
                    continue;
                }
                out = listOfOutgoing.get(0);
                queue.offer(out.getSink());
                path.addVertex(out.getSink());
                path.addEdge(cur, out.getSink(), out);
                if (!visited.contains(out.getSink())) {
                    queue.offer(out.getSink());
                }
                this.inputgraph.removeEdge(out);
            }
            paths.add(path);
        }
        return paths;
    }

    public void printEdgesOfVertex(String s2, String suffix) {
        EntityNode vertex = this.getGraphVertex(s2);
        assert (vertex != null);
        Set<EventEdge> incoming = this.inputgraph.incomingEdgesOf(vertex);
        List<EventEdge> list = this.sortBasedOnWeight(incoming);
        String fileName = "edgesOf" + s2;
        Set<EventEdge> outgoing = this.inputgraph.outgoingEdgesOf(vertex);
        List<EventEdge> list2 = this.sortBasedOnWeight(outgoing);
        try {
            String cur;
            int i;
            FileWriter writer = new FileWriter(s2 + "_" + suffix + "_edge_weights.txt");
            writer.write("Incoming: \n");
            for (i = 0; i < list.size(); ++i) {
                cur = this.outputEdge(list.get(i));
                writer.write(cur + "\n");
            }
            writer.write("Outgoing: \n");
            for (i = 0; i < list2.size(); ++i) {
                cur = this.outputEdge(list2.get(i));
                writer.write(cur + "\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printEdgesOfVertexes(String s2, String suffix, Map<Long, Double> time, Map<Long, Double> amount, Map<Long, Double> structure) {
        EntityNode vertex = this.getGraphVertex(s2);
        try {
            File file = new File(s2 + "_" + suffix + "_edge_weights.txt");
            FileWriter fileWriter = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            Set incoming = this.inputgraph.incomingEdgesOf(vertex);
            printWriter.println("Incoming: ");
            for (EventEdge e : incoming) {
                printWriter.println(e.toString());
                String weights = "TimeWeight: " + Double.toString(time.get(e.id)) + "AmountWeight: " + Double.toString(amount.get(e.id)) + "StructureWeight: " + Double.toString(structure.get(e.id));
                printWriter.println(weights);
            }
            printWriter.println("Outgoing: ");
            Set outgoing = this.inputgraph.outgoingEdgesOf(vertex);
            for (EventEdge e : outgoing) {
                printWriter.println(e.toString());
                String weights = "TimeWeight: " + Double.toString(time.get(e.id)) + "AmountWeight: " + Double.toString(amount.get(e.id)) + "StructureWeight: " + Double.toString(structure.get(e.id));
                printWriter.println(weights);
            }
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printEdgesOfVertexes(List<String> list, String suffix) {
        for (String v : list) {
            this.printEdgesOfVertex(v, suffix);
        }
    }

    public long getBiggestEdgeID() {
        Set edges = this.inputgraph.edgeSet();
        long res = 0L;
        for (EventEdge e : edges) {
            res = Math.max(e.id, res);
        }
        return res + 1L;
    }

    private String outputEdge(EventEdge e) {
        return e.toString();
    }

    private List<EventEdge> sortBasedOnWeight(Set<EventEdge> edges) {
        ArrayList<EventEdge> list = new ArrayList<EventEdge>(edges);
        Comparator<EventEdge> cmp = new Comparator<EventEdge>(){

            @Override
            public int compare(EventEdge a, EventEdge b) {
                double diff = b.weight - a.weight;
                if (diff == 0.0) {
                    return 0;
                }
                if (diff > 0.0) {
                    return 1;
                }
                return -1;
            }
        };
        Collections.sort(list, cmp);
        return list;
    }

    public double avergeEdgeWeight() {
        int nums = this.inputgraph.edgeSet().size();
        double sum2 = 0.0;
        for (EventEdge edge : this.inputgraph.edgeSet()) {
            sum2 += edge.weight;
        }
        return sum2 / ((double)nums * 1.0);
    }

    public void filterGraphBasedOnAverageWeight() {
        double averageEdgeWeight = this.avergeEdgeWeight() / 100.0;
        ArrayList edges = new ArrayList(this.inputgraph.edgeSet());
        for (int i = 0; i < edges.size(); ++i) {
            if (!(((EventEdge)edges.get((int)i)).weight < averageEdgeWeight)) continue;
            this.inputgraph.removeEdge((EventEdge)edges.get(i));
        }
        ArrayList list = new ArrayList(this.inputgraph.vertexSet());
        for (int i = 0; i < list.size(); ++i) {
            EntityNode v = (EntityNode)list.get(i);
            if (this.inputgraph.incomingEdgesOf(v).size() != 0 || this.inputgraph.outgoingEdgesOf(v).size() != 0) continue;
            this.inputgraph.removeVertex(v);
        }
    }

    public void filterGraphBasedOnVertexReputation() {
        ArrayList vlist = new ArrayList(this.inputgraph.vertexSet());
        for (int i = 0; i < vlist.size(); ++i) {
            EntityNode v = (EntityNode)vlist.get(i);
            if (v.reputation != 0.0) continue;
            ArrayList inc = new ArrayList(this.inputgraph.incomingEdgesOf(v));
            for (int j = 0; j < inc.size(); ++j) {
                this.inputgraph.removeEdge((EventEdge)inc.get(j));
            }
            ArrayList out = new ArrayList(this.inputgraph.outgoingEdgesOf(v));
            for (int j = 0; j < out.size(); ++j) {
                this.inputgraph.removeEdge((EventEdge)out.get(j));
            }
            this.inputgraph.removeVertex(v);
        }
    }

    public static void printReputation(DirectedPseudograph<EntityNode, EventEdge> graph, int step) {
        try {
            FileWriter writer = new FileWriter(String.valueOf(step) + ".txt");
            PrintWriter pwriter = new PrintWriter(writer);
            for (EntityNode v : graph.vertexSet()) {
                pwriter.write(v.getSignature() + ": " + String.valueOf(v.reputation) + "\n");
            }
            pwriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeSingleVertex() {
        ArrayList list = new ArrayList(this.inputgraph.vertexSet());
        for (int i = 0; i < list.size(); ++i) {
            EntityNode v = (EntityNode)list.get(i);
            if (this.inputgraph.incomingEdgesOf(v).size() != 0 || this.inputgraph.outgoingEdgesOf(v).size() != 0) continue;
            this.inputgraph.removeVertex(v);
        }
    }

    public static Map<String, Integer> countEdgeBasedOnNodeSignature(DirectedPseudograph<EntityNode, EventEdge> graph) {
        HashMap<String, Integer> res = new HashMap<String, Integer>();
        Set edgeSet = graph.edgeSet();
        for (EventEdge edge : edgeSet) {
            String key = IterateGraph.convertEdgeToString(edge);
            if (!res.containsKey(key)) {
                res.put(key, 1);
                continue;
            }
            res.put(key, (Integer)res.get(key) + 1);
        }
        return res;
    }

    public static Map<String, Integer> groupsEdges(List<DirectedPseudograph<EntityNode, EventEdge>> graphs) {
        HashMap<String, Integer> edgeCount = new HashMap<String, Integer>();
        for (DirectedPseudograph<EntityNode, EventEdge> graph : graphs) {
            Set edges = graph.edgeSet();
            for (EventEdge edge : edges) {
                String key = IterateGraph.convertEdgeToString(edge);
                if (!edgeCount.containsKey(key)) {
                    edgeCount.put(key, 0);
                }
                edgeCount.put(key, (Integer)edgeCount.get(key) + 1);
            }
        }
        return edgeCount;
    }

    public static String convertEdgeToString(EventEdge edge) {
        StringBuilder sb = new StringBuilder();
        sb.append(edge.getSource().getSignature());
        sb.append(" -> ");
        sb.append(edge.getSink().getSignature());
        return sb.toString();
    }

    public static Map<String, Double> getNodeReputation(DirectedPseudograph<EntityNode, EventEdge> graph) {
        HashMap<String, Double> nodeReputation = new HashMap<String, Double>();
        for (EntityNode node : graph.vertexSet()) {
            nodeReputation.put(node.getSignature(), node.reputation);
        }
        return nodeReputation;
    }

    public static void printEdgeByWeights(String fileName, DirectedPseudograph<EntityNode, EventEdge> graph) {
        ArrayList edgeList = new ArrayList(graph.edgeSet());
        HashMap edgeMap = new HashMap();
        for (int i = 0; i <= 9; ++i) {
            double d = (double)i / 10.0;
            edgeMap.put(d, new LinkedList());
        }
        for (EventEdge edge : edgeList) {
            if (edge.weight >= 0.9) {
                ((List)edgeMap.get(0.9)).add(edge);
                continue;
            }
            if (edge.weight >= 0.8) {
                ((List)edgeMap.get(0.8)).add(edge);
                continue;
            }
            if (edge.weight >= 0.7) {
                ((List)edgeMap.get(0.7)).add(edge);
                continue;
            }
            if (edge.weight >= 0.6) {
                ((List)edgeMap.get(0.6)).add(edge);
                continue;
            }
            if (edge.weight >= 0.5) {
                ((List)edgeMap.get(0.5)).add(edge);
                continue;
            }
            if (edge.weight >= 0.4) {
                ((List)edgeMap.get(0.4)).add(edge);
                continue;
            }
            if (edge.weight >= 0.3) {
                ((List)edgeMap.get(0.3)).add(edge);
                continue;
            }
            if (edge.weight >= 0.2) {
                ((List)edgeMap.get(0.2)).add(edge);
                continue;
            }
            if (edge.weight >= 0.1) {
                ((List)edgeMap.get(0.1)).add(edge);
                continue;
            }
            ((List)edgeMap.get(0.0)).add(edge);
        }
        for (Double k : edgeMap.keySet()) {
            IterateGraph.printEdges(k, fileName, (List)edgeMap.get(k));
        }
    }

    private static void printEdges(double wegihtLevel, String fileName, List<EventEdge> edges) {
        try {
            System.out.println(edges);
            File file = new File(fileName + "_" + Double.toString(wegihtLevel));
            FileWriter writer = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(writer);
            for (EventEdge edge : edges) {
                printWriter.println(edge.getSource().getSignature() + "->" + edge.getSink().getSignature() + ": " + Double.toString(edge.weight));
            }
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printVertexReputationLimitedByStepToFile(Map<String, Integer> stepInfo, Map<String, Double> reputations, int step) {
        try {
            File res = new File(String.format("%dStep_Node_reputation.txt", step));
            FileWriter fileWriter = new FileWriter(res);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            for (String node : stepInfo.keySet()) {
                if (stepInfo.get(node) != step) continue;
                printWriter.println(node + ": " + reputations.get(node).toString());
            }
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, EntityNode> getSignatureNodeMap(DirectedPseudograph<EntityNode, EventEdge> graph) {
        HashMap<String, EntityNode> map2 = new HashMap<String, EntityNode>();
        graph.vertexSet().stream().forEach(v -> map2.put(v.getSignature(), (EntityNode)v));
        return map2;
    }

    public static List<String> getCandidateEntryPoint(DirectedPseudograph<EntityNode, EventEdge> graph) {
        LinkedList<String> res = new LinkedList<String>();
        HashSet<String> libraries = new HashSet<String>(Arrays.asList(MetaConfig.midRP));
        for (EntityNode v : graph.vertexSet()) {
            if (v.isNetworkNode()) {
                res.add(v.getSignature());
                continue;
            }
            if (v.isProcessNode()) {
                Set incoming = graph.incomingEdgesOf(v);
                boolean sourceIsIP = false;
                boolean sourceIsProcess = false;
                for (EventEdge edge : incoming) {
                    EntityNode node = edge.getSource();
                    if (node.isNetworkNode()) {
                        sourceIsIP = true;
                        break;
                    }
                    if (!node.isProcessNode()) continue;
                    sourceIsProcess = true;
                    break;
                }
                if (sourceIsIP || sourceIsProcess) continue;
                res.add(v.getSignature());
                continue;
            }
            if (libraries.contains(v.getSignature()) || graph.incomingEdgesOf(v).size() != 0) continue;
            res.add(v.getSignature());
        }
        return res;
    }

    public static List<String> getCandidateEntryPoint(DirectedPseudograph<EntityNode, EventEdge> graph, Set<String> skip, Set<String> must) {
        LinkedList<String> res = new LinkedList<String>();
        HashSet<String> libraries = new HashSet<String>(Arrays.asList(MetaConfig.midRP));
        for (EntityNode v : graph.vertexSet()) {
            String sig = v.getSignature();
            if (skip.contains(sig)) continue;
            if (must.contains(sig)) {
                // empty if block
            }
            if (v.isNetworkNode()) {
                res.add(v.getSignature());
                continue;
            }
            if (v.isProcessNode()) {
                Set incoming = graph.incomingEdgesOf(v);
                boolean sourceIsIP = false;
                boolean sourceIsProcess = false;
                for (EventEdge edge : incoming) {
                    EntityNode node = edge.getSource();
                    if (node.isNetworkNode()) {
                        sourceIsIP = true;
                        break;
                    }
                    if (!node.isProcessNode()) continue;
                    sourceIsProcess = true;
                    break;
                }
                if (sourceIsIP || sourceIsProcess) continue;
                res.add(v.getSignature());
                continue;
            }
            if (libraries.contains(v.getSignature()) || graph.incomingEdgesOf(v).size() != 0) continue;
            res.add(v.getSignature());
        }
        return res;
    }

    public static void sortedSignatureBasedOnRP(List<String> signatures, Map<String, Double> nodeReputation) {
        Collections.sort(signatures, (a, b) -> ((Double)nodeReputation.get(b)).compareTo((Double)nodeReputation.get(a)));
    }

    public static void outputTopStarts(String resultDir, List<List<String>> starts, Map<String, Double> reputations) {
        try {
            JSONObject start_rank = new JSONObject();
            File startsReputation = new File(resultDir + "start_rank.txt");
            FileWriter fileWriter = new FileWriter(startsReputation);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            for (int i = 0; i < starts.size(); ++i) {
                List<String> list = starts.get(i);
                JSONArray points = new JSONArray();
                printWriter.println("-----------------------------------------");
                for (String s2 : list) {
                    points.add(s2);
                    printWriter.println(s2 + ": " + reputations.get(s2));
                }
                if (i == 0) {
                    start_rank.put("Process Start", points);
                    continue;
                }
                if (i == 1) {
                    start_rank.put("IP Start", points);
                    continue;
                }
                start_rank.put("File Start", points);
            }
            printWriter.close();
            File entryPointsJsonFile = new File(resultDir + "start_rank.json");
            FileWriter jsonWriter = new FileWriter(entryPointsJsonFile);
            jsonWriter.write(start_rank.toJSONString());
            jsonWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<List<String>> randomPickEntryStartsBasedOnCategory(List<List<String>> starts) {
        ArrayList<List<String>> res = new ArrayList<List<String>>();
        for (List<String> list : starts) {
            ArrayList<String> randomList = new ArrayList<String>(list);
            for (int i = 0; i < 100; ++i) {
                Collections.shuffle(randomList);
            }
            res.add(randomList);
        }
        return res;
    }

    public static List<String> getRandomStarts(DirectedPseudograph<EntityNode, EventEdge> graph, int number) {
        ArrayList<String> res = new ArrayList<String>();
        LinkedList<String> nodes = new LinkedList<String>();
        for (EntityNode v : graph.vertexSet()) {
            nodes.add(v.getSignature());
        }
        Collections.shuffle(nodes);
        for (int i = 0; i < number; ++i) {
            res.add((String)nodes.get(i));
        }
        return res;
    }

    public boolean checkMultihost(DirectedPseudograph<EntityNode, EventEdge> graph) {
        HashSet<String> hosts = new HashSet<String>();
        Set vertexs = graph.vertexSet();
        for (EntityNode v : vertexs) {
            if (v.isFileNode()) {
                hosts.add(v.getF().getLocation());
                continue;
            }
            if (v.isNetworkNode()) {
                hosts.add(v.getN().getLocation());
                continue;
            }
            hosts.add(v.getP().getLocation());
        }
        if (hosts.size() > 1) {
            System.out.println(hosts);
            return true;
        }
        return false;
    }

    public static List<String> getRandomStarts(List<String> nodes, int number) {
        int i;
        ArrayList<String> res = new ArrayList<String>();
        for (i = 0; i < 10; ++i) {
            Collections.shuffle(nodes);
        }
        for (i = 0; i < number; ++i) {
            res.add(nodes.get(i));
        }
        return res;
    }

    public int[] countFileOPBasedOnDataAmount(int number) {
        int[] res = new int[number];
        Set edgeSet = this.inputgraph.edgeSet();
        for (EventEdge edge : edgeSet) {
            EntityNode source = edge.getSource();
            EntityNode sink = edge.getSink();
            if (!source.isFileNode() && !sink.isFileNode()) continue;
            long amount = edge.getSize();
            int idx = (int)amount / 10000;
            if (idx >= res.length) {
                int n = res.length - 1;
                res[n] = res[n] + 1;
                continue;
            }
            int n = idx;
            res[n] = res[n] + 1;
        }
        return res;
    }

    public int[] countDataAmound(int number) {
        int[] res = new int[number];
        Set edgeSet = this.inputgraph.edgeSet();
        for (EventEdge edge : edgeSet) {
            int id;
            long amount = edge.getSize();
            long idx = amount / 10000L;
            if (idx < 0L) {
                System.out.println("Issue Amount:" + String.valueOf(amount));
            }
            if (idx >= (long)res.length) {
                int n = res.length - 1;
                res[n] = res[n] + 1;
                continue;
            }
            if (idx < 0L) {
                idx = 0L;
            }
            int n = id = Integer.valueOf(String.valueOf(idx)).intValue();
            res[n] = res[n] + 1;
        }
        return res;
    }
}

