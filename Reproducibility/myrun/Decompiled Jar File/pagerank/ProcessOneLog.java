/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jgrapht.graph.DirectedPseudograph;
import pagerank.ArgParser;
import pagerank.BackTrack;
import pagerank.CausalityPreserve;
import pagerank.EntityNode;
import pagerank.EventEdge;
import pagerank.GetGraph;
import pagerank.GraphSplit;
import pagerank.InferenceReputation;
import pagerank.IterateGraph;
import pagerank.MetaConfig;

public class ProcessOneLog {
    public static void main(String[] args) {
        ArgParser ap = new ArgParser(args);
        Map<String, String> argMap = ap.parseArgs();
        String[] localIP = MetaConfig.localIP;
        String[] midRP = MetaConfig.midRP;
        String path = argMap.get("path");
        String detection = argMap.get("detection");
        String highRPs = argMap.get("high");
        String[] highRP = highRPs == null ? new String[]{} : highRPs.split(",");
        String neutralRPs = argMap.get("neutral");
        String[] neutralRP = neutralRPs == null ? new String[]{} : neutralRPs.split(",");
        ArrayList<String> midRP2 = new ArrayList<String>();
        midRP2.addAll(Arrays.asList(midRP));
        midRP2.addAll(Arrays.asList(neutralRP));
        String lowRPs = argMap.get("low");
        String[] lowRP = lowRPs == null ? new String[]{} : lowRPs.split(",");
        String resultDir = argMap.get("res");
        String suffix = argMap.get("suffix");
        double threshold = Double.parseDouble(argMap.get("thresh"));
        boolean trackOrigin = argMap.containsKey("origin");
        String[] paths = path.split("/");
        ProcessOneLog.process(resultDir, suffix, threshold, trackOrigin, path, localIP, detection, highRP, midRP2.toArray(new String[midRP2.size()]), lowRP, paths[paths.length - 1], 0.0, new HashSet<String>(), null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void process(String resultDir, String suffix, double threshold, boolean trackOrigin, String logfile, String[] IP, String detection, String[] highRP, String[] midRP, String[] lowRP, String filename, double detectionSize, Set<String> seedSources, String[] criticalEdges) {
        OutputStream os = null;
        Object weightfile = null;
        try {
            long start = System.currentTimeMillis();
            File result_folder = new File(resultDir);
            if (!result_folder.exists()) {
                result_folder.mkdir();
            }
            String explogfile = resultDir + File.separator + filename + suffix + "_stats";
            os = new FileOutputStream(explogfile);
            GetGraph getGraph = new GetGraph(logfile, IP);
            getGraph.GenerateGraph();
            DirectedPseudograph<EntityNode, EventEdge> orignal = getGraph.getJg();
            long end = System.currentTimeMillis();
            System.out.println("Parsing time:" + (double)(end - start) / 1000.0);
            System.out.println("Original vertex number:" + orignal.vertexSet().size() + " edge number : " + orignal.edgeSet().size());
            os.write(("Original vertex number:" + orignal.vertexSet().size() + " edge number : " + orignal.edgeSet().size() + "\n").getBytes());
            start = System.currentTimeMillis();
            BackTrack backTrack = new BackTrack(orignal);
            backTrack.backTrackPOIEvent(detection);
            end = System.currentTimeMillis();
            System.out.println("Backtrack time:" + (double)(end - start) / 1000.0);
            System.out.println("After Backtrack vertex number is: " + backTrack.afterBackTrack.vertexSet().size() + " edge number: " + backTrack.afterBackTrack.edgeSet().size());
            os.write(("After Backtrack vertex number is: " + backTrack.afterBackTrack.vertexSet().size() + " edge number: " + backTrack.afterBackTrack.edgeSet().size() + "\n").getBytes());
            IterateGraph out = new IterateGraph(backTrack.afterBackTrack);
            out.exportGraph(resultDir + "BackTrack_" + filename + suffix);
            start = System.currentTimeMillis();
            CausalityPreserve CPR = new CausalityPreserve(backTrack.afterBackTrack);
            CPR.mergeEdgeFallInTheRange2(10.0);
            end = System.currentTimeMillis();
            System.out.println("CPR time:" + (double)(end - start) / 1000.0);
            System.out.println("After CPR vertex number is: " + CPR.afterMerge.vertexSet().size() + " edge number: " + CPR.afterMerge.edgeSet().size());
            os.write(("After CPR vertex number is: " + CPR.afterMerge.vertexSet().size() + " edge number: " + CPR.afterMerge.edgeSet().size() + "\n").getBytes());
            out = new IterateGraph(CPR.afterMerge);
            out.exportGraph(resultDir + "AfterCPR_" + filename + suffix);
            start = System.currentTimeMillis();
            GraphSplit split2 = new GraphSplit(CPR.afterMerge);
            split2.splitGraph();
            end = System.currentTimeMillis();
            System.out.println("split time:" + (double)(end - start) / 1000.0);
            System.out.println("After Split vertex number is: " + split2.inputGraph.vertexSet().size() + " edge number: " + split2.inputGraph.edgeSet().size());
            InferenceReputation infer = new InferenceReputation(split2.inputGraph);
            os.write(("After Split vertex number is: " + split2.inputGraph.vertexSet().size() + " edge number: " + split2.inputGraph.edgeSet().size() + "\n").getBytes());
            infer.setDetectionSize(detectionSize);
            infer.setSeedSources(seedSources);
            start = System.currentTimeMillis();
            infer.calculateWeights_ML(true, 3);
            end = System.currentTimeMillis();
            System.out.println("Weight computation time:" + (double)(end - start) / 1000.0);
            infer.initialReputation(highRP, midRP, lowRP);
            start = System.currentTimeMillis();
            infer.PageRankIteration2(highRP, midRP, lowRP, detection);
            end = System.currentTimeMillis();
            double timeCost = (double)(end - start) * 1.0 / 1000.0;
            System.out.println("Reputation propergation time is: " + timeCost);
            infer.removeIrrelaventVertices(detection);
            infer.exportGraph(resultDir + "Weight_" + filename + suffix);
            for (String[] v : infer.graph.vertexSet()) {
                if (!v.getSignature().equals(detection)) continue;
                os.write(("POI Reputation: " + v.getReputation()).getBytes());
                break;
            }
            HashSet<String> criticalNodes = new HashSet<String>();
            for (String edge : criticalEdges) {
                criticalNodes.add(edge.split(",")[0]);
                criticalNodes.add(edge.split(",")[1]);
            }
            int roots = 0;
            for (EntityNode n : infer.graph.vertexSet()) {
                if (infer.graph.incomingEdgesOf(n).size() != 0) continue;
                ++roots;
            }
            double totalCriticalWeights = 0.0;
            double totalNonCriticalWeights = 0.0;
            double totalCriticalRep = 0.0;
            double totalNonCriticalRep = 0.0;
            int criticalEdgeCount = 0;
            int criticalNodeCount = 0;
            HashSet<String> criticalEdgeSet = new HashSet<String>(Arrays.asList(criticalEdges));
            for (EventEdge e : infer.graph.edgeSet()) {
                if (criticalEdgeSet.contains(e.getSource().getSignature() + "," + e.getSink().getSignature())) {
                    ++criticalEdgeCount;
                    totalCriticalWeights += e.weight;
                    continue;
                }
                totalNonCriticalWeights += e.weight;
            }
            for (EntityNode n : infer.graph.vertexSet()) {
                if (criticalNodes.contains(n.getSignature())) {
                    ++criticalNodeCount;
                    totalCriticalRep += n.reputation;
                    continue;
                }
                totalNonCriticalRep += n.reputation;
            }
            System.out.println("entries:" + roots);
            System.out.println("#critical edges:" + criticalEdgeCount);
            System.out.println("#non-critical edges:" + (infer.graph.edgeSet().size() - criticalEdgeCount));
            System.out.println("#critical nodes:" + criticalNodeCount);
            System.out.println("#non-critical nodes:" + (infer.graph.vertexSet().size() - criticalNodeCount));
            System.out.println("avg critical edge weight:" + totalCriticalWeights / (double)criticalEdgeCount);
            System.out.println("avg non-critical edge weight:" + totalNonCriticalWeights / (double)(infer.graph.edgeSet().size() - criticalEdgeCount));
            System.out.println("avg critical node rep:" + totalCriticalRep / (double)criticalNodeCount);
            System.out.println("avg non-critical node rep:" + totalNonCriticalRep / (double)(infer.graph.vertexSet().size() - criticalNodeCount));
            Runtime rt = Runtime.getRuntime();
            String[] cmd = new String[]{"/bin/sh", "-c", "dot -T svg " + resultDir + "AfterCPR_" + filename + suffix + ".dot > " + resultDir + "AfterCPR_" + filename + suffix + ".svg"};
            rt.exec(cmd);
            cmd = new String[]{"/bin/sh", "-c", "dot -T svg " + resultDir + "Weight_" + filename + suffix + ".dot > " + resultDir + "Weight_" + filename + suffix + ".svg"};
            rt.exec(cmd);
            double avg = infer.getAvgWeight();
            double minCritical = 0.05;
            double startToMiss = minCritical / avg;
            if (1.0 - minCritical > 1.0E-8) {
                infer.filterGraphBasedOnAverageWeight(minCritical);
                infer.removeIsolatedIslands(detection);
            }
            infer.exportGraph(resultDir + "Filter_" + filename + suffix);
            System.out.println(String.format("critical edge number: %d", criticalEdges.length));
            os.write(String.format("\ncritical edge number: %d", criticalEdges.length).getBytes());
            System.out.println(String.format("minimum critical weight: %.3f", minCritical));
            os.write(String.format("\nminimum critical weight: %.3f", minCritical).getBytes());
            System.out.println(String.format("start to miss: %.3f", startToMiss));
            os.write(String.format("\nstart to miss: %.3f", startToMiss).getBytes());
            System.out.println("After Filter vertex number is: " + infer.graph.vertexSet().size() + " edge number: " + infer.graph.edgeSet().size());
            os.write(String.format("\nAfter Filter vertex number is: %d edge number: %d", infer.graph.vertexSet().size(), infer.graph.edgeSet().size()).getBytes());
            cmd = new String[]{"/bin/sh", "-c", "dot -T svg " + resultDir + "Filter_" + filename + suffix + ".dot > " + resultDir + "Filter_" + filename + suffix + ".svg"};
            rt.exec(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

