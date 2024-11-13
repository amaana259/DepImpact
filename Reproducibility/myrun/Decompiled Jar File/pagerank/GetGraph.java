/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.DirectedPseudograph;
import pagerank.EntityIdProvider;
import pagerank.EntityNameProvider;
import pagerank.EntityNode;
import pagerank.EventEdge;
import pagerank.EventEdgeProvider;
import pagerank.FtoPEvent;
import pagerank.IterateGraph;
import pagerank.NtoPEvent;
import pagerank.ProcessTheOriginalParserOutput;
import pagerank.PtoFEvent;
import pagerank.PtoNEvent;
import pagerank.PtoPEvent;

public class GetGraph {
    public DirectedPseudograph<EntityNode, EventEdge> jg;
    private String filePath;
    private String[] localIP;
    private HashMap<Long, EntityNode> entityNodeMap;
    private ProcessTheOriginalParserOutput sysdigProcess;
    private DOTExporter<EntityNode, EventEdge> exporter;
    public EntityNode POIEvent;
    private IterateGraph iter;
    private boolean hostOrNot;

    public GetGraph(String path, String[] localIP) {
        this.filePath = path;
        this.localIP = new String[localIP.length];
        for (int i = 0; i < localIP.length; ++i) {
            this.localIP[i] = localIP[i];
        }
        this.POIEvent = null;
        this.hostOrNot = false;
        this.jg = new DirectedPseudograph(EventEdge.class);
        this.entityNodeMap = new HashMap();
        this.sysdigProcess = new ProcessTheOriginalParserOutput(path, localIP);
        this.exporter = new DOTExporter<EntityNode, EventEdge>(new EntityIdProvider(), new EntityNameProvider(), new EventEdgeProvider());
    }

    public GetGraph(String path, String[] localIP, boolean host) {
        this.filePath = path;
        this.localIP = new String[localIP.length];
        for (int i = 0; i < localIP.length; ++i) {
            this.localIP[i] = localIP[i];
        }
        this.POIEvent = null;
        this.hostOrNot = host;
        this.jg = new DirectedPseudograph(EventEdge.class);
        this.entityNodeMap = new HashMap();
        this.sysdigProcess = new ProcessTheOriginalParserOutput(path, localIP, host);
        this.exporter = new DOTExporter<EntityNode, EventEdge>(new EntityIdProvider(), new EntityNameProvider(), new EventEdgeProvider());
    }

    public void setHostOrNot(boolean i) {
        this.hostOrNot = i;
        this.sysdigProcess.setHostOrNot(this.hostOrNot);
    }

    public DirectedPseudograph<EntityNode, EventEdge> getJg() {
        if (this.jg == null) {
            this.GenerateGraph();
            this.sysdigProcess.getParser().afterBuilding();
        }
        return this.jg;
    }

    public DOTExporter<EntityNode, EventEdge> getExporter() {
        return this.exporter;
    }

    public DirectedPseudograph<EntityNode, EventEdge> getOriginalGraph() {
        if (this.jg == null) {
            this.GenerateGraph();
        }
        return this.jg;
    }

    public void GenerateGraph() {
        Map<String, NtoPEvent> networkProcessMap = this.sysdigProcess.getNetworkProcessMap();
        Map<String, PtoNEvent> processNetworkMap = this.sysdigProcess.getProcessNetworkMap();
        Map<String, PtoFEvent> processFileMap = this.sysdigProcess.getProcessFileMap();
        Map<String, FtoPEvent> fileProcessMap = this.sysdigProcess.getFileProcessMap();
        Map<String, PtoPEvent> processProcessMap = this.sysdigProcess.getProcessProcessMap();
        this.addFileToProcessEvent(fileProcessMap);
        this.addNetworkToProcessEvent(networkProcessMap);
        this.addProcessToFileEvent(processFileMap);
        this.addProcessToProcessEvent(processProcessMap);
        this.addProcessToNetworkEvent(processNetworkMap);
        this.assignEdgeId();
    }

