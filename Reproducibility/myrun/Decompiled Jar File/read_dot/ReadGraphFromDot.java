/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package read_dot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.DOTImporter;
import org.jgrapht.graph.DirectedPseudograph;
import read_dot.DotEdgeProvider;
import read_dot.DotEntityProvider;
import read_dot.ExportEdgeProvider;
import read_dot.ExportNodeAttributeProvider;
import read_dot.NodeIdProvider;
import read_dot.NodeNameProvider;
import read_dot.SimpleEdge;
import read_dot.SimpleNode;

public class ReadGraphFromDot {
    DirectedPseudograph<SimpleNode, SimpleEdge> graph;

    public DirectedPseudograph<SimpleNode, SimpleEdge> readGraph(String file) {
        DOTImporter<SimpleNode, SimpleEdge> importer = new DOTImporter<SimpleNode, SimpleEdge>(new DotEntityProvider(), new DotEdgeProvider());
        this.graph = new DirectedPseudograph(SimpleEdge.class);
        try {
            importer.importGraph(this.graph, new File(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.graph;
    }

    public void exportGraph(String file) {
        DOTExporter<SimpleNode, SimpleEdge> exporter = new DOTExporter<SimpleNode, SimpleEdge>(new NodeIdProvider(), new NodeNameProvider(), new ExportEdgeProvider(), new ExportNodeAttributeProvider(), null);
        try {
            exporter.exportGraph(this.graph, new FileWriter(String.format("%s.dot", file)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void filter(String file) {
        Runtime rt = Runtime.getRuntime();
        File f = new File(file);
        File filter_dir = new File(f.getParent() + "/filter");
        if (!filter_dir.exists()) {
            filter_dir.mkdir();
        }
        try (PrintWriter pw = null;){
            pw = new PrintWriter(filter_dir + "/stats");
            pw.println(String.format("0- v:%d e:%d", this.graph.vertexSet().size(), this.graph.edgeSet().size()));
            for (int i = 1; i <= 99; ++i) {
                ArrayList edgeList = new ArrayList(this.graph.edgeSet());
                ArrayList nodeList = new ArrayList(this.graph.vertexSet());
                for (SimpleEdge e : edgeList) {
                    if (!(e.weight < (double)i * 0.01)) continue;
                    this.graph.removeEdge(e);
                }
                for (SimpleNode n : nodeList) {
                    if (!this.graph.incomingEdgesOf(n).isEmpty() || !this.graph.outgoingEdgesOf(n).isEmpty()) continue;
                    this.graph.removeVertex(n);
                }
                pw.println(String.format("%d- v:%d e:%d", i, this.graph.vertexSet().size(), this.graph.edgeSet().size()));
                if (this.graph.edgeSet().size() >= 100) continue;
                String filtered = filter_dir.getAbsolutePath() + "/filter_" + i;
                this.exportGraph(filtered);
                String[] cmd = new String[]{"/bin/sh", "-c", "dot -T svg " + filtered + ".dot > " + filtered + ".svg"};
                try {
                    rt.exec(cmd);
                    continue;
                } catch (IOException iOException) {
                    // empty catch block
                }
            }
        }
    }

    public void outputNodes(String caseName) {
        File f = new File("/home/lcl/work/nodes");
        if (!f.exists()) {
            f.mkdirs();
        }
        try (PrintWriter pw = new PrintWriter(f.getAbsolutePath() + "/" + caseName + ".node");){
            for (SimpleNode n : this.graph.vertexSet()) {
                pw.println(String.format("%s,%f", n.signature, n.reputation));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String file = "/home/lcl/work/res_analysis/res_attacks_ml/logs-vpn-filter-step5/Weight_vpn-filter.dot";
        ReadGraphFromDot reader = new ReadGraphFromDot();
        reader.readGraph(file);
        reader.outputNodes("vpn_filter_5");
    }
}

