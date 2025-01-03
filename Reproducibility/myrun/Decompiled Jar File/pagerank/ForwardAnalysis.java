/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.jgrapht.graph.DirectedPseudograph;
import pagerank.CausalityPreserve;
import pagerank.EntityNode;
import pagerank.EventEdge;
import pagerank.GetGraph;
import pagerank.IterateGraph;
import pagerank.MetaConfig;

public class ForwardAnalysis {
    DirectedPseudograph<EntityNode, EventEdge> input;

    ForwardAnalysis(DirectedPseudograph<EntityNode, EventEdge> graph) {
        this.input = graph;
    }

    private EntityNode findSource(String signature) {
        Set vertexSet = this.input.vertexSet();
        for (EntityNode e : vertexSet) {
            if (!e.getSignature().equals(signature)) continue;
            return e;
        }
        return null;
    }

    public DirectedPseudograph<EntityNode, EventEdge> fowardTrack(String signature, int stepLimit) {
        LinkedList<EntityNode> queue = new LinkedList<EntityNode>();
        EntityNode start = this.findSource(signature);
        queue.offer(start);
        assert (start != null);
        HashSet vertexInRes = new HashSet();
        DirectedPseudograph<EntityNode, EventEdge> res = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        int step = 0;
        LinkedList<EntityNode> nextQueue = new LinkedList<EntityNode>();
        while (!queue.isEmpty() && step < stepLimit) {
            EntityNode cur = (EntityNode)queue.poll();
            if (res.vertexSet().size() == 0) {
                this.addNodeToRes(cur, res);
                Set outgoing = this.input.outgoingEdgesOf(cur);
                for (EventEdge out : outgoing) {
                    EntityNode target = out.getSink();
                    if (this.addNodeToRes(target, res)) {
                        nextQueue.offer(target);
                    }
                    res.addEdge(cur, target, out);
                }
            } else {
                this.addNodeToRes(cur, res);
                BigDecimal earliestStart = this.getEarliestOPTime(cur, res);
                Set outgoing = this.input.outgoingEdgesOf(cur);
                for (EventEdge out : outgoing) {
                    if (out.startTime.compareTo(earliestStart) < 0) continue;
                    EntityNode target = out.getSink();
                    if (this.addNodeToRes(target, res)) {
                        nextQueue.offer(target);
                    }
                    res.addEdge(cur, target, out);
                }
            }
            if (queue.size() != 0 || nextQueue.size() == 0) continue;
            queue = nextQueue;
            nextQueue = new LinkedList();
            ++step;
        }
        return res;
    }

    public DirectedPseudograph<EntityNode, EventEdge> forwardLimitedByTime(String signature, BigDecimal POITime) {
        LinkedList<EntityNode> queue = new LinkedList<EntityNode>();
        EntityNode start = this.findSource(signature);
        queue.offer(start);
        assert (start != null);
        HashSet vertexInRes = new HashSet();
        DirectedPseudograph<EntityNode, EventEdge> res = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        LinkedList<EntityNode> nextQueue = new LinkedList<EntityNode>();
        while (!queue.isEmpty()) {
            while (!queue.isEmpty()) {
                Object out2;
                EntityNode cur = (EntityNode)queue.poll();
                if (res.vertexSet().size() == 0) {
                    this.addNodeToRes(cur, res);
                    Set outgoing = this.input.outgoingEdgesOf(cur);
                    for (Object out2 : outgoing) {
                        String outSig = ((EventEdge)out2).getSink().getSignature();
                        if (((EventEdge)out2).getStartTime().compareTo(POITime) >= 0) continue;
                        EntityNode target = ((EventEdge)out2).getSink();
                        if (this.addNodeToRes(target, res)) {
                            nextQueue.offer(target);
                        }
                        res.addEdge(cur, target, (EventEdge)out2);
                    }
                    continue;
                }
                this.addNodeToRes(cur, res);
                BigDecimal earliestStart = this.getEarliestOPTime(cur, res);
                Set outgoing = this.input.outgoingEdgesOf(cur);
                out2 = outgoing.iterator();
                while (out2.hasNext()) {
                    EventEdge out3 = (EventEdge)out2.next();
                    String outSig = out3.getSink().getSignature();
                    if (out3.getStartTime().compareTo(POITime) >= 0) continue;
                    EntityNode target = out3.getSink();
                    if (this.addNodeToRes(target, res)) {
                        nextQueue.offer(target);
                    }
                    res.addEdge(cur, target, out3);
                }
            }
            queue = nextQueue;
            nextQueue = new LinkedList();
        }
        try {
            FileWriter fileWriter = new FileWriter(new File("822Edge.txt"));
            for (EventEdge edge : this.input.edgeSet()) {
                String source = edge.getSource().getSignature();
                String target = edge.getSink().getSignature();
                if (!source.equals("29628wget") && !target.equals("29628wget")) continue;
                fileWriter.write(edge.toString() + "\n");
            }
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public List<DirectedPseudograph<EntityNode, EventEdge>> multipleForwardLimitedByPOI(List<String> forwardStarts, BigDecimal POITime) {
        ArrayList<DirectedPseudograph<EntityNode, EventEdge>> graphs = new ArrayList<DirectedPseudograph<EntityNode, EventEdge>>();
        for (String start : forwardStarts) {
            graphs.add(this.forwardLimitedByTime(start, POITime));
        }
        return graphs;
    }

    private boolean addNodeToRes(EntityNode node, DirectedPseudograph<EntityNode, EventEdge> res) {
        return res.addVertex(node);
    }

    private BigDecimal getEarliestOPTime(EntityNode node, DirectedPseudograph<EntityNode, EventEdge> res) {
        Set incoming = res.incomingEdgesOf(node);
        LinkedList edgeList = new LinkedList(incoming);
        assert (edgeList.size() > 0);
        edgeList.sort((a, b) -> a.startTime.compareTo(b.startTime));
        return ((EventEdge)edgeList.get((int)0)).startTime;
    }

    public static void main(String[] args) {
        String logfile = "C:\\Users\\fang2\\Desktop\\reptracker\\data\\attack_log\\cmd-inject.txt";
        GetGraph getGraph = new GetGraph(logfile, MetaConfig.localIP);
        getGraph.GenerateGraph();
        DirectedPseudograph<EntityNode, EventEdge> raw_graph = getGraph.getJg();
        ForwardAnalysis forwardTest = new ForwardAnalysis(raw_graph);
        String start = "172.31.77.48:46722->172.31.71.251:44444";
        int step = 10;
        DirectedPseudograph<EntityNode, EventEdge> forwardRes = forwardTest.fowardTrack(start, step);
        IterateGraph outputer = new IterateGraph(forwardRes);
        outputer.exportGraph("forwardTest");
        CausalityPreserve CPR = new CausalityPreserve(forwardRes);
        CPR.mergeEdgeFallInTheRange2(10.0);
        outputer = new IterateGraph(CPR.afterMerge);
        outputer.exportGraph("forward_aftermerge");
    }
}

