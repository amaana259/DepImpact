/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import logparsers.SysdigOutputParser;
import logparsers.SysdigOutputParserNoRegex;
import pagerank.Event;
import pagerank.FtoPEvent;
import pagerank.NtoPEvent;
import pagerank.PtoFEvent;
import pagerank.PtoNEvent;
import pagerank.PtoPEvent;

public class ProcessTheOriginalParserOutput {
    private Map<String, PtoFEvent> processFileMap;
    private Map<String, PtoNEvent> processNetworkMap;
    private Map<String, PtoPEvent> processProcessMap;
    private Map<String, NtoPEvent> networkProcessMap;
    private Map<String, FtoPEvent> fileProcessMap;
    private Map<String, PtoNEvent> pnmap;
    private Map<String, NtoPEvent> npmap;
    private SysdigOutputParser parser;
    private boolean hostOrNot;

    public ProcessTheOriginalParserOutput(String logFilePath, String[] localIP) {
        this.parser = new SysdigOutputParserNoRegex(logFilePath, localIP);
        try {
            this.parser.getEntities();
        } catch (IOException e) {
            System.out.println("The log file doesn't exist");
        }
        this.processFileMap = this.parser.getPfmap();
        this.pnmap = this.parser.getPnmap();
        this.processProcessMap = this.parser.getPpmap();
        this.npmap = this.parser.getNpmap();
        this.fileProcessMap = this.parser.getFpmap();
        this.processNetworkMap = new HashMap<String, PtoNEvent>();
        this.networkProcessMap = new HashMap<String, NtoPEvent>();
        this.hostOrNot = false;
    }

    public ProcessTheOriginalParserOutput(String logFilePath, String[] localIP, boolean host) {
        this.parser = new SysdigOutputParserNoRegex(logFilePath, localIP, host);
        try {
            this.parser.getEntities();
        } catch (IOException e) {
            System.out.println("The log file doesn't exist");
        }
        this.processFileMap = this.parser.getPfmap();
        this.pnmap = this.parser.getPnmap();
        this.processProcessMap = this.parser.getPpmap();
        this.npmap = this.parser.getNpmap();
        this.fileProcessMap = this.parser.getFpmap();
        this.processNetworkMap = new HashMap<String, PtoNEvent>();
        this.networkProcessMap = new HashMap<String, NtoPEvent>();
        this.hostOrNot = host;
    }

    public void setHostOrNot(boolean i) {
        this.hostOrNot = i;
        this.parser.setHostOrNot(this.hostOrNot);
    }

    public Map<String, PtoFEvent> getProcessFileMap() {
        return this.processFileMap;
    }

    public Map<String, PtoNEvent> getProcessNetworkMap() {
        return this.pnmap;
    }

    public Map<String, PtoPEvent> getProcessProcessMap() {
        return this.processProcessMap;
    }

    public Map<String, NtoPEvent> getNetworkProcessMap() {
        return this.npmap;
    }

    public Map<String, FtoPEvent> getFileProcessMap() {
        return this.fileProcessMap;
    }

    public Map<String, PtoNEvent> getPnmap() {
        return this.pnmap;
    }

    public Map<String, NtoPEvent> getNpmap() {
        return this.npmap;
    }

    public SysdigOutputParser getParser() {
        return this.parser;
    }

    public void reverseSourceAndSink() {
        Event reverse;
        String eventType;
        Event event;
        Set<String> keys2 = this.pnmap.keySet();
        for (String key : keys2) {
            event = this.pnmap.get(key);
            eventType = ((PtoNEvent)event).getEvent();
            if (eventType.equals("recvmsg") || eventType.equals("read") || eventType.equals("recvfrom")) {
                reverse = new NtoPEvent((PtoNEvent)event);
                this.networkProcessMap.put(key, (NtoPEvent)reverse);
                continue;
            }
            this.processNetworkMap.put(key, this.pnmap.get(key));
        }
        keys2 = this.npmap.keySet();
        for (String key : keys2) {
            event = this.npmap.get(key);
            eventType = ((NtoPEvent)event).getEvent();
            if (eventType.equals("read") || eventType.equals("recvmsg")) {
                reverse = new PtoNEvent((NtoPEvent)event);
                this.processNetworkMap.put(key, (PtoNEvent)reverse);
                continue;
            }
            this.networkProcessMap.put(key, this.npmap.get(key));
        }
    }

    public static void main(String[] args) throws Exception {
        String[] localIP = new String[]{"10.0.2.15"};
        ProcessTheOriginalParserOutput test2 = new ProcessTheOriginalParserOutput("pipInstall.txt", localIP);
        test2.reverseSourceAndSink();
        System.out.println("--------------------------");
        Map<String, PtoNEvent> map2 = test2.getProcessNetworkMap();
        System.out.println(map2.size());
        for (String key : map2.keySet()) {
            System.out.println(map2.get(key).getSink().getSrcAddress().equals(localIP[0]));
            System.out.println("Find the local ip");
        }
        Map<String, NtoPEvent> map22 = test2.getNetworkProcessMap();
        for (String key : map22.keySet()) {
            System.out.println(map22.get(key).getSource().getSrcAddress().equals(localIP[0]));
            System.out.println("Find the local ip");
        }
    }
}

