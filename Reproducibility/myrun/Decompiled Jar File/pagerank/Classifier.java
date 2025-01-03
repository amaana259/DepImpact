/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.jgrapht.graph.DirectedPseudograph;
import pagerank.EntityNode;
import pagerank.EventEdge;

public class Classifier {
    DirectedPseudograph<EntityNode, EventEdge> graph;

    public Classifier(DirectedPseudograph<EntityNode, EventEdge> graph) {
        this.graph = graph;
    }

    public List<double[]> createTrainingSet(Map<String, Set<String>> positive) {
        Set s2 = this.graph.edgeSet();
        ArrayList<double[]> l = new ArrayList<double[]>();
        for (EventEdge e : s2) {
            double[] weights = new double[]{e.amountWeight, e.timeWeight, e.structureWeight, positive.containsKey(((EntityNode)this.graph.getEdgeSource(e)).getSignature()) && positive.get(((EntityNode)this.graph.getEdgeSource(e)).getSignature()).contains(((EntityNode)this.graph.getEdgeTarget(e)).getSignature()) ? 1.0 : 0.0};
            l.add(weights);
        }
        return l;
    }

    public Map<String, Set<String>> buildMapFromFile(String path) throws FileNotFoundException, Exception {
        HashMap<String, Set<String>> m3 = new HashMap<String, Set<String>>();
        File f = new File(path);
        if (!f.exists() || !f.isFile()) {
            throw new FileNotFoundException(path);
        }
        FileReader in = new FileReader(f);
        Scanner sc = new Scanner(in);
        HashSet<String> sigs = new HashSet<String>();
        for (EntityNode v : this.graph.vertexSet()) {
            sigs.add(v.getSignature());
        }
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] pair = line.split(",");
            if (pair.length != 2) {
                throw new Exception("Can not parse input file: " + line);
            }
            if (!sigs.contains(pair[0])) {
                throw new Exception("Vertex not found: " + pair[0]);
            }
            if (!sigs.contains(pair[1])) {
                throw new Exception("Vertex not found: " + pair[1]);
            }
            m3.computeIfAbsent(pair[0], k -> new HashSet()).add(pair[1]);
        }
        in.close();
        return m3;
    }

    public void printTrainingSetSVM(List<double[]> l, String path, boolean append) throws IOException {
        File f = new File(path);
        if (!f.exists() || !f.isFile()) {
            f.createNewFile();
        }
        FileWriter fw = new FileWriter(f, append);
        for (double[] edge : l) {
            fw.write(String.format("%d %d:%f %d:%f %d:%f\n", (int)edge[3], 1, edge[0], 2, edge[1], 3, edge[2]));
        }
        fw.close();
    }
}

