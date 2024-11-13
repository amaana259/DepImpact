/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.clustering.MultiKMeansPlusPlusClusterer;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DirectedPseudograph;
import org.json.simple.JSONArray;
import org.junit.Assert;
import pagerank.EntityNode;
import pagerank.EventEdge;
import pagerank.EventEdgeWrapper;
import pagerank.ForwardAnalysis;
import pagerank.IterateGraph;

public class BackwardPropagate_pf {
    DirectedPseudograph<EntityNode, EventEdge> graph;
    private BigDecimal POITime;
    IterateGraph graphIterator;
    Map<Long, Double> weights;
    Map<Long, Double> timeWeights;
    Map<Long, Double> amountWeights;
    Map<Long, Double> structureWeights;
    double dumpingFactor;
    double detectionSize;
    Set<String> seedSources;
    DirectedPseudograph<EntityNode, EventEdge> originalGraph;
    ForwardAnalysis forwardAnalysis;
    Map<String, Integer> indegree;
    Map<String, Integer> outdegree;

    BackwardPropagate_pf(DirectedPseudograph<EntityNode, EventEdge> input) {
        this.graph = input;
        this.graphIterator = new IterateGraph(this.graph);
        this.weights = new HashMap<Long, Double>();
        this.timeWeights = new HashMap<Long, Double>();
        this.amountWeights = new HashMap<Long, Double>();
        this.structureWeights = new HashMap<Long, Double>();
        this.POITime = this.getPOITime();
        this.dumpingFactor = 0.85;
        this.detectionSize = 0.0;
        this.indegree = new HashMap<String, Integer>();
        this.outdegree = new HashMap<String, Integer>();
    }

    public void setDetectionSize(double value) {
        System.out.println("setDetection invoked: " + value);
        this.detectionSize = value;
    }

    public void calculateWeights() {
        System.out.println("calculateWeights invoked");
        Set vertexSet = this.graph.vertexSet();
        for (EntityNode n : vertexSet) {
            Set inEdges = this.graph.incomingEdgesOf(n);
            for (EventEdge inEdge : inEdges) {
                double timeWeight = this.getTimeWeight(inEdge);
                double dataWeight = this.getAmountWeight(inEdge);
                double structureWeight = this.getStructureWeight(inEdge);
                long edgeID = inEdge.id;
                this.timeWeights.put(edgeID, timeWeight);
                this.amountWeights.put(edgeID, dataWeight);
                this.structureWeights.put(edgeID, structureWeight);
            }
        }
        for (EntityNode n : vertexSet) {
            double structureTotal = this.getStructureWeightTotal(n);
            if (structureTotal < 1.0E-8) {
                structureTotal = 1.0;
            }
            double amountTotal = this.getAmountWeightTotal(n);
            double timeTotal = this.getTimeWeightTotal(n);
            Set outgoing = this.graph.outgoingEdgesOf(n);
            for (EventEdge e : outgoing) {
                e.timeWeight = this.timeWeights.get(e.id) / timeTotal;
                e.amountWeight = this.amountWeights.get(e.id) / amountTotal;
                e.structureWeight = this.structureWeights.get(e.id) / structureTotal;
                if (this.seedSources.contains(e.getSource().getSignature())) {
                    e.structureWeight = 1.0;
                }
                this.timeWeights.put(e.id, e.timeWeight);
                this.amountWeights.put(e.id, e.amountWeight);
                this.structureWeights.put(e.id, e.structureWeight);
            }
        }
        for (EntityNode n : vertexSet) {
            double amount = 0.0;
            Set outEdges = this.graph.outgoingEdgesOf(n);
            for (EventEdge e : outEdges) {
                amount += (double)e.getSize();
            }
            double wTotal = 0.0;
            if (amount < 1.0E-8) {
                for (EventEdge outEdge : outEdges) {
                    wTotal += outEdge.timeWeight * 0.5 + outEdge.structureWeight * 0.5;
                }
                for (EventEdge outEdge : outEdges) {
                    outEdge.weight = (0.5 * outEdge.timeWeight + outEdge.structureWeight * 0.5) / wTotal;
                    this.weights.put(outEdge.id, outEdge.weight);
                }
                continue;
            }
            for (EventEdge outEdge : outEdges) {
                wTotal += outEdge.timeWeight * 0.334 + outEdge.structureWeight * 0.333 + outEdge.amountWeight * 0.333;
            }
            for (EventEdge outEdge : outEdges) {
                outEdge.weight = (0.334 * outEdge.timeWeight + 0.333 * outEdge.structureWeight + 0.333 * outEdge.amountWeight) / wTotal * 0.99;
                this.weights.put(outEdge.id, outEdge.weight);
            }
        }
    }

    public void calculateWeightsRandom() {
        System.out.println("calculateWeightsRandom invoked");
        Set vertexSet = this.graph.vertexSet();
        for (EntityNode n : vertexSet) {
            Set inEdges = this.graph.incomingEdgesOf(n);
            for (EventEdge inEdge : inEdges) {
                double timeWeight = this.getTimeWeight(inEdge);
                double dataWeight = this.getAmountWeight(inEdge);
                double structureWeight = this.getStructureWeight(inEdge);
                long edgeID = inEdge.id;
                this.timeWeights.put(edgeID, timeWeight);
                this.amountWeights.put(edgeID, dataWeight);
                this.structureWeights.put(edgeID, structureWeight);
            }
        }
        for (EntityNode n : vertexSet) {
            double structureTotal = this.getStructureWeightTotal(n);
            if (structureTotal < 1.0E-8) {
                structureTotal = 1.0;
            }
            double amountTotal = this.getAmountWeightTotal(n);
            double timeTotal = this.getTimeWeightTotal(n);
            Set outgoing = this.graph.outgoingEdgesOf(n);
            for (EventEdge e : outgoing) {
                e.timeWeight = this.timeWeights.get(e.id) / timeTotal;
                e.amountWeight = this.amountWeights.get(e.id) / amountTotal;
                e.structureWeight = this.structureWeights.get(e.id) / structureTotal;
                if (this.seedSources.contains(e.getSource().getSignature())) {
                    e.structureWeight = 1.0;
                }
                this.timeWeights.put(e.id, e.timeWeight);
                this.amountWeights.put(e.id, e.amountWeight);
                this.structureWeights.put(e.id, e.structureWeight);
            }
        }
        for (EntityNode n : vertexSet) {
            double[] weightsCof;
            double amount = 0.0;
            Set outEdges = this.graph.outgoingEdgesOf(n);
            for (EventEdge e : outEdges) {
                amount += (double)e.getSize();
            }
            double wTotal = 0.0;
            if (amount < 1.0E-8) {
                weightsCof = this.getNCoefficient(2);
                for (EventEdge outEdge : outEdges) {
                    wTotal += outEdge.timeWeight * weightsCof[0] + outEdge.structureWeight * weightsCof[1];
                }
                for (EventEdge outEdge : outEdges) {
                    outEdge.weight = (weightsCof[0] * outEdge.timeWeight + outEdge.structureWeight * weightsCof[1]) / wTotal;
                    this.weights.put(outEdge.id, outEdge.weight);
                }
                continue;
            }
            weightsCof = this.getNCoefficient(3);
            for (EventEdge outEdge : outEdges) {
                wTotal += outEdge.timeWeight * weightsCof[0] + outEdge.structureWeight * weightsCof[1] + outEdge.amountWeight * weightsCof[2];
            }
            for (EventEdge outEdge : outEdges) {
                outEdge.weight = (outEdge.timeWeight * weightsCof[0] + outEdge.structureWeight * weightsCof[1] + outEdge.amountWeight * weightsCof[2]) / wTotal * 0.99;
                this.weights.put(outEdge.id, outEdge.weight);
            }
        }
    }

    private double getStructureWeightTotal(EntityNode n) {
        double total = 0.0;
        for (EventEdge e : this.graph.outgoingEdgesOf(n)) {
            total += this.structureWeights.get(e.id).doubleValue();
        }
        return total;
    }

    private double getAmountWeightTotal(EntityNode n) {
        double total = 0.0;
        for (EventEdge e : this.graph.outgoingEdgesOf(n)) {
            total += this.amountWeights.get(e.id).doubleValue();
        }
        return total;
    }

    private double getTimeWeightTotal(EntityNode n) {
        double total = 0.0;
        for (EventEdge e : this.graph.outgoingEdgesOf(n)) {
            total += this.timeWeights.get(e.id).doubleValue();
        }
        return total;
    }

