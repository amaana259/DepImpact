/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.graph.DirectedPseudograph;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import pagerank.BackTrack;
import pagerank.BackwardPropagate_pf;
import pagerank.CausalityPreserve;
import pagerank.EntityNode;
import pagerank.EventEdge;
import pagerank.Experiment;
import pagerank.GetGraph;
import pagerank.IterateGraph;
import pagerank.NODOZE;
import pagerank.NodozeDarpa;

public class ProcessOneLogCMD_19 {
    static OutputStream os = null;
    public static int topStarts = 3;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void process_backward(String resultDir, String suffix, double threshold, boolean trackOrigin, String logfile, String[] IP, String detection, String[] highRP, String[] midRP, String[] lowRP, String filename, double detectionSize, Set<String> seedSources, String[] criticalEdges, String mode, JSONObject jsonLog) {
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
            double timeCost = ProcessOneLogCMD_19.getTimeCost(startTime, endTime);
            System.out.println("Build Original Graph time cost is: " + timeCost);
            os.write(("Build Original Graph time cost is: " + timeCost + "\n").getBytes());
            jsonLog.put("origionVertexNumber", orignal.vertexSet().size());
            jsonLog.put("origionEdgeNumber", orignal.edgeSet().size());
            jsonLog.put("CostForOrigionGraph", timeCost);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void run_exp_backward_without_backtrack(DirectedPseudograph<EntityNode, EventEdge> backtrack, String resultDir, String suffix, double threshold, boolean trackOrigin, String logfile, String[] IP, String detection, String[] highRP, String[] midRP, String[] lowRP, String filename, double detectionSize, Set<String> seedSources, String[] criticalEdges, String mode, JSONObject jsonlog, String[] importantEntries, Experiment exp) {
        Object weightfile = null;
        try {
            os = new FileOutputStream(resultDir + filename + suffix + "_stats");
            long start = System.currentTimeMillis();
            long end = System.currentTimeMillis();
            os.write(String.format("Backtrack V: %d E: %d", backtrack.vertexSet().size(), backtrack.edgeSet().size()).getBytes());
            double timeCost = ProcessOneLogCMD_19.getTimeCost(start, end);
            CausalityPreserve CPR = new CausalityPreserve(backtrack);
            start = System.currentTimeMillis();
            double timeWindow = 10.0;
            CPR.mergeEdgeFallInTheRange2(timeWindow);
            System.out.println("The size of time window is :" + timeWindow + "(s)");
            end = System.currentTimeMillis();
            timeCost = ProcessOneLogCMD_19.getTimeCost(start, end);
            System.out.println("Edge Merge cost is: " + timeCost);
            os.write(("Edge Merge cost is: " + timeCost + "\n").getBytes());
            System.out.println("After CPR vertex number is: " + CPR.afterMerge.vertexSet().size() + " edge number: " + CPR.afterMerge.edgeSet().size());
            os.write(("After CPR vertex number is: " + CPR.afterMerge.vertexSet().size() + " edge number: " + CPR.afterMerge.edgeSet().size() + "\n").getBytes());
            ProcessOneLogCMD_19.putToJsonLog(jsonlog, "CPRVertexNumber", String.valueOf(CPR.afterMerge.vertexSet().size()));
            ProcessOneLogCMD_19.putToJsonLog(jsonlog, "CPREdgeNumber", String.valueOf(CPR.afterMerge.edgeSet().size()));
            ProcessOneLogCMD_19.putToJsonLog(jsonlog, "CPRTimeCost", String.valueOf(timeCost));
            IterateGraph out = new IterateGraph(CPR.afterMerge);
            out.exportGraph(resultDir + "AfterCPR_" + filename + suffix);
            BackwardPropagate_pf infer = new BackwardPropagate_pf(CPR.afterMerge);
            infer.setDetectionSize(detectionSize);
            infer.setSeedSources(seedSources);
            start = System.currentTimeMillis();
            switch (mode) {
                case "nonml": {
                    infer.calculateWeights();
                    break;
                }
                case "clusterall": {
                    infer.calculateWeights_ML_dec(true, 1, resultDir);
                    break;
                }
                case "nonoutlier": {
                    infer.calculateWeights_ML_dec(true, 2, resultDir);
                    break;
                }
                case "clusterlocal_dec": {
                    infer.calculateWeights_ML_dec(true, 3, resultDir);
                    break;
                }
                case "clusterlocal": {
                    infer.calculateWeights_ML_dec(true, 3, resultDir);
                    break;
                }
                case "localtime": {
                    infer.calculateWeights_Individual(true, "timeWeight", resultDir);
                    break;
                }
                case "localamount": {
                    infer.calculateWeights_Individual(true, "amountWeight", resultDir);
                    break;
                }
                case "localstruct": {
                    infer.calculateWeights_Individual(true, "structureWeight", resultDir);
                    break;
                }
                case "fanout": {
                    infer.calculateWeights_Fanout(true, resultDir);
                    break;
                }
                case "nonmlrandom": {
                    infer.calculateWeightsRandom();
                    break;
                }
                case "nodoze": {
                    ArrayList<String> fileMalicious = new ArrayList<String>();
                    fileMalicious.add(detection);
                    ArrayList ipMalicious = new ArrayList();
                    NodozeDarpa nodoze = new NodozeDarpa(exp.frequencyPath, exp.idAndNamePath, fileMalicious, backtrack, exp.POI, importantEntries);
                    long nodozeStart = System.currentTimeMillis();
                    int nodozeRes = nodoze.filterExp(backtrack);
                    long nodozeEnd = System.currentTimeMillis();
                    long nodozeTimeCost = nodozeEnd - nodozeStart;
                    File nodozeResFile = new File(resultDir + "/nodoze.txt");
                    FileWriter fileWriter = new FileWriter(nodozeResFile);
                    fileWriter.write("Nodoze Res:" + String.valueOf(nodozeRes) + "\n");
                    fileWriter.write("Nodoze time: " + String.valueOf(nodozeTimeCost));
                    System.out.println("Size of Nodoze Res: " + String.valueOf(nodozeRes));
                    System.out.println("Time of Nodoze: " + String.valueOf(nodozeTimeCost));
                    fileWriter.close();
                    return;
                }
                case "read_only": {
                    System.out.println("The mode is read_only!");
                    Map<String, Integer> res = infer.graphSizeWithoutReadonly();
                    File read_onlyRES = new File(resultDir + "/readOnly.txt");
                    FileWriter readOnlyFileWriter = new FileWriter(read_onlyRES);
                    for (String k : res.keySet()) {
                        readOnlyFileWriter.write(String.format("%s:%d", k, res.get(k)));
                    }
                    readOnlyFileWriter.close();
                    return;
                }
                default: {
                    System.out.println("Unknown mode: " + mode);
                }
            }
            end = System.currentTimeMillis();
            timeCost = ProcessOneLogCMD_19.getTimeCost(start, end);
            String timeCostInfo = String.format("Weight Calculation (%s) time cost is: ", mode) + timeCost + "\n";
            System.out.println(timeCostInfo);
            os.write(timeCostInfo.getBytes());
            ProcessOneLogCMD_19.putToJsonLog(jsonlog, "WeightCalculationTimeCost", String.valueOf(timeCost));
            ArrayList<String> skipmode = new ArrayList<String>();
            skipmode.add("fanout");
            if (!skipmode.contains(mode)) {
                infer.initialReputation(highRP, lowRP);
                start = System.currentTimeMillis();
                infer.PageRankIterationBackward(highRP, midRP, lowRP, detection);
                end = System.currentTimeMillis();
                timeCost = ProcessOneLogCMD_19.getTimeCost(start, end);
                timeCostInfo = "Propagation time cost is: " + timeCost + "\n";
                System.out.println(timeCostInfo);
                os.write(timeCostInfo.getBytes());
                ProcessOneLogCMD_19.putToJsonLog(jsonlog, "PropagationTimeCost", String.valueOf(timeCost));
                infer.exportGraph(resultDir + "Weight_" + filename + suffix);
                List<List<String>> forwardStarts = infer.getForwardStartsWithSkipAndMust(exp.skipEntry, exp.mustEntry);
                double randomAverageRank = ProcessOneLogCMD_19.getAverageRankForRandomMethod(forwardStarts, exp.entries);
                String randomRankInfo = "The average rank of important entries in random method is :" + String.valueOf(randomAverageRank);
                os.write(randomRankInfo.getBytes());
                List<String> nodesignatures = infer.graph.vertexSet().stream().map(v -> v.getSignature()).collect(Collectors.toList());
                List<String> randomStarts = IterateGraph.getRandomStarts(nodesignatures, 3);
                List<List<String>> randomStartsCategory = IterateGraph.randomPickEntryStartsBasedOnCategory(forwardStarts);
                Map<String, Double> nodeReputation = IterateGraph.getNodeReputation(infer.graph);
                IterateGraph.outputTopStarts(resultDir, forwardStarts, nodeReputation);
                ProcessOneLogCMD_19.outputStartReputation(forwardStarts, infer.graph, resultDir);
                double rankOfImportantentry = ProcessOneLogCMD_19.getAverageRankForImportantEntry(forwardStarts, nodeReputation, exp.entries);
                String rankRes = "The average rank important entries processed by method is: " + String.valueOf(rankOfImportantentry);
                os.write(rankRes.getBytes());
                boolean outputFilterGraph = true;
                ProcessOneLogCMD_19.filter_graph_by_forward_category(forwardStarts, backtrack, "sysrep", resultDir, filename, suffix, "1", 3, infer, outputFilterGraph);
                outputFilterGraph = true;
                List<String> entryPoints = IterateGraph.getCandidateEntryPoint(infer.graph);
                JSONObject entryJson = new JSONObject();
                entryJson.put("EntryPointsNumber", entryPoints.size());
                ProcessOneLogCMD_19.putToJsonLog(jsonlog, "EntryPointsNumber", String.valueOf(entryPoints.size()));
                JSONArray ponintsJson = new JSONArray();
                entryPoints.stream().forEach(s2 -> ponintsJson.add(s2));
                entryJson.put("EntryPoints", ponintsJson);
                File entryPointsJsonFile = new File(resultDir + filename + suffix + "_entry_points.json");
                FileWriter jsonWriter = new FileWriter(entryPointsJsonFile);
                jsonWriter.write(entryJson.toJSONString());
                jsonWriter.close();
            }
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
    public static void run_exp_backward(DirectedPseudograph<EntityNode, EventEdge> orignal, String resultDir, String suffix, double threshold, boolean trackOrigin, String logfile, String[] IP, String detection, String[] highRP, String[] midRP, String[] lowRP, String filename, double detectionSize, Set<String> seedSources, String[] criticalEdges, String mode, JSONObject jsonlog, String[] importantEntries, Experiment exp) {
        Object weightfile = null;
        try {
            os = new FileOutputStream(resultDir + filename + suffix + "_stats");
            long start = System.currentTimeMillis();
            BackTrack backTrack = new BackTrack(orignal);
            backTrack.backTrackPOIEvent(detection);
            long end = System.currentTimeMillis();
            double timeCost = ProcessOneLogCMD_19.getTimeCost(start, end);
            System.out.println("BackTrack time cost is: " + timeCost);
            os.write(("BackTrack time cost is: " + timeCost + "\n").getBytes());
            System.out.println("After Backtrack vertex number is: " + backTrack.afterBackTrack.vertexSet().size() + " edge number: " + backTrack.afterBackTrack.edgeSet().size());
            os.write(("After Backtrack vertex number is: " + backTrack.afterBackTrack.vertexSet().size() + " edge number: " + backTrack.afterBackTrack.edgeSet().size() + "\n").getBytes());
            jsonlog.put("BackTrackVertexNumber", backTrack.afterBackTrack.vertexSet().size());
            jsonlog.put("BackTrackEdgeNumber", backTrack.afterBackTrack.edgeSet().size());
            jsonlog.put("BackTrackTimeCost", timeCost);
            IterateGraph out = new IterateGraph(backTrack.afterBackTrack);
            for (EntityNode v2 : backTrack.afterBackTrack.vertexSet()) {
                if (!v2.getSignature().equals("/tmp/passwords.tar.bz2")) continue;
                System.out.println("The node is in the graph in backtrack.");
                System.out.println(v2.getSignature());
            }
            out.exportGraph(resultDir + "BackTrackTest_" + filename + suffix);
            out.exportGraph(resultDir + "BackTrack_" + filename + suffix);
            CausalityPreserve CPR = new CausalityPreserve(backTrack.afterBackTrack);
            start = System.currentTimeMillis();
            double timeWindow = 10.0;
            CPR.mergeEdgeFallInTheRange2(timeWindow);
            System.out.println("The size of time window is :" + timeWindow + "(s)");
            end = System.currentTimeMillis();
            timeCost = ProcessOneLogCMD_19.getTimeCost(start, end);
            System.out.println("Edge Merge cost is: " + timeCost);
            os.write(("Edge Merge cost is: " + timeCost + "\n").getBytes());
            System.out.println("After CPR vertex number is: " + CPR.afterMerge.vertexSet().size() + " edge number: " + CPR.afterMerge.edgeSet().size());
            os.write(("After CPR vertex number is: " + CPR.afterMerge.vertexSet().size() + " edge number: " + CPR.afterMerge.edgeSet().size() + "\n").getBytes());
            ProcessOneLogCMD_19.putToJsonLog(jsonlog, "CPRVertexNumber", String.valueOf(CPR.afterMerge.vertexSet().size()));
            ProcessOneLogCMD_19.putToJsonLog(jsonlog, "CPREdgeNumber", String.valueOf(CPR.afterMerge.edgeSet().size()));
            ProcessOneLogCMD_19.putToJsonLog(jsonlog, "CPRTimeCost", String.valueOf(timeCost));
            out = new IterateGraph(CPR.afterMerge);
            out.exportGraph(resultDir + "AfterCPR_" + filename + suffix);
            BackwardPropagate_pf infer = new BackwardPropagate_pf(CPR.afterMerge);
            infer.setDetectionSize(detectionSize);
            infer.setSeedSources(seedSources);
            start = System.currentTimeMillis();
            switch (mode) {
                case "nonml": {
                    infer.calculateWeights();
                    break;
                }
                case "clusterall": {
                    infer.calculateWeights_ML_dec(true, 1, resultDir);
                    break;
                }
                case "nonoutlier": {
                    infer.calculateWeights_ML_dec(true, 2, resultDir);
                    break;
                }
                case "clusterlocal_dec": {
                    infer.calculateWeights_ML_dec(true, 3, resultDir);
                    break;
                }
                case "clusterlocal": {
                    infer.calculateWeights_ML_dec(true, 3, resultDir);
                    break;
                }
                case "localtime": {
                    infer.calculateWeights_Individual(true, "timeWeight", resultDir);
                    break;
                }
                case "localamount": {
                    infer.calculateWeights_Individual(true, "amountWeight", resultDir);
                    break;
                }
                case "localstruct": {
                    infer.calculateWeights_Individual(true, "structureWeight", resultDir);
                    break;
                }
                case "fanout": {
                    infer.calculateWeights_Fanout(true, resultDir);
                    break;
                }
                case "nonmlrandom": {
                    infer.calculateWeightsRandom();
                    break;
                }
                case "nodoze": {
                    ArrayList<String> fileMalicious = new ArrayList<String>();
                    fileMalicious.add(detection);
                    ArrayList<String> ipMalicious = new ArrayList<String>();
                    NODOZE nodoze = new NODOZE(fileMalicious, ipMalicious, orignal, backTrack.afterBackTrack, CPR.afterMerge, detection, importantEntries);
                    long nodozeStart = System.currentTimeMillis();
                    int nodozeRes = nodoze.filterExp();
                    long nodozeEnd = System.currentTimeMillis();
                    long nodozeTimeCost = nodozeEnd - nodozeStart;
                    File nodozeResFile = new File(resultDir + "/nodoze.txt");
                    FileWriter fileWriter = new FileWriter(nodozeResFile);
                    fileWriter.write("Nodoze Res:" + String.valueOf(nodozeRes) + "\n");
                    fileWriter.write("Nodoze time: " + String.valueOf(nodozeTimeCost));
                    System.out.println("Size of Nodoze Res: " + String.valueOf(nodozeRes));
                    System.out.println("Time of Nodoze: " + String.valueOf(nodozeTimeCost));
                    fileWriter.close();
                    return;
                }
                case "read_only": {
                    Map<String, Integer> res = infer.graphSizeWithoutReadonly();
                    File read_onlyRES = new File(resultDir + "/readOnly.txt");
                    FileWriter readOnlyFileWriter = new FileWriter(read_onlyRES);
                    for (String k : res.keySet()) {
                        readOnlyFileWriter.write(String.format("%s:%d", k, res.get(k)));
                    }
                    readOnlyFileWriter.close();
                }
                default: {
                    System.out.println("Unknown mode: " + mode);
                }
            }
            end = System.currentTimeMillis();
            timeCost = ProcessOneLogCMD_19.getTimeCost(start, end);
            String timeCostInfo = String.format("Weight Calculation (%s) time cost is: ", mode) + timeCost + "\n";
            System.out.println(timeCostInfo);
            os.write(timeCostInfo.getBytes());
            ProcessOneLogCMD_19.putToJsonLog(jsonlog, "WeightCalculationTimeCost", String.valueOf(timeCost));
            ArrayList<String> skipmode = new ArrayList<String>();
            skipmode.add("fanout");
            ArrayList<String> modeCanFilter = new ArrayList<String>();
            modeCanFilter.add("clusterall");
            if (!skipmode.contains(mode)) {
                infer.initialReputation(highRP, lowRP);
                start = System.currentTimeMillis();
                infer.PageRankIterationBackward(highRP, midRP, lowRP, detection);
                end = System.currentTimeMillis();
                timeCost = ProcessOneLogCMD_19.getTimeCost(start, end);
                timeCostInfo = "Propagation time cost is: " + timeCost + "\n";
                System.out.println(timeCostInfo);
                os.write(timeCostInfo.getBytes());
                ProcessOneLogCMD_19.putToJsonLog(jsonlog, "PropagationTimeCost", String.valueOf(timeCost));
                System.out.println();
                DirectedPseudograph<EntityNode, EventEdge> graphAfterBack = infer.graph;
                for (EntityNode v3 : graphAfterBack.vertexSet()) {
                    if (!v3.getSignature().equals("/tmp/passwords.tar.bz2")) continue;
                    System.out.println("The node is in the graph in infer");
                    System.out.println(v3.getSignature());
                }
                infer.exportGraph(resultDir + "Weight_" + filename + suffix);
                List<List<String>> forwardStarts = infer.getForwardStartsWithSkipAndMust(exp.skipEntry, exp.mustEntry);
                double randomAverageRank = ProcessOneLogCMD_19.getAverageRankForRandomMethod(forwardStarts, exp.entries);
                String randomRankInfo = "The average rank of important entries in random method is :" + String.valueOf(randomAverageRank);
                os.write(randomRankInfo.getBytes());
                ProcessOneLogCMD_19.outputStartReputation(forwardStarts, infer.graph, resultDir);
                Map<String, Double> nodeReputation = IterateGraph.getNodeReputation(infer.graph);
                double rankOfImportantentry = ProcessOneLogCMD_19.getAverageRankForImportantEntry(forwardStarts, nodeReputation, exp.entries);
                String rankRes = "The average rank important entries processed by method is: " + String.valueOf(rankOfImportantentry);
                os.write(rankRes.getBytes());
                if (modeCanFilter.indexOf(mode) != -1) {
                    List<String> nodesignatures = infer.graph.vertexSet().stream().map(v -> v.getSignature()).collect(Collectors.toList());
                    List<String> randomStarts = IterateGraph.getRandomStarts(nodesignatures, 3);
                    List<List<String>> randomStartsCategory = IterateGraph.randomPickEntryStartsBasedOnCategory(forwardStarts);
                    IterateGraph.outputTopStarts(resultDir, forwardStarts, nodeReputation);
                    boolean outputFilterGraph = true;
                    ProcessOneLogCMD_19.filter_graph_by_forward_category(forwardStarts, orignal, "DepImpact", resultDir, filename, suffix, "1", 3, infer, outputFilterGraph);
                    outputFilterGraph = true;
                    List<String> entryPoints = IterateGraph.getCandidateEntryPoint(infer.graph);
                    JSONObject entryJson = new JSONObject();
                    entryJson.put("EntryPointsNumber", entryPoints.size());
                    ProcessOneLogCMD_19.putToJsonLog(jsonlog, "EntryPointsNumber", String.valueOf(entryPoints.size()));
                    JSONArray ponintsJson = new JSONArray();
                    entryPoints.stream().forEach(s2 -> ponintsJson.add(s2));
                    entryJson.put("EntryPoints", ponintsJson);
                    File entryPointsJsonFile = new File(resultDir + filename + suffix + "_entry_points.json");
                    FileWriter jsonWriter = new FileWriter(entryPointsJsonFile);
                    jsonWriter.write(entryJson.toJSONString());
                    jsonWriter.close();
                }
            }
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

    private static void outputStartReputation(List<List<String>> starts, DirectedPseudograph<EntityNode, EventEdge> graph, String resultDir) {
        HashSet<String> startEntries = new HashSet<String>();
        for (int i = 0; i < starts.size(); ++i) {
            for (int j = 0; j < starts.get(i).size(); ++j) {
                startEntries.add(starts.get(i).get(j));
            }
        }
        JSONObject jsonObject = new JSONObject();
        Set vertexs = graph.vertexSet();
        for (EntityNode v : vertexs) {
            if (!startEntries.contains(v.getSignature())) continue;
            jsonObject.put(v.getSignature(), v.reputation);
        }
        try {
            FileWriter writer = new FileWriter(new File(resultDir + "/sysrep_entry_and_reputation.json"));
            writer.write(jsonObject.toJSONString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double getTimeCost(long start, long end) {
        return (double)(end - start) * 1.0 / 1000.0;
    }

    public static Timestamp getTimeStamp() {
        Calendar calendar = Calendar.getInstance();
        Timestamp currentTimestamp = new Timestamp(calendar.getTime().getTime());
        return currentTimestamp;
    }

    public static void putToJsonLog(JSONObject jsonLog, String key, String value) {
        jsonLog.put(key, value);
    }

    public static void filter_graph_by_forward_category(List<List<String>> starts, DirectedPseudograph<EntityNode, EventEdge> orignal, String method, String resultDir, String filename, String suffix, String time, int startLimitForEachCategory, BackwardPropagate_pf infer, boolean outputGraph) {
        try {
            File folderForFtime;
            File resFolderForFilter = new File(resultDir + "/" + method);
            if (!resFolderForFilter.exists()) {
                resFolderForFilter.mkdir();
            }
            if (!(folderForFtime = new File(resFolderForFilter.getAbsolutePath() + "/" + time)).exists()) {
                folderForFtime.mkdir();
            }
            File recordStarts = new File(folderForFtime.getAbsolutePath() + "/forward_starts_" + filename + "_" + time + "_" + method + ".txt");
            FileWriter fileWriter = new FileWriter(recordStarts);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println("Entry Points for forward:");
            JSONObject json = new JSONObject();
            int startsNum = 0;
            for (List<String> start : starts) {
                for (int r = 0; r < startLimitForEachCategory && r < start.size(); ++r) {
                    printWriter.println(start.get(r));
                    DirectedPseudograph<EntityNode, EventEdge> backresFilteredByForwad = infer.combineBackwardAndForwardForGivenStart(start.get(r), orignal);
                    IterateGraph out = new IterateGraph(backresFilteredByForwad);
                    if (outputGraph) {
                        out.exportGraph(folderForFtime.getAbsolutePath() + "/filtered_by_forward_" + String.valueOf(startsNum) + "_" + filename + "_" + method + suffix);
                    }
                    ++startsNum;
                }
            }
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void filter_graph_by_forward(List<String> start, DirectedPseudograph<EntityNode, EventEdge> orignal, String method, String resultDir, String filename, String suffix, String time, BackwardPropagate_pf infer, boolean output) {
        try {
            File folderForFtime;
            File resFolderForFilter = new File(resultDir + "/" + method);
            if (!resFolderForFilter.exists()) {
                resFolderForFilter.mkdir();
            }
            if (!(folderForFtime = new File(resFolderForFilter.getAbsolutePath() + "/" + time)).exists()) {
                folderForFtime.mkdir();
            }
            File recordStarts = new File(folderForFtime.getAbsolutePath() + "/forward_starts_" + filename + "_" + time + "_" + method + ".txt");
            FileWriter fileWriter = new FileWriter(recordStarts);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println("Entry Points for forward:");
            int startsNum = 0;
            for (String s2 : start) {
                printWriter.println(s2);
                DirectedPseudograph<EntityNode, EventEdge> backresFilteredByForwad = infer.combineBackwardAndForwardForGivenStart(s2, orignal);
                System.out.println("Size of randome start: " + backresFilteredByForwad.vertexSet().size());
                IterateGraph out = new IterateGraph(backresFilteredByForwad);
                if (output) {
                    out.exportGraph(folderForFtime.getAbsolutePath() + "/filtered_by_forward_" + String.valueOf(startsNum) + "_" + filename + "_" + method + suffix);
                }
                ++startsNum;
            }
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Set<String> convertCriticalEdge(String[] criticalEdges) {
        HashSet<String> res = new HashSet<String>();
        for (String s2 : criticalEdges) {
            String[] srcAndTarget = s2.split(",");
            String curEdge = srcAndTarget[0] + " -> " + srcAndTarget[1];
            res.add(curEdge);
        }
        return res;
    }

    private static double getAverageRankForRandomMethod(List<List<String>> entries2, String[] importants) {
        ArrayList<String> lists = new ArrayList<String>();
        for (int i = 0; i < entries2.size(); ++i) {
            for (int j = 0; j < entries2.get(i).size(); ++j) {
                lists.add(entries2.get(i).get(j));
            }
        }
        ArrayList<Double> subRes = new ArrayList<Double>();
        for (int i = 0; i < 10; ++i) {
            Collections.shuffle(lists);
            ArrayList<Double> res = new ArrayList<Double>();
            for (int j = 0; j < importants.length; ++j) {
                int index = lists.indexOf(importants[j]);
                assert (index >= 0);
                res.add((double)(index + 1) * 1.0);
            }
            subRes.add(ProcessOneLogCMD_19.calculateAverage(res));
        }
        return ProcessOneLogCMD_19.calculateAverage(subRes);
    }

    private static double calculateAverage(List<Double> num) {
        double sum2 = 0.0;
        for (Double d : num) {
            sum2 += d.doubleValue();
        }
        return sum2 / (double)num.size();
    }

    private static double getAverageRankForImportantEntry(List<List<String>> entries2, Map<String, Double> reputations, String[] importants) {
        ArrayList<Map.Entry<String, Double>> targetList = new ArrayList<Map.Entry<String, Double>>();
        ArrayList<String> entryList = new ArrayList<String>();
        for (int i = 0; i < entries2.size(); ++i) {
            for (int j = 0; j < entries2.get(i).size(); ++j) {
                entryList.add(entries2.get(i).get(j));
            }
        }
        Collections.sort(entryList);
        for (Map.Entry<String, Double> e : reputations.entrySet()) {
            String key = e.getKey();
            int idx = Collections.binarySearch(entryList, key);
            if (idx < 0) continue;
            targetList.add(e);
        }
        Collections.sort(targetList, (a, b) -> ((Double)b.getValue()).compareTo((Double)a.getValue()));
        ArrayList<String> sortedList = new ArrayList<String>();
        for (int i = 0; i < targetList.size(); ++i) {
            sortedList.add((String)((Map.Entry)targetList.get(i)).getKey());
        }
        ArrayList<Double> ranks = new ArrayList<Double>();
        for (int i = 0; i < importants.length; ++i) {
            String e = importants[i];
            ranks.add((double)(sortedList.indexOf(e) + 1) * 1.0);
        }
        return ProcessOneLogCMD_19.calculateAverage(ranks);
    }
}

