/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package read_dot;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import pagerank.Experiment;
import read_dot.ReadGraphFromDot;

public class CalculateAverageRank {
    public static String findCorrespondingProperty(String caseName, String propertyFolder) throws Exception {
        HashMap res = new HashMap();
        File folder = new File(propertyFolder);
        for (File f : folder.listFiles()) {
            String caseName2 = caseName.split("-")[1];
            if (f.getAbsolutePath().indexOf(caseName2) == -1) continue;
            return f.getAbsolutePath();
        }
        return "Not find the corresponding property file";
    }

    public static void main(String[] args) throws Exception {
        String caseFolder = "D:\\cluster_all_plus_filter";
        String propertyFolder = "C:\\Users\\fang2\\OneDrive\\Desktop\\reptracker\\reptracker\\input_11\\large_properties";
        File folder = new File(caseFolder);
        HashMap<String, String> resFolderToProperty = new HashMap<String, String>();
        for (File f : folder.listFiles()) {
            if (!f.isDirectory()) continue;
            resFolderToProperty.put(f.getAbsolutePath(), CalculateAverageRank.findCorrespondingProperty(f.getAbsolutePath(), propertyFolder));
        }
        String[] category = new String[]{"IP Start", "File Start", "Process Start"};
        JSONObject averageRanking = new JSONObject();
        for (String k : resFolderToProperty.keySet()) {
            int i;
            boolean rankSum = false;
            Experiment exp = new Experiment(new File((String)resFolderToProperty.get(k)));
            String[] entries2 = exp.getEntries();
            ReadGraphFromDot readGraphFromDot = new ReadGraphFromDot();
            String caseName = k.split("-")[1];
            LinkedList<String> vertex = new LinkedList<String>();
            File candidiates = new File(k + "/" + caseName + "_entry_points.json");
            FileReader fileReader = new FileReader(candidiates);
            JSONParser jsonParser = new JSONParser();
            JSONObject candidateJson = (JSONObject)jsonParser.parse(fileReader);
            JSONArray entryPoints = (JSONArray)candidateJson.get("EntryPoints");
            fileReader.close();
            int time = 100;
            LinkedList<Integer> res = new LinkedList<Integer>();
            for (i = 0; i < entryPoints.size(); ++i) {
                vertex.add(entryPoints.get(i).toString());
            }
            for (i = 0; i < time; ++i) {
                Collections.shuffle(vertex);
                for (String entry : entries2) {
                    int index = vertex.indexOf(entry);
                    res.add(index);
                }
            }
            int sum2 = 0;
            for (Integer t : res) {
                sum2 += t.intValue();
            }
            double average = (double)sum2 / ((double)res.size() * 1.0);
            System.out.println(k + " Random Rank: ");
            System.out.println(average);
            JSONObject resJson = new JSONObject();
            resJson.put("Average Random Rank", average);
            resJson.put("Random Times", time);
            File rankRes = new File(k + "/RandomRankAverage_" + caseName + ".json");
            FileWriter fileWriter = new FileWriter(rankRes);
            fileWriter.write(resJson.toJSONString());
            fileWriter.close();
        }
    }
}

