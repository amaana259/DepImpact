/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.jgrapht.graph.DirectedPseudograph;
import pagerank.EntityNode;
import pagerank.EventEdge;
import pagerank.IterateGraph;

public class CausalityPreserve {
    DirectedPseudograph<EntityNode, EventEdge> input;
    IterateGraph graphIter;
    DirectedPseudograph<EntityNode, EventEdge> afterMerge;

    CausalityPreserve(DirectedPseudograph<EntityNode, EventEdge> input) {
        this.input = (DirectedPseudograph)input.clone();
    }

    public DirectedPseudograph<EntityNode, EventEdge> CPR(int choose) {
        System.out.println("CPR invoked: " + choose);
        if (choose < 1 || choose > 3) {
            String usage = "The legal parameter:(int) 1,2,3\nMethod 1: this merge method considers about time and event type\nMethod 2: this merge method only considers event type\nMethod 3: thie merge method doesn't consider about time and event type";
            throw new IllegalArgumentException(usage);
        }
        if (choose == 1) {
            return this.mergeConsiderTimeAndType();
        }
        if (choose == 2) {
            return this.mergeConsiderType();
        }
        return this.mergeWithoutConsideringTimeAndType();
    }

    private DirectedPseudograph<EntityNode, EventEdge> mergeConsiderTimeAndType() {
        Set<EventEdge> edgeSet = this.input.edgeSet();
        LinkedList edgeList = new LinkedList(edgeSet);
        Collections.sort(edgeList, (a, b) -> a.getStartTime().compareTo(b.getStartTime()));
        Iterator iter = edgeList.iterator();
        Map<String, Map<EntityNode, Map<EntityNode, Stack<EventEdge>>>> pairStacks = this.initializePairStack(edgeSet);
        while (iter.hasNext()) {
            EventEdge cur = (EventEdge)iter.next();
            EntityNode source = cur.getSource();
            EntityNode target = cur.getSink();
            Stack<EventEdge> stack = pairStacks.get(cur.getEvent()).get(source).get(target);
            if (stack.isEmpty()) {
                stack.push(cur);
                continue;
            }
            EventEdge edgePrevious = stack.pop();
            if (this.backwardCheck(edgePrevious, cur, source)) {
                edgePrevious = this.merge(edgePrevious, cur);
                stack.push(edgePrevious);
                continue;
            }
            stack.push(cur);
        }
        this.afterMerge = this.input;
        return this.afterMerge;
    }

    public DirectedPseudograph<EntityNode, EventEdge> mergeEdgeFallInTheRange(double range) {
        Comparator<EventEdge> cmp = new Comparator<EventEdge>(){

            @Override
            public int compare(EventEdge a, EventEdge b) {
                if (a.getStartTime().compareTo(b.getStartTime()) == 0) {
                    return a.getEndTime().compareTo(b.getEndTime());
                }
                return a.getStartTime().compareTo(b.getStartTime());
            }
        };
        BigDecimal timeDiff = new BigDecimal(range);
        Set<EventEdge> edgeSet = this.input.edgeSet();
        LinkedList edgeList = new LinkedList(edgeSet);
        Collections.sort(edgeList, cmp);
        Iterator iter = edgeList.iterator();
        Map<String, Map<EntityNode, Map<EntityNode, Stack<EventEdge>>>> pairStacks = this.initializePairStack(edgeSet);
        while (iter.hasNext()) {
            EventEdge cur = (EventEdge)iter.next();
            EntityNode source = cur.getSource();
            EntityNode target = cur.getSink();
            Stack<EventEdge> stack = pairStacks.get(cur.getEvent()).get(source).get(target);
            if (stack.isEmpty()) {
                stack.push(cur);
                continue;
            }
            EventEdge edgePrevious = stack.pop();
            BigDecimal diff = cur.getStartTime().subtract(edgePrevious.endTime);
            if (diff.compareTo(timeDiff) <= 0) {
                edgePrevious = this.merge(edgePrevious, cur);
                stack.push(edgePrevious);
                continue;
            }
            stack.push(edgePrevious);
            stack.push(cur);
        }
        this.afterMerge = this.input;
        return this.afterMerge;
    }

    public DirectedPseudograph<EntityNode, EventEdge> mergeEdgeFallInTheRange2(double range) {
        DirectedPseudograph<EntityNode, EventEdge> merged = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        for (EntityNode n : this.input.vertexSet()) {
            merged.addVertex(n);
        }
        Comparator<EventEdge> cmp = new Comparator<EventEdge>(){

            @Override
            public int compare(EventEdge a, EventEdge b) {
                if (a.getStartTime().compareTo(b.getStartTime()) == 0) {
                    return a.getEndTime().compareTo(b.getEndTime());
                }
                return a.getStartTime().compareTo(b.getStartTime());
            }
        };
        BigDecimal timeDiff = new BigDecimal(range);
        Set<EventEdge> edgeSet = this.input.edgeSet();
        LinkedList edgeList = new LinkedList(edgeSet);
        Collections.sort(edgeList, cmp);
        Iterator iter = edgeList.iterator();
        Map<String, Map<EntityNode, Map<EntityNode, Stack<EventEdge>>>> pairStacks = this.initializePairStack(edgeSet);
        while (iter.hasNext()) {
            EventEdge cur = (EventEdge)iter.next();
            EntityNode source = cur.getSource();
            EntityNode target = cur.getSink();
            Stack<EventEdge> stack = pairStacks.get(cur.getEvent()).get(source).get(target);
            if (stack.isEmpty()) {
                stack.push(cur);
                continue;
            }
            EventEdge edgePrevious = stack.pop();
            BigDecimal diff = cur.getStartTime().subtract(edgePrevious.endTime);
            if (diff.compareTo(timeDiff) <= 0) {
                edgePrevious = edgePrevious.merge(cur);
                stack.push(edgePrevious);
                continue;
            }
            stack.push(edgePrevious);
            stack.push(cur);
        }
        for (String event : pairStacks.keySet()) {
            for (EntityNode source : pairStacks.get(event).keySet()) {
                for (EntityNode sink : pairStacks.get(event).get(source).keySet()) {
                    Stack<EventEdge> s2 = pairStacks.get(event).get(source).get(sink);
                    for (EventEdge e : s2) {
                        merged.addEdge(source, sink, e);
                    }
                }
            }
        }
        this.afterMerge = merged;
        return this.afterMerge;
    }

