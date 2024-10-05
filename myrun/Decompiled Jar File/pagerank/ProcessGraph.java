/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.DirectedPseudograph;
import pagerank.EntityNode;
import pagerank.EventEdge;
import pagerank.GetGraph;
import pagerank.IterateGraph;

public class ProcessGraph {
    private GetGraph getGraph;
    DirectedPseudograph<EntityNode, EventEdge> jGraph;
    public DirectedPseudograph<EntityNode, EventEdge> backTrack;
    DirectedPseudograph<EntityNode, EventEdge> afterCPR;
    DirectedPseudograph<EntityNode, EventEdge> graphForPR;
    DOTExporter<EntityNode, EventEdge> exporter;
    String input = null;
    String type = null;
    public BigDecimal POItime;
    EntityNode POIEvent;

    public ProcessGraph(String filePath, String[] localIP) {
        this.getGraph = new GetGraph(filePath, localIP);
        this.getGraph.GenerateGraph();
        this.jGraph = this.getGraph.getJg();
        this.exporter = this.getGraph.getExporter();
        this.POItime = null;
    }

    private Map<Long, EntityNode> getNodeMap(DirectedPseudograph<EntityNode, EventEdge> graph) {
        HashMap<Long, EntityNode> map2 = new HashMap<Long, EntityNode>();
        Set vertexs = graph.vertexSet();
        for (EntityNode v : vertexs) {
            map2.put(v.getID(), v);
        }
        return map2;
    }

    public void backTrack(String input, String type) {
        this.backTrack = this.getGraph.backTrackPoi(input, type);
        this.input = input;
        this.type = type;
    }

    public void backTrack(String input) {
        this.backTrack = this.getGraph.backTrackPoi(input);
        this.input = input;
    }

    public void backTrackWithHopCount(String input, String type, int count2) {
        this.getGraph.backTrackWithHopCount(input, type, count2);
    }

    public void outputBackTrack(String input, String type) throws IOException {
        this.exporter.exportGraph(this.backTrack, new FileWriter(String.format("%s_backTrack.dot", input)));
    }

    public void outputGraph(String name, DirectedPseudograph<EntityNode, EventEdge> graph) throws IOException {
        this.exporter.exportGraph(graph, new FileWriter(String.format("%s_.dot", name)));
    }

    public void CPR() {
        DirectedPseudograph<EntityNode, EventEdge> copyOfBackTrack = this.backTrack;
        Set set = copyOfBackTrack.edgeSet();
        List<EventEdge> edgeList = new LinkedList<EventEdge>(set);
        edgeList = this.sortAccordingStartTime(edgeList);
        System.out.println("Finish sort List");
        Iterator<EventEdge> iter = edgeList.iterator();
        HashMap<EntityNode, Map<EntityNode, Deque<EventEdge>>> mapOfStack = new HashMap<EntityNode, Map<EntityNode, Deque<EventEdge>>>();
        while (iter.hasNext()) {
            EventEdge cur = iter.next();
            EntityNode u = cur.getSource();
            EntityNode v = cur.getSink();
            if (mapOfStack.containsKey(u)) {
                Map values2 = (Map)mapOfStack.get(u);
                if (values2.containsKey(v)) {
                    Deque edgeStack = (Deque)values2.get(v);
                    if (edgeStack.isEmpty()) {
                        System.out.println("Here is not correct");
                        break;
                    }
                    EventEdge earlyEdge = (EventEdge)edgeStack.pop();
                    if (this.forwardCheck(earlyEdge, cur, v) && this.backwardCheck(earlyEdge, cur, u)) {
                        earlyEdge.merge(cur);
                        edgeStack.push(earlyEdge);
                        continue;
                    }
                    edgeStack.push(cur);
                    continue;
                }
                ArrayDeque<EventEdge> stack = new ArrayDeque<EventEdge>();
                stack.push(cur);
                values2.put(v, stack);
                continue;
            }
            ArrayDeque<EventEdge> stack = new ArrayDeque<EventEdge>();
            stack.push(cur);
            HashMap<EntityNode, ArrayDeque<EventEdge>> value = new HashMap<EntityNode, ArrayDeque<EventEdge>>();
            value.put(v, stack);
            mapOfStack.put(u, value);
        }
        this.getCPR(mapOfStack);
    }

