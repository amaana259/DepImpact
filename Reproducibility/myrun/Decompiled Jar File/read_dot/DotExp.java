/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package read_dot;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jgrapht.graph.DirectedPseudograph;
import org.json.simple.JSONObject;
import pagerank.Experiment;
import read_dot.ReadGraphFromDot;
import read_dot.SeedSetter;
import read_dot.SimpleEdge;
import read_dot.SimpleNode;

public class DotExp {
    File dotFile;
    File propertyFile;
    DirectedPseudograph<SimpleNode, SimpleEdge> graph;
    ReadGraphFromDot readDot;
    SeedSetter seedSetter;
    Experiment exp;

    DotExp(String dotFile, String propertyFile) {
        this.dotFile = new File(dotFile);
        this.propertyFile = new File(propertyFile);
        this.readDot = new ReadGraphFromDot();
        this.seedSetter = new SeedSetter();
        this.graph = this.readDot.readGraph(dotFile);
        this.clearInitialReputation();
        String logName = "curl";
        File logFile = new File(logName);
        try {
            this.exp = new Experiment(logFile, this.propertyFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startExp() {
        this.seedSetter.setSeedReputation(this.graph, this.exp);
        this.PageRankIteration2(this.exp.highRP, this.exp.midRP, this.exp.lowRP, this.exp.POI);
        this.outputRes();
    }

    public void outputRes() {
        Integer n;
        Integer n2;
        String caseName = this.getCaseName();
        Double reputaion = null;
        Integer libraryCount = 0;
        Integer highAndLowSeed = 0;
        Set vertexSet = this.graph.vertexSet();
        List<String> high = this.exp.getHighRP();
        for (SimpleNode v : vertexSet) {
            if (v.signature.equals(this.exp.POI)) {
                reputaion = v.reputation;
            }
            if (this.graph.incomingEdgesOf(v).size() != 0) continue;
            Integer n3 = libraryCount;
            Integer n4 = libraryCount = Integer.valueOf(libraryCount + 1);
        }
        for (String s2 : this.exp.highRP) {
            if (s2.equals("")) continue;
            n2 = highAndLowSeed;
            n = highAndLowSeed = Integer.valueOf(highAndLowSeed + 1);
        }
        for (String s2 : this.exp.lowRP) {
            if (s2.equals("")) continue;
            n2 = highAndLowSeed;
            n = highAndLowSeed = Integer.valueOf(highAndLowSeed + 1);
        }
        JSONObject resJson = new JSONObject();
        resJson.put("Case", caseName);
        resJson.put("POI_Reputation", reputaion);
        resJson.put("Library_Count", libraryCount);
        resJson.put("Special_Seed", highAndLowSeed);
        String jsonFileName = caseName + ".json";
        try {
            FileWriter file = new FileWriter(jsonFileName);
            file.write(resJson.toJSONString());
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearInitialReputation() {
        for (SimpleNode v : this.graph.vertexSet()) {
            v.reputation = 0.0;
        }
    }

    public void PageRankIteration2(String[] highRP, String[] midRP, String[] lowRP, String detection) {
        double alarmlevel = 0.85;
        Set vertexSet = this.graph.vertexSet();
        HashSet<String> sources = new HashSet<String>(Arrays.asList(highRP));
        sources.addAll(Arrays.asList(lowRP));
        sources.addAll(Arrays.asList(midRP));
        double fluctuation = 1.0;
        int iterTime = 0;
        while (fluctuation >= 1.0E-5) {
            double culmativediff = 0.0;
            ++iterTime;
            Map<Long, Double> preReputation = this.getReputation();
            for (SimpleNode v : vertexSet) {
                if (v.signature.equals(detection)) {
                    System.out.println(v.reputation);
                }
                if (sources.contains(v.signature)) continue;
                Set edges = this.graph.incomingEdgesOf(v);
                double rep = 0.0;
                if (edges.size() == 0) {
                    rep = v.reputation;
                }
                for (SimpleEdge edge : edges) {
                    SimpleNode source = edge.from;
                    rep += preReputation.get(source.id) * edge.weight;
                }
                culmativediff += Math.abs(rep - preReputation.get(v.id));
                v.reputation = rep;
            }
            fluctuation = culmativediff;
        }
        System.out.println(String.format("After %d times iteration, the reputation of each vertex is stable", iterTime));
    }

    private Map<Long, Double> getReputation() {
        Set vertexSet = this.graph.vertexSet();
        HashMap<Long, Double> map2 = new HashMap<Long, Double>();
        for (SimpleNode node : vertexSet) {
            map2.put(node.id, node.reputation);
        }
        return map2;
    }

    private String getCaseName() {
        String caseName = this.dotFile.getName();
        System.out.println(caseName);
        String[] arr = caseName.split("\\.");
        return arr[0];
    }

    public static void main(String[] args) {
        String dotFile = "C:\\Users\\fang2\\Desktop\\reptrack_ccs2\\reptracker\\input\\Exp_using_dot\\dot_files\\ThreeFileRW_ggg.dot";
        String pro = "C:\\Users\\fang2\\Desktop\\reptrack_ccs2\\reptracker\\input\\Exp_using_dot\\properties_for_all\\ThreeFileRW_ggg.property";
        DotExp exp = new DotExp(dotFile, pro);
        exp.startExp();
    }
}

