/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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

public class NodozeDarpa {
    Set<String> fileMalicious;
    Map<Integer, Map<String, Map<Integer, Integer>>> frequencyMap;
    Map<Integer, Double> inProcess;
    Map<Integer, Double> outProcess;
    Map<String, Double> fileScore;
    Map<String, Double> ipScore;
    String poiEvent;
    DirectedPseudograph<EntityNode, EventEdge> backtrack;
    Set<String> importantEntries;
    String[] changeDirection = new String[]{"read", "recvmsg", " loadlibrary", "accept", "recvfrom", "read_socket_params"};
    Set<String> directionChange = new HashSet<String>(Arrays.asList(this.changeDirection));
    BigDecimal timeThresh;

    NodozeDarpa(String frequencyFilePath, String idAndName, List<String> file, DirectedPseudograph<EntityNode, EventEdge> backtrack, String poi, String[] importantEntries) {
        this.fileMalicious = new HashSet<String>(file);
        this.frequencyMap = new HashMap<Integer, Map<String, Map<Integer, Integer>>>();
        this.poiEvent = poi;
        this.backtrack = backtrack;
        this.inProcess = new HashMap<Integer, Double>();
        this.outProcess = new HashMap<Integer, Double>();
        this.fileScore = new HashMap<String, Double>();
        this.ipScore = new HashMap<String, Double>();
        this.importantEntries = new HashSet<String>();
        for (String s2 : importantEntries) {
            this.importantEntries.add(s2);
        }
        this.initialize(frequencyFilePath);
        this.calculateProcessScore();
        this.updateFileAndIPScore(idAndName);
    }

    private void updateFileAndIPScore(String idAndName) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map jsonObj = mapper.readValue(Paths.get(idAndName, new String[0]).toFile(), Map.class);
            for (Map.Entry entry : jsonObj.entrySet()) {
                Map info;
                String type = (String)entry.getKey();
                if (type.equals("process")) continue;
                if (type.equals("file")) {
                    info = (Map)entry.getValue();
                    for (Map.Entry docs : info.entrySet()) {
                        Map docInfo = (Map)docs.getValue();
                        for (Map.Entry doc : docInfo.entrySet()) {
                            String key = (String)doc.getKey();
                            if (!key.equals("name")) continue;
                            String name = (String)doc.getValue();
                            if (name.endsWith("txt")) {
                                this.fileScore.put(name, 1.0);
                                continue;
                            }
                            this.fileScore.put(name, 0.0);
                        }
                    }
                    continue;
                }
                if (!type.equals("network")) continue;
                info = (Map)entry.getValue();
                for (Map.Entry net : info.entrySet()) {
                    Map netInfo = (Map)net.getValue();
                    String src = "";
                    String srcport = "";
                    String dst = "";
                    String dstport = "";
                    String connection = "";
                    for (Map.Entry detail : netInfo.entrySet()) {
                        String key = (String)detail.getKey();
                        if (key.equals("src")) {
                            src = (String)detail.getValue();
                            continue;
                        }
                        if (key.equals("srcport")) {
                            srcport = (String)detail.getValue();
                            continue;
                        }
                        if (key.equals("dst")) {
                            dst = (String)detail.getValue();
                            continue;
                        }
                        if (!key.equals("dstport")) continue;
                        dstport = (String)detail.getValue();
                    }
                    this.ipScore.put(connection, 1.0);
                }
            }
            System.out.println("Finished updateFileAndIPScore.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateProcessScore() {
        HashMap<Integer, Double> total = new HashMap<Integer, Double>();
        for (Integer t : this.inProcess.keySet()) {
            if (this.outProcess.containsKey(t)) {
                total.put(t, this.inProcess.get(t) + this.outProcess.get(t));
                continue;
            }
            total.put(t, this.inProcess.get(t));
        }
        for (Integer t : this.outProcess.keySet()) {
            if (this.inProcess.keySet().contains(t)) continue;
            total.put(t, this.outProcess.get(t));
        }
        for (Integer t : total.keySet()) {
            double degree = (Double)total.get(t);
            double indegree = this.inProcess.getOrDefault(t, 0.0);
            double outdegree = this.outProcess.getOrDefault(t, 0.0);
            this.inProcess.put(t, indegree / degree);
            this.outProcess.put(t, outdegree / degree);
        }
        System.out.println("Finished calculateProcessScore");
    }

