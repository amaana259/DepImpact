/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jgrapht.graph.DirectedPseudograph;
import pagerank.EntityNode;
import pagerank.EventEdge;
import pagerank.IterateGraph;

public class EigenTrust {
    private DirectedPseudograph graph;
    private Set<EntityNode> preTrust;
    private Set<EntityNode> roots;
    IterateGraph graphiterator;

    public EigenTrust(DirectedPseudograph graph) {
        this.graph = graph;
        this.graphiterator = new IterateGraph(graph);
        this.preTrust = new HashSet<EntityNode>();
        this.roots = new HashSet<EntityNode>();
    }

    public EigenTrust(DirectedPseudograph graph, List<String> nodes) {
        this(graph);
        this.setPreTrust(nodes);
    }

    private boolean setPreTrust(List<String> nodes) {
        if (this.preTrust == null) {
            return false;
        }
        HashSet<String> trustLabels = new HashSet<String>(nodes);
        Set vertices = this.graph.vertexSet();
        for (EntityNode v : vertices) {
            if (!trustLabels.contains(v.getSignature())) continue;
            this.preTrust.add(v);
            System.out.println("pre-trust: " + v.getSignature());
        }
        return true;
    }

    public void initTrust() {
        assert (this.graph != null);
        Set vertices = this.graph.vertexSet();
        double denom = this.preTrust.size() == 0 ? (double)vertices.size() : (double)this.preTrust.size();
        for (EntityNode v : vertices) {
            if (this.graph.incomingEdgesOf(v).size() == 0 || this.preTrust.contains(v)) {
                v.setReputation(1.0 / denom);
            } else {
                v.setReputation(0.0);
            }
            System.out.printf("initial trust of %s: %f\n", v.getSignature(), v.reputation);
        }
    }

    public void initLocalTrust() {
        assert (this.graph != null);
        Set vertices = this.graph.vertexSet();
        for (EntityNode v : vertices) {
            Set incoming = this.graph.incomingEdgesOf(v);
            double total = 0.0;
            for (EventEdge e : incoming) {
                e.weight = e.getSize();
                total += e.weight;
            }
            if (total < 1.0E-8) {
                this.roots.add(v);
                continue;
            }
            for (EventEdge e : incoming) {
                e.weight /= total;
            }
        }
    }

    public void EigenTrustIteration() {
        double dampingFactor = 0.85;
        assert (this.graph != null);
        Set vertexSet = this.graph.vertexSet();
        double fluctuation = 1.0;
        int iterTime = 0;
        System.out.println();
        while (fluctuation >= 1.0E-8) {
            Map<EntityNode, Double> preReputation = this.getReputation();
            double culmativediff = 0.0;
            ++iterTime;
            for (EntityNode v : vertexSet) {
                double rep = 0.0;
                if (this.roots.contains(v)) {
                    for (EntityNode vPre : this.preTrust) {
                        rep += preReputation.get(vPre).doubleValue();
                    }
                    rep /= this.preTrust.size() == 0 ? 1.0 : (double)this.preTrust.size();
                } else {
                    Set edges = this.graph.incomingEdgesOf(v);
                    for (EventEdge edge : edges) {
                        EntityNode source = edge.getSource();
                        rep += preReputation.get(source) * edge.weight;
                    }
                }
                rep = rep * dampingFactor + (1.0 - dampingFactor) * (double)(this.preTrust.contains(v) ? (this.preTrust.size() == 0 ? 0 : 1 / this.preTrust.size()) : 0);
                culmativediff += Math.abs(rep - preReputation.get(v));
                v.setReputation(rep);
            }
            fluctuation = culmativediff;
        }
        System.out.println(String.format("After %d times iteration, the reputation of each vertex is stable", iterTime));
    }

    private Map<EntityNode, Double> getReputation() {
        Set vertexSet = this.graph.vertexSet();
        HashMap<EntityNode, Double> map2 = new HashMap<EntityNode, Double>();
        for (EntityNode node : vertexSet) {
            map2.put(node, node.getReputation());
        }
        return map2;
    }

    public void exportGraph(String path) {
        this.graphiterator.exportGraph(path);
    }
}

