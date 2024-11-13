/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DirectedPseudograph;
import pagerank.EntityNode;
import pagerank.EventEdge;
import pagerank.IterateGraph;

public class BackTrack {
    DirectedPseudograph<EntityNode, EventEdge> originalGraph;
    IterateGraph iterateGraph;
    DirectedPseudograph<EntityNode, EventEdge> afterBackTrack;
    private Map<String, Integer> stepInfo;
    private CycleDetector<EntityNode, EventEdge> cycleDetector;

    BackTrack(DirectedPseudograph<EntityNode, EventEdge> input) {
        this.originalGraph = (DirectedPseudograph)input.clone();
        this.iterateGraph = new IterateGraph(this.originalGraph);
        this.stepInfo = new HashMap<String, Integer>();
        this.cycleDetector = new CycleDetector<EntityNode, EventEdge>(this.originalGraph);
    }

    public DirectedPseudograph<EntityNode, EventEdge> backTrackPOIEvent(String str) {
        System.out.println("backTrackPOIEvent invoked: " + str);
        DirectedPseudograph<EntityNode, EventEdge> backTrack = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        EntityNode start = this.iterateGraph.getGraphVertex(str);
        BigDecimal latestOPTime = this.iterateGraph.getLatestOperationTime(start);
        HashMap<EntityNode, BigDecimal> timeThresolds = new HashMap<EntityNode, BigDecimal>();
        timeThresolds.put(start, latestOPTime);
        HashSet<EntityNode> nodeInTheQueue = new HashSet<EntityNode>();
        LinkedList<EntityNode> queue = new LinkedList<EntityNode>();
        nodeInTheQueue.add(start);
        queue.offer(start);
        while (!queue.isEmpty()) {
            EntityNode cur = (EntityNode)queue.poll();
            backTrack.addVertex(cur);
            Set incoming = this.originalGraph.incomingEdgesOf(cur);
            BigDecimal curThresold = (BigDecimal)timeThresolds.get(cur);
            for (EventEdge e : incoming) {
                BigDecimal thresoldForSource;
                if (e.getStartTime().compareTo(curThresold) > 0) continue;
                EntityNode source = e.getSource();
                backTrack.addVertex(source);
                timeThresolds.putIfAbsent(source, BigDecimal.ZERO);
                BigDecimal bigDecimal = thresoldForSource = e.endTime.compareTo(curThresold) < 0 ? e.endTime : curThresold;
                if (((BigDecimal)timeThresolds.get(source)).compareTo(thresoldForSource) < 0) {
                    timeThresolds.put(source, thresoldForSource);
                }
                backTrack.addEdge(source, cur, e);
                if (nodeInTheQueue.contains(source)) continue;
                nodeInTheQueue.add(source);
                queue.offer(source);
            }
        }
        this.afterBackTrack = backTrack;
        return backTrack;
    }

    public DirectedPseudograph<EntityNode, EventEdge> backTrackPOIEventWithStep(String str) {
        System.out.println("backTrackPOIEvent invoked: " + str);
        DirectedPseudograph<EntityNode, EventEdge> backTrack = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        EntityNode start = this.iterateGraph.getGraphVertex(str);
        BigDecimal latestOPTime = this.iterateGraph.getLatestOperationTime(start);
        HashMap<EntityNode, BigDecimal> timeThresolds = new HashMap<EntityNode, BigDecimal>();
        timeThresolds.put(start, latestOPTime);
        HashSet<EntityNode> nodeInTheQueue = new HashSet<EntityNode>();
        LinkedList<EntityNode> queue = new LinkedList<EntityNode>();
        nodeInTheQueue.add(start);
        queue.offer(start);
        int step = 1;
        this.stepInfo.put(start.getSignature(), step);
        while (!queue.isEmpty()) {
            EntityNode cur = (EntityNode)queue.poll();
            backTrack.addVertex(cur);
            Set incoming = this.originalGraph.incomingEdgesOf(cur);
            BigDecimal curThresold = (BigDecimal)timeThresolds.get(cur);
            for (EventEdge e : incoming) {
                BigDecimal thresoldForSource;
                if (e.getStartTime().compareTo(curThresold) > 0) continue;
                EntityNode source = e.getSource();
                backTrack.addVertex(source);
                if (!this.stepInfo.containsKey(source.getSignature())) {
                    this.stepInfo.put(source.getSignature(), this.stepInfo.get(cur.getSignature()) + 1);
                } else {
                    Set<EntityNode> cycleContainsSource = this.cycleDetector.findCyclesContainingVertex(source);
                    if (!cycleContainsSource.contains(cur)) {
                        step = Math.max(this.stepInfo.get(source.getSignature()), this.stepInfo.get(cur.getSignature()) + 1);
                        this.stepInfo.put(source.getSignature(), step);
                    }
                }
                timeThresolds.putIfAbsent(source, BigDecimal.ZERO);
                BigDecimal bigDecimal = thresoldForSource = e.endTime.compareTo(curThresold) < 0 ? e.endTime : curThresold;
                if (((BigDecimal)timeThresolds.get(source)).compareTo(thresoldForSource) < 0) {
                    timeThresolds.put(source, thresoldForSource);
                }
                backTrack.addEdge(source, cur, e);
                if (nodeInTheQueue.contains(source)) continue;
                nodeInTheQueue.add(source);
                queue.offer(source);
            }
        }
        this.afterBackTrack = backTrack;
        return backTrack;
    }

