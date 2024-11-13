/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import pagerank.EventEdge;

public class Test {
    List<List<String>> curlCaseEdgeSignatures = new ArrayList<List<String>>();

    Test() {
        this.curlCaseEdgeSignatures.add(Arrays.asList("192.168.29.234:40402->208.118.235.20:80", "curl", "recvfrom"));
        this.curlCaseEdgeSignatures.add(Arrays.asList("curl", "/home/lcl/target.tar.bz2", "write"));
        this.curlCaseEdgeSignatures.add(Arrays.asList("/home/lcl/target.tar.bz2", "cp", "read"));
        this.curlCaseEdgeSignatures.add(Arrays.asList("cp", "/home/lcl/target", "write"));
    }

    public List<EventEdge> findCriticalEdgesForCase(List<List<String>> edgeSignatures, List<EventEdge> allEdges) {
        ArrayList<EventEdge> criticalEdges = new ArrayList<EventEdge>();
        for (List<String> currEdgeSignature : edgeSignatures) {
            for (EventEdge edge : allEdges) {
                String sourceSignature = edge.getSource().getSignature();
                String sinkSignature = edge.getSink().getSignature();
                String eventSignature = edge.getEvent();
                if (edge.getType().equals("PtoF") || edge.getType().equals("PtoN")) {
                    sourceSignature = edge.getSource().getP().getName();
                }
                if (edge.getType().equals("FtoP") || edge.getType().equals("NtoP")) {
                    sinkSignature = edge.getSink().getP().getName();
                }
                if (!sourceSignature.equals(currEdgeSignature.get(0)) || !sinkSignature.equals(currEdgeSignature.get(1)) || !eventSignature.equals(currEdgeSignature.get(2))) continue;
                criticalEdges.add(edge);
            }
        }
        return criticalEdges;
    }

    public static void main(String[] args) {
        long current = System.currentTimeMillis();
        System.out.println(current);
        int a = 0;
        for (int i = 0; i < 100000000; ++i) {
            ++a;
        }
        long end = System.currentTimeMillis();
        System.out.println(end);
        double timeCost = (double)(end - current) * 1.0 / 1000.0;
        System.out.println("Time cost is: " + timeCost);
    }
}

