/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import org.jgrapht.graph.DirectedPseudograph;
import pagerank.BackTrack;
import pagerank.CausalityPreserve;
import pagerank.EntityNode;
import pagerank.EventEdge;
import pagerank.GetGraph;
import pagerank.GraphSplit;
import pagerank.InferenceReputation;
import pagerank.IterateGraph;
import pagerank.MetaConfig;

public class ProcessOneLogTestCL {
    public static void main(String[] args) {
        String[] localIP = MetaConfig.localIP;
        String[] midRP = MetaConfig.midRP;
        String path = "/home/lcl/chmod.txt";
        String detection = "/home/lcl/target";
        double threshold = 0.1;
        String[] highRP = new String[]{};
        String[] lowRP = new String[]{"192.168.29.234:40402->208.118.235.20:80"};
        String resultDir = "results/exp/";
        String suffix = "";
        boolean trackOrigin = false;
        String[] paths = path.split("/");
        ProcessOneLogTestCL.process(resultDir, suffix, threshold, trackOrigin, path, localIP, detection, highRP, midRP, lowRP, paths[paths.length - 1].split("\\.")[0], 0.0, new HashSet<String>());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void process(String resultDir, String suffix, double threshold, boolean trackOrigin, String logfile, String[] IP, String detection, String[] highRP, String[] midRP, String[] lowRP, String filename, double detectionSize, Set<String> seedSources) {
        OutputStream os = null;
        Object weightfile = null;
        PrintWriter pw = null;
        try {
            os = new FileOutputStream(resultDir + filename + suffix + "_stats");
            GetGraph getGraph = new GetGraph(logfile, IP);
            getGraph.GenerateGraph();
            DirectedPseudograph<EntityNode, EventEdge> orignal = getGraph.getJg();
            System.out.println("Original vertex number:" + orignal.vertexSet().size() + " edge number : " + orignal.edgeSet().size());
            os.write(("Original vertex number:" + orignal.vertexSet().size() + " edge number : " + orignal.edgeSet().size() + "\n").getBytes());
            BackTrack backTrack = new BackTrack(orignal);
            backTrack.backTrackPOIEvent(detection);
            System.out.println("After Backtrack vertex number is: " + backTrack.afterBackTrack.vertexSet().size() + " edge number: " + backTrack.afterBackTrack.edgeSet().size());
            os.write(("After Backtrack vertex number is: " + backTrack.afterBackTrack.vertexSet().size() + " edge number: " + backTrack.afterBackTrack.edgeSet().size() + "\n").getBytes());
            IterateGraph out = new IterateGraph(backTrack.afterBackTrack);
            out.exportGraph(resultDir + "BackTrack_" + filename + suffix);
            CausalityPreserve CPR1 = new CausalityPreserve(backTrack.afterBackTrack);
            CPR1.CPR(1);
            System.out.println("After CPR1 vertex number is: " + CPR1.afterMerge.vertexSet().size() + " edge number: " + CPR1.afterMerge.edgeSet().size());
            os.write(("After CPR1 vertex number is: " + CPR1.afterMerge.vertexSet().size() + " edge number: " + CPR1.afterMerge.edgeSet().size() + "\n").getBytes());
            CausalityPreserve CPR = new CausalityPreserve(backTrack.afterBackTrack);
            CPR.CPR(2);
            System.out.println("After CPR2 vertex number is: " + CPR.afterMerge.vertexSet().size() + " edge number: " + CPR.afterMerge.edgeSet().size());
            os.write(("After CPR2 vertex number is: " + CPR.afterMerge.vertexSet().size() + " edge number: " + CPR.afterMerge.edgeSet().size() + "\n").getBytes());
            out = new IterateGraph(CPR.afterMerge);
            out.exportGraph(resultDir + "AfterCPR_" + filename + suffix);
            GraphSplit split2 = new GraphSplit(CPR.afterMerge);
            split2.splitGraph();
            System.out.println("After Split vertex number is: " + split2.inputGraph.vertexSet().size() + " edge number: " + split2.inputGraph.edgeSet().size());
            InferenceReputation infer = new InferenceReputation(split2.inputGraph);
            os.write(("After Split vertex number is: " + split2.inputGraph.vertexSet().size() + " edge number: " + split2.inputGraph.edgeSet().size() + "\n").getBytes());
            infer.setDetectionSize(detectionSize);
            infer.setSeedSources(seedSources);
            infer.calculateWeights_ML(true, 3);
            pw = new PrintWriter("results/weights/" + filename);
            for (EventEdge e : infer.graph.edgeSet()) {
                boolean outlier = infer.graph.incomingEdgesOf(e.getSink()).size() == 1;
                pw.println(e.getSource().getSignature() + " -> " + e.getSink().getSignature() + "," + e.timeWeight + "," + e.amountWeight + "," + e.structureWeight + "," + outlier);
            }
            infer.initialReputation(highRP, midRP, lowRP);
            infer.PageRankIteration2(highRP, midRP, lowRP, detection);
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
            double avg = infer.getAvgWeight();
            for (double i = 0.0; i <= 2.5; i += 0.05) {
                try {
                    infer.filterGraphBasedOnAverageWeight(i * avg);
                    infer.removeIsolatedIslands(detection);
                    infer.exportGraph(String.format(resultDir + "Filter_%.2f_" + filename + suffix, i));
                    System.out.println(String.format("%.2f - After Filter vertex number is: ", i) + infer.graph.vertexSet().size() + " edge number: " + infer.graph.edgeSet().size());
                    os.write(String.format("\n %.2f - After Filter vertex number is: %d edge number: %d", i, infer.graph.vertexSet().size(), infer.graph.edgeSet().size()).getBytes());
                    cmd = new String[]{"/bin/sh", "-c", "dot -T svg " + String.format(resultDir + "Filter_%.2f_" + filename + suffix, i) + ".dot > " + String.format(resultDir + "Filter_%.2f_" + filename + suffix, i) + ".svg"};
                    rt.exec(cmd);
                    continue;
                } catch (Exception e) {
                    System.out.println(String.format("\n %.2f - After Filter vertex number is: -1 edge number: -1", i));
                    os.write(String.format("\n %.2f - After Filter vertex number is: -1 edge number: -1", i).getBytes());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
                pw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