    private void assignEdgeId() {
        long edgeID = 1L;
        for (EventEdge e : this.jg.edgeSet()) {
            e.id = edgeID++;
        }
    }

    public void exportGraph() throws Exception {
        DOTExporter<EntityNode, EventEdge> exporter = new DOTExporter<EntityNode, EventEdge>(new EntityIdProvider(), new EntityNameProvider(), new EventEdgeProvider());
        this.GenerateGraph();
        exporter.exportGraph(this.jg, new FileWriter("dot_output.dot"));
    }

    private void addProcessToFileEvent(Map<String, PtoFEvent> pfmap) {
        Set<String> keys2 = pfmap.keySet();
        for (String key : keys2) {
            EntityNode source = null;
            EntityNode sink = null;
            if (this.entityNodeMap.containsKey(pfmap.get(key).getSource().getUniqID())) {
                source = this.entityNodeMap.get(pfmap.get(key).getSource().getUniqID());
            } else {
                source = new EntityNode(pfmap.get(key).getSource());
                this.entityNodeMap.put(source.getID(), source);
            }
            if (this.entityNodeMap.containsKey(pfmap.get(key).getSink().getUniqID())) {
                sink = this.entityNodeMap.get(pfmap.get(key).getSink().getUniqID());
            } else {
                sink = new EntityNode(pfmap.get(key).getSink());
                this.entityNodeMap.put(sink.getID(), sink);
            }
            this.jg.addVertex(source);
            this.jg.addVertex(sink);
            EventEdge edge = new EventEdge(pfmap.get(key));
            this.jg.addEdge(source, sink, edge);
        }
    }

    private void addFileToProcessEvent(Map<String, FtoPEvent> fpmap) {
        Set<String> keys2 = fpmap.keySet();
        for (String key : keys2) {
            EntityNode source = null;
            EntityNode sink = null;
            if (this.entityNodeMap.containsKey(fpmap.get(key).getSource().getUniqID())) {
                source = this.entityNodeMap.get(fpmap.get(key).getSource().getUniqID());
            } else {
                source = new EntityNode(fpmap.get(key).getSource());
                this.entityNodeMap.put(source.getID(), source);
            }
            if (this.entityNodeMap.containsKey(fpmap.get(key).getSink().getUniqID())) {
                sink = this.entityNodeMap.get(fpmap.get(key).getSink().getUniqID());
            } else {
                sink = new EntityNode(fpmap.get(key).getSink());
                this.entityNodeMap.put(sink.getID(), sink);
            }
            this.jg.addVertex(source);
            this.jg.addVertex(sink);
            EventEdge edge = new EventEdge(fpmap.get(key));
            this.jg.addEdge(source, sink, edge);
        }
    }

    private void addProcessToProcessEvent(Map<String, PtoPEvent> ppmap) {
        Set<String> keys2 = ppmap.keySet();
        for (String key : keys2) {
            EntityNode source = null;
            EntityNode sink = null;
            if (this.entityNodeMap.containsKey(ppmap.get(key).getSource().getUniqID())) {
                source = this.entityNodeMap.get(ppmap.get(key).getSource().getUniqID());
            } else {
                source = new EntityNode(ppmap.get(key).getSource());
                this.entityNodeMap.put(source.getID(), source);
            }
            if (this.entityNodeMap.containsKey(ppmap.get(key).getSink().getUniqID())) {
                sink = this.entityNodeMap.get(ppmap.get(key).getSink().getUniqID());
            } else {
                sink = new EntityNode(ppmap.get(key).getSink());
                this.entityNodeMap.put(sink.getID(), sink);
            }
            this.jg.addVertex(source);
            this.jg.addVertex(sink);
            EventEdge edge = new EventEdge(ppmap.get(key));
            this.jg.addEdge(source, sink, edge);
        }
    }