    private void getCPR(Map<EntityNode, Map<EntityNode, Deque<EventEdge>>> mapOfStacks) {
        DirectedPseudograph<EntityNode, EventEdge> res = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        for (EntityNode u : mapOfStacks.keySet()) {
            Map<EntityNode, Deque<EventEdge>> cur = mapOfStacks.get(u);
            for (EntityNode v : cur.keySet()) {
                res.addVertex(u);
                res.addVertex(v);
                while (!cur.get(v).isEmpty()) {
                    EventEdge edge = cur.get(v).pop();
                    res.addEdge(u, v, edge);
                }
            }
        }
        this.afterCPR = res;
    }

    public void ouptGraphAfterCPR() throws IOException {
        if (this.afterCPR == null) {
            System.out.println("You need Run CPR firstly");
        }
        this.exporter.exportGraph(this.afterCPR, new FileWriter(String.format("%s_afterCPR.dot", this.input)));
    }

    private List<EventEdge> sortAccordingStartTime(List<EventEdge> list) {
        Comparator<EventEdge> cmp = new Comparator<EventEdge>(){

            @Override
            public int compare(EventEdge a, EventEdge b) {
                return a.getStartTime().compareTo(b.getStartTime());
            }
        };
        list.sort(cmp);
        return list;
    }

    private boolean backwardCheck(EventEdge p, EventEdge l, EntityNode u) {
        Set incoming = this.backTrack.incomingEdgesOf(u);
        Object[] endTimes = new BigDecimal[]{p.getEndTime(), l.getEndTime()};
        Arrays.sort(endTimes);
        for (EventEdge edge : incoming) {
            BigDecimal[] timeWindow = edge.getInterval();
            if (!this.isOverlap(timeWindow, (BigDecimal[])endTimes)) continue;
            return false;
        }
        return true;
    }

    private boolean forwardCheck(EventEdge p, EventEdge l, EntityNode u) {
        Object[] startTime = new BigDecimal[]{p.getStartTime(), l.getStartTime()};
        Set outgoing = this.backTrack.outgoingEdgesOf(u);
        Arrays.sort(startTime);
        for (EventEdge edge : outgoing) {
            BigDecimal[] timeWindow = edge.getInterval();
            if (!this.isOverlap(timeWindow, (BigDecimal[])startTime)) continue;
            return false;
        }
        return true;
    }

    private boolean isOverlap(BigDecimal[] a, BigDecimal[] b) {
        return a[1].compareTo(b[0]) >= 0 && a[1].compareTo(b[1]) <= 0 || a[0].compareTo(b[0]) >= 0 && a[0].compareTo(b[1]) <= 0;
    }

    private void EdgeSourcetest() {
        Set set = this.backTrack.edgeSet();
        HashSet<EntityNode> nodes = new HashSet<EntityNode>();
        for (EventEdge edge : set) {
            nodes.add(edge.getSink());
            nodes.add(edge.getSource());
        }
    }

    public int getNumOfEdgesInOriginalGraph() {
        return this.jGraph.edgeSet().size();
    }

    public int getNumOfEdges() {
        if (this.afterCPR == null) {
            System.out.println("You need Run getCPR firstly.");
            return -1;
        }
        return this.afterCPR.edgeSet().size();
    }