    private void initialize(String frequencyFilePath) {
        try {
            System.out.println("Initialize function is called to read value from json file.");
            ObjectMapper mapper = new ObjectMapper();
            Map jsonObj = mapper.readValue(Paths.get(frequencyFilePath, new String[0]).toFile(), Map.class);
            for (Map.Entry entries2 : jsonObj.entrySet()) {
                Integer obj1 = Integer.valueOf((String)entries2.getKey());
                Map objCount = (Map)entries2.getValue();
                for (Map.Entry count2 : objCount.entrySet()) {
                    this.updateFrequencyMap(obj1, count2);
                }
            }
            System.out.println("Finished Initialize function.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateFrequencyMap(Integer obj1, Map.Entry<?, ?> entry) {
        String optype = (String)entry.getKey();
        Map dstCount = (Map)entry.getValue();
        for (Map.Entry count2 : dstCount.entrySet()) {
            Integer obj2 = Integer.valueOf((String)count2.getKey());
            Integer times = Integer.valueOf((String)count2.getValue());
            if (this.directionChange.contains(optype)) {
                this.updateCount(obj2, obj1, optype, times);
                this.inProcess.put(obj1, this.inProcess.getOrDefault(obj1, 0.0) + 1.0);
                continue;
            }
            this.updateCount(obj1, obj2, optype, times);
            this.outProcess.put(obj1, this.outProcess.getOrDefault(obj1, 0.0) + 1.0);
        }
    }

    private void updateCount(Integer src, Integer dst, String optype, Integer times) {
        this.frequencyMap.putIfAbsent(src, new HashMap());
        Map<String, Map<Integer, Integer>> typeCount = this.frequencyMap.get(src);
        typeCount.putIfAbsent(optype, new HashMap());
        Map<Integer, Integer> dstCount = typeCount.get(optype);
        dstCount.put(dst, times);
    }

    public int filterExp(DirectedPseudograph<EntityNode, EventEdge> backtrack) {
        IterateGraph helper = new IterateGraph(backtrack);
        EntityNode target = helper.getGraphVertex(this.poiEvent);
        Set vertex = backtrack.vertexSet();
        HashSet<EntityNode> targetVertex = new HashSet<EntityNode>();
        targetVertex.add(target);
        LinkedList<GraphPath<EntityNode, EventEdge>> allPaths = new LinkedList<GraphPath<EntityNode, EventEdge>>();
        AllDirectedPaths<EntityNode, EventEdge> findPath = new AllDirectedPaths<EntityNode, EventEdge>(backtrack);
        for (EntityNode v : vertex) {
            if (v.getSignature().equals(this.poiEvent) || backtrack.inDegreeOf(v) != 0) continue;
            List<GraphPath<EntityNode, EventEdge>> path = findPath.getAllPaths(v, target, true, (Integer)6);
            allPaths.addAll(path);
            System.out.println("For current the paths is: " + String.valueOf(allPaths.size()));
            if (allPaths.size() < 18000) continue;
            break;
        }
        System.out.println("Paths size: " + String.valueOf(allPaths.size()));
        Map<Integer, List<Integer>> lengthIndex = this.getLengthIndex(allPaths);
        Map<Integer, Double> pathScores = this.calculateScoreForEachPath(allPaths);
        Map<Integer, Double> averageScoreForEachLength = this.calculateScoreForEachLength(lengthIndex, pathScores);
        List<Integer> filterRes = this.filterBasedOnScore(averageScoreForEachLength, pathScores, allPaths);
        System.out.println("------filterResSize-----");
        System.out.println(filterRes.size());
        Set<Long> edgeInPath = this.getEdgeInFilterRes(filterRes, allPaths);
        return edgeInPath.size();
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

    private Map<Integer, Double> calculateScoreForEachPath(List<GraphPath<EntityNode, EventEdge>> allPaths) {
        HashMap<Integer, Double> pathScore = new HashMap<Integer, Double>();
        for (int i = 0; i < allPaths.size(); ++i) {
            GraphPath<EntityNode, EventEdge> curPath = allPaths.get(i);
            List<EventEdge> edges = curPath.getEdgeList();
            double score = 1.0;
            for (EventEdge e : edges) {
                String src = e.getSource().getSignature();
                int srcID = this.convertID(e.getSource().getID());
                int dstID = this.convertID(e.getSink().getID());
                String dst = e.getSink().getSignature();
                String evt = e.getEvent();
                double scoreSrc = this.importantEntries.contains(src) ? 0.0 : 1.0;
                double dstScore = this.importantEntries.contains(dst) ? 0.0 : 1.0;
                score *= scoreSrc * this.calculatePossibilityScore(srcID, dstID, evt) * dstScore;
            }
            score = 1.0 - score;
            pathScore.put(i, score);
        }
        return pathScore;
    }

    private double calculatePossibilityScore(int src, int dst, String evt) {
        double giveEventTimes = 0.0;
        if (this.frequencyMap.containsKey(src) && this.frequencyMap.get(src).containsKey(evt)) {
            giveEventTimes = this.frequencyMap.get(src).get(evt).getOrDefault(dst, 0).intValue();
        }
        double givenSrcAndEvt = this.getSumBasedOnEvtSrcAndType(src, evt);
        return giveEventTimes / givenSrcAndEvt;
    }

    private double getSumBasedOnEvtSrcAndType(int evtSrc, String evtType) {
        if (!this.frequencyMap.containsKey(evtSrc)) {
            System.out.println("The input evtSrc doesn't not exist in FrequencyMap");
            return 1.0;
        }
        Map<String, Map<Integer, Integer>> events = this.frequencyMap.get(evtSrc);
        if (!events.containsKey(evtType)) {
            System.out.println("The given evt type is not found for " + evtSrc);
            return 1.0;
        }
        Map<Integer, Integer> givenTypeEvents = events.get(evtType);
        double sum2 = 0.0;
        for (Integer dst : givenTypeEvents.keySet()) {
            sum2 += (double)givenTypeEvents.get(dst).intValue();
        }
        return sum2;
    }

    private int convertID(long id) {
        return Integer.valueOf(String.valueOf(id));
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
}

