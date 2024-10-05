/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DirectedPseudograph;
import pagerank.EntityNode;
import pagerank.EventEdge;
import pagerank.IterateGraph;

public class NODOZE {
    Set<String> ipMalicious;
    Set<String> fileMalicious;
    Map<String, Map<String, Map<String, Double>>> frequencyMap;
    Map<String, Double> inProcess;
    Map<String, Double> outProcess;
    Map<String, Double> fileScore;
    Map<String, Double> ipScore;
    DirectedPseudograph<EntityNode, EventEdge> rawGraph;
    DirectedPseudograph<EntityNode, EventEdge> backtrack;
    DirectedPseudograph<EntityNode, EventEdge> graphAfterCPR;
    String poiEvent;
    IterateGraph helper;
    Set<String> importantEntries = new HashSet<String>();

    NODOZE(List<String> ip, List<String> file, DirectedPseudograph<EntityNode, EventEdge> original, DirectedPseudograph<EntityNode, EventEdge> backtrack, DirectedPseudograph<EntityNode, EventEdge> graphAfterCPR, String poi, String[] importantEntries) {
        this.ipMalicious = new HashSet<String>(ip);
        this.fileMalicious = new HashSet<String>(file);
        this.frequencyMap = new HashMap<String, Map<String, Map<String, Double>>>();
        this.rawGraph = original;
        this.backtrack = backtrack;
        this.graphAfterCPR = graphAfterCPR;
        this.poiEvent = poi;
        this.helper = new IterateGraph(graphAfterCPR);
        this.inProcess = new HashMap<String, Double>();
        this.outProcess = new HashMap<String, Double>();
        this.fileScore = new HashMap<String, Double>();
        this.ipScore = new HashMap<String, Double>();
        for (String e : importantEntries) {
            this.importantEntries.add(e);
        }
        this.initialize();
    }

    private void initialize() {
        this.updateFrequencyMap();
        this.updateInAndOutProcessMap();
        this.updateFileScoreMap();
        this.updateIPScore();
    }

    public int filterExp() {
        EntityNode target = this.helper.getGraphVertex(this.poiEvent);
        Set vertex = this.graphAfterCPR.vertexSet();
        HashSet<EntityNode> targetVertex = new HashSet<EntityNode>();
        targetVertex.add(target);
        LinkedList<GraphPath<EntityNode, EventEdge>> allPaths = new LinkedList<GraphPath<EntityNode, EventEdge>>();
        AllDirectedPaths<EntityNode, EventEdge> findPath = new AllDirectedPaths<EntityNode, EventEdge>(this.graphAfterCPR);
        for (EntityNode v : vertex) {
            if (v.getSignature().equals(this.poiEvent) || this.graphAfterCPR.inDegreeOf(v) != 0) continue;
            List<GraphPath<EntityNode, EventEdge>> path = findPath.getAllPaths(v, target, true, (Integer)7);
            allPaths.addAll(path);
            if (allPaths.size() < 18000) continue;
            break;
        }
        System.out.println("Paths size: " + String.valueOf(allPaths.size()));
        Map<Integer, List<Integer>> lengthIndex = this.getLengthIndex(allPaths);
        Map<Integer, Double> pathScores = this.calculateScoreForEachPath(allPaths);
        System.out.println("------PathScores--------");
        System.out.println(pathScores);
        Map<Integer, Double> averageScoreForEachLength = this.calculateScoreForEachLength(lengthIndex, pathScores);
        System.out.println("---------Average Path Score for each length");
        System.out.println(averageScoreForEachLength);
        List<Integer> filterRes = this.filterBasedOnScore(averageScoreForEachLength, pathScores, allPaths);
        System.out.println("------filterResSize-----");
        System.out.println(filterRes.size());
        Set<Long> edgeInPath = this.getEdgeInFilterRes(filterRes, allPaths);
        return edgeInPath.size();
    }

    private Set<Long> getEdgeInFilterRes(List<Integer> filterRes, List<GraphPath<EntityNode, EventEdge>> allPaths) {
        HashSet<Long> res = new HashSet<Long>();
        for (Integer i : filterRes) {
            GraphPath<EntityNode, EventEdge> path = allPaths.get(i);
            List<EventEdge> edge = path.getEdgeList();
            for (EventEdge e : edge) {
                res.add(e.getID());
            }
        }
        return res;
    }

    private List<Integer> filterBasedOnScore(Map<Integer, Double> averScoreForEachLength, Map<Integer, Double> pathScores, List<GraphPath<EntityNode, EventEdge>> allPath) {
        ArrayList<Integer> res = new ArrayList<Integer>();
        for (int i = 0; i < allPath.size(); ++i) {
            GraphPath<EntityNode, EventEdge> curPath = allPath.get(i);
            Double pathScore = pathScores.get(i);
            Double standard = averScoreForEachLength.get(curPath.getLength()) * 1.000000000000003;
            if (!(pathScore >= standard)) continue;
            res.add(i);
        }
        return res;
    }

