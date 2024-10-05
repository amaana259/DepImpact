/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jgrapht.graph.DirectedPseudograph;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import pagerank.BackTrack;
import pagerank.BackwardPropagate;
import pagerank.CausalityPreserve;
import pagerank.EntityNode;
import pagerank.EventEdge;
import pagerank.GetGraph;
import pagerank.GraphSplit;
import pagerank.InferenceReputation;
import pagerank.IterateGraph;

public class ProcessOneLogTest {
    static OutputStream os = null;
    public static int topStarts = 3;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void process(String resultDir, String suffix, double threshold, boolean trackOrigin, String logfile, String[] IP, String detection, String[] highRP, String[] midRP, String[] lowRP, String filename, double detectionSize, Set<String> seedSources, String[] criticalEdges, String mode, String do_split) {
        Object weightfile = null;
        try {
            os = new FileOutputStream(resultDir + filename + suffix + "_stats");
            GetGraph getGraph = new GetGraph(logfile, IP);
            long startTime = System.currentTimeMillis();
            getGraph.GenerateGraph();
            DirectedPseudograph<EntityNode, EventEdge> orignal = getGraph.getJg();
            System.out.println("Original vertex number:" + orignal.vertexSet().size() + " edge number : " + orignal.edgeSet().size());
            os.write(("Original vertex number:" + orignal.vertexSet().size() + " edge number : " + orignal.edgeSet().size() + "\n").getBytes());
            long endTime = System.currentTimeMillis();
            double timeCost = ProcessOneLogTest.getTimeCost(startTime, endTime);
            System.out.println("Build Original Graph time cost is: " + timeCost);
            os.write(("Build Original Graph time cost is: " + timeCost + "\n").getBytes());
            ProcessOneLogTest.run_exp(orignal, resultDir, suffix, threshold, trackOrigin, logfile, IP, detection, highRP, midRP, lowRP, filename, detectionSize, seedSources, criticalEdges, mode, do_split);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void process_backward(String resultDir, String suffix, double threshold, boolean trackOrigin, String logfile, String[] IP, String detection, String[] highRP, String[] midRP, String[] lowRP, String filename, double detectionSize, Set<String> seedSources, String[] criticalEdges, String mode) {
        Object weightfile = null;
        try {
            os = new FileOutputStream(resultDir + filename + suffix + "_stats");
            GetGraph getGraph = new GetGraph(logfile, IP);
            long startTime = System.currentTimeMillis();
            getGraph.GenerateGraph();
            DirectedPseudograph<EntityNode, EventEdge> orignal = getGraph.getJg();
            System.out.println("Original vertex number:" + orignal.vertexSet().size() + " edge number : " + orignal.edgeSet().size());
            os.write(("Original vertex number:" + orignal.vertexSet().size() + " edge number : " + orignal.edgeSet().size() + "\n").getBytes());
            long endTime = System.currentTimeMillis();
            double timeCost = ProcessOneLogTest.getTimeCost(startTime, endTime);
            System.out.println("Build Original Graph time cost is: " + timeCost);
            os.write(("Build Original Graph time cost is: " + timeCost + "\n").getBytes());
            ProcessOneLogTest.run_exp_backward_test(orignal, resultDir, suffix, threshold, trackOrigin, logfile, IP, detection, highRP, midRP, lowRP, filename, detectionSize, seedSources, criticalEdges, mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void run_exp_backward_test(DirectedPseudograph<EntityNode, EventEdge> orignal, String resultDir, String suffix, double threshold, boolean trackOrigin, String logfile, String[] IP, String detection, String[] highRP, String[] midRP, String[] lowRP, String filename, double detectionSize, Set<String> seedSources, String[] criticalEdges, String mode) {
        Object weightfile = null;
        try {
            os = new FileOutputStream(resultDir + filename + suffix + "_stats");
            long start = System.currentTimeMillis();
            BackTrack backTrack = new BackTrack(orignal);
            backTrack.backTrackPOIEvent(detection);
            long end = System.currentTimeMillis();
            double timeCost = ProcessOneLogTest.getTimeCost(start, end);
            System.out.println("BackTrack time cost is: " + timeCost);
            os.write(("BackTrack time cost is: " + timeCost + "\n").getBytes());
            System.out.println("After Backtrack vertex number is: " + backTrack.afterBackTrack.vertexSet().size() + " edge number: " + backTrack.afterBackTrack.edgeSet().size());
            os.write(("After Backtrack vertex number is: " + backTrack.afterBackTrack.vertexSet().size() + " edge number: " + backTrack.afterBackTrack.edgeSet().size() + "\n").getBytes());
            IterateGraph out = new IterateGraph(backTrack.afterBackTrack);
            out.exportGraph(resultDir + "BackTrack_" + filename + suffix);
            CausalityPreserve CPR = new CausalityPreserve(backTrack.afterBackTrack);
            start = System.currentTimeMillis();
            double timeWindow = 10.0;
            CPR.mergeEdgeFallInTheRange2(timeWindow);
            System.out.println("The size of time window is :" + timeWindow + "(s)");
            end = System.currentTimeMillis();
            timeCost = ProcessOneLogTest.getTimeCost(start, end);
            System.out.println("Edge Merge cost is: " + timeCost);
            os.write(("Edge Merge cost is: " + timeCost + "\n").getBytes());
            System.out.println("After CPR vertex number is: " + CPR.afterMerge.vertexSet().size() + " edge number: " + CPR.afterMerge.edgeSet().size());
            os.write(("After CPR vertex number is: " + CPR.afterMerge.vertexSet().size() + " edge number: " + CPR.afterMerge.edgeSet().size() + "\n").getBytes());
            out = new IterateGraph(CPR.afterMerge);
            out.exportGraph(resultDir + "AfterCPR_" + filename + suffix);
            BackwardPropagate infer = new BackwardPropagate(CPR.afterMerge);
            infer.setDetectionSize(detectionSize);
            infer.setSeedSources(seedSources);
            start = System.currentTimeMillis();
            switch (mode) {
                case "nonml": {
                    infer.calculateWeights();
                    break;
                }
                case "clusterall": {
                    infer.calculateWeights_ML_dec(true, 1);
                    break;
                }
                case "nonoutlier": {
                    infer.calculateWeights_ML_dec(true, 2);
                    break;
                }
                case "clusterlocal": {
                    infer.calculateWeights_ML_dec(true, 3);
                    break;
                }
                case "localtime": {
                    infer.calculateWeights_Individual(true, "timeWeight");
                    break;
                }
                case "localamount": {
                    infer.calculateWeights_Individual(true, "amountWeight");
                    break;
                }
                case "localstruct": {
                    infer.calculateWeights_Individual(true, "structureWeight");
                    break;
                }
                case "fanout": {
                    infer.calculateWeights_Fanout(true);
                    break;
                }
                default: {
                    System.out.println("Unknown mode: " + mode);
                }
            }
            end = System.currentTimeMillis();
            timeCost = ProcessOneLogTest.getTimeCost(start, end);
            String timeCostInfo = String.format("Weight Calculation (%s) time cost is: ", mode) + timeCost + "\n";
            System.out.println(timeCostInfo);
            os.write(timeCostInfo.getBytes());
            infer.initialReputation(highRP, lowRP);
            start = System.currentTimeMillis();
            infer.PageRankIterationBackward(highRP, midRP, lowRP, detection);
            end = System.currentTimeMillis();
            timeCost = ProcessOneLogTest.getTimeCost(start, end);
            timeCostInfo = "Propagation time cost is: " + timeCost + "\n";
            System.out.println(timeCostInfo);
            os.write(timeCostInfo.getBytes());
            infer.exportGraph(resultDir + "Weight_" + filename + suffix);
            List<List<String>> forwardStarts = infer.getForwardStarts();
            Map<String, Double> nodeReputation = IterateGraph.getNodeReputation(infer.graph);
            IterateGraph.outputTopStarts(resultDir, forwardStarts, nodeReputation);
            int startsNum = 0;
            for (List<String> starts : forwardStarts) {
                for (int r = 0; r < topStarts && r < starts.size(); ++r) {
                    DirectedPseudograph<EntityNode, EventEdge> backresFilteredByForwad = infer.combineBackwardAndForwardForGivenStart(starts.get(r), orignal);
                    out = new IterateGraph(backresFilteredByForwad);
                    out.exportGraph(resultDir + "filtered_by_forward_" + String.valueOf(startsNum) + filename + suffix);
                    ++startsNum;
                }
            }
            List<String> entryPoints = IterateGraph.getCandidateEntryPoint(infer.graph);
            JSONObject entryJson = new JSONObject();
            entryJson.put("EntryPointsNumber", entryPoints.size());
            JSONArray ponintsJson = new JSONArray();
            entryPoints.stream().forEach(s2 -> ponintsJson.add(s2));
            entryJson.put("EntryPoints", ponintsJson);
            File entryPointsJsonFile = new File(resultDir + filename + suffix + "entry_points.json");
            FileWriter jsonWriter = new FileWriter(entryPointsJsonFile);
            jsonWriter.write(entryJson.toJSONString());
            jsonWriter.close();
            IterateGraph.printEdgeByWeights(filename, infer.graph);
            IterateGraph debugger = new IterateGraph(infer.graph);
            ArrayList<String> nodes = new ArrayList<String>();
            nodes.add("7009python3");
            nodes.add("7047python3");
            Runtime rt = Runtime.getRuntime();
            String[] cmd = new String[]{"/bin/sh", "-c", "dot -T svg " + resultDir + "AfterCPR_" + filename + suffix + ".dot > " + resultDir + "AfterCPR_" + filename + suffix + ".svg"};
            rt.exec(cmd);
            cmd = new String[]{"/bin/sh", "-c", "dot -T svg " + resultDir + "Weight_" + filename + suffix + ".dot > " + resultDir + "Weight_" + filename + suffix + ".svg"};
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void run_exp(DirectedPseudograph<EntityNode, EventEdge> orignal, String resultDir, String suffix, double threshold, boolean trackOrigin, String logfile, String[] IP, String detection, String[] highRP, String[] midRP, String[] lowRP, String filename, double detectionSize, Set<String> seedSources, String[] criticalEdges, String mode, String do_split) {
        Object weightfile = null;
        try {
            os = new FileOutputStream(resultDir + filename + suffix + "_stats");
            long start = System.currentTimeMillis();
            BackTrack backTrack = new BackTrack(orignal);
            backTrack.backTrackPOIEvent(detection);
            long end = System.currentTimeMillis();
            double timeCost = ProcessOneLogTest.getTimeCost(start, end);
            System.out.println("BackTrack time cost is: " + timeCost);
            os.write(("BackTrack time cost is: " + timeCost + "\n").getBytes());
            System.out.println("After Backtrack vertex number is: " + backTrack.afterBackTrack.vertexSet().size() + " edge number: " + backTrack.afterBackTrack.edgeSet().size());
            os.write(("After Backtrack vertex number is: " + backTrack.afterBackTrack.vertexSet().size() + " edge number: " + backTrack.afterBackTrack.edgeSet().size() + "\n").getBytes());
            IterateGraph out = new IterateGraph(backTrack.afterBackTrack);
            out.exportGraph(resultDir + "BackTrack_" + filename + suffix);
            CausalityPreserve CPR = new CausalityPreserve(backTrack.afterBackTrack);
            start = System.currentTimeMillis();
            double timeWindow = 10.0;
            CPR.mergeEdgeFallInTheRange2(timeWindow);
            System.out.println("The size of time window is :" + timeWindow + "(s)");
            end = System.currentTimeMillis();
            timeCost = ProcessOneLogTest.getTimeCost(start, end);
            System.out.println("Edge Merge cost is: " + timeCost);
            os.write(("Edge Merge cost is: " + timeCost + "\n").getBytes());
            System.out.println("After CPR vertex number is: " + CPR.afterMerge.vertexSet().size() + " edge number: " + CPR.afterMerge.edgeSet().size());
            os.write(("After CPR vertex number is: " + CPR.afterMerge.vertexSet().size() + " edge number: " + CPR.afterMerge.edgeSet().size() + "\n").getBytes());
            out = new IterateGraph(CPR.afterMerge);
            out.exportGraph(resultDir + "AfterCPR_" + filename + suffix);
            GraphSplit split2 = new GraphSplit(CPR.afterMerge);
            start = System.currentTimeMillis();
            if (do_split.equals("split")) {
                split2.splitGraph();
            }
            end = System.currentTimeMillis();
            timeCost = ProcessOneLogTest.getTimeCost(start, end);
            System.out.println("Node split time cost is: " + timeCost);
            os.write(("Node split time cost is: " + timeCost + "\n").getBytes());
            System.out.println("After Split vertex number is: " + split2.inputGraph.vertexSet().size() + " edge number: " + split2.inputGraph.edgeSet().size());
            InferenceReputation infer = new InferenceReputation(split2.inputGraph);
            os.write(("After Split vertex number is: " + split2.inputGraph.vertexSet().size() + " edge number: " + split2.inputGraph.edgeSet().size() + "\n").getBytes());
            infer.setDetectionSize(detectionSize);
            infer.setSeedSources(seedSources);
            start = System.currentTimeMillis();
            switch (mode) {
                case "nonml": {
                    infer.calculateWeights();
                    break;
                }
                case "clusterall": {
                    infer.calculateWeights_ML(true, 1);
                    break;
                }
                case "nonoutlier": {
                    infer.calculateWeights_ML(true, 2);
                    break;
                }
                case "clusterlocal": {
                    infer.calculateWeights_ML(true, 3);
                    break;
                }
                case "localtime": {
                    infer.calculateWeights_Individual(true, "timeWeight");
                    break;
                }
                case "localamount": {
                    infer.calculateWeights_Individual(true, "amountWeight");
                    break;
                }
                case "localstruct": {
                    infer.calculateWeights_Individual(true, "structureWeight");
                    break;
                }
                case "fanout": {
                    break;
                }
                default: {
                    System.out.println("Unknown mode: " + mode);
                }
            }
            end = System.currentTimeMillis();
            timeCost = ProcessOneLogTest.getTimeCost(start, end);
            String timeCostInfo = String.format("Weight Calculation (%s) time cost is: ", mode) + timeCost + "\n";
            System.out.println(timeCostInfo);
            os.write(timeCostInfo.getBytes());
            infer.initialReputation(highRP, midRP, lowRP);
            start = System.currentTimeMillis();
            infer.PageRankIteration2(highRP, midRP, lowRP, detection);
            end = System.currentTimeMillis();
            timeCost = ProcessOneLogTest.getTimeCost(start, end);
            timeCostInfo = "Propagation time cost is: " + timeCost + "\n";
            System.out.println(timeCostInfo);
            os.write(timeCostInfo.getBytes());
            infer.exportGraph(resultDir + "Weight_" + filename + suffix);
            for (EntityNode v : infer.graph.vertexSet()) {
                if (!v.getSignature().equals(detection)) continue;
                os.write(("POI Reputation: " + v.getReputation()).getBytes());
                break;
            }
            Runtime rt = Runtime.getRuntime();
            String[] cmd = new String[]{"/bin/sh", "-c", "dot -T svg " + resultDir + "AfterCPR_" + filename + suffix + ".dot > " + resultDir + "AfterCPR_" + filename + suffix + ".svg"};
            rt.exec(cmd);
            cmd = new String[]{"/bin/sh", "-c", "dot -T svg " + resultDir + "Weight_" + filename + suffix + ".dot > " + resultDir + "Weight_" + filename + suffix + ".svg"};
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

    public static double getTimeCost(long start, long end) {
        return (double)(end - start) * 1.0 / 1000.0;
    }
}

