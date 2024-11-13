/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package read_dot;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.jgrapht.graph.DirectedPseudograph;
import pagerank.Experiment;
import read_dot.SimpleEdge;
import read_dot.SimpleNode;

public class SeedSetter {
    Random random = new Random();

    public void setSeedReputation(DirectedPseudograph<SimpleNode, SimpleEdge> graph, Experiment exp) {
        LinkedList library = new LinkedList();
        Set nodes = graph.vertexSet();
        List<String> highRP = exp.getHighRP();
        List<String> lowRP = exp.getLowRP();
        for (SimpleNode v : nodes) {
            if (highRP.contains(v.signature) || lowRP.contains(v.signature)) {
                if (highRP.contains(v.signature)) {
                    v.reputation = 1.0;
                }
                if (!lowRP.contains(v.signature)) continue;
                v.reputation = 0.0;
                continue;
            }
            if (graph.incomingEdgesOf(v).size() != 0) continue;
            v.reputation = this.getLibraryReputation("uniform");
        }
    }

    private double getLibraryReputation(String distribution) {
        if (distribution.equals("uniform")) {
            return this.uniformReputation();
        }
        return 0.0;
    }

    private double uniformReputation() {
        int rand = this.random.nextInt(100);
        if (rand <= 19) {
            return 0.3;
        }
        if (rand <= 39) {
            return 0.4;
        }
        if (rand <= 59) {
            return 0.5;
        }
        if (rand <= 79) {
            return 0.6;
        }
        return 0.7;
    }
}