    private DirectedPseudograph<EntityNode, EventEdge> mergeWithoutConsideringTimeAndType() {
        HashMap map2 = new HashMap();
        Set edgeSet = this.input.edgeSet();
        ArrayList edgeList = new ArrayList(edgeSet);
        Collections.sort(edgeList, (a, b) -> a.getEndTime().compareTo(b.getEndTime()));
        for (EventEdge e : edgeList) {
            EntityNode source = e.getSource();
            EntityNode target = e.getSink();
            e.setEdgeEvent("NullAfterMerge");
            if (!map2.containsKey(source)) {
                map2.put(source, new HashMap());
            }
            if (((Map)map2.get(source)).containsKey(target)) {
                EventEdge previous = (EventEdge)((Map)map2.get(source)).get(target);
                previous = this.merge(previous, e);
                ((Map)map2.get(source)).put(target, previous);
                continue;
            }
            ((Map)map2.get(source)).put(target, e);
        }
        this.afterMerge = this.input;
        return this.afterMerge;
    }

    private DirectedPseudograph<EntityNode, EventEdge> mergeConsiderType() {
        Set<EventEdge> edgeSet = this.input.edgeSet();
        LinkedList edgeList = new LinkedList(edgeSet);
        Collections.sort(edgeList, (a, b) -> a.getStartTime().compareTo(b.getStartTime()));
        Iterator iter = edgeList.iterator();
        Map<String, Map<EntityNode, Map<EntityNode, Stack<EventEdge>>>> pairStacks = this.initializePairStack(edgeSet);
        while (iter.hasNext()) {
            EventEdge cur = (EventEdge)iter.next();
            EntityNode source = cur.getSource();
            EntityNode target = cur.getSink();
            Stack<EventEdge> stack = pairStacks.get(cur.getEvent()).get(source).get(target);
            if (stack.isEmpty()) {
                stack.push(cur);
                continue;
            }
            EventEdge edgePrevious = stack.pop();
            edgePrevious = this.merge(edgePrevious, cur);
            stack.push(edgePrevious);
        }
        this.afterMerge = this.input;
        return this.afterMerge;
    }

    private EventEdge merge(EventEdge previous, EventEdge cur) {
        previous = previous.merge(cur);
        this.input.removeEdge(cur);
        return previous;
    }

    private Map<String, Map<EntityNode, Map<EntityNode, Stack<EventEdge>>>> initializePairStack(Set<EventEdge> edges) {
        HashMap<String, Map<EntityNode, Map<EntityNode, Stack<EventEdge>>>> map2 = new HashMap<String, Map<EntityNode, Map<EntityNode, Stack<EventEdge>>>>();
        for (EventEdge e : edges) {
            String event = e.getEvent();
            map2.putIfAbsent(event, new HashMap());
            Map eventMap = (Map)map2.get(event);
            EntityNode source = e.getSource();
            EntityNode target = e.getSink();
            eventMap.putIfAbsent(source, new HashMap());
            ((Map)eventMap.get(source)).putIfAbsent(target, new Stack());
        }
        return map2;
    }

    private DirectedPseudograph<EntityNode, EventEdge> getCPR(Map<EntityNode, Map<EntityNode, Deque<EventEdge>>> mapOfStacks) {
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
        return res;
    }

    private boolean backwardCheck(EventEdge p, EventEdge l, EntityNode u) {
        Set incoming = this.input.incomingEdgesOf(u);
        Object[] endTimes = new BigDecimal[]{p.getEndTime(), l.getEndTime()};
        if (p.getEndTime() == null) {
            System.out.println(p.getID());
            System.out.println(p.getEvent());
        }
        if (l.getEndTime() == null) {
            System.out.println(l.getID());
            System.out.println(l.getEvent());
        }
        Arrays.sort(endTimes);
        for (EventEdge edge : incoming) {
            BigDecimal[] timeWindow = edge.getInterval();
            if (!this.isOverlap(timeWindow, (BigDecimal[])endTimes)) continue;
            return false;
        }
        return true;
    }

    private boolean forwardCheck(EventEdge p, EventEdge l, EntityNode u, BigDecimal curTime) {
        Object[] startTime = new BigDecimal[]{p.getStartTime(), l.getStartTime()};
        Set outgoing = this.input.outgoingEdgesOf(u);
        ArrayList<EventEdge> outgongCandidates = new ArrayList<EventEdge>();
        for (EventEdge e : outgoing) {
            if (e.getStartTime().compareTo(curTime) >= 0) continue;
            outgongCandidates.add(e);
        }
        Arrays.sort(startTime);
        for (EventEdge edge : outgongCandidates) {
            BigDecimal[] timeWindow = edge.getInterval();
            if (!this.isOverlap(timeWindow, (BigDecimal[])startTime)) continue;
            return false;
        }
        return true;
    }

    private boolean isOverlap(BigDecimal[] a, BigDecimal[] b) {
        return a[1].compareTo(b[0]) >= 0 && a[0].compareTo(b[1]) <= 0;
    }
}