    public void filterByFile(String[] str) {
        DirectedPseudograph<EntityNode, EventEdge> graphAfterFilter = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        HashMap map2 = new HashMap();
        for (EventEdge e : this.afterCPR.edgeSet()) {
            Map sub;
            EntityNode source = e.getSource();
            EntityNode sink = e.getSink();
            boolean flag1 = false;
            boolean flag2 = false;
            for (String s2 : str) {
                if (source.getSignature().startsWith(s2)) {
                    flag1 = true;
                }
                if (!sink.getSignature().startsWith(s2)) continue;
                flag2 = true;
            }
            if (flag1 || flag2) continue;
            if (!map2.containsKey(source)) {
                sub = new HashMap();
                map2.put(source, sub);
            }
            if (!(sub = (Map)map2.get(source)).containsKey(sink)) {
                ArrayList edgeList = new ArrayList();
                sub.put(sink, edgeList);
            }
            ((List)sub.get(sink)).add(e);
        }
        for (EntityNode source : map2.keySet()) {
            Map sub = (Map)map2.get(source);
            for (EntityNode sink : sub.keySet()) {
                graphAfterFilter.addVertex(source);
                graphAfterFilter.addVertex(sink);
                List edges = (List)sub.get(sink);
                for (EventEdge e : edges) {
                    graphAfterFilter.addEdge(source, sink, e);
                }
            }
        }
        try {
            this.outputGraph("filterResult", graphAfterFilter);
        } catch (IOException e) {
            System.out.println("The filter output is not normal");
        }
    }

    public void splitNodeOfGraph() {
        if (this.afterCPR == null) {
            System.out.println("Run CPR first");
            return;
        }
        if (this.input == null) {
            System.out.println("Lack the parameter to find start for split");
            return;
        }
        System.out.println("Before split: " + this.afterCPR.vertexSet().size());
        EntityNode start = this.getNode(this.afterCPR);
        Map<EntityNode, Map<EntityNode, TreeMap<BigDecimal, EventEdge>>> dictgraph = this.getDictGraph(this.afterCPR);
        long curMaxEdgeID = this.getMaxEdgeID(this.afterCPR);
        long curMaxVertexID = this.getMaxVertexID(this.afterCPR);
        DirectedPseudograph aftersplit = new DirectedPseudograph(EventEdge.class);
        LinkedList<EntityNode> queue = new LinkedList<EntityNode>();
        queue.offer(start);
        HashMap origianlToSplit = new HashMap();
        while (!queue.isEmpty()) {
            for (int size2 = queue.size(); size2 > 0; --size2) {
                EntityNode cur = (EntityNode)queue.poll();
                Set set = this.afterCPR.incomingEdgesOf(cur);
            }
        }
    }