    public DirectedPseudograph<EntityNode, EventEdge> backTrackPOIEvent2(String str) {
        DirectedPseudograph<EntityNode, EventEdge> backTrack = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        EntityNode start = this.iterateGraph.getGraphVertex(str);
        BigDecimal latestOPTime = this.iterateGraph.getLatestOperationTime(start);
        HashMap<EntityNode, BigDecimal> timeThresold = new HashMap<EntityNode, BigDecimal>();
        timeThresold.put(start, latestOPTime);
        LinkedList edgeList = new LinkedList(this.originalGraph.edgeSet());
        Collections.sort(edgeList, (a, b) -> b.getStartTime().compareTo(a.getStartTime()));
        backTrack.addVertex(start);
        for (EventEdge e : edgeList) {
            EntityNode source = e.getSource();
            EntityNode target = e.getSink();
            if (!backTrack.containsVertex(target)) continue;
            BigDecimal targetThresold = (BigDecimal)timeThresold.get(target);
            if (e.getStartTime().compareTo(targetThresold) >= 0) continue;
            if (!backTrack.containsVertex(source)) {
                backTrack.addVertex(source);
                BigDecimal thresoldForObject = e.getEndTime().compareTo(targetThresold) < 0 ? e.getEndTime() : targetThresold;
                timeThresold.put(source, thresoldForObject);
            }
            backTrack.addEdge(source, target, e);
        }
        this.afterBackTrack = backTrack;
        return backTrack;
    }

    public void printGraph() {
        assert (this.afterBackTrack != null);
        IterateGraph iter = new IterateGraph(this.afterBackTrack);
        iter.exportGraph("backtrackTest");
    }

    public void exportGraph(String file) {
        assert (this.afterBackTrack != null);
        IterateGraph iter = new IterateGraph(this.afterBackTrack);
        iter.exportGraph(file);
    }

    public Map<String, Integer> getBackwardStepInfo() {
        return Collections.unmodifiableMap(this.stepInfo);
    }

    public DirectedPseudograph<EntityNode, EventEdge> mergeGraph(DirectedPseudograph<EntityNode, EventEdge> latest, DirectedPseudograph<EntityNode, EventEdge> previous) {
        Set<EntityNode> latestCandidates = this.getCandidateNodes(latest, true);
        Set<EntityNode> recentPrevious = this.getCandidateNodes(previous, false);
        DirectedPseudograph<EntityNode, EventEdge> res = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        Set<String> connectionPoints = this.findConnectionPoints(latestCandidates, recentPrevious);
        if (connectionPoints.size() == 0) {
            return latest;
        }
        for (String connection : connectionPoints) {
            String[] tokens = connection.split("->");
            HashMap<String, BigDecimal> timeThres = new HashMap<String, BigDecimal>();
            String connectionInLatest = String.format("%s->%s", tokens[1], tokens[0]);
            IterateGraph iter = new IterateGraph(latest);
            BigDecimal threshold1 = iter.getLatestOperationTimeByOutgoing(connectionInLatest).subtract(new BigDecimal("5"));
            timeThres.put(connection, threshold1);
            DirectedPseudograph<EntityNode, EventEdge> filterPrevious = this.backTrackPoiEventWithGivenThreshold(previous, connection, threshold1);
            res = this.combineTwoGraph(latest, filterPrevious, connection);
        }
        return res;
    }