    private void addNetworkToProcessEvent(Map<String, NtoPEvent> npmap) {
        Set<String> keys2 = npmap.keySet();
        for (String key : keys2) {
            EntityNode source = null;
            EntityNode sink = null;
            if (this.entityNodeMap.containsKey(npmap.get(key).getSource().getUniqID())) {
                source = this.entityNodeMap.get(npmap.get(key).getSource().getUniqID());
            } else {
                source = new EntityNode(npmap.get(key).getSource());
                this.entityNodeMap.put(source.getID(), source);
            }
            if (this.entityNodeMap.containsKey(npmap.get(key).getSink().getUniqID())) {
                sink = this.entityNodeMap.get(npmap.get(key).getSink().getUniqID());
            } else {
                sink = new EntityNode(npmap.get(key).getSink());
                this.entityNodeMap.put(sink.getID(), sink);
            }
            this.jg.addVertex(source);
            this.jg.addVertex(sink);
            EventEdge edge = new EventEdge(npmap.get(key));
            this.jg.addEdge(source, sink, edge);
        }
    }

    private void addProcessToNetworkEvent(Map<String, PtoNEvent> map2) {
        Set<String> keys2 = map2.keySet();
        for (String key : keys2) {
            EntityNode source = null;
            EntityNode sink = null;
            if (this.entityNodeMap.containsKey(map2.get(key).getSource().getUniqID())) {
                source = this.entityNodeMap.get(map2.get(key).getSource().getUniqID());
            } else {
                source = new EntityNode(map2.get(key).getSource());
                this.entityNodeMap.put(source.getID(), source);
            }
            if (this.entityNodeMap.containsKey(map2.get(key).getSink().getUniqID())) {
                sink = this.entityNodeMap.get(map2.get(key).getSink().getUniqID());
            } else {
                sink = new EntityNode(map2.get(key).getSink());
                this.entityNodeMap.put(sink.getID(), sink);
            }
            this.jg.addVertex(source);
            this.jg.addVertex(sink);
            EventEdge edge = new EventEdge(map2.get(key));
            this.jg.addEdge(source, sink, edge);
        }
    }

    public void bfs(String input, String type) throws IOException {
        EntityNode start = this.getGraphVertex(input, type);
        LinkedList<EntityNode> queue = new LinkedList<EntityNode>();
        if (start != null) {
            this.bfs(start, queue, input);
        } else {
            System.out.println("Your input doesn't exist in the graph");
        }
    }

    private void bfs(EntityNode start, Queue<EntityNode> queue, String input) throws IOException {
        DirectedPseudograph<EntityNode, EventEdge> subgraph = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        DirectedPseudograph<EntityNode, EventEdge> graph = this.jg;
        HashSet<EntityNode> set = new HashSet<EntityNode>();
        HashSet<EventEdge> edgeSet = new HashSet<EventEdge>();
        queue.offer(start);
        while (!queue.isEmpty()) {
            EntityNode cur = queue.poll();
            Set outgoing = this.jg.outgoingEdgesOf(cur);
            Set incoming = this.jg.incomingEdgesOf(cur);
            EntityNode subcur = new EntityNode(cur);
            subgraph.addVertex(subcur);
            for (EventEdge o : outgoing) {
                if (set.add((EntityNode)this.jg.getEdgeTarget(o))) {
                    queue.offer((EntityNode)graph.getEdgeTarget(o));
                }
                EntityNode target = (EntityNode)graph.getEdgeTarget(o);
                if (!edgeSet.add(o)) continue;
                subgraph.addVertex(target);
                subgraph.addEdge(subcur, target, o);
            }
            for (EventEdge i : incoming) {
                if (set.add((EntityNode)this.jg.getEdgeSource(i))) {
                    queue.offer((EntityNode)graph.getEdgeSource(i));
                }
                EntityNode newSource = (EntityNode)graph.getEdgeSource(i);
                if (!edgeSet.add(i)) continue;
                subgraph.addVertex(newSource);
                subgraph.addEdge(newSource, subcur, i);
            }
        }
        DOTExporter<EntityNode, EventEdge> exporter = new DOTExporter<EntityNode, EventEdge>(new EntityIdProvider(), new EntityNameProvider(), new EventEdgeProvider());
        exporter.exportGraph(subgraph, new FileWriter(String.format("%s_dot_output.dot", input)));
    }

