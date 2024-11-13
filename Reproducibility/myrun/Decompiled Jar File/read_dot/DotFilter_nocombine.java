/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package read_dot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.graph.DirectedPseudograph;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import pagerank.Experiment;
import read_dot.DotFilterExp;
import read_dot.ReadGraphFromDot;
import read_dot.SimpleEdge;
import read_dot.SimpleNode;

public class DotFilter_nocombine {
    public File resFolder;
    public File propertyFolder;

    DotFilter_nocombine(String resPath, String propertyFolder) {
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

    public double calculateReductionRateComparedWithCPR(Set<String> critical, Set<String> edge, int totalAfterCPR) {
        return (double)(totalAfterCPR - edge.size()) * 1.0 / (double)totalAfterCPR;
    }

    public double calculateMissingRate(Set<String> critical, Set<String> edge) {
        double res = 0.0;
        System.out.println("Current Edge:");
        for (String s2 : edge) {
            System.out.println(s2);
        }
        System.out.println("---------------------------");
        for (String s2 : critical) {
            if (edge.contains(s2)) continue;
            res += 1.0;
            System.out.println("Missing Edge: " + s2);
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
                res.addAll(DotFilter_nocombine.getChildrenFolder(f.getAbsolutePath()));
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

    public static void run_exp_topN(List<String> filterResFolder, String propertyPath, int ranks) throws Exception {
        LinkedList<String> sysrepResFolder = new LinkedList<String>();
        for (String s2 : filterResFolder) {
            if (s2.indexOf("sysrep") == -1) continue;
            sysrepResFolder.add(s2);
        }
        ArrayList<Integer> rankList = new ArrayList<Integer>();
        for (int i = 0; i < ranks; ++i) {
            rankList.add(i);
        }
        for (String s3 : sysrepResFolder) {
            DotFilter_nocombine exp = new DotFilter_nocombine(s3, propertyPath);
            String json_log_path = DotFilter_nocombine.getfilterResCorrespondingJsonLog(s3);
            JSONParser jsonParser = new JSONParser();
            FileReader fileReader = new FileReader(json_log_path);
            JSONObject json_log = (JSONObject)jsonParser.parse(fileReader);
            int cprgraphsize = Integer.valueOf((String)json_log.get("CPREdgeNumber"));
            File sysrep_folder = new File(s3 + "/" + 1);
            LinkedList<String> sysrep_filter_graph = new LinkedList<String>();
            HashMap entryScores = new HashMap();
            File usedEntries = null;
            for (File f : sysrep_folder.listFiles()) {
                if (f.toString().indexOf("dot") == -1) {
                    usedEntries = f;
                    continue;
                }
                sysrep_filter_graph.add(f.getAbsolutePath().toString());
            }
            Map<File, String> filterGraphToEntry = DotFilter_nocombine.getFilterGraphToEntry(sysrep_folder, usedEntries);
            List<File> sortedForwardRes = DotFilter_nocombine.sortedFilteringResult(filterGraphToEntry, s3);
            List<String> topCandidates = DotFilter_nocombine.getCorrespondingFilePath(sysrep_filter_graph, rankList, s3 + "/1/");
            JSONObject jsonRes2Combin = new JSONObject();
            File propertyFile = DotFilter_nocombine.findPropertyFile(propertyPath, s3);
            JSONObject curCombinRes = new JSONObject();
            Set<String> combineEdges = DotFilter_nocombine.getCombinedGraphEdges(topCandidates);
            Experiment experiment = new Experiment(propertyFile);
            String[] criticalEdges = experiment.getCriticalEdges();
            Set<String> critialSet = exp.addCriticalEdgesToSet(criticalEdges);
            double missingRate = exp.calculateMissingRate(critialSet, combineEdges);
            double redundantRate = exp.calculateRedundantRate(critialSet, combineEdges);
            double reductionRateCPR = exp.calculateReductionRateComparedWithCPR(critialSet, combineEdges, cprgraphsize);
            String rankValue = String.valueOf(ranks);
            curCombinRes.put(rankValue + "missing", missingRate);
            curCombinRes.put(rankValue + "redundant", redundantRate);
            curCombinRes.put(rankValue + "reductionCPR", reductionRateCPR);
            curCombinRes.put(rankValue + "EdgeNum", combineEdges.size());
            List<Integer> trueFalsePositiveNegativeRes = DotFilter_nocombine.calculateTrueFalsePositiveNegative(critialSet, combineEdges, cprgraphsize);
            curCombinRes.put(rankValue + "TruePositive", trueFalsePositiveNegativeRes.get(0));
            curCombinRes.put(rankValue + "TrueNegative", trueFalsePositiveNegativeRes.get(1));
            curCombinRes.put(rankValue + "FalsePositive", trueFalsePositiveNegativeRes.get(2));
            curCombinRes.put(rankValue + "FalseNegative", trueFalsePositiveNegativeRes.get(3));
            jsonRes2Combin.put(rankValue, curCombinRes);
            File resFile = new File(s3 + "/filter_res_" + rankValue + "_combin.json");
            FileWriter fileWriter = new FileWriter(resFile);
            fileWriter.write(jsonRes2Combin.toJSONString());
            fileWriter.close();
        }
    }

    public static void run_exp_topN_sorted_by_score(List<String> filterResFolder, String propertyPath) throws Exception {
        LinkedList<String> sysrepResFolder = new LinkedList<String>();
        for (String s2 : filterResFolder) {
            if (s2.indexOf("sysrep") == -1) continue;
            sysrepResFolder.add(s2);
        }
        for (String s2 : sysrepResFolder) {
            int j;
            System.out.println("Current Case: " + s2);
            DotFilter_nocombine exp = new DotFilter_nocombine(s2, propertyPath);
            String json_log_path = DotFilter_nocombine.getfilterResCorrespondingJsonLog(s2);
            System.out.println("Current case json log path: " + json_log_path);
            JSONParser jsonParser = new JSONParser();
            FileReader fileReader = new FileReader(json_log_path);
            JSONObject json_log = (JSONObject)jsonParser.parse(fileReader);
            int cprgraphsize = Integer.valueOf((String)json_log.get("CPREdgeNumber"));
            File sysrep_folder = new File(s2 + "/" + 1);
            LinkedList<String> sysrep_filter_graph = new LinkedList<String>();
            HashMap entryScores = new HashMap();
            File usedEntries = null;
            for (File f : sysrep_folder.listFiles()) {
                if (f.toString().indexOf("dot") == -1) {
                    usedEntries = f;
                    continue;
                }
                sysrep_filter_graph.add(f.getAbsolutePath().toString());
            }
            Map<File, String> filterGraphToEntry = DotFilter_nocombine.getFilterGraphToEntry(sysrep_folder, usedEntries);
            List<File> sortedForwardRes = DotFilter_nocombine.sortedFilteringResult(filterGraphToEntry, s2);
            JSONObject jsonRes2Combin = new JSONObject();
            File propertyFile = DotFilter_nocombine.findPropertyFile(propertyPath, s2);
            int numberOfNode = sortedForwardRes.size();
            double finalMissing = 0.0;
            double finalRedundantRate = 0.0;
            double finalReductionRateCPR = 0.0;
            double finalEdgeNum = 0.0;
            double finalTruePositive = 0.0;
            double finalTrueNegative = 0.0;
            double finalFalsePositive = 0.0;
            double finalFalseNegative = 0.0;
            for (j = 0; j < sortedForwardRes.size(); ++j) {
                ArrayList<String> topCandidates = new ArrayList<String>();
                for (int i = 0; i < j + 1; ++i) {
                    topCandidates.add(sortedForwardRes.get(i).getAbsolutePath());
                }
                JSONObject curCombinRes = new JSONObject();
                Set<String> combineEdges = DotFilter_nocombine.getCombinedGraphEdges(topCandidates);
                Experiment experiment = new Experiment(propertyFile);
                String[] criticalEdges = experiment.getCriticalEdges();
                Set<String> critialSet = exp.addCriticalEdgesToSet(criticalEdges);
                double missingRate = exp.calculateMissingRate(critialSet, combineEdges);
                double redundantRate = exp.calculateRedundantRate(critialSet, combineEdges);
                double reductionRateCPR = exp.calculateReductionRateComparedWithCPR(critialSet, combineEdges, cprgraphsize);
                String rankValue = String.valueOf(j + 1);
                curCombinRes.put(rankValue + "missing", missingRate);
                curCombinRes.put(rankValue + "redundant", redundantRate);
                curCombinRes.put(rankValue + "reductionCPR", reductionRateCPR);
                curCombinRes.put(rankValue + "EdgeNum", combineEdges.size());
                List<Integer> trueFalsePositiveNegativeRes = DotFilter_nocombine.calculateTrueFalsePositiveNegative(critialSet, combineEdges, cprgraphsize);
                curCombinRes.put(rankValue + "TruePositive", trueFalsePositiveNegativeRes.get(0));
                curCombinRes.put(rankValue + "TrueNegative", trueFalsePositiveNegativeRes.get(1));
                curCombinRes.put(rankValue + "FalsePositive", trueFalsePositiveNegativeRes.get(2));
                curCombinRes.put(rankValue + "FalseNegative", trueFalsePositiveNegativeRes.get(3));
                curCombinRes.put(rankValue + "CriticalEdge", criticalEdges.length);
                jsonRes2Combin.put(rankValue, curCombinRes);
                finalMissing = missingRate;
                finalRedundantRate = redundantRate;
                finalReductionRateCPR = reductionRateCPR;
                finalEdgeNum = combineEdges.size();
                finalTruePositive = trueFalsePositiveNegativeRes.get(0).intValue();
                finalTrueNegative = trueFalsePositiveNegativeRes.get(1).intValue();
                finalFalsePositive = trueFalsePositiveNegativeRes.get(2).intValue();
                finalFalseNegative = trueFalsePositiveNegativeRes.get(3).intValue();
            }
            for (j = numberOfNode + 1; j <= 9; ++j) {
                JSONObject curCombinRes = new JSONObject();
                String rankValue = String.valueOf(j);
                curCombinRes.put(rankValue + "missing", finalMissing);
                curCombinRes.put(rankValue + "redundant", finalRedundantRate);
                curCombinRes.put(rankValue + "reductionCPR", finalReductionRateCPR);
                curCombinRes.put(rankValue + "EdgeNum", finalEdgeNum);
                curCombinRes.put(rankValue + "TruePositive", finalTruePositive);
                curCombinRes.put(rankValue + "TrueNegative", finalTrueNegative);
                curCombinRes.put(rankValue + "FalsePositive", finalFalsePositive);
                curCombinRes.put(rankValue + "FalseNegative", finalFalseNegative);
                jsonRes2Combin.put(rankValue, curCombinRes);
            }
            File resFile = new File(s2 + "/filter_res_socrted_by_score.json");
            FileWriter fileWriter = new FileWriter(resFile);
            fileWriter.write(jsonRes2Combin.toJSONString());
            fileWriter.close();
        }
    }

    private static Map<File, String> getFilterGraphToEntry(File sysrep_folder, File usedEntries) {
        LinkedList<File> dotFiles = new LinkedList<File>();
        for (File f : sysrep_folder.listFiles()) {
            if (!f.getName().endsWith("dot")) continue;
            dotFiles.add(f);
        }
        Collections.sort(dotFiles, (a, b) -> a.getName().compareTo(b.getName()));
        HashMap<File, String> res = new HashMap<File, String>();
        try {
            FileReader fileReader = new FileReader(usedEntries);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            int idx = 0;
            while (line != null) {
                if (line.startsWith("Entry")) {
                    line = bufferedReader.readLine();
                    continue;
                }
                res.put((File)dotFiles.get(idx), line);
                ++idx;
                line = bufferedReader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    private static List<File> sortedFilteringResult(Map<File, String> map2, String sysrep_res_folder) {
        LinkedList<File> res = new LinkedList<File>();
        HashMap<File, Double> fileScore = new HashMap<File, Double>();
        File sysrepFolder = new File(sysrep_res_folder);
        File startEntryScore = new File(sysrepFolder.getParent() + "/sysrep_entry_and_reputation.json");
        try {
            FileReader reader = new FileReader(startEntryScore);
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject)parser.parse(reader);
            for (File f : map2.keySet()) {
                String entry = map2.get(f);
                Double score = (Double)jsonObject.get(entry);
                fileScore.put(f, score);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LinkedList entryList = new LinkedList(fileScore.entrySet());
        Collections.sort(entryList, (a, b) -> ((Double)b.getValue()).compareTo((Double)a.getValue()));
        for (int i = 0; i < entryList.size(); ++i) {
            res.add((File)((Map.Entry)entryList.get(i)).getKey());
        }
        return res;
    }

    private static File findPropertyFile(String propertyPath, String sysrep_res_folder) {
        Path sysrep_res_folder_path = Paths.get(sysrep_res_folder, new String[0]);
        Path parentFolder = sysrep_res_folder_path.getParent();
        String caseName = parentFolder.getFileName().toString().split("-")[1];
        String caseCategory = parentFolder.getFileName().toString().split("-")[0];
        File properFolder = new File(propertyPath);
        for (File property : properFolder.listFiles()) {
            if (property.getName().indexOf(caseName) == -1 || !property.getName().startsWith(caseCategory)) continue;
            return property;
        }
        System.out.println("Doesn't find corresponding property file for " + sysrep_res_folder);
        return null;
    }

    private static Set<String> getCombinedGraphEdges(List<String> combins) {
        HashSet<String> edges = new HashSet<String>();
        for (String s2 : combins) {
            ReadGraphFromDot reader = new ReadGraphFromDot();
            DirectedPseudograph<SimpleNode, SimpleEdge> graph = reader.readGraph(s2);
            graph.edgeSet().stream().forEach(e -> edges.add(e.toString()));
        }
        return edges;
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
        DotFilter_nocombine.generateCombine(filterres, res, tmp, n);
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
            DotFilter_nocombine.generateCombine(filterRes, combinations, cur, size2);
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

    public static void run_exp_randomN(List<String> folders, String propertyPath, int N) throws Exception {
        for (String s2 : folders) {
            DotFilter_nocombine exp = new DotFilter_nocombine(s2, propertyPath);
            String json_log_path = DotFilter_nocombine.getfilterResCorrespondingJsonLog(s2);
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
                Set<String> candidates = DotFilter_nocombine.getRandomCaidiate(sysrep_filter_graph, N);
                LinkedList<Set<String>> twoCombinsArray = new LinkedList<Set<String>>();
                twoCombinsArray.add(candidates);
                Map<Integer, String> correspondingToProperty = exp.getCorrespondingPropertyFileForCombination(twoCombinsArray);
                for (int i = 0; i < twoCombinsArray.size(); ++i) {
                    Set combins = (Set)twoCombinsArray.get(i);
                    String point_combination = DotFilter_nocombine.convertToPointCombin(combins);
                    JSONObject curCombinRes = new JSONObject();
                    Set<String> combinedEdges = DotFilter_nocombine.getCombinedGraphEdges(combins);
                    Experiment experiment = new Experiment(new File(correspondingToProperty.get(i)));
                    String[] criticalEdges = experiment.getCriticalEdges();
                    Set<String> critialSet = exp.addCriticalEdgesToSet(criticalEdges);
                    double missingRate = exp.calculateMissingRate(critialSet, combinedEdges);
                    double redundantRate = exp.calculateRedundantRate(critialSet, combinedEdges);
                    double reductionRateCPR = exp.calculateReductionRateComparedWithCPR(critialSet, combinedEdges, cprgraphsize);
                    String rankValue = String.valueOf(N);
                    curCombinRes.put(rankValue + "missing", missingRate);
                    curCombinRes.put(rankValue + "redundant", redundantRate);
                    curCombinRes.put(rankValue + "reductionCPR", reductionRateCPR);
                    curCombinRes.put(rankValue + "EdgeNum", combinedEdges.size());
                    List<Integer> trueFalsePositiveNegativeRes = DotFilter_nocombine.calculateTrueFalsePositiveNegative(critialSet, combinedEdges, cprgraphsize);
                    curCombinRes.put(rankValue + "TruePositive", trueFalsePositiveNegativeRes.get(0));
                    curCombinRes.put(rankValue + "TrueNegative", trueFalsePositiveNegativeRes.get(1));
                    curCombinRes.put(rankValue + "FalsePositive", trueFalsePositiveNegativeRes.get(2));
                    curCombinRes.put(rankValue + "FalseNegative", trueFalsePositiveNegativeRes.get(3));
                    jsonRes2Combin.put(rankValue, curCombinRes);
                }
                File resFile = new File(s2 + "/" + String.format("filter_res_%drandom", N) + String.valueOf(j) + ".json");
                FileWriter fileWriter = new FileWriter(resFile);
                fileWriter.write(jsonRes2Combin.toJSONString());
                fileWriter.close();
            }
        }
    }

    private static Set<String> getRandomCaidiate(List<String> files, int number) {
        HashSet<String> candidates = new HashSet<String>();
        Collections.shuffle(files);
        for (int i = 0; i < number * 3 && i < files.size(); ++i) {
            candidates.add(files.get(i));
        }
        return candidates;
    }

    private static List<String> getCorrespondingFilePath(List<String> candidate_res, List<Integer> rank, String prefix) {
        assert (rank.size() <= 3 && rank.size() > 0);
        assert (candidate_res.size() > 0);
        LinkedList<String> res = new LinkedList<String>();
        HashSet<String> candidates = new HashSet<String>();
        for (Integer r : rank) {
            int cur = r;
            for (int t = 0; t < 3; ++t) {
                candidates.add(String.valueOf(cur));
                cur += 3;
            }
        }
        for (String file_path : candidate_res) {
            Path path = Paths.get(file_path, new String[0]);
            String fileName = path.getFileName().toString();
            for (String candidate : candidates) {
                if (fileName.indexOf(candidate) == -1) continue;
                res.add(prefix + fileName);
            }
        }
        return res;
    }

    private static List<Integer> calculateTrueFalsePositiveNegative(Set<String> criticaledges, Set<String> combine, int cprgraphsize) {
        int truePositive = 0;
        int trueNegative = 0;
        int falsePositive = 0;
        int falseNegative = 0;
        for (String edge : criticaledges) {
            if (combine.contains(edge)) {
                ++truePositive;
                continue;
            }
            ++falseNegative;
        }
        for (String edge : combine) {
            if (criticaledges.contains(edge)) continue;
            ++falsePositive;
        }
        trueNegative = cprgraphsize - combine.size();
        ArrayList<Integer> res = new ArrayList<Integer>();
        res.add(truePositive);
        res.add(trueNegative);
        res.add(falsePositive);
        res.add(falseNegative);
        return res;
    }

    public static Map<String, Double> readEntryPointsReputation(File rankFile) throws Exception {
        HashMap<String, Double> res = new HashMap<String, Double>();
        FileReader fileReader = new FileReader(rankFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = bufferedReader.readLine();
        while (line != null) {
            if (line.startsWith("----")) {
                line = bufferedReader.readLine();
                continue;
            }
            if (line.indexOf(":") != -1) {
                String[] tokens = line.split(" ");
                res.put(tokens[0].substring(0, tokens[0].length() - 1), Double.parseDouble(tokens[1]));
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        return res;
    }

    public static List<String> readEntryPointsForForward(File file) throws Exception {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = bufferedReader.readLine();
        LinkedList<String> res = new LinkedList<String>();
        while (line != null) {
            if (!line.startsWith("Entry Points")) {
                res.add(line);
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        return res;
    }

    static List<Map.Entry<String, Double>> sortedEntryPointsNoCategory(Map<String, Double> reputations, List<String> candidates) {
        HashMap<String, Double> candidateScores = new HashMap<String, Double>();
        for (String s2 : candidates) {
            candidateScores.put(s2, reputations.get(s2));
        }
        LinkedList<Map.Entry<String, Double>> res = new LinkedList<Map.Entry<String, Double>>(candidateScores.entrySet());
        Collections.sort(res, (a, b) -> ((Double)b.getValue()).compareTo((Double)a.getValue()));
        return res;
    }

    static List<String> getFilePathForEntryReputation(String path) throws Exception {
        File methodFolder = new File(path);
        LinkedList<String> res = new LinkedList<String>();
        for (File caseRes : methodFolder.listFiles()) {
            if (!caseRes.isDirectory()) continue;
            res.add(caseRes.getAbsolutePath().toString() + "\\start_rank.txt");
        }
        return res;
    }

    static List<String> getFilePathForEntryUsedInForward(String path) {
        File methodFolder = new File(path);
        LinkedList<String> sysrep = new LinkedList<String>();
        for (File caseRes : methodFolder.listFiles()) {
            if (!caseRes.isDirectory()) continue;
            sysrep.add(caseRes.getAbsolutePath().toString() + "\\sysrep\\1");
        }
        LinkedList<String> res = new LinkedList<String>();
        for (String s2 : sysrep) {
            File resFolder = new File(s2);
            for (File f : resFolder.listFiles()) {
                if (f.getName().indexOf("starts") == -1) continue;
                res.add(resFolder.getAbsolutePath() + "\\" + f.getName());
            }
        }
        return res;
    }

    static void merge_filter(List<String> filterResFolder, String propertyPath, String methodPath) throws Exception {
        LinkedList<String> sysrepResFolder = new LinkedList<String>();
        for (String s2 : filterResFolder) {
            if (s2.indexOf("sysrep") == -1) continue;
            sysrepResFolder.add(s2);
        }
        List<String> entryPointsRep = DotFilter_nocombine.getFilePathForEntryReputation(methodPath);
        System.out.println(entryPointsRep);
        List<String> entryPointsUsedInForward = DotFilter_nocombine.getFilePathForEntryUsedInForward(methodPath);
        System.out.println(entryPointsUsedInForward);
        for (int i = 0; i < sysrepResFolder.size(); ++i) {
            String json_log_path = DotFilter_nocombine.getfilterResCorrespondingJsonLog((String)sysrepResFolder.get(i));
            JSONParser jsonParser = new JSONParser();
            FileReader fileReader = new FileReader(json_log_path);
            JSONObject json_log = (JSONObject)jsonParser.parse(fileReader);
            int cprgraphsize = Integer.valueOf((String)json_log.get("CPREdgeNumber"));
            DotFilter_nocombine exp = new DotFilter_nocombine((String)sysrepResFolder.get(i), propertyPath);
            File curCase = new File((String)sysrepResFolder.get(i));
            File reputationFile = new File(entryPointsRep.get(i));
            File entryUsedInFilter = new File(entryPointsUsedInForward.get(i));
            Map<String, Double> reputation = DotFilter_nocombine.readEntryPointsReputation(reputationFile);
            List<String> topEntries = DotFilter_nocombine.readEntryPointsForForward(entryUsedInFilter);
            List<Map.Entry<String, Double>> sortedEntries = DotFilter_nocombine.sortedEntryPointsNoCategory(reputation, topEntries);
            JSONObject jsonRes = new JSONObject();
            List<File> filteredGraph = DotFilter_nocombine.getFilteredGraphForCurrentCase(curCase);
            for (File f2 : filteredGraph) {
                System.out.println(f2.getAbsolutePath());
            }
            HashSet<String> combins = new HashSet<String>();
            for (int j = 0; j < 9; ++j) {
                if (j >= topEntries.size()) {
                    jsonRes.put(j, new JSONObject());
                    continue;
                }
                Map.Entry<String, Double> jthEntry = sortedEntries.get(j);
                String entrySignature = jthEntry.getKey();
                int idx = topEntries.indexOf(entrySignature);
                File correspondFilteredGraph = filteredGraph.get(idx);
                combins.add(correspondFilteredGraph.getAbsolutePath());
                Set<String> combineEdges = DotFilter_nocombine.getCombinedGraphEdges(combins);
                File propertyFile = DotFilter_nocombine.findPropertyFile(propertyPath, (String)sysrepResFolder.get(i));
                Experiment experiment = new Experiment(propertyFile);
                String[] criticalEdges = experiment.getCriticalEdges();
                Set<String> critialSet = exp.addCriticalEdgesToSet(criticalEdges);
                double missingRate = exp.calculateMissingRate(critialSet, combineEdges);
                double redundantRate = exp.calculateRedundantRate(critialSet, combineEdges);
                double reductionRateCPR = exp.calculateReductionRateComparedWithCPR(critialSet, combineEdges, cprgraphsize);
                System.out.println(missingRate);
                System.out.println(reductionRateCPR);
                JSONObject curCombinRes = new JSONObject();
                String rankValue = String.valueOf(j + 1);
                curCombinRes.put(rankValue + "missing", missingRate);
                curCombinRes.put(rankValue + "redundant", redundantRate);
                curCombinRes.put(rankValue + "reductionCPR", reductionRateCPR);
                curCombinRes.put(rankValue + "EdgeNum", combineEdges.size());
                List<Integer> trueFalsePositiveNegativeRes = DotFilter_nocombine.calculateTrueFalsePositiveNegative(critialSet, combineEdges, cprgraphsize);
                curCombinRes.put(rankValue + "TruePositive", trueFalsePositiveNegativeRes.get(0));
                curCombinRes.put(rankValue + "TrueNegative", trueFalsePositiveNegativeRes.get(1));
                curCombinRes.put(rankValue + "FalsePositive", trueFalsePositiveNegativeRes.get(2));
                curCombinRes.put(rankValue + "FalseNegative", trueFalsePositiveNegativeRes.get(3));
                jsonRes.put(rankValue, curCombinRes);
                File resFile = new File((String)sysrepResFolder.get(i) + "/filter_res_merge_combin.json");
                FileWriter fileWriter = new FileWriter(resFile);
                fileWriter.write(jsonRes.toJSONString());
                fileWriter.close();
            }
        }
    }

    private static List<File> getFilteredGraphForCurrentCase(File curCase) {
        LinkedList<File> res = new LinkedList<File>();
        for (File f : curCase.listFiles()) {
            if (!f.isDirectory()) continue;
            for (File f2 : f.listFiles()) {
                if (!f2.getName().endsWith("dot")) continue;
                res.add(f2);
            }
        }
        return res;
    }

    public static void printOutAverageRank(List<String> filterResFolder, String propertyPath) throws Exception {
        LinkedList<String> sysrepResFolder = new LinkedList<String>();
        for (String s2 : filterResFolder) {
            if (s2.indexOf("sysrep") == -1) continue;
            sysrepResFolder.add(s2);
        }
        for (String s2 : sysrepResFolder) {
            File sysrepFolder = new File(s2);
            File parentFile = sysrepFolder.getParentFile();
            File propertyFile = DotFilter_nocombine.findPropertyFile(propertyPath, s2);
            Experiment experiment = new Experiment(propertyFile);
            File startRank = new File(parentFile.toString() + "/start_rank.json");
            FileReader fileReader = new FileReader(startRank);
            JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject)parser.parse(fileReader);
            JSONArray ips = (JSONArray)jsonObj.get("IP Start");
            LinkedList<String> ip = new LinkedList<String>();
            for (int i = 0; i < ips.size(); ++i) {
                ip.add((String)ips.get(i));
            }
            LinkedList<String> file = new LinkedList<String>();
            JSONArray files = (JSONArray)jsonObj.get("File Start");
            for (int i = 0; i < files.size(); ++i) {
                file.add((String)files.get(i));
            }
            LinkedList<String> process = new LinkedList<String>();
            JSONArray processJson = (JSONArray)jsonObj.get("Process Start");
            for (int i = 0; i < processJson.size(); ++i) {
                process.add((String)processJson.get(i));
            }
            String[] importantEntries = experiment.getEntries();
            double average = 0.0;
            for (String r : importantEntries) {
                if (ip.indexOf(r) != -1) {
                    average += (double)(ip.indexOf(r) + 1);
                }
                if (file.indexOf(r) != -1) {
                    average += (double)(file.indexOf(r) + 1);
                }
                if (process.indexOf(r) == -1) continue;
                average += (double)(process.indexOf(r) + 1);
            }
            System.out.println(String.format("%s:%s", s2, Double.toString(average / (double)importantEntries.length)));
        }
    }

    public static void printOutAverageRankOnlyCandidate(List<String> filterResFolder, String propertyPath) throws Exception {
        LinkedList<String> sysrepResFolder = new LinkedList<String>();
        for (String s2 : filterResFolder) {
            if (s2.indexOf("sysrep") == -1) continue;
            sysrepResFolder.add(s2);
        }
        for (String s2 : sysrepResFolder) {
            File sysrepFolder = new File(s2);
            File parentFile = sysrepFolder.getParentFile();
            File propertyFile = DotFilter_nocombine.findPropertyFile(propertyPath, s2);
            Experiment experiment = new Experiment(propertyFile);
            File startRank = new File(parentFile.toString() + "/start_rank.json");
            FileReader fileReader = new FileReader(startRank);
            JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject)parser.parse(fileReader);
            JSONArray ips = (JSONArray)jsonObj.get("IP Start");
            JSONArray files = (JSONArray)jsonObj.get("File Start");
            JSONArray processJson = (JSONArray)jsonObj.get("Process Start");
            List<Map.Entry<String, Double>> sortedIPs = DotFilter_nocombine.sortedEntry(ips, s2);
            List<Map.Entry<String, Double>> sortedFiles = DotFilter_nocombine.sortedEntry(files, s2);
            List<Map.Entry<String, Double>> sortedProcess = DotFilter_nocombine.sortedEntry(processJson, s2);
            ArrayList<Map.Entry<String, Double>> total = new ArrayList<Map.Entry<String, Double>>();
            total.addAll(sortedIPs);
            total.addAll(sortedFiles);
            total.addAll(sortedProcess);
            Collections.sort(total, (a, b) -> ((Double)b.getValue()).compareTo((Double)a.getValue()));
            String[] importantEntries = experiment.getEntries();
            ArrayList<String> topCandidate = new ArrayList<String>();
            for (Map.Entry entry : total) {
                topCandidate.add((String)entry.getKey());
            }
            int numberOfImportant = importantEntries.length;
            double d = 0.0;
            for (int i = 0; i < importantEntries.length; ++i) {
                int idx = topCandidate.indexOf(importantEntries[i]);
                d += (double)idx * 1.0 + 1.0;
            }
            System.out.println(String.format("%s average rank is: %.2f", s2, d / (double)numberOfImportant));
        }
    }

    private static List<Map.Entry<String, Double>> sortedEntry(JSONArray jarr, String sysrepFolder) {
        HashMap<String, Double> map2 = new HashMap<String, Double>();
        File sysFolder = new File(sysrepFolder);
        File caseFolder = sysFolder.getParentFile();
        File reputaitonJson = null;
        for (File f : caseFolder.listFiles()) {
            if (!f.getName().endsWith("reputation.json")) continue;
            reputaitonJson = f;
        }
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject)jsonParser.parse(new FileReader(reputaitonJson));
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < jarr.size(); ++i) {
            String key = (String)jarr.get(i);
            Double reputation = (Double)jsonObject.get(key);
            map2.put(key, reputation);
        }
        ArrayList<Map.Entry<String, Double>> entryList = new ArrayList<Map.Entry<String, Double>>(map2.entrySet());
        Collections.sort(entryList, (a, b) -> ((Double)b.getValue()).compareTo((Double)a.getValue()));
        if (entryList.size() >= 3) {
            return entryList.subList(0, 3);
        }
        return entryList;
    }

    public static void main(String[] args) throws Exception {
        String path = "D:\\2021usenix\\clusterall";
        String propertyPath = "C:\\Users\\fang2\\OneDrive\\Desktop\\reptracker\\reptracker\\input_11\\ndss";
        List<String> filterResFolders = DotFilter_nocombine.getChildrenFolder(path);
        DotFilter_nocombine.printOutAverageRankOnlyCandidate(filterResFolders, propertyPath);
    }
}

