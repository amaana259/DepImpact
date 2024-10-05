/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pagerank.BackTrack;
import pagerank.CausalityPreserve;
import pagerank.GetGraph;
import pagerank.GraphSplit;

public class ExperimentRun {
    private File foler;
    private Map<String, String> nameToFile;
    private String fileOfPoiFile;
    private File POIfile;
    private Map<String, String> Pois;
    public String[] localIP;
    private Map<String, List<Integer>> results;

    ExperimentRun(String folderPath, String fileOfPoi) {
        File[] files;
        this.foler = new File(folderPath);
        this.fileOfPoiFile = fileOfPoi;
        this.POIfile = new File(this.fileOfPoiFile);
        this.nameToFile = new HashMap<String, String>();
        this.Pois = new HashMap<String, String>();
        this.results = new HashMap<String, List<Integer>>();
        for (File f : files = this.foler.listFiles()) {
            this.nameToFile.put(f.getName(), f.getAbsolutePath());
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(this.POIfile));
            String line = reader.readLine();
            while (line != null) {
                String[] strs = line.split(":");
                this.Pois.put(strs[0], strs[1]);
                line = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printFileNames() {
        for (String s2 : this.nameToFile.keySet()) {
            System.out.println(this.nameToFile.get(s2));
        }
    }

    public void printPois() {
        for (String s2 : this.Pois.keySet()) {
            System.out.println(s2);
        }
    }

    public void readLocalIP(String[] s2) {
        this.localIP = s2;
    }

    public void generateOrigianlGraph() {
        if (this.localIP == null || this.localIP.length == 0) {
            System.out.println("Need ip input");
        }
        for (String s2 : this.nameToFile.keySet()) {
            GetGraph getGraph = new GetGraph(this.nameToFile.get(s2), this.localIP);
            String filePath = this.nameToFile.get(s2);
            getGraph.GenerateGraph();
            if (!this.results.containsKey(filePath)) {
                this.results.put(this.nameToFile.get(s2), new ArrayList());
            }
            this.results.get(filePath).add(getGraph.jg.vertexSet().size());
            this.results.get(filePath).add(getGraph.jg.edgeSet().size());
            String POISignature = this.Pois.get(s2);
            BackTrack backTrack = new BackTrack(getGraph.getOriginalGraph());
            backTrack.backTrackPOIEvent(POISignature);
            this.results.get(filePath).add(backTrack.afterBackTrack.vertexSet().size());
            this.results.get(filePath).add(backTrack.afterBackTrack.edgeSet().size());
            CausalityPreserve reduction = new CausalityPreserve(backTrack.afterBackTrack);
            reduction.CPR(1);
            this.results.get(filePath).add(reduction.afterMerge.vertexSet().size());
            this.results.get(filePath).add(reduction.afterMerge.edgeSet().size());
            GraphSplit split2 = new GraphSplit(reduction.afterMerge);
            split2.splitGraph();
            this.results.get(filePath).add(split2.inputGraph.vertexSet().size());
            this.results.get(filePath).add(split2.inputGraph.edgeSet().size());
        }
        System.out.println(this.results);
    }

    public void outputResult() throws IOException {
        PrintWriter printWriter = new PrintWriter("result.txt", "UTF-8");
        for (String s2 : this.results.keySet()) {
            List<Integer> list = this.results.get(s2);
            String result = String.format("%s, %s, %d, %d, %s, %d, %d,%s, %d, %d,%s, %d, %d", s2, "original size", list.get(0), list.get(1), "after backtrack", list.get(2), list.get(3), "after CPR", list.get(4), list.get(5), "after rebuild logical", list.get(6), list.get(7));
            printWriter.println(result);
        }
        printWriter.close();
    }

    public static void main(String[] args) {
        String path = "/home/fang/thesis2/Data/Expdata2";
        String pathOfPois = "/home/fang/thesis2/Data/Poievents.txt";
        String[] locapIPS = new String[]{"10.0.2.15"};
        ExperimentRun expRun = new ExperimentRun(path, pathOfPois);
        expRun.readLocalIP(locapIPS);
        expRun.generateOrigianlGraph();
        try {
            expRun.outputResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