    public void calculateWeights_ML_dec(boolean normalizeByOutEdges, int mode, String resDir) {
        Set inEdges;
        System.out.println("calculateWeights_ML_dec invoked: " + normalizeByOutEdges);
        Set vertexSet = this.graph.vertexSet();
        for (EntityNode n : vertexSet) {
            inEdges = this.graph.incomingEdgesOf(n);
            for (EventEdge inEdge : inEdges) {
                this.timeWeights.put(inEdge.id, this.getTimeWeight(inEdge));
                this.amountWeights.put(inEdge.id, this.getAmountWeight(inEdge));
                this.structureWeights.put(inEdge.id, this.getStructureWeight(inEdge));
                this.printEdgeWeights(inEdge);
            }
        }
        this.preprocessWeights(this.timeWeights, normalizeByOutEdges);
        this.preprocessWeights(this.amountWeights, normalizeByOutEdges);
        this.preprocessWeights(this.structureWeights, normalizeByOutEdges);
        for (EntityNode n : vertexSet) {
            inEdges = this.graph.incomingEdgesOf(n);
            for (EventEdge inEdge : inEdges) {
                if (!this.seedSources.contains(inEdge.getSink().getSignature())) continue;
                System.out.println();
                System.out.println("Edges from seed sources: EventEdge " + inEdge.getID() + " (" + inEdge.getSource().getID() + " " + inEdge.getSource().getSignature() + " ->" + inEdge.getEvent() + " " + inEdge.getSink().getID() + " " + inEdge.getSink().getSignature() + ")");
                System.out.println("Normalized structureWeight before hard set: " + this.structureWeights.get(inEdge.id));
                this.structureWeights.put(inEdge.id, 1.0);
                System.out.println("Normalized structureWeight after hard set: " + this.structureWeights.get(inEdge.id));
            }
        }
        for (EntityNode n : vertexSet) {
            inEdges = this.graph.incomingEdgesOf(n);
            for (EventEdge inEdge : inEdges) {
                inEdge.timeWeight = this.timeWeights.get(inEdge.id);
                inEdge.amountWeight = this.amountWeights.get(inEdge.id);
                inEdge.structureWeight = this.structureWeights.get(inEdge.id);
            }
        }
        ArrayList<EventEdge> allEdges = new ArrayList<EventEdge>(this.graph.edgeSet());
        List<Double> finalWeights = null;
        if (mode == 1) {
            System.out.println("computeFinalWeight v1 invoked");
            finalWeights = this.computeFinalWeights(allEdges);
        } else if (mode == 2) {
            System.out.println("computeFinalWeight v2 invoked");
            finalWeights = this.computeFinalWeights_v2(allEdges);
        } else if (mode == 3) {
            System.out.println("computeFinalWeight v3 invoked");
            finalWeights = this.computeFinalWeights_v3(allEdges);
        }
        for (int i = 0; i < allEdges.size(); ++i) {
            ((EventEdge)allEdges.get((int)i)).weight = finalWeights.get(i);
        }
        for (Object n : vertexSet) {
            Set outEdges = this.graph.outgoingEdgesOf((EntityNode)n);
            double weightTotalForOutEdges = 0.0;
            for (EventEdge outEdge : outEdges) {
                weightTotalForOutEdges += outEdge.weight;
            }
            if (weightTotalForOutEdges < 1.0E-8) continue;
            for (EventEdge outEdge : outEdges) {
                outEdge.weight = outEdge.weight / weightTotalForOutEdges * 0.99;
                this.weights.put(outEdge.id, outEdge.weight);
            }
        }
        LinkedList<Double> weightList = new LinkedList<Double>();
        for (EventEdge edge : allEdges) {
            weightList.add(edge.weight);
        }
        try {
            File file = new File(resDir + "/clusterall_weights.json");
            FileWriter fileWriter = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            JSONArray jsonArray = new JSONArray();
            for (Double d : weightList) {
                jsonArray.add(d);
            }
            printWriter.write(jsonArray.toJSONString());
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void calculateWeights_Individual(boolean normalizeByInEdges, String weightType, String resDir) {
        Set inEdges;
        System.out.println("calculateWeights_Individual invoked: " + normalizeByInEdges + " for featureType: " + weightType);
        Set vertexSet = this.graph.vertexSet();
        for (EntityNode n : vertexSet) {
            inEdges = this.graph.incomingEdgesOf(n);
            for (EventEdge inEdge : inEdges) {
                this.timeWeights.put(inEdge.id, this.getTimeWeight(inEdge));
                this.amountWeights.put(inEdge.id, this.getAmountWeight(inEdge));
                this.structureWeights.put(inEdge.id, 0.0);
                this.printEdgeWeights(inEdge);
            }
        }
        this.preprocessWeights(this.timeWeights, normalizeByInEdges);
        this.preprocessWeights(this.amountWeights, normalizeByInEdges);
        this.preprocessWeights(this.structureWeights, normalizeByInEdges);
        for (EntityNode n : vertexSet) {
            inEdges = this.graph.incomingEdgesOf(n);
            for (EventEdge inEdge : inEdges) {
                if (!this.seedSources.contains(inEdge.getSink().getSignature())) continue;
                System.out.println();
                System.out.println("Edges from seed sources: EventEdge " + inEdge.getID() + " (" + inEdge.getSource().getID() + " " + inEdge.getSource().getSignature() + " ->" + inEdge.getEvent() + " " + inEdge.getSink().getID() + " " + inEdge.getSink().getSignature() + ")");
                System.out.println("Normalized structureWeight before hard set: " + this.structureWeights.get(inEdge.id));
                this.structureWeights.put(inEdge.id, 1.0);
                System.out.println("Normalized structureWeight after hard set: " + this.structureWeights.get(inEdge.id));
            }
        }
        for (EntityNode n : vertexSet) {
            inEdges = this.graph.incomingEdgesOf(n);
            for (EventEdge inEdge : inEdges) {
                inEdge.timeWeight = this.timeWeights.get(inEdge.id);
                inEdge.amountWeight = this.amountWeights.get(inEdge.id);
                inEdge.structureWeight = this.structureWeights.get(inEdge.id);
            }
        }
        ArrayList<EventEdge> allEdges = new ArrayList<EventEdge>(this.graph.edgeSet());
        List<Double> finalWeights = this.computeFinalWeights(allEdges);
        for (int i = 0; i < allEdges.size(); ++i) {
            ((EventEdge)allEdges.get((int)i)).weight = finalWeights.get(i);
        }
        for (EntityNode n : vertexSet) {
            Set outgoingEdges = this.graph.outgoingEdgesOf(n);
            double weightTotalForOutEdges = 0.0;
            for (EventEdge outEdge : outgoingEdges) {
                weightTotalForOutEdges += outEdge.weight;
            }
            if (weightTotalForOutEdges < 1.0E-8) continue;
            for (EventEdge outEdge : outgoingEdges) {
                outEdge.weight /= weightTotalForOutEdges;
                this.weights.put(outEdge.id, outEdge.weight);
            }
        }
        try {
            File file = new File(resDir + "/" + weightType + "_weights.txt");
            FileWriter fileWriter = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            JSONArray jsonArray = new JSONArray();
            for (Double d : finalWeights) {
                jsonArray.add(d);
            }
            printWriter.write(jsonArray.toJSONString());
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void calculateWeights_Fanout(boolean normalizeByOutEdges, String resDir) {
        System.out.println("calculateWeights_Fanout invoked: " + normalizeByOutEdges);
        Set vertexSet = this.graph.vertexSet();
        for (EntityNode v : vertexSet) {
            int idegree = this.graph.inDegreeOf(v);
            int odegree = this.graph.outDegreeOf(v);
            this.indegree.put(v.getSignature(), idegree);
            this.outdegree.put(v.getSignature(), odegree);
        }
        HashMap fanoutWeights = new HashMap();
        Set edges = this.graph.edgeSet();
        LinkedList<Double> finalWeights = new LinkedList<Double>();
        for (EventEdge e : edges) {
            finalWeights.add(this.getFanoutWeight(e));
        }
        try {
            File file = new File(resDir + "/fanout_weights.txt");
            FileWriter fileWriter = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            JSONArray jsonArray = new JSONArray();
            for (Double d : finalWeights) {
                jsonArray.add(d);
            }
            printWriter.write(jsonArray.toJSONString());
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Write weights to file for fanout!");
    }

    private List<Double> computeFinalWeights(List<EventEdge> allEdges) {
        System.out.println("computeFinalWeights invoked!");
        List<Cluster<EventEdgeWrapper>> clusterResults = this.clusterEdges(allEdges, "multiKmeansPlusPlus");
        List<Double> finalWeights = this.dimReduction(allEdges, clusterResults);
        this.scaleRange(finalWeights);
        return finalWeights;
    }

    private List<Double> computeFinalWeights_v2(List<EventEdge> allEdges) {
        System.out.println("computeFinalWeights_v2 invoked!");
        ArrayList<EventEdge> nonOutlierEdges = new ArrayList<EventEdge>();
        System.out.println();
        for (EventEdge edge : allEdges) {
            if (this.graph.incomingEdgesOf(edge.getSink()).size() > 1) {
                nonOutlierEdges.add(edge);
                continue;
            }
            System.out.print("Outlier edge: ");
            this.printEdgeWeights(edge);
        }
        List<Cluster<EventEdgeWrapper>> clusterResults = this.clusterEdges(nonOutlierEdges, "multiKmeansPlusPlus");
        List<Double> finalWeights = this.dimReduction(allEdges, clusterResults);
        this.scaleRange(finalWeights);
        return finalWeights;
    }

    private List<Double> computeFinalWeights_v3(List<EventEdge> allEdges) {
        System.out.println("computeFinalWeights_v3 invoked!");
        ArrayList<Double> finalWeights = new ArrayList<Double>();
        for (int i = 0; i < allEdges.size(); ++i) {
            finalWeights.add(0.0);
        }
        Set vertexSet = this.graph.vertexSet();
        for (EntityNode n : vertexSet) {
            ArrayList<EventEdge> outEdges = new ArrayList<EventEdge>(this.graph.outgoingEdgesOf(n));
            if (outEdges.size() == 0) {
                System.out.println("No outgoing edges");
                continue;
            }
            if (outEdges.size() == 1) {
                System.out.println("Only 1 outgoing edge (outlier edge)");
                finalWeights.set(allEdges.indexOf(outEdges.get(0)), 1.0);
                continue;
            }
            List<Cluster<EventEdgeWrapper>> clusterResults = this.clusterEdges(outEdges, "multiKmeansPlusPlus");
            if (clusterResults.size() == 1) {
                double amount = 0.0;
                for (EventEdge eventEdge : outEdges) {
                    amount += (double)eventEdge.getSize();
                }
                double wTotal = 0.0;
                if (amount < 1.0E-8) {
                    for (EventEdge outEdge : outEdges) {
                        finalWeights.set(allEdges.indexOf(outEdge), 0.5 * outEdge.timeWeight + 0.5 * outEdge.structureWeight);
                    }
                    continue;
                }
                for (EventEdge outEdge : outEdges) {
                    finalWeights.set(allEdges.indexOf(outEdge), 0.3333 * outEdge.timeWeight + 0.3333 * outEdge.structureWeight + 0.3334 * outEdge.amountWeight);
                }
                continue;
            }
            List<Double> weightsForOutEdges = this.dimReduction(outEdges, clusterResults);
            System.out.println("weights before scaling: " + weightsForOutEdges.toString());
            this.scaleRange(weightsForOutEdges);
            System.out.println("weights after scaling: " + weightsForOutEdges.toString());
            double weightTotalForOutEdges = 0.0;
            for (double weight : weightsForOutEdges) {
                weightTotalForOutEdges += weight;
            }
            System.out.println("Total weight for outgoing edges: " + weightTotalForOutEdges);
            ArrayList<Double> arrayList = new ArrayList<Double>();
            if (weightTotalForOutEdges > 1.0E-8) {
                for (double weight : weightsForOutEdges) {
                    arrayList.add(weight / (1.0 * weightTotalForOutEdges));
                }
                System.out.println("weights after normalizing by outgoing edges: " + ((Object)arrayList).toString());
            }
            for (EventEdge edge : outEdges) {
                finalWeights.set(allEdges.indexOf(edge), weightsForOutEdges.get(outEdges.indexOf(edge)));
            }
        }
        return finalWeights;
    }

    private List<Double> computeFinalWeights_v3_Individual(List<EventEdge> allEdges, String weightType) {
        System.out.println("computeFinalWeights_v3_Individual invoked!");
        ArrayList<Double> finalWeights = new ArrayList<Double>();
        for (int i = 0; i < allEdges.size(); ++i) {
            finalWeights.add(0.0);
        }
        Set vertexSet = this.graph.vertexSet();
        for (EntityNode n : vertexSet) {
            ArrayList inEdges = new ArrayList(this.graph.incomingEdgesOf(n));
            if (inEdges.size() == 0) {
                System.out.println("No incoming edges");
                continue;
            }
            if (inEdges.size() == 1) {
                System.out.println("Only 1 incoming edge (outlier edge)");
                finalWeights.set(allEdges.indexOf(inEdges.get(0)), 1.0);
                continue;
            }
            if (weightType.equals("timeWeight")) {
                for (EventEdge edge : inEdges) {
                    finalWeights.set(allEdges.indexOf(edge), edge.timeWeight);
                }
                continue;
            }
            if (weightType.equals("amountWeight")) {
                for (EventEdge edge : inEdges) {
                    finalWeights.set(allEdges.indexOf(edge), edge.amountWeight);
                }
                continue;
            }
            if (weightType.equals("structureWeight")) {
                for (EventEdge edge : inEdges) {
                    finalWeights.set(allEdges.indexOf(edge), edge.structureWeight);
                }
                continue;
            }
            System.out.println("Unsupported weightType: " + weightType);
        }
        return finalWeights;
    }

    private List<Cluster<EventEdgeWrapper>> clusterEdges(List<EventEdge> edges, String clusteringMethod) {
        KMeansPlusPlusClusterer<EventEdgeWrapper> kmeansPlusPlusClusterer;
        ArrayList<EventEdgeWrapper> allEdgeWrappers = new ArrayList<EventEdgeWrapper>();
        for (EventEdge edge : edges) {
            allEdgeWrappers.add(new EventEdgeWrapper(edge));
        }
        ArrayList<Cluster<EventEdgeWrapper>> clusterResults = null;
        if (clusteringMethod.equals("kmeansPlusPlus")) {
            kmeansPlusPlusClusterer = new KMeansPlusPlusClusterer<EventEdgeWrapper>(2, 100000);
            clusterResults = new ArrayList(kmeansPlusPlusClusterer.cluster(allEdgeWrappers));
        } else if (clusteringMethod.equals("dbscan")) {
            DBSCANClusterer<EventEdgeWrapper> dbscanClusterer = new DBSCANClusterer<EventEdgeWrapper>(4.0, 1);
            clusterResults = dbscanClusterer.cluster(allEdgeWrappers);
        } else if (clusteringMethod.equals("multiKmeansPlusPlus")) {
            kmeansPlusPlusClusterer = new KMeansPlusPlusClusterer(2, 100000);
            MultiKMeansPlusPlusClusterer<EventEdgeWrapper> multiKmeansPlusPlusClusterer = new MultiKMeansPlusPlusClusterer<EventEdgeWrapper>(kmeansPlusPlusClusterer, 20);
            clusterResults = new ArrayList(multiKmeansPlusPlusClusterer.cluster(allEdgeWrappers));
        } else {
            System.out.println("Do not support the clustering method " + clusteringMethod);
        }
        this.printClusterResults(clusteringMethod, clusterResults);
        return clusterResults;
    }

    private List<Double> dimReduction(List<EventEdge> allEdges, List<Cluster<EventEdgeWrapper>> clusterResults) {
        EventEdge edge;
        int i;
        assert (clusterResults.size() == 2);
        double[][] weights2DArrayAll = new double[allEdges.size()][];
        double[][] weights2DArrayG0 = new double[clusterResults.get(0).getPoints().size()][];
        double[][] weights2DArrayG1 = new double[clusterResults.get(1).getPoints().size()][];
        boolean seedEdgeInG0 = false;
        boolean seedEdgeInG1 = false;
        for (i = 0; i < allEdges.size(); ++i) {
            edge = allEdges.get(i);
            weights2DArrayAll[i] = new double[]{edge.timeWeight, edge.amountWeight, edge.structureWeight};
        }
        for (i = 0; i < clusterResults.get(0).getPoints().size(); ++i) {
            edge = clusterResults.get(0).getPoints().get(i).getEventEdge();
            weights2DArrayG0[i] = new double[]{edge.timeWeight, edge.amountWeight, edge.structureWeight};
            if (!this.seedSources.contains(edge.getSource().getSignature())) continue;
            seedEdgeInG0 = true;
        }
        for (i = 0; i < clusterResults.get(1).getPoints().size(); ++i) {
            edge = clusterResults.get(1).getPoints().get(i).getEventEdge();
            weights2DArrayG1[i] = new double[]{edge.timeWeight, edge.amountWeight, edge.structureWeight};
            if (!this.seedSources.contains(edge.getSource().getSignature())) continue;
            seedEdgeInG1 = true;
        }
        Array2DRowRealMatrix weightsMatrixAll = new Array2DRowRealMatrix(weights2DArrayAll);
        Array2DRowRealMatrix weightsMatrixG0 = new Array2DRowRealMatrix(weights2DArrayG0);
        Array2DRowRealMatrix weightsMatrixG1 = new Array2DRowRealMatrix(weights2DArrayG1);
        RealVector projectionVector = this.computeProjectionVector(weightsMatrixG0, weightsMatrixG1);
        System.out.println("projectionVector before adjusting direction:");
        this.printRealVector(projectionVector);
        System.out.println();
        this.adjustProjectionVectorDirection(projectionVector, weightsMatrixG0, weightsMatrixG1, seedEdgeInG0, seedEdgeInG1);
        System.out.println("projectionVector after adjusting direction:");
        this.printRealVector(projectionVector);
        System.out.println();
        System.out.println("weightsMatrixAll:");
        this.printRealMatrix(weightsMatrixAll);
        System.out.println();
        RealVector weightsProjectedAll = weightsMatrixAll.operate(projectionVector);
        double[] finalWeights = weightsProjectedAll.toArray();
        return new ArrayList<Double>(Arrays.asList(ArrayUtils.toObject(finalWeights)));
    }

    private RealVector computeProjectionVector(RealMatrix matrixG0, RealMatrix matrixG1) {
        int i;
        RealVector mu0 = new ArrayRealVector(new double[]{0.0, 0.0, 0.0});
        for (int i2 = 0; i2 < matrixG0.getRowDimension(); ++i2) {
            mu0 = mu0.add(matrixG0.getRowVector(i2));
        }
        mu0.mapDivideToSelf(matrixG0.getRowDimension());
        RealVector mu1 = new ArrayRealVector(new double[]{0.0, 0.0, 0.0});
        for (int i3 = 0; i3 < matrixG1.getRowDimension(); ++i3) {
            mu1 = ((RealVector)mu1).add(matrixG1.getRowVector(i3));
        }
        ((RealVector)mu1).mapDivideToSelf(matrixG1.getRowDimension());
        System.out.println("Mean vector of group 0 mu0:");
        this.printRealVector(mu0);
        System.out.println();
        System.out.println("Mean vector of group 1 mu1:");
        this.printRealVector(mu1);
        System.out.println();
        RealMatrix sw = new Array2DRowRealMatrix(3, 3);
        for (i = 0; i < matrixG0.getRowDimension(); ++i) {
            sw = sw.add(matrixG0.getRowVector(i).subtract(mu0).outerProduct(matrixG0.getRowVector(i).subtract(mu0)));
        }
        for (i = 0; i < matrixG1.getRowDimension(); ++i) {
            sw = sw.add(matrixG1.getRowVector(i).subtract(mu1).outerProduct(matrixG1.getRowVector(i).subtract(mu1)));
        }
        System.out.println("Within-group scattering matrix sw:");
        this.printRealMatrix(sw);
        System.out.println();
        RealMatrix sb = mu0.subtract(mu1).outerProduct(mu0.subtract(mu1));
        System.out.println("Between-group scattering matrix sb:");
        this.printRealMatrix(sb);
        System.out.println();
        DecompositionSolver solver = new SingularValueDecomposition(sw).getSolver();
        RealMatrix swInv = solver.getInverse();
        System.out.println("MP pseudo-inverse of sw, swInv:");
        this.printRealMatrix(swInv);
        System.out.println();
        boolean isSwSingular = !solver.isNonSingular();
        RealVector projectionVector = null;
        if (isSwSingular) {
            System.out.println("sw is singular");
            if (matrixG0.getRowDimension() == 1 && matrixG1.getRowDimension() == 1) {
                System.out.println("Both group 0 and group 1 only have 1 edge. Singular sw = matrix(0)");
            }
            if (sw.getRow(2)[0] == 0.0 && sw.getRow(2)[1] == 0.0 && sw.getRow(2)[2] == 0.0) {
                System.out.println("3rd row of sw is all-zero");
            }
            RealVector projectionVectorCandidate1 = mu0.subtract(mu1);
            projectionVectorCandidate1.mapDivideToSelf(projectionVectorCandidate1.getNorm());
            double fisherObjectiveNumerator1 = this.fisherObjectiveNumerator(sb, projectionVectorCandidate1);
            System.out.println("Fisher objective numerator for candidate projection vector (mu0-mu1)/norm is:" + fisherObjectiveNumerator1);
            System.out.println("Fisher objective denominator for candidate projection vector (mu0-mu1)/norm is:" + this.fisherObjectiveDenominator(sw, projectionVectorCandidate1));
            RealVector projectionVectorCandidate2 = swInv.operate(mu0.subtract(mu1));
            projectionVectorCandidate2.mapDivideToSelf(projectionVectorCandidate2.getNorm());
            double fisherObjectiveNumerator2 = this.fisherObjectiveNumerator(sb, projectionVectorCandidate2);
            System.out.println("Fisher objective numerator for candidate projection vector (swInv*(mu0-mu1))/norm is:" + fisherObjectiveNumerator2);
            System.out.println("Fisher objective denominator for candidate projection vector (swInv*(mu0-mu1))/norm is:" + this.fisherObjectiveDenominator(sw, projectionVectorCandidate2));
            if (fisherObjectiveNumerator1 > fisherObjectiveNumerator2) {
                System.out.println("projection vector = (mu0-mu1)/norm");
                projectionVector = projectionVectorCandidate1;
            } else if (fisherObjectiveNumerator2 > fisherObjectiveNumerator1) {
                System.out.println("projection vector = (swInv*(mu0-mu1))/norm");
                projectionVector = projectionVectorCandidate2;
            } else {
                System.out.println("projection vector = (mu0-mu1)/norm");
                projectionVector = projectionVectorCandidate1;
            }
        } else {
            System.out.println("sw is non-singular");
            System.out.println("projection vector = swInv*(mu0-mu1)");
            projectionVector = swInv.operate(mu0.subtract(mu1));
            projectionVector.mapDivideToSelf(projectionVector.getNorm());
        }
        Assert.assertNotNull(projectionVector);
        System.out.println("projectionVector after self-normalization:");
        this.printRealVector(projectionVector);
        System.out.println();
        return projectionVector;
    }

    private RealVector computeProjectionVector_old_v2(RealMatrix matrixG0, RealMatrix matrixG1) {
        int i;
        RealVector mu0 = new ArrayRealVector(new double[]{0.0, 0.0, 0.0});
        for (int i2 = 0; i2 < matrixG0.getRowDimension(); ++i2) {
            mu0 = mu0.add(matrixG0.getRowVector(i2));
        }
        mu0.mapDivideToSelf(matrixG0.getRowDimension());
        RealVector mu1 = new ArrayRealVector(new double[]{0.0, 0.0, 0.0});
        for (int i3 = 0; i3 < matrixG1.getRowDimension(); ++i3) {
            mu1 = ((RealVector)mu1).add(matrixG1.getRowVector(i3));
        }
        ((RealVector)mu1).mapDivideToSelf(matrixG1.getRowDimension());
        System.out.println("Mean vector of group 0 mu0:");
        this.printRealVector(mu0);
        System.out.println();
        System.out.println("Mean vector of group 1 mu1:");
        this.printRealVector(mu1);
        System.out.println();
        RealMatrix sw = new Array2DRowRealMatrix(3, 3);
        for (i = 0; i < matrixG0.getRowDimension(); ++i) {
            sw = sw.add(matrixG0.getRowVector(i).subtract(mu0).outerProduct(matrixG0.getRowVector(i).subtract(mu0)));
        }
        for (i = 0; i < matrixG1.getRowDimension(); ++i) {
            sw = sw.add(matrixG1.getRowVector(i).subtract(mu1).outerProduct(matrixG1.getRowVector(i).subtract(mu1)));
        }
        System.out.println("Within-group scattering matrix sw:");
        this.printRealMatrix(sw);
        System.out.println();
        RealMatrix sb = mu0.subtract(mu1).outerProduct(mu0.subtract(mu1));
        System.out.println("Between-group scattering matrix sb:");
        this.printRealMatrix(sb);
        System.out.println();
        DecompositionSolver solver = new SingularValueDecomposition(sw).getSolver();
        RealMatrix swInv = solver.getInverse();
        System.out.println("MP pseudo-inverse of sw, swInv:");
        this.printRealMatrix(swInv);
        System.out.println();
        boolean isSwSingular = !solver.isNonSingular();
        RealVector projectionVector = null;
        if (isSwSingular) {
            System.out.println("sw is singular");
            if (matrixG0.getRowDimension() == 1 && matrixG1.getRowDimension() == 1) {
                System.out.println("Both group 0 and group 1 only have 1 edge. Singular sw = matrix(0)");
            }
            if (sw.getRow(2)[0] == 0.0 && sw.getRow(2)[1] == 0.0 && sw.getRow(2)[2] == 0.0) {
                System.out.println("3rd row of sw is all-zero");
            }
            ArrayRealVector projectionVectorCandidate1 = new ArrayRealVector(new double[]{0.1, 0.5, 0.4});
            ((RealVector)projectionVectorCandidate1).mapDivideToSelf(((RealVector)projectionVectorCandidate1).getNorm());
            double fisherObjectiveNumerator1 = this.fisherObjectiveNumerator(sb, projectionVectorCandidate1);
            System.out.println("Fisher objective numerator for candidate projection vector (0.1, 0.5, 0.4)/norm is:" + fisherObjectiveNumerator1);
            System.out.println("Fisher objective denominator for candidate projection vector (0.1, 0.5, 0.4)/norm is:" + this.fisherObjectiveDenominator(sw, projectionVectorCandidate1));
            RealVector projectionVectorCandidate2 = mu0.subtract(mu1);
            projectionVectorCandidate2.mapDivideToSelf(projectionVectorCandidate2.getNorm());
            double fisherObjectiveNumerator2 = this.fisherObjectiveNumerator(sb, projectionVectorCandidate2);
            System.out.println("Fisher objective numerator for candidate projection vector (mu0-mu1)/norm is:" + fisherObjectiveNumerator2);
            System.out.println("Fisher objective denominator for candidate projection vector (mu0-mu1)/norm is:" + this.fisherObjectiveDenominator(sw, projectionVectorCandidate2));
            RealVector projectionVectorCandidate3 = swInv.operate(mu0.subtract(mu1));
            projectionVectorCandidate3.mapDivideToSelf(projectionVectorCandidate3.getNorm());
            double fisherObjectiveNumerator3 = this.fisherObjectiveNumerator(sb, projectionVectorCandidate3);
            System.out.println("Fisher objective numerator for candidate projection vector (swInv*(mu0-mu1))/norm is:" + fisherObjectiveNumerator3);
            System.out.println("Fisher objective denominator for candidate projection vector (swInv*(mu0-mu1))/norm is:" + this.fisherObjectiveDenominator(sw, projectionVectorCandidate3));
            if (fisherObjectiveNumerator1 > fisherObjectiveNumerator2 && fisherObjectiveNumerator1 > fisherObjectiveNumerator3) {
                System.out.println("projection vector = (0.1, 0.5, 0.4)/norm");
                projectionVector = projectionVectorCandidate1;
            } else if (fisherObjectiveNumerator2 > fisherObjectiveNumerator1 && fisherObjectiveNumerator2 > fisherObjectiveNumerator3) {
                System.out.println("projection vector = (mu0-mu1)/norm");
                projectionVector = projectionVectorCandidate2;
            } else if (fisherObjectiveNumerator3 > fisherObjectiveNumerator1 && fisherObjectiveNumerator3 > fisherObjectiveNumerator2) {
                System.out.println("projection vector = (swInv*(mu0-mu1))/norm");
                projectionVector = projectionVectorCandidate3;
            } else {
                System.out.println("projection vector = (mu0-mu1)/norm");
                projectionVector = projectionVectorCandidate2;
            }
        } else {
            System.out.println("sw is non-singular");
            System.out.println("projection vector = swInv*(mu0-mu1)");
            projectionVector = swInv.operate(mu0.subtract(mu1));
            projectionVector.mapDivideToSelf(projectionVector.getNorm());
        }
        Assert.assertNotNull(projectionVector);
        System.out.println("projectionVector after self-normalization:");
        this.printRealVector(projectionVector);
        System.out.println();
        return projectionVector;
    }

    private RealVector computeProjectionVector_old_v1(RealMatrix matrixG0, RealMatrix matrixG1) {
        RealVector projectionVector;
        RealVector mu0 = new ArrayRealVector(new double[]{0.0, 0.0, 0.0});
        for (int i = 0; i < matrixG0.getRowDimension(); ++i) {
            mu0 = mu0.add(matrixG0.getRowVector(i));
        }
        mu0.mapDivideToSelf(matrixG0.getRowDimension());
        RealVector mu1 = new ArrayRealVector(new double[]{0.0, 0.0, 0.0});
        for (int i = 0; i < matrixG1.getRowDimension(); ++i) {
            mu1 = ((RealVector)mu1).add(matrixG1.getRowVector(i));
        }
        ((RealVector)mu1).mapDivideToSelf(matrixG1.getRowDimension());
        System.out.println("Mean vector of group 0 mu0:");
        this.printRealVector(mu0);
        System.out.println("Mean vector of group 1 mu1:");
        this.printRealVector(mu1);
        if (matrixG0.getRowDimension() == 1 && matrixG1.getRowDimension() == 1) {
            projectionVector = mu0.subtract(mu1);
        } else {
            int i;
            RealMatrix sw = new Array2DRowRealMatrix(3, 3);
            for (i = 0; i < matrixG0.getRowDimension(); ++i) {
                sw = sw.add(matrixG0.getRowVector(i).subtract(mu0).outerProduct(matrixG0.getRowVector(i).subtract(mu0)));
            }
            for (i = 0; i < matrixG1.getRowDimension(); ++i) {
                sw = sw.add(matrixG1.getRowVector(i).subtract(mu1).outerProduct(matrixG1.getRowVector(i).subtract(mu1)));
            }
            System.out.println("Within-group scattering matrix sw:");
            this.printRealMatrix(sw);
            DecompositionSolver solver = new SingularValueDecomposition(sw).getSolver();
            RealMatrix swInv = solver.getInverse();
            System.out.println("swInv:");
            this.printRealMatrix(swInv);
            projectionVector = swInv.operate(mu0.subtract(mu1));
        }
        projectionVector.mapDivideToSelf(projectionVector.getNorm());
        return projectionVector;
    }

    private double fisherObjective(RealMatrix sb, RealMatrix sw, RealVector v) {
        double numerator = sb.preMultiply(v).dotProduct(v);
        double denominator = sw.preMultiply(v).dotProduct(v);
        System.out.println("v^T*sb*v: " + numerator);
        System.out.println("v^T*sw*v: " + denominator);
        return numerator / denominator;
    }

    private double fisherObjectiveNumerator(RealMatrix sb, RealVector v) {
        return sb.preMultiply(v).dotProduct(v);
    }

    private double fisherObjectiveDenominator(RealMatrix sw, RealVector v) {
        return sw.preMultiply(v).dotProduct(v);
    }

    private void adjustProjectionVectorDirection(RealVector projectionVector, RealMatrix matrixG0, RealMatrix matrixG1, boolean seedEdgeInG0, boolean seedEdgeInG1) {
        if (projectionVector.getEntry(0) <= 0.0 && projectionVector.getEntry(1) <= 0.0 && projectionVector.getEntry(2) <= 0.0) {
            System.out.println("All three dimensions of projection vector are non-positive. Negate the vector.");
            projectionVector.mapMultiplyToSelf(-1.0);
        } else if (projectionVector.getEntry(0) >= 0.0 && projectionVector.getEntry(1) >= 0.0 && projectionVector.getEntry(2) >= 0.0) {
            System.out.println("All three dimensions of projection vector are non-negative. Don't negate the vector.");
        } else {
            System.out.println("One or two dimensions of projection vector are negative. Negate by condition.");
            RealVector mu0 = new ArrayRealVector(new double[]{0.0, 0.0, 0.0});
            for (int i = 0; i < matrixG0.getRowDimension(); ++i) {
                mu0 = mu0.add(matrixG0.getRowVector(i));
            }
            mu0.mapDivideToSelf(matrixG0.getRowDimension());
            RealVector mu1 = new ArrayRealVector(new double[]{0.0, 0.0, 0.0});
            for (int i = 0; i < matrixG1.getRowDimension(); ++i) {
                mu1 = mu1.add(matrixG1.getRowVector(i));
            }
            mu1.mapDivideToSelf(matrixG1.getRowDimension());
            if (seedEdgeInG0 && !seedEdgeInG1) {
                System.out.println("Cluster 0 has seed edges but cluster 1 hasn't.");
                if (mu0.dotProduct(projectionVector) < mu1.dotProduct(projectionVector)) {
                    System.out.println("Negate projection vector to make sure that the cluster that contains seed edges has a higher projected mean.");
                    projectionVector.mapMultiplyToSelf(-1.0);
                }
            } else if (seedEdgeInG1 && !seedEdgeInG0) {
                System.out.println("Cluster 1 has seed edges but cluster 0 hasn't.");
                if (mu1.dotProduct(projectionVector) < mu0.dotProduct(projectionVector)) {
                    System.out.println("Negate projection vector to make sure that the cluster that contains seed edges has a higher projected mean.");
                    projectionVector.mapMultiplyToSelf(-1.0);
                }
            } else {
                if (seedEdgeInG0 && seedEdgeInG1) {
                    System.out.println("Cluster 0 and 1 both contain/don't contain seed edges.");
                } else if (!seedEdgeInG0 && !seedEdgeInG1) {
                    System.out.println("Cluster 0 and 1 both don't contain seed edges.");
                }
                if (matrixG0.getRowDimension() < matrixG1.getRowDimension() && mu0.dotProduct(projectionVector) < mu1.dotProduct(projectionVector)) {
                    System.out.println("Negate projection vector to make sure that the cluster 0 that contains fewer edges has a higher projected mean.");
                    projectionVector.mapMultiplyToSelf(-1.0);
                } else if (matrixG1.getRowDimension() < matrixG0.getRowDimension() && mu1.dotProduct(projectionVector) < mu0.dotProduct(projectionVector)) {
                    System.out.println("Negate projection vector to make sure that the cluster 1 that contains fewer edges has a higher projected mean.");
                    projectionVector.mapMultiplyToSelf(-1.0);
                }
            }
        }
    }

    private double getCombineWeight(EventEdge edge, double timeTotal, double amountTotal, double structureTotal) {
        return 0.1 * (edge.timeWeight / timeTotal) + 0.5 * (edge.amountWeight / amountTotal) + 0.4 * (edge.structureWeight / structureTotal);
    }

    private double getStructureWeight_old_v1(EventEdge e) {
        EntityNode source = e.getSource();
        EntityNode target = e.getSink();
        double numOfInEdgesOfTarget = (double)this.graph.incomingEdgesOf(target).size() * 1.0;
        double weight = 0.0;
        double numOfInEdgesOfSource = (double)this.graph.incomingEdgesOf(source).size() * 1.0;
        weight = numOfInEdgesOfSource == 0.0 ? 1.0 / numOfInEdgesOfTarget : 1.0 / numOfInEdgesOfTarget + 1.0 / numOfInEdgesOfSource;
        if (weight == 0.0) {
            System.out.println("numOfInEdgesOfSource: " + numOfInEdgesOfSource);
            System.out.println("numOfInEdgesOfTarget: " + numOfInEdgesOfTarget);
        }
        return weight;
    }

    private double getStructureWeight_old_v2(EventEdge e) {
        EntityNode source = e.getSource();
        int incomingNumber = this.graph.inDegreeOf(source);
        if (incomingNumber == 0) {
            return 0.0;
        }
        return 1.0 / ((double)incomingNumber * 1.0);
    }

    public void setSeedSources(Set<String> set) {
        System.out.println("setSeedSource invoked!");
        this.seedSources = set;
    }

    private double getStructureWeightForward(EventEdge e) {
        EntityNode source = e.getSource();
        if (this.seedSources.contains(source.getSignature())) {
            return (double)this.graph.edgeSet().size() * 1.0;
        }
        int inDegree = this.graph.inDegreeOf(source);
        int outDegree = this.graph.outDegreeOf(source);
        return (double)inDegree / ((double)outDegree * 1.0);
    }

    private double getStructureWeight(EventEdge e) {
        EntityNode sink = e.getSink();
        int inDegree = this.graph.inDegreeOf(sink);
        int outDegree = this.graph.outDegreeOf(sink);
        if (this.seedSources.contains(sink.getSignature())) {
            return (double)this.graph.edgeSet().size() * 1.0;
        }
        return (double)outDegree / ((double)inDegree * 1.0);
    }

    private double getFanoutWeight(EventEdge e) {
        EntityNode source = e.getSource();
        int inDegree = this.indegree.get(source.getSignature());
        int outDegree = this.outdegree.get(source.getSignature());
        double offset = 1.0E-6;
        if (source.getF() != null && inDegree == 0) {
            return 0.0 + offset;
        }
        return 1.0 / ((double)outDegree * 1.0) + offset;
    }

    private double getWeightAboueEdgesNumber(EntityNode e) {
        double weightBasedOnEdgeNumber = 0.0;
        Set<EntityNode> sourceOfIncoming = this.getSources(e);
        for (EntityNode node : sourceOfIncoming) {
            Set sourceFornode = this.graph.incomingEdgesOf(node);
            if (sourceFornode.size() == 0) {
                weightBasedOnEdgeNumber += 1.0 / ((double)sourceOfIncoming.size() * 1.0);
                continue;
            }
            weightBasedOnEdgeNumber += 1.0 / ((double)sourceOfIncoming.size() * 1.0) + 1.0 / ((double)sourceFornode.size() * 1.0);
        }
        return weightBasedOnEdgeNumber;
    }

    public void PageRankIteration(String detection) {
        Set vertexSet = this.graph.vertexSet();
        double fluctuation = 1.0;
        int iterTime = 0;
        System.out.println();
        while (fluctuation >= 1.0E-13) {
            double culmativediff = 0.0;
            ++iterTime;
            Map<Long, Double> preReputation = this.getReputation();
            for (EntityNode v : vertexSet) {
                Set edges;
                if (v.getSignature().equals(detection)) {
                    System.out.println(v.reputation);
                }
                if ((edges = this.graph.incomingEdgesOf(v)).size() == 0) continue;
                double rep = 0.0;
                for (EventEdge edge : edges) {
                    EntityNode source = edge.getSource();
                    int numberOfOutEgeFromSource = this.graph.outDegreeOf(source);
                    double total_weight = 0.0;
                    rep += preReputation.get(source.getID()) * this.weights.get(edge.id);
                }
                rep = rep * this.dumpingFactor + (1.0 - this.dumpingFactor) / (double)vertexSet.size();
                culmativediff += Math.abs(rep - preReputation.get(v.getID()));
                v.setReputation(rep);
            }
            fluctuation = culmativediff;
        }
        System.out.println(String.format("After %d times iteration, the reputation of each vertex is stable", iterTime));
    }

    public void PageRankIterationBackward(String[] highRP, String[] midRP, String[] lowRP, String detection) {
        double alarmlevel = 0.85;
        Set vertexSet = this.graph.vertexSet();
        HashSet<String> sources = new HashSet<String>(Arrays.asList(highRP));
        sources.addAll(Arrays.asList(lowRP));
        double fluctuation = 1.0;
        int iterTime = 0;
        while (fluctuation >= 1.0E-5 && iterTime < 3000) {
            double culmativediff = 0.0;
            ++iterTime;
            Map<Long, Double> preReputation = this.getReputation();
            for (EntityNode v : vertexSet) {
                if (sources.contains(v.getSignature())) continue;
                Set edges = this.graph.outgoingEdgesOf(v);
                double rep = 0.0;
                for (EventEdge edge : edges) {
                    EntityNode sink = edge.getSink();
                    rep += preReputation.get(sink.getID()) * edge.weight;
                }
                culmativediff += Math.abs(rep - preReputation.get(v.getID()));
                v.setReputation(rep);
            }
            fluctuation = culmativediff;
        }
        System.out.println(String.format("After %d times iteration, the reputation of each vertex is stable", iterTime));
        if (iterTime >= 3000) {
            System.out.println("After 3000 round updates, the vertex reputation is still not stable. Break out!");
        }
    }

    @Deprecated
    public void PageRankIterationBackwardLimitedByStepInfo(String[] highRP, String[] midRP, String[] lowRP, String detection, Map<String, Integer> stepInfo) {
        double alarmlevel = 0.85;
        Set vertexSet = this.graph.vertexSet();
        HashSet<String> sources = new HashSet<String>(Arrays.asList(highRP));
        sources.addAll(Arrays.asList(lowRP));
        try {
            File stepInfoFile = new File("stepInfo.txt");
            FileWriter fileWriter = new FileWriter(stepInfoFile);
            PrintWriter pwriter = new PrintWriter(fileWriter);
            for (String key : stepInfo.keySet()) {
                pwriter.println(key + ": " + stepInfo.get(key).toString());
            }
            pwriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        double fluctuation = 1.0;
        int iterTime = 0;
        while (fluctuation >= 1.0E-5) {
            double culmativediff = 0.0;
            ++iterTime;
            Map<Long, Double> preReputation = this.getReputation();
            for (EntityNode v : vertexSet) {
                if (v.getSignature().equals(detection)) {
                    System.out.println(v.reputation);
                }
                if (sources.contains(v.getSignature())) continue;
                Set edges = this.graph.outgoingEdgesOf(v);
                double rep = 0.0;
                for (EventEdge edge : edges) {
                    EntityNode sink = edge.getSink();
                    if (stepInfo.get(sink.getSignature()) >= stepInfo.get(v.getSignature())) continue;
                    rep += preReputation.get(sink.getID()) * edge.weight;
                }
                culmativediff += Math.abs(rep - preReputation.get(v.getID()));
                v.setReputation(rep);
            }
            fluctuation = culmativediff;
            if (iterTime > 20) continue;
            IterateGraph.printReputation(this.graph, iterTime);
        }
        System.out.println(String.format("After %d times iteration, the reputation of each vertex is stable", iterTime));
    }

    protected void normalizeWeightsAfterFiltering() {
        Set vertices = this.graph.vertexSet();
        for (EntityNode v : vertices) {
            double totalWeight = 0.0;
            Set edges = this.graph.incomingEdgesOf(v);
            for (EventEdge e : edges) {
                totalWeight += e.weight;
            }
            for (EventEdge e : edges) {
                e.weight /= totalWeight;
            }
        }
    }

    protected void fixReputation(String[] highRP) {
        Map<Long, Double> reputation = this.getReputation();
        HashSet<String> s2 = new HashSet<String>(Arrays.asList(highRP));
        double high_rep = 0.0;
        int count2 = 0;
        for (EntityNode v : this.graph.vertexSet()) {
            if (!s2.contains(v.getSignature())) continue;
            high_rep += reputation.get(v.getID()).doubleValue();
            ++count2;
        }
        high_rep /= (double)count2;
        for (EntityNode v : this.graph.vertexSet()) {
            v.setReputation(Math.min(1.0 - (high_rep - reputation.get(v.getID())) / high_rep, 1.0));
        }
    }

    protected void extractSuspects(double threshold) {
        ArrayList vertices = new ArrayList(this.graph.vertexSet());
        for (EntityNode v : vertices) {
            if (!(v.reputation >= threshold)) continue;
            this.graph.removeVertex(v);
        }
    }

    private Map<Long, Double> getReputation() {
        Set vertexSet = this.graph.vertexSet();
        HashMap<Long, Double> map2 = new HashMap<Long, Double>();
        for (EntityNode node : vertexSet) {
            map2.put(node.getID(), node.getReputation());
        }
        return map2;
    }

    public void exportGraph(String name) {
        this.graphIterator.exportGraph(name);
    }

    private void preprocessWeights(Map<Long, Double> weights, boolean normalizeByOutEdges) {
        if (normalizeByOutEdges) {
            this.normalizeWeightsByOutEdges(weights);
        } else {
            this.standardizeWeights(weights);
        }
    }

    private void standardizeWeights(Map<Long, Double> weights) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (Long edgeID : weights.keySet()) {
            stats.addValue(weights.get(edgeID));
        }
        double mean = stats.getMean();
        double std = stats.getStandardDeviation();
        for (long edgeID : weights.keySet()) {
            double standardizedWeight = (weights.get(edgeID) - mean) / std;
            weights.put(edgeID, standardizedWeight);
        }
    }

    private void normalizeWeightsByOutEdges(Map<Long, Double> weights) {
        for (EntityNode n : this.graph.vertexSet()) {
            Set outgoing = this.graph.outgoingEdgesOf(n);
            double weightTotal = 0.0;
            for (EventEdge out : outgoing) {
                weightTotal += weights.get(out.id).doubleValue();
            }
            if (!(weightTotal > Double.MIN_VALUE)) continue;
            for (EventEdge out : outgoing) {
                double normalizedWeight = weights.get(out.id) / weightTotal;
                weights.put(out.id, normalizedWeight);
            }
        }
    }

    private void scaleRange(List<Double> numbers) {
        double max2;
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (double n : numbers) {
            stats.addValue(n);
        }
        double min2 = stats.getMin();
        double secondMin = max2 = stats.getMax();
        for (double n : numbers) {
            if (n == min2 || !(n < secondMin)) continue;
            secondMin = n;
        }
        double offset = (secondMin - min2) / 100.0;
        System.out.println("Scaling statistics --- min: " + min2 + " max: " + max2 + " secondMin: " + secondMin + " offset: " + offset + " scaledMin: " + offset / (max2 - min2));
        for (int i = 0; i < numbers.size(); ++i) {
            numbers.set(i, (numbers.get(i) - min2 + offset) / (max2 - min2));
        }
    }

    public void setReliableReputation(String[] strs) {
        HashSet<String> set = new HashSet<String>(Arrays.asList(strs));
        Set vertexSet = this.graph.vertexSet();
        for (EntityNode v : vertexSet) {
            if (!set.contains(v.getSignature())) continue;
            v.setReputation(1.0);
        }
    }

    private double getTimeWeight_old(EventEdge edge) {
        if (edge.getEndTime().equals(this.POITime)) {
            return 1.0;
        }
        BigDecimal diff = this.POITime.subtract(edge.getEndTime());
        if (diff.compareTo(new BigDecimal(1)) > 0) {
            return 1.0 / diff.doubleValue();
        }
        double res = Math.log(1.0 / diff.doubleValue());
        if (res == 0.0) {
            System.out.println("timeWight should not be zero");
        }
        if (res < 0.0) {
            System.out.println("Minus TimeWeight:" + res);
        }
        return res;
    }

    private double getTimeWeight(EventEdge edge) {
        double res;
        if (edge.getEndTime().equals(this.POITime)) {
            double pseudoMinDiff = 1.0E-10;
            res = Math.log(1.0 + 1.0 / Math.abs(pseudoMinDiff));
        } else {
            res = Math.log(1.0 + 1.0 / Math.abs(edge.getEndTime().doubleValue() - this.POITime.doubleValue()));
        }
        return res;
    }

    private double getAmountWeight_old(EventEdge edge) {
        return edge.getSize();
    }

    private double getAmountWeight(EventEdge edge) {
        return 1.0 / (Math.abs((double)edge.getSize() - this.detectionSize) + 1.0E-4);
    }

    public void printWeights() throws Exception {
        PrintWriter writer = new PrintWriter(String.format("%s.txt", "EdgeWeights"));
        if (this.weights == null) {
            System.out.println("weithis is null or size equal to zero");
        }
        System.out.println(this.weights.keySet().size());
        for (Long id : this.weights.keySet()) {
            writer.println(String.format("%d: %f", id, this.weights.get(id)));
        }
        writer.close();
    }

    private BigDecimal getPOITime() {
        BigDecimal res = BigDecimal.ZERO;
        Set edges = this.graph.edgeSet();
        for (EventEdge e : edges) {
            if (e.getEndTime().compareTo(res) <= 0) continue;
            res = e.getEndTime();
        }
        return res;
    }

    public void printReputation() {
        this.graphIterator.printVertexReputation();
    }

    private void printEdgeWeights(EventEdge edge) {
        System.out.println("EventEdge " + edge.getID() + " (" + edge.getSource().getID() + " " + edge.getSource().getSignature() + " ->" + edge.getEvent() + " " + edge.getSink().getID() + " " + edge.getSink().getSignature() + ")\t\t\t timeWeight:" + this.timeWeights.get(edge.id) + " amountWeight: " + this.amountWeights.get(edge.id) + " structureWeight: " + this.structureWeights.get(edge.id) + " finalWeight: " + this.weights.get(edge.id));
    }

    private void printClusterResults(String clusterMethod, List<Cluster<EventEdgeWrapper>> clusterResults) {
        System.out.println();
        System.out.println(clusterMethod + " clustering:");
        for (int i = 0; i < clusterResults.size(); ++i) {
            System.out.println();
            System.out.println("Cluster " + i);
            for (EventEdgeWrapper edgeWrapper : clusterResults.get(i).getPoints()) {
                EventEdge edge = edgeWrapper.getEventEdge();
                this.printEdgeWeights(edge);
            }
        }
        System.out.println();
    }

    private void printRealMatrix(RealMatrix matrix) {
        for (int i = 0; i < matrix.getRowDimension(); ++i) {
            for (int j = 0; j < matrix.getColumnDimension(); ++j) {
                System.out.print(matrix.getEntry(i, j) + " ");
            }
            System.out.println();
        }
    }

    private void printRealVector(RealVector vector) {
        for (int i = 0; i < vector.getDimension(); ++i) {
            System.out.print(vector.getEntry(i) + " ");
        }
        System.out.println();
    }

    public void checkTimeAndAmount() {
        Set edges = this.graph.edgeSet();
        for (EventEdge edge : edges) {
            if (edge.getDuration().equals(BigDecimal.ZERO)) {
                System.out.println("this is because amount is zero");
                System.out.println(edge.getID());
                System.out.println(edge.getSource().getSignature());
            }
            if (edge.getSize() != 0L) continue;
            System.out.println("this is because size is zero");
            System.out.println(edge.getID());
            System.out.println(edge.getSource().getSignature());
        }
    }

    private boolean someWithDataSomeNoData(EntityNode n) {
        Set edges = this.graph.incomingEdgesOf(n);
        boolean oneEdgeNoData = false;
        boolean oneEdgeWithData = false;
        for (EventEdge e : edges) {
            if (e.getSize() == 0L) {
                oneEdgeNoData = true;
            }
            if (e.getSize() != 0L) {
                oneEdgeWithData = true;
            }
            if (!oneEdgeNoData || !oneEdgeWithData) continue;
            return true;
        }
        return false;
    }

    public void initialReputation(String[] signature_high, String[] signature_low) {
        Set set = this.graph.vertexSet();
        HashSet<String> highReputation = new HashSet<String>(Arrays.asList(signature_high));
        HashSet<String> lowReputation = new HashSet<String>(Arrays.asList(signature_low));
        System.out.println(highReputation);
        for (EntityNode node : set) {
            System.out.println(node.getSignature());
            if (highReputation.contains(node.getSignature())) {
                System.out.println(node.getSignature() + " has high reputation");
                node.reputation = 1.0;
                continue;
            }
            if (lowReputation.contains(node.getSignature())) {
                node.reputation = 0.0;
                continue;
            }
            if (this.graph.incomingEdgesOf(node).size() != 0) continue;
            node.reputation = 0.0;
        }
    }

    public void printConstantPartOfPageRank() {
        double res = (1.0 - this.dumpingFactor) / (double)this.graph.vertexSet().size();
        System.out.println("The constant part of Page Rank:" + res);
    }

    public void checkWeightsAfterCalculation() {
        Set vertexSet = this.graph.vertexSet();
        for (EntityNode node : vertexSet) {
            Set incoming = this.graph.incomingEdgesOf(node);
            double res = 0.0;
            for (EventEdge edge : incoming) {
                res += edge.weight;
            }
            if (incoming.size() == 0 || !(Math.abs(res - 1.0) >= 1.0E-5)) continue;
            System.out.println("Target: " + node.getSignature());
            for (EventEdge edge : incoming) {
                edge.printInfo();
            }
            System.out.println("-----------");
        }
    }

    public void onlyPrintHighestWeights(String start) {
        EntityNode v1 = this.graphIterator.getGraphVertex(start);
        HashMap<Long, EntityNode> map2 = new HashMap<Long, EntityNode>();
        map2.put(v1.getID(), new EntityNode(v1));
        DirectedPseudograph<EntityNode, EventEdge> result = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        LinkedList<EntityNode> queue = new LinkedList<EntityNode>();
        queue.offer(v1);
        while (!queue.isEmpty()) {
            EntityNode copy1;
            EntityNode node = (EntityNode)queue.poll();
            Set<EventEdge> incoming = this.graph.incomingEdgesOf(node);
            Set<EventEdge> outgoing = this.graph.outgoingEdgesOf(node);
            EventEdge incomingHighestWeight = this.getHighestWeightEdge(incoming);
            EventEdge outgoingHighestWeight = this.getHighestWeightEdge(outgoing);
            if (incomingHighestWeight != null) {
                if (!map2.containsKey(incomingHighestWeight.getSource().getID())) {
                    map2.put(incomingHighestWeight.getSource().getID(), new EntityNode(incomingHighestWeight.getSource()));
                    queue.offer(incomingHighestWeight.getSource());
                }
                EventEdge incomingCopy = new EventEdge(incomingHighestWeight);
                copy1 = (EntityNode)map2.get(node.getID());
                EntityNode copy2 = (EntityNode)map2.get(incomingHighestWeight.getSource().getID());
                result.addVertex(copy1);
                result.addVertex(copy2);
                result.addEdge(copy2, copy1, incomingCopy);
            }
            if (outgoingHighestWeight == null) continue;
            if (!map2.containsKey(outgoingHighestWeight.getSink().getID())) {
                map2.put(outgoingHighestWeight.getSink().getID(), new EntityNode(outgoingHighestWeight.getSink()));
                queue.offer(outgoingHighestWeight.getSink());
            }
            EventEdge outgoingCopy = new EventEdge(outgoingHighestWeight);
            copy1 = (EntityNode)map2.get(node.getID());
            EntityNode copy3 = (EntityNode)map2.get(outgoingCopy.getSink().getID());
            result.addVertex(copy1);
            result.addVertex(copy3);
            result.addEdge(copy1, copy3, outgoingCopy);
        }
        System.out.println("dEBUG: " + result.vertexSet().size());
        IterateGraph iter = new IterateGraph(result);
        iter.exportGraph("HighestWeight");
    }

    private EventEdge getHighestWeightEdge(Set<EventEdge> edges) {
        ArrayList<EventEdge> edgeList = new ArrayList<EventEdge>(edges);
        if (edgeList.size() == 0) {
            return null;
        }
        EventEdge res = (EventEdge)edgeList.get(0);
        for (int i = 1; i < edgeList.size(); ++i) {
            if (!(res.weight < ((EventEdge)edgeList.get((int)i)).weight)) continue;
            res = (EventEdge)edgeList.get(i);
        }
        return res;
    }

    private Set<EntityNode> getSources(EntityNode e) {
        Set edges = this.graph.incomingEdgesOf(e);
        HashSet<EntityNode> sources = new HashSet<EntityNode>();
        for (EventEdge edge : edges) {
            sources.add(edge.getSource());
        }
        assert (sources.size() <= edges.size());
        return sources;
    }

    public double getAvgWeight() {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (EventEdge edge : this.graph.edgeSet()) {
            stats.addValue(edge.weight);
        }
        return stats.getMean();
    }

    public double getStdWeight() {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (EventEdge edge : this.graph.edgeSet()) {
            stats.addValue(edge.weight);
        }
        return stats.getStandardDeviation();
    }

    public void filterGraphBasedOnAverageWeight(double threshold) {
        ArrayList edges = new ArrayList(this.graph.edgeSet());
        System.out.println("threshold: " + threshold);
        for (int i = 0; i < edges.size(); ++i) {
            if (!(((EventEdge)edges.get((int)i)).weight < threshold)) continue;
            this.graph.removeEdge((EventEdge)edges.get(i));
        }
        ArrayList list = new ArrayList(this.graph.vertexSet());
        for (int i = 0; i < list.size(); ++i) {
            EntityNode v = (EntityNode)list.get(i);
            if (this.graph.incomingEdgesOf(v).size() != 0 || this.graph.outgoingEdgesOf(v).size() != 0) continue;
            this.graph.removeVertex(v);
        }
    }

    public void removeIsolatedIslands(String POI) {
        ConnectivityInspector<EntityNode, EventEdge> ci = new ConnectivityInspector<EntityNode, EventEdge>(this.graph);
        Set<EntityNode> verticesConnectedToPOI = ci.connectedSetOf(this.graphIterator.getGraphVertex(POI));
        ArrayList list = new ArrayList(this.graph.vertexSet());
        for (int i = 0; i < list.size(); ++i) {
            EntityNode v = (EntityNode)list.get(i);
            if (verticesConnectedToPOI.contains(v)) continue;
            this.graph.removeVertex(v);
        }
    }

    public void removeIrrelaventVertices(String POI) {
        EntityNode POIVertex = this.graphIterator.getGraphVertex(POI);
        LinkedList<EventEdge> queue = new LinkedList<EventEdge>(this.graph.incomingEdgesOf(POIVertex));
        HashSet<EntityNode> ancestors = new HashSet<EntityNode>();
        ancestors.add(POIVertex);
        while (!queue.isEmpty()) {
            EntityNode v = (EntityNode)this.graph.getEdgeSource((EventEdge)queue.pollLast());
            ancestors.add(v);
            for (Object e : this.graph.incomingEdgesOf(v)) {
                if (ancestors.contains(this.graph.getEdgeSource((EventEdge)e))) continue;
                queue.addFirst((EventEdge)e);
            }
        }
        queue = new LinkedList(this.graph.outgoingEdgesOf(POIVertex));
        HashSet<EntityNode> children = new HashSet<EntityNode>();
        children.add(POIVertex);
        while (!queue.isEmpty()) {
            EntityNode v = (EntityNode)this.graph.getEdgeTarget((EventEdge)queue.pollLast());
            children.add(v);
            for (EventEdge e : this.graph.outgoingEdgesOf(v)) {
                if (children.contains(this.graph.getEdgeTarget(e))) continue;
                queue.addFirst(e);
            }
        }
        ancestors.addAll(children);
        ArrayList list = new ArrayList(this.graph.vertexSet());
        for (int i = 0; i < list.size(); ++i) {
            EntityNode v = (EntityNode)list.get(i);
            if (ancestors.contains(v)) continue;
            this.graph.removeVertex(v);
        }
    }

    public long getDataAmount(String signature) {
        EntityNode node = this.graphIterator.getGraphVertex(signature);
        long res = 0L;
        Set edges = this.graph.incomingEdgesOf(node);
        for (EventEdge e : edges) {
            res += e.getSize();
        }
        return res;
    }

    private double gaussian(double center, double x, double sigma) {
        return Math.exp(-Math.pow(x - center, 2.0) / (2.0 * sigma * sigma)) / Math.sqrt(Math.PI * 2 * sigma * sigma);
    }

    private double adjustedSigmoid(double x) {
        return 2.0 * (1.0 / (1.0 + Math.pow(Math.E, -1.0 * x))) - 1.0;
    }

    public DirectedPseudograph<EntityNode, EventEdge> combineBackwardAndForwardForGivenStarts(List<String> starts, DirectedPseudograph<EntityNode, EventEdge> original) {
        this.forwardAnalysis = new ForwardAnalysis(original);
        BigDecimal POITime = this.getPOITime();
        List<DirectedPseudograph<EntityNode, EventEdge>> forwardGraphs = this.forwardAnalysis.multipleForwardLimitedByPOI(starts, POITime);
        Map<String, Integer> forwardGraphEdgeUnion = IterateGraph.groupsEdges(forwardGraphs);
        DirectedPseudograph<EntityNode, EventEdge> backFilterByForwardUion = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        for (EventEdge edge : this.graph.edgeSet()) {
            String signatureOfCurrentEdge = IterateGraph.convertEdgeToString(edge);
            if (!forwardGraphEdgeUnion.containsKey(signatureOfCurrentEdge)) continue;
            backFilterByForwardUion.addVertex(edge.getSource());
            backFilterByForwardUion.addVertex(edge.getSink());
            backFilterByForwardUion.addEdge(edge.getSource(), edge.getSink(), edge);
        }
        Map<String, Double> graphReputation = IterateGraph.getNodeReputation(this.graph);
        for (EntityNode node : backFilterByForwardUion.vertexSet()) {
            node.reputation = graphReputation.get(node.getSignature());
        }
        return backFilterByForwardUion;
    }

    public List<List<String>> getForwardStarts() {
        LinkedList<List<String>> res = new LinkedList<List<String>>();
        List<String> candidates = IterateGraph.getCandidateEntryPoint(this.graph);
        Map<String, Double> graphReputation = IterateGraph.getNodeReputation(this.graph);
        Map<String, EntityNode> signatureToNode = IterateGraph.getSignatureNodeMap(this.graph);
        LinkedList<String> processCandidate = new LinkedList<String>();
        LinkedList<String> ipCandidate = new LinkedList<String>();
        LinkedList<String> fileCandidate = new LinkedList<String>();
        for (String sign : candidates) {
            EntityNode node = signatureToNode.get(sign);
            if (node.isProcessNode()) {
                processCandidate.add(sign);
                continue;
            }
            if (node.isNetworkNode()) {
                ipCandidate.add(sign);
                continue;
            }
            fileCandidate.add(sign);
        }
        IterateGraph.sortedSignatureBasedOnRP(processCandidate, graphReputation);
        IterateGraph.sortedSignatureBasedOnRP(ipCandidate, graphReputation);
        IterateGraph.sortedSignatureBasedOnRP(fileCandidate, graphReputation);
        res.add(processCandidate);
        res.add(ipCandidate);
        res.add(fileCandidate);
        return res;
    }

    public List<List<String>> getForwardStartsWithSkipAndMust(Set<String> skip, Set<String> must) {
        LinkedList<List<String>> res = new LinkedList<List<String>>();
        List<String> candidates = IterateGraph.getCandidateEntryPoint(this.graph, skip, must);
        Map<String, Double> graphReputation = IterateGraph.getNodeReputation(this.graph);
        Map<String, EntityNode> signatureToNode = IterateGraph.getSignatureNodeMap(this.graph);
        LinkedList<String> processCandidate = new LinkedList<String>();
        LinkedList<String> ipCandidate = new LinkedList<String>();
        LinkedList<String> fileCandidate = new LinkedList<String>();
        for (String sign : candidates) {
            EntityNode node = signatureToNode.get(sign);
            if (node.isProcessNode()) {
                processCandidate.add(sign);
                continue;
            }
            if (node.isNetworkNode()) {
                ipCandidate.add(sign);
                continue;
            }
            fileCandidate.add(sign);
        }
        IterateGraph.sortedSignatureBasedOnRP(processCandidate, graphReputation);
        IterateGraph.sortedSignatureBasedOnRP(ipCandidate, graphReputation);
        IterateGraph.sortedSignatureBasedOnRP(fileCandidate, graphReputation);
        res.add(processCandidate);
        res.add(ipCandidate);
        res.add(fileCandidate);
        return res;
    }

    public DirectedPseudograph<EntityNode, EventEdge> combineBackwardAndForwardForGivenStart(String start, DirectedPseudograph<EntityNode, EventEdge> original) {
        this.forwardAnalysis = new ForwardAnalysis(original);
        BigDecimal POITime = this.getPOITime();
        DirectedPseudograph<EntityNode, EventEdge> forwardGraph = this.forwardAnalysis.forwardLimitedByTime(start, POITime);
        ArrayList<DirectedPseudograph<EntityNode, EventEdge>> forwardGraphs = new ArrayList<DirectedPseudograph<EntityNode, EventEdge>>();
        forwardGraphs.add(forwardGraph);
        Map<String, Integer> forwardGraphEdgeUnion = IterateGraph.groupsEdges(forwardGraphs);
        DirectedPseudograph<EntityNode, EventEdge> backFilterByForwardUion = new DirectedPseudograph<EntityNode, EventEdge>(EventEdge.class);
        for (EventEdge edge : this.graph.edgeSet()) {
            String signatureOfCurrentEdge = IterateGraph.convertEdgeToString(edge);
            if (!forwardGraphEdgeUnion.containsKey(signatureOfCurrentEdge)) continue;
            backFilterByForwardUion.addVertex(edge.getSource());
            backFilterByForwardUion.addVertex(edge.getSink());
            backFilterByForwardUion.addEdge(edge.getSource(), edge.getSink(), edge);
        }
        Map<String, Double> graphReputation = IterateGraph.getNodeReputation(this.graph);
        for (EntityNode node : backFilterByForwardUion.vertexSet()) {
            node.reputation = graphReputation.get(node.getSignature());
        }
        return backFilterByForwardUion;
    }

    public void PageRankIterationOneTimeUpdate(String[] highRP, String[] midRP, String[] lowRP, String detection) {
        double alarmlevel = 0.85;
        Set vertexSet = this.graph.vertexSet();
        HashSet<String> sources = new HashSet<String>(Arrays.asList(highRP));
        sources.addAll(Arrays.asList(lowRP));
        double fluctuation = 1.0;
        int iterTime = 0;
        while (fluctuation >= 1.0E-5) {
            double culmativediff = 0.0;
            ++iterTime;
            Map<Long, Double> preReputation = this.getReputation();
            for (EntityNode v : vertexSet) {
                if (sources.contains(v.getSignature())) continue;
                Set edges = this.graph.outgoingEdgesOf(v);
                double rep = 0.0;
                for (EventEdge edge : edges) {
                    EntityNode sink = edge.getSink();
                    rep += preReputation.get(sink.getID()) * edge.weight;
                }
                if (v.reputation != 0.0) continue;
                culmativediff += Math.abs(rep - preReputation.get(v.getID()));
                v.setReputation(rep);
            }
            fluctuation = culmativediff;
            if (iterTime > 20) continue;
            IterateGraph.printReputation(this.graph, iterTime);
        }
        System.out.println(String.format("After %d times iteration, the reputation of each vertex is stable", iterTime));
    }

    private double[] getNCoefficient(int n) {
        int i;
        double[] res = new double[n];
        Random rand = new Random();
        double sum2 = 0.0;
        for (i = 0; i < res.length; ++i) {
            res[i] = rand.nextDouble();
            sum2 += res[i];
        }
        for (i = 0; i < res.length; ++i) {
            res[i] = res[i] / sum2;
        }
        return res;
    }

    Map<String, Integer> graphSizeWithoutReadonly() {
        HashMap<String, Integer> res = new HashMap<String, Integer>();
        res.put("#Vertices before:", this.graph.vertexSet().size());
        res.put("#Edges beore:", this.graph.edgeSet().size());
        Set vertices = this.graph.vertexSet();
        HashSet<Long> edgeRemove = new HashSet<Long>();
        HashSet<String> vertexRemove = new HashSet<String>();
        HashSet<String> vertex = new HashSet<String>();
        for (EntityNode n : vertices) {
            if (n.isFileNode() && this.graph.inDegreeOf(n) == 0) {
                Set edges = this.graph.outgoingEdgesOf(n);
                for (EventEdge e : edges) {
                    edgeRemove.add(e.getID());
                }
                vertexRemove.add(n.getSignature());
            }
            vertex.add(n.getSignature());
        }
        res.put("#Vertices after:", this.graph.vertexSet().size() - vertexRemove.size());
        res.put("#Edges after:", this.graph.edgeSet().size() - edgeRemove.size());
        return res;
    }
}

