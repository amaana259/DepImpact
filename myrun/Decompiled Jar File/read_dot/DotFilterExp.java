/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package read_dot;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.graph.DirectedPseudograph;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import pagerank.Experiment;
import read_dot.ReadGraphFromDot;
import read_dot.SimpleEdge;
import read_dot.SimpleNode;

public class DotFilterExp {
    public File resFolder;
    public File propertyFolder;

    DotFilterExp(String resPath, String propertyFolder) {
        this.resFolder = new File(resPath);
        this.propertyFolder = new File(propertyFolder);
        ReadGraphFromDot dotreader = new ReadGraphFromDot();
        try {
            if (!this.resFolder.isDirectory()) {
                throw new Exception("Input resPath is not a folder path");
            }
            if (!this.propertyFolder.isDirectory()) {
                throw new Exception("Input propertyFolder is not a folder path");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<File> findFilteredGraph(String path) {
        File folder = new File(path);
        if (!folder.isDirectory()) {
            System.out.println("The input path is not a folder");
        }
        LinkedList<File> CPR = new LinkedList<File>();
        for (File f : Objects.requireNonNull(folder.listFiles())) {
            if (f.isDirectory()) {
                List<File> subCPR = this.findFilteredGraph(f.getAbsolutePath());
                CPR.addAll(subCPR);
                continue;
            }
            if (f.getAbsolutePath().indexOf("filtered") == -1) continue;
            CPR.add(f);
        }
        return CPR;
    }

    public Map<String, String> getCorrespondingPropertyFile(List<File> cpr_graphs) {
        List cprParentFolder = cpr_graphs.stream().map(f -> f.getParentFile()).collect(Collectors.toList());
        ArrayList caseName = new ArrayList(cprParentFolder.size());
        HashMap<String, String> pathMap = new HashMap<String, String>();
        for (int i = 0; i < cprParentFolder.size(); ++i) {
            File parent = (File)cprParentFolder.get(i);
            String name = parent.getAbsolutePath().split("-")[1];
            for (File property : this.propertyFolder.listFiles()) {
                String caseProperty;
                if (property.getName().indexOf("property") == -1 || name.indexOf(caseProperty = property.getName().split("_")[1]) == -1) continue;
                pathMap.put(cpr_graphs.get(i).getAbsolutePath(), property.getAbsolutePath());
            }
        }
        return pathMap;
    }

    public void run_exp(String path, String propertyPath) throws Exception {
        String json_log_path = DotFilterExp.getfilterResCorrespondingJsonLog(path);
        JSONParser jsonParser = new JSONParser();
        FileReader fileReader = new FileReader(json_log_path);
        JSONObject json_log = (JSONObject)jsonParser.parse(fileReader);
        int cprgraphsize = Integer.valueOf((String)json_log.get("CPREdgeNumber"));
        DotFilterExp exp = new DotFilterExp(path, propertyPath);
        JSONObject jsonRes = new JSONObject();
        List<File> filteredRes = exp.findFilteredGraph(path);
        Map<String, String> correspondingToProperty = this.getCorrespondingPropertyFile(filteredRes);
        for (String filterGraph : correspondingToProperty.keySet()) {
            JSONObject curCaseRes = new JSONObject();
            ReadGraphFromDot reader = new ReadGraphFromDot();
            DirectedPseudograph<SimpleNode, SimpleEdge> graph = reader.readGraph(filterGraph);
            System.out.println(correspondingToProperty.get(filterGraph));
            Experiment experiment = new Experiment(new File(correspondingToProperty.get(filterGraph)));
            String[] criticalEdges = experiment.getCriticalEdges();
            Set<String> critialSet = this.addCriticalEdgesToSet(criticalEdges);
            Set<String> edgesInFilterGraph = graph.edgeSet().stream().map(e -> e.toString()).collect(Collectors.toSet());
            double missingRate = this.calculateMissingRate(critialSet, edgesInFilterGraph);
            double redundantRate = this.calculateRedundantRate(critialSet, edgesInFilterGraph);
            double reductionRateCPR = this.calculateReductionRateComparedWithCPR(critialSet, edgesInFilterGraph, cprgraphsize);
            curCaseRes.put("missing", missingRate);
            curCaseRes.put("redundant", redundantRate);
            curCaseRes.put("reductionCPR", reductionRateCPR);
            curCaseRes.put("EdgeNumber", edgesInFilterGraph.size());
            jsonRes.put(filterGraph, curCaseRes);
        }
        File resFile = new File(path + "/filter_res.json");
        FileWriter fileWriter = new FileWriter(resFile);
        fileWriter.write(jsonRes.toJSONString());
        fileWriter.close();
    }

    public double calculateReductionRateComparedWithCPR(Set<String> critical, Set<String> edge, int totalAfterCPR) {
        return (double)(totalAfterCPR - edge.size()) * 1.0 / (double)totalAfterCPR;
    }

    public double calculateMissingRate(Set<String> critical, Set<String> edge) {
        double res = 0.0;
        for (String s2 : critical) {
            if (edge.contains(s2)) continue;
            res += 1.0;
        }
        return res / (double)critical.size();
    }

    public double calculateReductionRate(Set<String> critical, Set<String> edge) {
        return 0.0;
    }

    public double calculateRedundantRate(Set<String> critical, Set<String> edges) {
        double res = 0.0;
        for (String s2 : edges) {
            if (critical.contains(s2)) continue;
            res += 1.0;
        }
        return res / (double)critical.size();
    }

    public Set<String> addCriticalEdgesToSet(String[] edges) throws Exception {
        HashSet<String> set = new HashSet<String>();
        for (int i = 0; i < edges.length; ++i) {
            String[] tokens = edges[i].split(",");
            String from = tokens[0].trim();
            String to = tokens[1].trim();
            String edge = from + "=>" + to;
            set.add(edge);
        }
        return set;
    }

    public static List<String> getChildrenFolder(String folderPath) {
        File folder = new File(folderPath);
        LinkedList<String> res = new LinkedList<String>();
        if (!folder.isDirectory()) {
            return res;
        }
        String name = folder.getName();
        if (name.equals("random") || name.equals("randomCategory") || name.equals("sysrep")) {
            res.add(folderPath);
        } else {
            for (File f : folder.listFiles()) {
                res.addAll(DotFilterExp.getChildrenFolder(f.getAbsolutePath()));
            }
        }
        return res;
    }

    public static String getfilterResCorrespondingJsonLog(String filterResFolder) {
        File folder_obj = new File(filterResFolder);
        File parent_folder = folder_obj.getParentFile();
        for (File f : parent_folder.listFiles()) {
            if (!f.isFile() || f.toString().indexOf("json_log") == -1) continue;
            return f.getAbsolutePath().toString();
        }
        return "Doesnot find json log";
    }

    public static void run_exp_top2(List<String> filterResFolder, String propertyPath) throws Exception {
        LinkedList<String> sysrepResFolder = new LinkedList<String>();
        for (String s2 : filterResFolder) {
            if (s2.indexOf("sysrep") == -1) continue;
            sysrepResFolder.add(s2);
        }
        for (String s2 : sysrepResFolder) {
            DotFilterExp exp = new DotFilterExp(s2, propertyPath);
            String json_log_path = DotFilterExp.getfilterResCorrespondingJsonLog(s2);
            JSONParser jsonParser = new JSONParser();
            FileReader fileReader = new FileReader(json_log_path);
            JSONObject json_log = (JSONObject)jsonParser.parse(fileReader);
            int cprgraphsize = Integer.valueOf((String)json_log.get("CPREdgeNumber"));
            File sysrep_folder = new File(s2 + "/" + 1);
            LinkedList<String> sysrep_filter_graph = new LinkedList<String>();
            for (File f : sysrep_folder.listFiles()) {
                if (f.toString().indexOf("dot") == -1) continue;
                sysrep_filter_graph.add(f.getAbsolutePath().toString());
            }
            JSONObject jsonRes2Combin = new JSONObject();
            Set<Set<String>> twoCombins = DotFilterExp.getGivenNCombination(sysrep_filter_graph, 2);
            LinkedList<Set<String>> twoCombinsArray = new LinkedList<Set<String>>(twoCombins);
            Map<Integer, String> correspondingToProperty = exp.getCorrespondingPropertyFileForCombination(twoCombinsArray);
            for (int i = 0; i < twoCombinsArray.size(); ++i) {
                Set combins = (Set)twoCombinsArray.get(i);
                String point_combination = DotFilterExp.convertToPointCombin(combins);
                JSONObject curCombinRes = new JSONObject();
                Set<String> combinedEdges = DotFilterExp.getCombinedGraphEdges(combins);
                Experiment experiment = new Experiment(new File(correspondingToProperty.get(i)));
                String[] criticalEdges = experiment.getCriticalEdges();
                Set<String> critialSet = exp.addCriticalEdgesToSet(criticalEdges);
                double missingRate = exp.calculateMissingRate(critialSet, combinedEdges);
                double redundantRate = exp.calculateRedundantRate(critialSet, combinedEdges);
                double reductionRateCPR = exp.calculateReductionRateComparedWithCPR(critialSet, combinedEdges, cprgraphsize);
                curCombinRes.put("2missing", missingRate);
                curCombinRes.put("2redundant", redundantRate);
                curCombinRes.put("2reductionCPR", reductionRateCPR);
                curCombinRes.put("2EdgeNum", combinedEdges.size());
                jsonRes2Combin.put(point_combination, curCombinRes);
            }
            File resFile = new File(s2 + "/filter_res2combin.json");
            FileWriter fileWriter = new FileWriter(resFile);
            fileWriter.write(jsonRes2Combin.toJSONString());
            fileWriter.close();
        }
    }

    public static void run_exp_top3(List<String> filterResFolder, String propertyPath) throws Exception {
        LinkedList<String> sysrepResFolder = new LinkedList<String>();
        for (String s2 : filterResFolder) {
            if (s2.indexOf("sysrep") == -1) continue;
            sysrepResFolder.add(s2);
        }
        for (String s2 : sysrepResFolder) {
            DotFilterExp exp = new DotFilterExp(s2, propertyPath);
            String json_log_path = DotFilterExp.getfilterResCorrespondingJsonLog(s2);
            JSONParser jsonParser = new JSONParser();
            FileReader fileReader = new FileReader(json_log_path);
            JSONObject json_log = (JSONObject)jsonParser.parse(fileReader);
            int cprgraphsize = Integer.valueOf((String)json_log.get("CPREdgeNumber"));
            File sysrep_folder = new File(s2 + "/" + 1);
            LinkedList<String> sysrep_filter_graph = new LinkedList<String>();
            for (File f : sysrep_folder.listFiles()) {
                if (f.toString().indexOf("dot") == -1) continue;
                sysrep_filter_graph.add(f.getAbsolutePath().toString());
            }
            JSONObject jsonRes2Combin = new JSONObject();
            Set<Set<String>> threeCombins = DotFilterExp.getGivenNCombination(sysrep_filter_graph, 3);
            LinkedList<Set<String>> threeCombinsArray = new LinkedList<Set<String>>(threeCombins);
            Map<Integer, String> correspondingToProperty = exp.getCorrespondingPropertyFileForCombination(threeCombinsArray);
            for (int i = 0; i < threeCombinsArray.size(); ++i) {
                Set combins = (Set)threeCombinsArray.get(i);
                String point_combination = DotFilterExp.convertToPointCombin(combins);
                JSONObject curCombinRes = new JSONObject();
                Set<String> combinedEdges = DotFilterExp.getCombinedGraphEdges(combins);
                Experiment experiment = new Experiment(new File(correspondingToProperty.get(i)));
                String[] criticalEdges = experiment.getCriticalEdges();
                Set<String> critialSet = exp.addCriticalEdgesToSet(criticalEdges);
                double missingRate = exp.calculateMissingRate(critialSet, combinedEdges);
                double redundantRate = exp.calculateRedundantRate(critialSet, combinedEdges);
                double reductionRateCPR = exp.calculateReductionRateComparedWithCPR(critialSet, combinedEdges, cprgraphsize);
                curCombinRes.put("3missing", missingRate);
                curCombinRes.put("3redundant", redundantRate);
                curCombinRes.put("3reductionCPR", reductionRateCPR);
                curCombinRes.put("3EdgeNum", combinedEdges.size());
                jsonRes2Combin.put(point_combination, curCombinRes);
            }
            File resFile = new File(s2 + "/filter_res3combin.json");
            FileWriter fileWriter = new FileWriter(resFile);
            fileWriter.write(jsonRes2Combin.toJSONString());
            fileWriter.close();
        }
    }

    private static Set<String> getCombinedGraphEdges(Set<String> combins) {
        HashSet<String> edges = new HashSet<String>();
        for (String s2 : combins) {
            ReadGraphFromDot reader = new ReadGraphFromDot();
            DirectedPseudograph<SimpleNode, SimpleEdge> graph = reader.readGraph(s2);
            graph.edgeSet().stream().forEach(e -> edges.add(e.toString()));
        }
        return edges;
    }

    public Map<Integer, String> getCorrespondingPropertyFileForCombination(List<Set<String>> combins) {
        HashMap<Integer, String> map2 = new HashMap<Integer, String>();
        for (int i = 0; i < combins.size(); ++i) {
            ArrayList cases = new ArrayList(combins.get(i));
            String caseName = (String)cases.get(0);
            String name = caseName.split("-")[1].split("\\\\")[0];
            for (File property : this.propertyFolder.listFiles()) {
                String caseProperty;
                if (property.getName().indexOf("property") == -1 || name.indexOf(caseProperty = property.getName().split("_")[1]) == -1) continue;
                map2.put(i, property.getAbsolutePath());
            }
        }
        return map2;
    }

    public static String convertToPointCombin(Set<String> combins) {
        StringBuilder sb = new StringBuilder();
        for (String s2 : combins) {
            String[] tokens = s2.split("\\\\");
            char[] cArray = tokens[tokens.length - 1].toCharArray();
            int n = cArray.length;
            for (int i = 0; i < n; ++i) {
                Character c = Character.valueOf(cArray[i]);
                if (!Character.isDigit(c.charValue())) continue;
                if (sb.length() == 0) {
                    sb.append(c);
                    continue;
                }
                sb.append("-");
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static Set<Set<String>> getGivenNCombination(List<String> filterres, int n) {
        assert (filterres.size() >= n);
        HashSet<Set<String>> res = new HashSet<Set<String>>();
        HashSet<String> tmp = new HashSet<String>();
        DotFilterExp.generateCombine(filterres, res, tmp, n);
        return res;
    }

    private static void generateCombine(List<String> filterRes, Set<Set<String>> combinations, Set<String> cur, int size2) {
        if (cur.size() == size2) {
            HashSet<String> oneRes = new HashSet<String>(cur);
            combinations.add(oneRes);
            return;
        }
        for (String s2 : filterRes) {
            if (cur.contains(s2)) continue;
            cur.add(s2);
            DotFilterExp.generateCombine(filterRes, combinations, cur, size2);
            cur.remove(s2);
        }
    }

    public static void get_one_point_res(List<String> filterResFolder, String propertyPath) throws Exception {
        for (String p : filterResFolder) {
            DotFilterExp exp = new DotFilterExp(p, propertyPath);
            exp.run_exp(p, propertyPath);
            DotFilterExp.getfilterResCorrespondingJsonLog(p);
        }
    }

    public static void printOutCriticalEdgeNum(String properPath) throws Exception {
        File properFiles = new File(properPath);
        for (File property : properFiles.listFiles()) {
            String[] edges;
            Experiment exp = new Experiment(property);
            System.out.println(property.toString());
            for (String s2 : edges = exp.getCriticalEdges()) {
                System.out.println(s2);
            }
            System.out.println(exp.getCriticalEdges().length);
        }
    }

    public static void printCriticalEdgeNumber(String propertyPath) throws Exception {
        File propertyFolder = new File(propertyPath);
        JSONObject criticalEdge = new JSONObject();
        for (File f : propertyFolder.listFiles()) {
            if (f.getName().indexOf("property") == -1) continue;
            Experiment exp = new Experiment(f);
            String caseName = f.getName().split("_")[1];
            String[] criticalEdges = exp.getCriticalEdges();
            criticalEdge.put(caseName, criticalEdges.length);
        }
        File resJson = new File(propertyPath + "/criticalEdgeNum.json");
        FileWriter fileWriter = new FileWriter(resJson);
        fileWriter.write(criticalEdge.toJSONString());
        fileWriter.close();
    }

    public static List<String> getfilterResRandomFolder(List<String> input, String suffix) {
        ArrayList<String> folders = new ArrayList<String>();
        for (String s2 : input) {
            if (s2.indexOf(suffix) == -1) continue;
            folders.add(s2);
        }
        return folders;
    }

    public static void run_exp_random_2(List<String> folders, String propertyPath) throws Exception {
        for (String s2 : folders) {
            DotFilterExp exp = new DotFilterExp(s2, propertyPath);
            String json_log_path = DotFilterExp.getfilterResCorrespondingJsonLog(s2);
            JSONParser jsonParser = new JSONParser();
            FileReader fileReader = new FileReader(json_log_path);
            JSONObject json_log = (JSONObject)jsonParser.parse(fileReader);
            int cprgraphsize = Integer.valueOf((String)json_log.get("CPREdgeNumber"));
            for (int j = 0; j < 20; ++j) {
                File sysrep_folder = new File(s2 + "/" + String.valueOf(j));
                LinkedList<String> sysrep_filter_graph = new LinkedList<String>();
                for (File f : sysrep_folder.listFiles()) {
                    if (f.toString().indexOf("dot") == -1) continue;
                    sysrep_filter_graph.add(f.getAbsolutePath().toString());
                }
                JSONObject jsonRes2Combin = new JSONObject();
                Set<Set<String>> twoCombins = DotFilterExp.getGivenNCombination(sysrep_filter_graph, 2);
                LinkedList<Set<String>> twoCombinsArray = new LinkedList<Set<String>>(twoCombins);
                Map<Integer, String> correspondingToProperty = exp.getCorrespondingPropertyFileForCombination(twoCombinsArray);
                for (int i = 0; i < twoCombinsArray.size(); ++i) {
                    Set combins = (Set)twoCombinsArray.get(i);
                    String point_combination = DotFilterExp.convertToPointCombin(combins);
                    JSONObject curCombinRes = new JSONObject();
                    Set<String> combinedEdges = DotFilterExp.getCombinedGraphEdges(combins);
                    Experiment experiment = new Experiment(new File(correspondingToProperty.get(i)));
                    String[] criticalEdges = experiment.getCriticalEdges();
                    Set<String> critialSet = exp.addCriticalEdgesToSet(criticalEdges);
                    double missingRate = exp.calculateMissingRate(critialSet, combinedEdges);
                    double redundantRate = exp.calculateRedundantRate(critialSet, combinedEdges);
                    double reductionRateCPR = exp.calculateReductionRateComparedWithCPR(critialSet, combinedEdges, cprgraphsize);
                    curCombinRes.put("2missing", missingRate);
                    curCombinRes.put("2redundant", redundantRate);
                    curCombinRes.put("2reductionCPR", reductionRateCPR);
                    curCombinRes.put("2EdgeNum", combinedEdges.size());
                    jsonRes2Combin.put(point_combination, curCombinRes);
                }
                File resFile = new File(s2 + "/filter_res2combin_" + String.valueOf(j) + ".json");
                FileWriter fileWriter = new FileWriter(resFile);
                fileWriter.write(jsonRes2Combin.toJSONString());
                fileWriter.close();
            }
        }
    }

    public static void run_exp_random_3(List<String> folders, String propertyPath) throws Exception {
        for (String s2 : folders) {
            DotFilterExp exp = new DotFilterExp(s2, propertyPath);
            String json_log_path = DotFilterExp.getfilterResCorrespondingJsonLog(s2);
            JSONParser jsonParser = new JSONParser();
            FileReader fileReader = new FileReader(json_log_path);
            JSONObject json_log = (JSONObject)jsonParser.parse(fileReader);
            int cprgraphsize = Integer.valueOf((String)json_log.get("CPREdgeNumber"));
            for (int j = 0; j < 20; ++j) {
                File sysrep_folder = new File(s2 + "/" + String.valueOf(j));
                LinkedList<String> sysrep_filter_graph = new LinkedList<String>();
                for (File f : sysrep_folder.listFiles()) {
                    if (f.toString().indexOf("dot") == -1) continue;
                    sysrep_filter_graph.add(f.getAbsolutePath().toString());
                }
                JSONObject jsonRes2Combin = new JSONObject();
                Set<Set<String>> twoCombins = DotFilterExp.getGivenNCombination(sysrep_filter_graph, 3);
                LinkedList<Set<String>> twoCombinsArray = new LinkedList<Set<String>>(twoCombins);
                Map<Integer, String> correspondingToProperty = exp.getCorrespondingPropertyFileForCombination(twoCombinsArray);
                for (int i = 0; i < twoCombinsArray.size(); ++i) {
                    Set combins = (Set)twoCombinsArray.get(i);
                    String point_combination = DotFilterExp.convertToPointCombin(combins);
                    JSONObject curCombinRes = new JSONObject();
                    Set<String> combinedEdges = DotFilterExp.getCombinedGraphEdges(combins);
                    Experiment experiment = new Experiment(new File(correspondingToProperty.get(i)));
                    String[] criticalEdges = experiment.getCriticalEdges();
                    Set<String> critialSet = exp.addCriticalEdgesToSet(criticalEdges);
                    double missingRate = exp.calculateMissingRate(critialSet, combinedEdges);
                    double redundantRate = exp.calculateRedundantRate(critialSet, combinedEdges);
                    double reductionRateCPR = exp.calculateReductionRateComparedWithCPR(critialSet, combinedEdges, cprgraphsize);
                    curCombinRes.put("3missing", missingRate);
                    curCombinRes.put("3redundant", redundantRate);
                    curCombinRes.put("3reductionCPR", reductionRateCPR);
                    curCombinRes.put("3EdgeNum", combinedEdges.size());
                    jsonRes2Combin.put(point_combination, curCombinRes);
                }
                File resFile = new File(s2 + "/filter_res3combin_" + String.valueOf(j) + ".json");
                FileWriter fileWriter = new FileWriter(resFile);
                fileWriter.write(jsonRes2Combin.toJSONString());
                fileWriter.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String path = "D:\\cluster_all_plus_filter";
        String propertyPath = "C:\\Users\\fang2\\OneDrive\\Desktop\\reptracker\\reptracker\\input_11\\large_properties";
        List<String> filterResFolders = DotFilterExp.getChildrenFolder(path);
        List<String> randomFolder = DotFilterExp.getfilterResRandomFolder(filterResFolders, "randomCategory");
        DotFilterExp.get_one_point_res(filterResFolders, propertyPath);
        DotFilterExp.run_exp_top2(filterResFolders, propertyPath);
        DotFilterExp.run_exp_top3(filterResFolders, propertyPath);
    }
}