    private Map<Integer, Double> calculateScoreForEachLength(Map<Integer, List<Integer>> lengthIndex, Map<Integer, Double> pathScores) {
        HashMap<Integer, Double> average = new HashMap<Integer, Double>();
        for (Integer length : lengthIndex.keySet()) {
            List<Integer> paths = lengthIndex.get(length);
            double scoreSum = 0.0;
            for (Integer i : paths) {
                scoreSum += pathScores.get(i).doubleValue();
            }
            double averageScore = scoreSum / (double)paths.size();
            average.put(length, averageScore);
        }
        return average;
    }

    private Map<Integer, Double> calculateScoreForEachPath(List<GraphPath<EntityNode, EventEdge>> allPaths) {
        HashMap<Integer, Double> pathScore = new HashMap<Integer, Double>();
        for (int i = 0; i < allPaths.size(); ++i) {
            GraphPath<EntityNode, EventEdge> curPath = allPaths.get(i);
            List<EventEdge> edges = curPath.getEdgeList();
            double score = 1.0;
            for (EventEdge e : edges) {
                String src = e.getSource().getSignature();
                String dst = e.getSink().getSignature();
                String evt = e.getEvent();
                double scoreSrc = this.importantEntries.contains(src) ? 0.0 : 1.0;
                double dstScore = this.importantEntries.contains(dst) ? 0.0 : 1.0;
                score *= scoreSrc * this.calculatePossibilityScore(src, dst, evt) * dstScore;
            }
            score = 1.0 - score;
            pathScore.put(i, score);
        }
        return pathScore;
    }

    private Map<Integer, List<Integer>> getLengthIndex(List<GraphPath<EntityNode, EventEdge>> allPaths) {
        HashMap<Integer, List<Integer>> lengthIndex = new HashMap<Integer, List<Integer>>();
        for (int i = 0; i < allPaths.size(); ++i) {
            GraphPath<EntityNode, EventEdge> curPath = allPaths.get(i);
            int length = curPath.getLength();
            if (!lengthIndex.containsKey(length)) {
                lengthIndex.put(length, new LinkedList());
            }
            ((List)lengthIndex.get(length)).add(i);
        }
        return lengthIndex;
    }

    private void updateInAndOutProcessMap() {
        Set vertex = this.rawGraph.vertexSet();
        for (EntityNode v : vertex) {
            if (!v.isProcessNode()) continue;
            String signature = v.getSignature();
            double total = (double)(this.rawGraph.inDegreeOf(v) + this.rawGraph.outDegreeOf(v)) * 1.0;
            if (!this.inProcess.containsKey(signature)) {
                this.inProcess.put(signature, (double)this.rawGraph.inDegreeOf(v) / total);
            }
            if (this.outProcess.containsKey(signature)) continue;
            this.outProcess.put(signature, (double)this.rawGraph.outDegreeOf(v) / total);
        }
    }

    private void updateIPScore() {
        Set vertex = this.rawGraph.vertexSet();
        for (EntityNode v : vertex) {
            String signature;
            if (!v.isNetworkNode() || this.ipScore.containsKey(signature = v.getSignature())) continue;
            this.ipScore.put(signature, 1.0);
        }
    }

    private void updateFrequencyMap() {
        Set edgeSet = this.rawGraph.edgeSet();
        for (EventEdge edge : edgeSet) {
            String src = edge.getSource().getSignature();
            String dst = edge.getSink().getSignature();
            String evt = edge.getEvent();
            if (!this.frequencyMap.containsKey(src)) {
                this.frequencyMap.put(src, new HashMap());
            }
            if (!this.frequencyMap.get(src).containsKey(evt)) {
                this.frequencyMap.get(src).put(evt, new HashMap());
            }
            this.frequencyMap.get(src).get(evt).put(dst, this.frequencyMap.get(src).get(evt).getOrDefault(dst, 0.0) + 1.0);
        }
    }

    private void updateFileScoreMap() {
        Set vertex = this.rawGraph.vertexSet();
        for (EntityNode v : vertex) {
            String signature;
            if (!v.isFileNode() || this.fileScore.containsKey(signature = v.getSignature())) continue;
            if (signature.endsWith("txt")) {
                this.fileScore.put(signature, 1.0);
                continue;
            }
            this.fileScore.put(signature, 0.0);
        }
    }

    private double getSumBasedOnEvtSrcAndType(String evtSrc, String evtType) {
        Map<String, Map<String, Double>> events;
        if (!this.frequencyMap.containsKey(evtSrc)) {
            System.out.println("The input evtSrc doesn't not exist in FrequencyMap");
        }
        if (!(events = this.frequencyMap.get(evtSrc)).containsKey(evtType)) {
            System.out.println("The given evt type is not found for " + evtSrc);
        }
        Map<String, Double> givenTypeEvents = events.get(evtType);
        double sum2 = 0.0;
        for (String dst : givenTypeEvents.keySet()) {
            sum2 += givenTypeEvents.get(dst).doubleValue();
        }
        return sum2;
    }

    private double calculatePossibilityScore(String src, String dst, String evt) {
        double giveEventTimes = this.frequencyMap.get(src).get(evt).get(dst);
        double givenSrcAndEvt = this.getSumBasedOnEvtSrcAndType(src, evt);
        return giveEventTimes / givenSrcAndEvt;
    }

    private double calculateInScore(String src) {
        if (this.inProcess.containsKey(src)) {
            return this.inProcess.get(src);
        }
        return 1.0;
    }
}