    public void bfsWithHopCount(String input, String type, int hopCount) throws IOException {
        EntityNode start = this.getGraphVertex(input, type);
        LinkedList<EntityNode> queue = new LinkedList<EntityNode>();
        if (start != null) {
            this.bfs(start, queue, input, hopCount);
        } else {
            System.out.println("Your input doesn't exist in the graph");
        }
    }

    public DirectedPseudograph<EntityNode, EventEdge> backTrackPoi(String input, String type) {
        EntityNode start = this.getGraphVertex(input, type);
        return this.backTrackPoi(start);
    }

    public DirectedPseudograph<EntityNode, EventEdge> backTrackPoi(String input) {
        EntityNode start = this.getGraphVertex(input);
        return this.backTrackPoi(start);
    }

    private DirectedPseudograph<EntityNode, EventEdge> backTrackPoi(EntityNode node) {
        EntityNode start;
        this.POIEvent = start = node;
        DirectedPseudograph<EntityNode, EventEdge> subgraph = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        if (start == null) {
            System.out.println("Can't find the input node in the graph");
        }
        LinkedList<EventEdge> queue = new LinkedList<EventEdge>();
        HashMap<EntityNode, EventEdge> map2 = new HashMap<EntityNode, EventEdge>();
        this.getPoiEvents(start, map2);
        for (EventEdge e : map2.values()) {
            queue.offer(e);
        }
        HashSet<EntityNode> vertexSet = new HashSet<EntityNode>();
        HashSet<EventEdge> edgeSet = new HashSet<EventEdge>();
        HashSet<EventEdge> removeDuplicate = new HashSet<EventEdge>();
        int level = 0;
        while (!queue.isEmpty()) {
            LinkedList<EventEdge> nextStep = new LinkedList<EventEdge>();
            while (!queue.isEmpty()) {
                EventEdge edge = (EventEdge)queue.poll();
                EntityNode target = (EntityNode)this.jg.getEdgeTarget(edge);
                EntityNode source = (EntityNode)this.jg.getEdgeSource(edge);
                Set incoming = this.jg.incomingEdgesOf(source);
                EventEdge poi = (EventEdge)map2.get(source);
                for (EventEdge e : incoming) {
                    if (e.getEndTime().compareTo(edge.getEndTime()) >= 0 || !removeDuplicate.add(e)) continue;
                    nextStep.offer(e);
                }
                if (vertexSet.add(source)) {
                    subgraph.addVertex(source);
                }
                if (vertexSet.add(target)) {
                    subgraph.addVertex(target);
                }
                if (!edgeSet.add(edge)) continue;
                subgraph.addEdge(source, target, edge);
            }
            queue = nextStep;
            System.out.println("BackTrackPoi test level step:" + String.valueOf(++level));
        }
        try {
            this.exporter.exportGraph(subgraph, new FileWriter("backTrackpoi.dot"));
        } catch (IOException e) {
            System.out.println("IO exception");
        }
        return subgraph;
    }