    private DirectedPseudograph<EntityNode, EventEdge> combineTwoGraph(DirectedPseudograph<EntityNode, EventEdge> latest, DirectedPseudograph<EntityNode, EventEdge> previous, String connection) {
        String[] tokens = connection.split("->");
        String connectionInLatest = String.format("%s->%s", tokens[1], tokens[0]);
        IterateGraph iter = new IterateGraph(latest);
        Set edges = previous.edgeSet();
        long latestBigID = iter.getBiggestEdgeID();
        for (EventEdge e : edges) {
            EventEdge edge;
            EntityNode sourceAdded;
            EntityNode source = e.getSource();
            EntityNode sink = e.getSink();
            if (sink.getSignature().equals(connectionInLatest)) {
                sourceAdded = new EntityNode(source);
                latest.addVertex(sourceAdded);
                EntityNode target = iter.getGraphVertex(connectionInLatest);
                edge = new EventEdge(e.getType(), e.getStartTime(), e.getEndTime(), e.getSize(), sourceAdded, target, ++latestBigID);
                edge.setEdgeEvent(e.getEvent());
                latest.addEdge(sourceAdded, target, edge);
                continue;
            }
            sourceAdded = new EntityNode(source);
            EntityNode targetAdded = new EntityNode(sink);
            edge = new EventEdge(e.getType(), e.getStartTime(), e.getEndTime(), e.getSize(), sourceAdded, targetAdded, ++latestBigID);
            edge.setEdgeEvent(e.getEvent());
            latest.addEdge(sourceAdded, targetAdded, edge);
        }
        return latest;
    }

    public DirectedPseudograph<EntityNode, EventEdge> backTrackPoiEventWithGivenThreshold(DirectedPseudograph<EntityNode, EventEdge> input, String poi, BigDecimal thresh) {
        System.out.println("backTrackPOIEventWithGivenThreshold invoked: " + poi + " " + thresh.toString());
        DirectedPseudograph<EntityNode, EventEdge> backTrack = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        IterateGraph iter = new IterateGraph(input);
        EntityNode start = iter.getGraphVertex(poi);
        HashMap<EntityNode, BigDecimal> timeThresolds = new HashMap<EntityNode, BigDecimal>();
        timeThresolds.put(start, thresh);
        HashSet<EntityNode> nodeInTheQueue = new HashSet<EntityNode>();
        LinkedList<EntityNode> queue = new LinkedList<EntityNode>();
        nodeInTheQueue.add(start);
        queue.offer(start);
        while (!queue.isEmpty()) {
            EntityNode cur = (EntityNode)queue.poll();
            backTrack.addVertex(cur);
            Set incoming = input.incomingEdgesOf(cur);
            BigDecimal curThresold = (BigDecimal)timeThresolds.get(cur);
            for (EventEdge e : incoming) {
                BigDecimal thresoldForSource;
                if (e.getStartTime().compareTo(curThresold) > 0) continue;
                EntityNode source = e.getSource();
                backTrack.addVertex(source);
                timeThresolds.putIfAbsent(source, BigDecimal.ZERO);
                BigDecimal bigDecimal = thresoldForSource = e.endTime.compareTo(curThresold) < 0 ? e.endTime : curThresold;
                if (((BigDecimal)timeThresolds.get(source)).compareTo(thresoldForSource) < 0) {
                    timeThresolds.put(source, thresoldForSource);
                }
                backTrack.addEdge(source, cur, e);
                if (nodeInTheQueue.contains(source)) continue;
                nodeInTheQueue.add(source);
                queue.offer(source);
            }
        }
        return backTrack;
    }

    private Set<String> findConnectionPoints(Set<EntityNode> latest, Set<EntityNode> previous) {
        HashSet<String> res = new HashSet<String>();
        HashSet<String> targets = new HashSet<String>();
        for (EntityNode e : previous) {
            targets.add(e.getSignature());
        }
        for (EntityNode e : latest) {
            String sig1 = e.getSignature();
            String[] tokens = sig1.split("->");
            String target = String.format("%s->%s", tokens[1], tokens[0]);
            if (!targets.contains(target)) continue;
            res.add(target);
        }
        return res;
    }

    private Set<EntityNode> getCandidateNodes(DirectedPseudograph<EntityNode, EventEdge> graph, boolean in) {
        HashSet<EntityNode> res = new HashSet<EntityNode>();
        Set candidates = graph.vertexSet();
        for (EntityNode v : candidates) {
            if (in) {
                if (graph.inDegreeOf(v) != 0 || !v.isNetworkNode()) continue;
                res.add(v);
                continue;
            }
            if (graph.outDegreeOf(v) != 0 || !v.isNetworkNode()) continue;
            res.add(v);
        }
        return res;
    }
}