    public Map<EntityNode, Map<EntityNode, TreeMap<BigDecimal, EventEdge>>> getDictGraph(DirectedPseudograph<EntityNode, EventEdge> graph) {
        Set vertexSet = graph.vertexSet();
        HashMap<EntityNode, Map<EntityNode, TreeMap<BigDecimal, EventEdge>>> dict = new HashMap<EntityNode, Map<EntityNode, TreeMap<BigDecimal, EventEdge>>>();
        for (EntityNode v : vertexSet) {
            HashMap map2 = new HashMap();
            dict.put(v, map2);
        }
        for (EntityNode v : vertexSet) {
            Set inEdges = graph.incomingEdgesOf(v);
            for (EventEdge e : inEdges) {
                EntityNode source = e.getSource();
                Map sortedEdges = (Map)dict.get(v);
                if (!sortedEdges.containsKey(source)) {
                    sortedEdges.put(source, new TreeMap());
                }
                ((TreeMap)sortedEdges.get(source)).put(e.getEndTime(), e);
            }
        }
        return dict;
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

    private Set<EntityNode> getSourceNodes(Set<EventEdge> incomingEdgesSet) {
        HashSet<EntityNode> sourceNodes = new HashSet<EntityNode>();
        for (EventEdge e : incomingEdgesSet) {
            sourceNodes.add(e.getSource());
        }
        return sourceNodes;
    }

    private Set<EventEdge> getEdgesBetweenTwoVertex(EntityNode cur, EntityNode s2, DirectedPseudograph<EntityNode, EventEdge> afterCPR) {
        Set outEdges = afterCPR.outgoingEdgesOf(s2);
        HashSet<EventEdge> res = new HashSet<EventEdge>();
        for (EventEdge e : outEdges) {
            if (!e.getSink().equals(cur)) continue;
            res.add(e);
        }
        return res;
    }

    private boolean needSplit(EntityNode v) {
        if (this.afterCPR == null || v == null) {
            throw new IllegalArgumentException("empty parameter of needSplit");
        }
        Set outgoingEdges = this.afterCPR.outgoingEdgesOf(v);
        if (outgoingEdges == null || outgoingEdges.size() == 0) {
            return false;
        }
        HashMap<EntityNode, Integer> counts = new HashMap<EntityNode, Integer>();
        for (EventEdge e : outgoingEdges) {
            counts.put(e.getSink(), counts.getOrDefault(e.getSink(), 0) + 1);
            if ((Integer)counts.get(e.getSink()) <= 1) continue;
            return true;
        }
        return false;
    }

    private EntityNode getNode(DirectedPseudograph<EntityNode, EventEdge> afterCPR) {
        Set vertexSet = afterCPR.vertexSet();
        EntityNode start = null;
        for (EntityNode node : vertexSet) {
            if (!node.getSignature().equals(this.input)) continue;
            start = node;
            break;
        }
        if (start != null) {
            return start;
        }
        System.out.println("Can't find the node in the graph after CPR");
        return null;
    }

    public EntityNode getPOIEvent() {
        EntityNode node;
        if (this.afterCPR == null) {
            System.out.println("Please run CPR firstly");
        }
        this.POIEvent = node = this.getNode(this.afterCPR);
        return node;
    }

    public void updatePOItime() {
        if (this.afterCPR == null) {
            System.out.println("Please run CPR firstly");
        }
        if (this.POIEvent == null) {
            this.getPOIEvent();
        }
        Set poiIncoming = this.afterCPR.incomingEdgesOf(this.POIEvent);
        BigDecimal t = BigDecimal.ZERO;
        for (EventEdge e : poiIncoming) {
            if (e.getEndTime().compareTo(t) <= 0) continue;
            t = e.getEndTime();
        }
        this.POItime = t;
    }

    private long getMaxEdgeID() {
        if (this.afterCPR == null) {
            throw new IllegalArgumentException("afterCPR is null");
        }
        Set edgeSet = this.afterCPR.edgeSet();
        long max2 = Long.MIN_VALUE;
        for (EventEdge e : edgeSet) {
            max2 = Math.max(max2, e.getID());
        }
        return max2;
    }

    public void testSplit() {
        if (this.backTrack == null) {
            System.out.println(" To test split please Run BarckTrack first");
            return;
        }
        if (this.input == null) {
            System.out.println("Lack the parameter to find start for split");
            return;
        }
        Map<Long, EntityNode> nodeMap = this.getNodeMap(this.backTrack);
        for (long l : nodeMap.keySet()) {
            System.out.println(l);
        }
        System.out.println("Before split: " + this.backTrack.vertexSet().size());
        EntityNode start = this.getNode(this.backTrack);
        HashSet setOfVertex = new HashSet(this.backTrack.vertexSet());
        HashMap<Long, EntityNode> enmap = new HashMap<Long, EntityNode>();
        long maxVertexID = this.getMaxVertexID(this.backTrack);
        long maxEdgeID = this.getMaxEdgeID(this.backTrack);
        for (EntityNode v : setOfVertex) {
            enmap.put(v.getID(), v);
            Set outgoingEdges = this.backTrack.outgoingEdgesOf(v);
            Set incomingEdges = this.backTrack.incomingEdgesOf(v);
            if (!this.needSplitForTest(v)) continue;
            for (EventEdge outEdge : outgoingEdges) {
                EntityNode splitedVertex = new EntityNode(v, maxVertexID++);
                nodeMap.put(splitedVertex.getID(), splitedVertex);
                this.backTrack.addVertex(splitedVertex);
                EventEdge newOutedge = new EventEdge(outEdge, ++maxEdgeID);
                Set nodeSets = this.backTrack.vertexSet();
                if (!nodeSets.contains(splitedVertex)) {
                    System.out.println("nodeSets doesn't contain splitedVertex");
                }
                if (!nodeSets.contains(outEdge.getSink())) {
                    System.out.println();
                    System.out.println(outEdge.getSink().getID());
                    System.out.println(newOutedge.getSink().getSignature());
                    System.out.println("nodeSet doesn't contain edge sink");
                }
                this.backTrack.addEdge(splitedVertex, nodeMap.get(outEdge.getSink().getID()), newOutedge);
                for (EventEdge inEdge : incomingEdges) {
                    if (inEdge.getStartTime().compareTo(newOutedge.getEndTime()) < 1) continue;
                    EventEdge newInEdge = new EventEdge(inEdge, ++maxEdgeID);
                    this.backTrack.addEdge(inEdge.getSource(), splitedVertex, newInEdge);
                }
            }
            this.backTrack.removeVertex(v);
        }
        System.out.println("After split: " + this.backTrack.vertexSet().size());
    }

    private boolean needSplitForTest(EntityNode v) {
        if (this.backTrack == null || v == null) {
            throw new IllegalArgumentException("empty parameter of needSplit");
        }
        Set outgoingEdges = this.backTrack.outgoingEdgesOf(v);
        if (outgoingEdges == null || outgoingEdges.size() == 0) {
            return false;
        }
        HashMap<EntityNode, Integer> counts = new HashMap<EntityNode, Integer>();
        for (EventEdge e : outgoingEdges) {
            counts.put(e.getSink(), counts.getOrDefault(e.getSink(), 0) + 1);
            if ((Integer)counts.get(e.getSink()) <= 1) continue;
            return true;
        }
        return false;
    }

    public void inferReputation() {
        Map map2;
        HashMap weights = new HashMap();
        Set vertexSet = this.afterCPR.vertexSet();
        if (this.POItime == null) {
            this.updatePOItime();
        }
        for (EntityNode v : vertexSet) {
            map2 = new HashMap();
            weights.put(v.getID(), map2);
            for (EntityNode v2 : vertexSet) {
                ((HashMap)weights.get(v.getID())).put(v2.getID(), 0.0);
            }
        }
        for (EntityNode v : vertexSet) {
            EntityNode from;
            Set edges = this.afterCPR.incomingEdgesOf(v);
            double[] timeweights = new double[edges.size()];
            long[] amountweights = new long[edges.size()];
            double timetotal = 0.0;
            long amounttotal = 0L;
            for (EventEdge edge : edges) {
                timetotal += this.timeWeight(edge);
                amounttotal += this.amountWeight(edge);
            }
            double wtotal = 0.0;
            for (EventEdge edge : edges) {
                from = edge.getSource();
                wtotal += this.timeWeight(edge) / timetotal * (double)(this.amountWeight(edge) / amounttotal);
            }
            for (EventEdge edge : edges) {
                from = edge.getSource();
                double w = this.timeWeight(edge) / timetotal * (double)(this.amountWeight(edge) / amounttotal) / wtotal;
                ((HashMap)weights.get(v.getID())).put(from.getID(), w);
            }
        }
        for (Long a : weights.keySet()) {
            map2 = (Map)weights.get(a);
            for (Long b : map2.keySet()) {
                if ((Double)map2.get(b) == 0.0) continue;
                System.out.println(b);
            }
        }
    }

    private double timeWeight(EventEdge edge) {
        if (edge.getEndTime().equals(this.POItime)) {
            return 1.0;
        }
        BigDecimal diff = this.POItime.subtract(edge.getEndTime());
        return 1.0 / diff.doubleValue();
    }

    private long amountWeight(EventEdge edge) {
        return edge.getSize();
    }

    public int getOrigianlGraphVertexNumber() {
        return this.jGraph.vertexSet().size();
    }

    public int getOrigianlGraphEdgeNumber() {
        return this.jGraph.edgeSet().size();
    }

    public static void main(String[] args) {
        String[] localIP = new String[]{"192.168.122.1"};
        ProcessGraph test2 = new ProcessGraph("DataForSplit.txt", localIP);
        test2.backTrack("/home/fang/thesis2/code_about_data/test_output.txt", "File");
        test2.backTrackWithHopCount("/home/fang/thesis2/code_about_data/test_output.txt", "File", 2);
        test2.getDictGraph(test2.backTrack);
        IterateGraph iterateGraph = new IterateGraph(test2.backTrack);
    }
}