    public void backTrackWithHopCount(String input, String type, int hopcount) {
        EntityNode start = this.getGraphVertex(input, type);
        DirectedPseudograph<EntityNode, EventEdge> subgraph = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        if (start == null) {
            System.out.println("The input doesn't exist in the graph");
        }
        LinkedList<EventEdge> queue = new LinkedList<EventEdge>();
        HashMap<EntityNode, EventEdge> map2 = new HashMap<EntityNode, EventEdge>();
        this.getPoiEvents(start, map2);
        for (EventEdge e : map2.values()) {
            queue.offer(e);
        }
        int level = 1;
        while (!queue.isEmpty() && hopcount >= 1) {
            LinkedList<EventEdge> nextStep = new LinkedList<EventEdge>();
            HashSet<EntityNode> vertexSet = new HashSet<EntityNode>();
            HashSet<EventEdge> edgeSet = new HashSet<EventEdge>();
            HashSet<EventEdge> removeDuplicate = new HashSet<EventEdge>();
            while (!queue.isEmpty()) {
                EventEdge edge = (EventEdge)queue.poll();
                EntityNode target = (EntityNode)this.jg.getEdgeTarget(edge);
                EntityNode source = (EntityNode)this.jg.getEdgeSource(edge);
                Set incoming = this.jg.incomingEdgesOf(source);
                EventEdge poi = (EventEdge)map2.get(source);
                for (EventEdge e : incoming) {
                    if (e.getStartTime().compareTo(edge.getEndTime()) >= 0 || !removeDuplicate.add(e)) continue;
                    nextStep.offer(e);
                }
                if (vertexSet.add(source)) {
                    subgraph.addVertex(source);
                }
                if (vertexSet.add(target)) {
                    subgraph.addVertex(target);
                }
                if (!edgeSet.add(edge)) continue;
                subgraph.addEdge(source, target, edge);
            }
            queue = nextStep;
            --hopcount;
            try {
                this.exporter.exportGraph(subgraph, new FileWriter(String.format("backTrack_%s_%d_ouput.dot", "test", level)));
            } catch (IOException e) {
                System.out.println("IO Exception");
            }
            ++level;
        }
    }

    private void getPoiEvents(EntityNode node, Map<EntityNode, EventEdge> map2) {
        HashSet<EventEdge> incoming = new HashSet<EventEdge>(this.jg.incomingEdgesOf(node));
        HashSet<EntityNode> vertexSet = new HashSet<EntityNode>();
        for (EventEdge i : incoming) {
            List<EventEdge> sortedEdges;
            EntityNode source = (EntityNode)this.jg.getEdgeSource(i);
            if (!vertexSet.add(source)) continue;
            Set<EventEdge> outgoing = this.jg.outgoingEdgesOf(source);
            List<EventEdge> edgesBetweenSourceAndTarget = this.getEdgesBetweenSourceAndTarget(outgoing, incoming);
            if (edgesBetweenSourceAndTarget.size() == 0) {
                System.out.println("Edges Between Source and Target is not correct");
            }
            if ((sortedEdges = this.sortAccordingStartTime(edgesBetweenSourceAndTarget)).size() == 0) {
                System.out.println("sorted edges is not correct");
            }
            map2.put(source, sortedEdges.get(sortedEdges.size() - 1));
        }
    }

    private List<EventEdge> getEdgesBetweenSourceAndTarget(Set<EventEdge> sourceOutgoing, Set<EventEdge> incoming) {
        ArrayList<EventEdge> res = new ArrayList<EventEdge>();
        for (EventEdge edge : sourceOutgoing) {
            if (!incoming.contains(edge)) continue;
            res.add(edge);
        }
        return res;
    }

    private void bfs(EntityNode start, Queue<EntityNode> queue, String input, int hopCount) throws IOException {
        DirectedPseudograph<EntityNode, EventEdge> subgraph = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        DirectedPseudograph<EntityNode, EventEdge> graph = this.jg;
        HashSet<EntityNode> set = new HashSet<EntityNode>();
        HashSet<EventEdge> edgeSet = new HashSet<EventEdge>();
        DOTExporter<EntityNode, EventEdge> exporter = new DOTExporter<EntityNode, EventEdge>(new EntityIdProvider(), new EntityNameProvider(), new EventEdgeProvider());
        queue.offer(start);
        int level = 0;
        while (!queue.isEmpty() && hopCount >= 1) {
            ++level;
            LinkedList<EntityNode> nextStep = new LinkedList<EntityNode>();
            while (!queue.isEmpty()) {
                EntityNode cur = queue.poll();
                Set outgoing = this.jg.outgoingEdgesOf(cur);
                Set incoming = this.jg.incomingEdgesOf(cur);
                EntityNode subcur = new EntityNode(cur);
                subgraph.addVertex(subcur);
                for (EventEdge o : outgoing) {
                    if (set.add((EntityNode)this.jg.getEdgeTarget(o))) {
                        nextStep.offer((EntityNode)graph.getEdgeTarget(o));
                    }
                    EntityNode target = (EntityNode)graph.getEdgeTarget(o);
                    if (!edgeSet.add(o)) continue;
                    subgraph.addVertex(target);
                    subgraph.addEdge(subcur, target, o);
                }
                for (EventEdge i : incoming) {
                    if (set.add((EntityNode)this.jg.getEdgeSource(i))) {
                        nextStep.offer((EntityNode)graph.getEdgeSource(i));
                    }
                    EntityNode source = (EntityNode)graph.getEdgeSource(i);
                    if (!edgeSet.add(i)) continue;
                    subgraph.addVertex(source);
                    subgraph.addEdge(source, subcur, i);
                }
            }
            exporter.exportGraph(subgraph, new FileWriter(String.format("%s_%d_dot_output.dot", "bfs", level)));
            queue = nextStep;
            --hopCount;
        }
    }

    private EntityNode getGraphVertex(String input, String type) {
        EntityNode start = null;
        LinkedList queue = new LinkedList();
        if (type.equals("Network")) {
            for (Long key : this.entityNodeMap.keySet()) {
                EntityNode e = this.entityNodeMap.get(key);
                if (e.getN() == null || !e.getN().getSrcAddress().equals(input)) continue;
                start = e;
            }
        } else if (type.equals("File")) {
            for (Long key : this.entityNodeMap.keySet()) {
                EntityNode e = this.entityNodeMap.get(key);
                if (e.getF() == null || !e.getF().getPath().equals(input)) continue;
                start = e;
            }
        } else {
            for (Long key : this.entityNodeMap.keySet()) {
                EntityNode e = this.entityNodeMap.get(key);
                if (e.getP() == null || !e.getP().getName().equals(input)) continue;
                start = e;
                break;
            }
        }
        if (start != null) {
            return start;
        }
        System.out.println("Your input doesn't exist in the graph");
        return null;
    }

    private EntityNode getGraphVertex(String input) {
        for (EntityNode e : this.entityNodeMap.values()) {
            if (!e.getSignature().equals(input)) continue;
            return e;
        }
        return null;
    }

    private List<EventEdge> sortAccordingStartTime(Set<EventEdge> set) {
        Comparator<EventEdge> cmp = new Comparator<EventEdge>(){

            @Override
            public int compare(EventEdge a, EventEdge b) {
                return a.getStartTime().compareTo(b.getStartTime());
            }
        };
        LinkedList<EventEdge> res = new LinkedList<EventEdge>(set);
        res.sort(cmp);
        return res;
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
        Set incoming = this.jg.incomingEdgesOf(u);
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
        Set outgoing = this.jg.outgoingEdgesOf(u);
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

    private void testGetVertex() {
        System.out.println("All process in graph");
        for (Long key : this.entityNodeMap.keySet()) {
            if (this.entityNodeMap.get(key).getP() == null) continue;
            System.out.println(this.entityNodeMap.get(key).getP().getName());
        }
    }

    public EntityNode getPOIEvent() {
        return this.POIEvent;
    }

    public void exportGraph(String file) {
        if (this.jg == null) {
            this.GenerateGraph();
        }
        this.iter = new IterateGraph(this.jg);
        this.iter.exportGraph(file);
    }

    public static void main(String[] args) throws Exception {
        String[] localIP = new String[]{"10.0.2.15"};
        GetGraph test2 = new GetGraph("/home/fang/thesis2/Data/Expdata2/aptgetInstallUnrar.txt", localIP);
        test2.GenerateGraph();
        IterateGraph iterateGraph = new IterateGraph(test2.jg);
        iterateGraph.bfs("/usr/bin/unrar-nonfree.dpkg-new");
    }
}

