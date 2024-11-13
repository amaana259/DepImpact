/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package logparsers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import logparsers.SysdigOutputParser;
import pagerank.FileEntity;
import pagerank.FtoPEvent;
import pagerank.MetaConfig;
import pagerank.NetworkEntity;
import pagerank.NtoPEvent;
import pagerank.Process;
import pagerank.PtoFEvent;
import pagerank.PtoNEvent;
import pagerank.PtoPEvent;

public class SysdigOutputParserStream
implements SysdigOutputParser {
    private HashMap<String, Process> processHashMap;
    private HashMap<String, FileEntity> fileHashMap;
    private HashMap<String, NetworkEntity> networkHashMap;
    private File record;
    private HashMap<String, PtoFEvent> pfmap;
    private HashMap<String, PtoPEvent> ppmap;
    private HashMap<String, FtoPEvent> fpmap;
    private HashMap<String, PtoNEvent> pnmap;
    private HashMap<String, NtoPEvent> npmap;
    private Set<String> PtoF;
    private Set<String> PtoP;
    private Set<String> FtoP;
    private Set<String> NtoP;
    private Set<String> PtoN;
    private Set<String> localIPS;
    private boolean hostOrNot;
    private long uniqID;

    public SysdigOutputParserStream(String path, String[] localIP) {
        try {
            this.record = new File(path);
            if (!this.record.exists()) {
                throw new FileNotFoundException("The input file is not exist");
            }
        } catch (FileNotFoundException e) {
            System.err.print("Message: " + e.getMessage());
        }
        this.processHashMap = new HashMap();
        this.fileHashMap = new HashMap();
        this.networkHashMap = new HashMap();
        this.pfmap = new HashMap();
        this.ppmap = new HashMap();
        this.fpmap = new HashMap();
        this.pfmap = new HashMap();
        this.pnmap = new HashMap();
        this.npmap = new HashMap();
        this.PtoF = new HashSet<String>();
        this.PtoP = new HashSet<String>();
        this.FtoP = new HashSet<String>();
        this.PtoN = new HashSet<String>();
        this.NtoP = new HashSet<String>();
        String[] ptopSystemCall = MetaConfig.ptopSystemCall;
        String[] ptofSystemCall = MetaConfig.ptofSystemCall;
        String[] ftopSystemCall = MetaConfig.ftopSystemCall;
        String[] ptonSystemCall = MetaConfig.ptonSystemCall;
        String[] ntopSystemCall = MetaConfig.ntopSystemCall;
        this.hostOrNot = false;
        for (String str : ptopSystemCall) {
            this.PtoP.add(str);
        }
        for (String str : ptofSystemCall) {
            this.PtoF.add(str);
        }
        for (String str : ftopSystemCall) {
            this.FtoP.add(str);
        }
        for (String str : ptonSystemCall) {
            this.PtoN.add(str);
        }
        for (String str : ntopSystemCall) {
            this.NtoP.add(str);
        }
        this.uniqID = 0L;
        this.localIPS = new HashSet<String>();
        for (String str : localIP) {
            this.localIPS.add(str);
        }
    }

    @Override
    public void setHostOrNot(boolean i) {
        this.hostOrNot = i;
    }

    public static void main(String[] args) throws Exception {
        block2: {
            String[] localIP = new String[]{"192.168.29.234"};
            SysdigOutputParserStream test2 = new SysdigOutputParserStream("input/logs_extended/curl.txt", localIP);
            test2.getEntities();
            HashMap<String, Process> pmap = test2.getProcessHashMap();
            HashMap<String, FileEntity> fmap = test2.getFileHashMap();
            HashMap<String, NetworkEntity> networkHashMap = test2.getNetworkHashMap();
            Map npmap = test2.getNpmap();
            Map pnmap = test2.getPnmap();
            Set<String> ids = pmap.keySet();
            PrintWriter prWriter = new PrintWriter(String.format("%s.txt", "testOutput"));
            System.out.println("Process to File event:---------------------------");
            System.out.println("Process to Process event:---------------------------");
            System.out.println("Network to Process event: ------------------------");
            Iterator iterator2 = ((HashMap)npmap).keySet().iterator();
            if (!iterator2.hasNext()) break block2;
            String key = (String)iterator2.next();
            NtoPEvent np = (NtoPEvent)((HashMap)npmap).get(key);
            if (np.getSource() == null) {
                System.out.println("NPevent Source is null");
            }
            if (np.getSink() == null) {
                System.out.println("NPevent Sink is null");
            }
            System.out.println(np.getSource().getSrcAddress().equals(localIP[0]));
            System.out.println("Find local IP");
        }
    }

    public HashMap<String, PtoNEvent> getPnmap() {
        return this.pnmap;
    }

    public HashMap<String, PtoFEvent> getPfmap() {
        return this.pfmap;
    }

    public HashMap<String, PtoPEvent> getPpmap() {
        return this.ppmap;
    }

    public HashMap<String, FtoPEvent> getFpmap() {
        return this.fpmap;
    }

    public HashMap<String, NtoPEvent> getNpmap() {
        return this.npmap;
    }

    public HashMap<String, Process> getProcessHashMap() {
        return this.processHashMap;
    }

    public HashMap<String, FileEntity> getFileHashMap() {
        return this.fileHashMap;
    }

    public HashMap<String, NetworkEntity> getNetworkHashMap() {
        return this.networkHashMap;
    }

    @Override
    public void getEntities() throws IOException {
        Scanner sc = new Scanner(this.record);
        int numLines = 0;
        while (sc.hasNextLine()) {
            PtoPEvent pp;
            String startMs;
            String startS;
            BigDecimal duration;
            BigDecimal end;
            Object endTime;
            ++numLines;
            String s2 = sc.nextLine();
            String[] parts = s2.split(" ");
            double reput = 0.0;
            int hopCount = 0;
            long id = 0L;
            String pid = null;
            String timestamp1 = null;
            String timestamp2 = null;
            String path = null;
            String name = null;
            String srcIP = null;
            String srcPort = null;
            String destIP = null;
            String destPort = null;
            String event = null;
            String direction = null;
            String latency = null;
            String cpu = null;
            String pid2 = null;
            String name2 = null;
            Object cwd = null;
            Object args = null;
            String ip = null;
            long size2 = 0L;
            for (int i = 0; i < parts.length; ++i) {
                String childProcess;
                String[] childAndName;
                if (i == 0) {
                    id = Long.valueOf(parts[i]);
                }
                if (i == 1) {
                    String[] times = parts[i].split("\\.");
                    timestamp1 = times[0];
                    timestamp2 = times[1];
                }
                if (i == 2) {
                    cpu = parts[i];
                }
                if (i == 3) {
                    name = parts[i];
                }
                if (i == 4) {
                    pid = parts[i].substring(1, parts[i].length() - 1);
                }
                if (i == 5) {
                    direction = parts[i];
                }
                if (i == 6) {
                    event = parts[i];
                }
                if (i < 7) continue;
                if (parts[i].startsWith("fd")) {
                    for (int index = 0; index < parts[i].length(); ++index) {
                        String interNet;
                        if (parts[i].charAt(index) != '>') continue;
                        if (parts[i].charAt(index - 1) == 'f') {
                            path = parts[i].substring(index + 1, parts[i].length() - 1);
                            break;
                        }
                        if (parts[i].charAt(index - 1) != 't' && parts[i].charAt(index - 1) != 'u' || parts[i].charAt(index - 2) != '6' && parts[i].charAt(index - 2) != '4' || (interNet = parts[i].substring(index + 1, parts[i].length() - 1)) == null) continue;
                        String[] portsAndIp = this.getIPandPorts(interNet);
                        srcIP = portsAndIp[0];
                        srcPort = portsAndIp[1];
                        destIP = portsAndIp[2];
                        destPort = portsAndIp[3];
                        ip = interNet;
                        break;
                    }
                }
                if (parts[i].startsWith("size") && parts[i].length() >= 6) {
                    String sub = parts[i].substring(5);
                    boolean allDigit = true;
                    for (int v = 0; v < sub.length(); ++v) {
                        if (Character.isDigit(sub.charAt(v))) continue;
                        allDigit = false;
                    }
                    if (allDigit) {
                        size2 = Long.parseLong(sub);
                    }
                }
                if (parts[i].startsWith("latency")) {
                    int beginIndex = parts[i].indexOf("=");
                    latency = parts[i].substring(beginIndex + 1);
                }
                if (event.equals("clone") && parts[i].startsWith("res") && (childAndName = (childProcess = parts[i].substring(4)).split("\\(")).length >= 2) {
                    pid2 = childAndName[0];
                    name2 = childAndName[1].substring(0, childAndName[1].length() - 1);
                }
                if (!event.equals("execve") || !parts[i].startsWith("ptid")) continue;
                String parentProcess = parts[i].substring(5);
                String[] ptidAndName = parentProcess.split("\\(");
                pid2 = ptidAndName[0];
                name2 = ptidAndName[1].substring(0, ptidAndName[1].length() - 1);
            }
            if (!this.processHashMap.containsKey(pid + name)) {
                Process proc = new Process(reput, id, hopCount, pid, null, null, null, timestamp1, timestamp2, name, this.uniqID++);
                this.processHashMap.put(proc.getPidAndName(), proc);
            }
            if (pid2 != null && name2 != null && !this.processHashMap.containsKey(pid2 + name2)) {
                Process child = new Process(reput, id, hopCount, pid2, null, null, null, timestamp1, timestamp2, name2, this.uniqID++);
                this.processHashMap.put(child.getPidAndName(), child);
            }
            if (path != null && !this.fileHashMap.containsKey(path)) {
                FileEntity fileTarget = new FileEntity(reput, id, hopCount, timestamp1, timestamp2, null, null, path, this.uniqID++);
                this.fileHashMap.put(fileTarget.getPath(), fileTarget);
            }
            if (srcIP != null && destIP != null && !this.networkHashMap.containsKey(srcIP + ":" + srcPort + "->" + destIP + ":" + destPort)) {
                NetworkEntity networkEntity = new NetworkEntity(reput, id, hopCount, timestamp1, timestamp2, srcIP, destIP, srcPort, destPort, this.uniqID++);
                this.networkHashMap.put(networkEntity.getSrcAndDstIP(), networkEntity);
            }
            if (this.PtoF.contains(event)) {
                if (direction.equals(">") && path != null) {
                    this.addProcessToFileEvent(pid, name, timestamp1, timestamp2, path, event, size2, id);
                } else if (direction.equals("<") && latency != null) {
                    this.processToFileSetEnd(timestamp1, timestamp2, latency, id);
                }
            }
            if (this.PtoP.contains(event) && direction.equals("<") && pid2 != null && name2 != null && !latency.equals("0")) {
                Process source = this.processHashMap.get(pid + name);
                Process sink = this.processHashMap.get(pid2 + name2);
                endTime = timestamp1 + "." + timestamp2;
                end = new BigDecimal((String)endTime);
                duration = new BigDecimal(latency);
                BigDecimal startTime = end.subtract(duration);
                String start = startTime.toString();
                startS = start.split("\\.")[0];
                startMs = start.split("\\.")[1];
                pp = new PtoPEvent("Process To Process", startS, startMs, source, sink, event, id);
                this.ppmap.put(start, pp);
                pp.setEndTime((String)endTime);
            }
            if ((event.equals("read") || event.equals("readv") || event.equals("recvfrom")) && direction.equals(">") && path != null && pid != null && name != null) {
                this.addFileToProcessEvent(pid, name, timestamp1, timestamp2, cpu, path, event, size2, id);
            }
            if (this.PtoN.contains(event) && ip != null && this.localIPS.contains(srcIP) && direction.equals(">")) {
                this.addProcessToNextworkEvent(srcIP, srcPort, destIP, destPort, pid, name, timestamp1, timestamp2, size2, event, cpu, id);
            }
            if (this.NtoP.contains(event) && ip != null && direction.equals(">")) {
                this.addNetworkToProcessEvent(srcIP, srcPort, destIP, destPort, pid, name, timestamp1, timestamp2, size2, event, cpu, id);
            }
            if (direction.equals("<") && latency != null) {
                BigDecimal duration2 = new BigDecimal(latency);
                duration2 = duration2.scaleByPowerOfTen(-9);
                String end2 = timestamp1 + "." + timestamp2;
                endTime = new BigDecimal(end2);
                BigDecimal startTime = ((BigDecimal)endTime).subtract(duration2);
                String start = startTime.toString();
                String startS2 = start.split("\\.")[0];
                String startMs2 = start.split("\\.")[1];
                String key = pid + name + startS2 + startMs2;
                if (this.npmap.containsKey(key)) {
                    this.setNetworkToProcessEnd(this.npmap.get(key), timestamp1, timestamp2);
                } else if (this.fpmap.containsKey(key)) {
                    this.setFileToProcessEnd(this.fpmap.get(key), timestamp1, timestamp2);
                } else if (this.pnmap.containsKey(key)) {
                    this.setProcessToNetworkEnd(this.pnmap.get(key), timestamp1, timestamp2);
                }
            }
            if (!event.equals("execve") || !direction.equals("<")) continue;
            Process newProcss = this.processHashMap.get(pid + name);
            Process parent = this.processHashMap.get(pid2 + name2);
            endTime = timestamp1 + "." + timestamp2;
            end = new BigDecimal((String)endTime);
            duration = new BigDecimal(latency);
            duration = duration.scaleByPowerOfTen(-9);
            BigDecimal start = end.subtract(duration);
            String startTime = start.toString();
            startS = startTime.split("\\.")[0];
            startMs = startTime.split("\\.")[1];
            pp = new PtoPEvent("Process To Process", startS, startMs, parent, newProcss, event, id);
            pp.setEndTime((String)endTime);
            this.ppmap.put(timestamp1 + "." + startMs, pp);
        }
    }

    @Override
    public void afterBuilding() {
    }

    private String[] getIPandPorts(String str) {
        String[] res = new String[4];
        String[] srcAndDest = str.split("->");
        if (srcAndDest.length < 2) {
            System.out.println(str.length());
            System.out.println(str);
            throw new ArrayIndexOutOfBoundsException("wired form of IP");
        }
        String[] src = srcAndDest[0].split(":");
        String[] dest = srcAndDest[1].split(":");
        res[0] = src[0];
        res[1] = src[1];
        res[2] = dest[0];
        res[3] = dest[1];
        return res;
    }

    private void addProcessToFileEvent(String pid, String name, String timestamp1, String timestamp2, String path, String event, long size2, long id) {
        Process p = this.processHashMap.get(pid + name);
        FileEntity f = this.fileHashMap.get(path);
        String start = timestamp1 + "." + timestamp2;
        PtoFEvent pf = new PtoFEvent("ProcessToFile", timestamp1, timestamp2, p, f, event, size2, id);
        this.pfmap.put(start, pf);
    }

    private void processToFileSetEnd(String timestamp1, String timestamp2, String latency, long id) {
        String end = timestamp1 + "." + timestamp2;
        BigDecimal duration = new BigDecimal(latency);
        BigDecimal endTime = new BigDecimal(end);
        BigDecimal startTime = endTime.subtract(duration = duration.scaleByPowerOfTen(-9));
        String key = startTime.toString();
        if (!this.pfmap.containsKey(key)) {
            return;
        }
        this.pfmap.get(key).setEndTime(end);
    }

    private void addProcessToProcessEvent(String pid, String name, String timestamp1, String timestamp2, String event, long id) {
        Process source = this.processHashMap.get(pid + name);
        String start = timestamp1 + "." + timestamp2;
        PtoPEvent pp = new PtoPEvent("Process To Process", timestamp1, timestamp2, source, null, event, id);
        this.ppmap.put(start, pp);
    }

    private void setProcessToProcessEventSinkAndEnd(String pid, String name, String timestamp1, String timestamp2, String latency) {
        Process sink = this.processHashMap.get(pid + name);
        BigDecimal duration = new BigDecimal(latency);
        duration = duration.scaleByPowerOfTen(-9);
        String end = timestamp1 + "." + timestamp2;
        BigDecimal endTime = new BigDecimal(end);
        BigDecimal startTime = endTime.subtract(duration);
        String key = startTime.toString();
        PtoPEvent event = null;
        if (this.ppmap.containsKey(key)) {
            event = this.ppmap.get(key);
        }
        if (event != null) {
            event.setSink(sink);
            event.setEndTime(end);
        }
    }

    private void addFileToProcessEvent(String pid, String name, String timestamp1, String timestamp2, String cpu, String path, String event, long size2, long id) {
        FileEntity source;
        Process sink = this.processHashMap.get(pid + name);
        if (sink == null) {
            System.out.println("Pid and name not in Dict: " + pid + " " + name);
        }
        if ((source = this.fileHashMap.get(path)) == null) {
            System.out.println("Path not int file Dict: " + path);
        }
        FtoPEvent fp = new FtoPEvent("FileToProcess", timestamp1, timestamp2, source, sink, event, id);
        fp.updateSize(size2);
        this.fpmap.put(pid + name + timestamp1 + timestamp2, fp);
    }

    private void addNetworkToProcessEvent(String srcIP, String srcP, String dstIp, String dstP, String pid, String name, String timestamp1, String timestamp2, long size2, String event, String cpu, long id) {
        NetworkEntity source = this.networkHashMap.get(srcIP + ":" + srcP + "->" + dstIp + ":" + dstP);
        Process sink = this.processHashMap.get(pid + name);
        NtoPEvent np = new NtoPEvent("NetworkToProcess", timestamp1, timestamp2, source, sink, event, id);
        np.updateSize(size2);
        String key = pid + name + timestamp1 + timestamp2;
        this.npmap.put(key, np);
    }

    private void setNetworkToProcessEnd(NtoPEvent np, String timestamp1, String timestamp2) {
        String end = timestamp1 + "." + timestamp2;
        np.setEndTime(end);
    }

    private void setFileToProcessEnd(FtoPEvent fp, String timestamp1, String timestamp2) {
        String end = timestamp1 + "." + timestamp2;
        fp.setEndTime(end);
    }

    private void addProcessToNextworkEvent(String srcIP, String srcP, String dstIp, String dstP, String pid, String name, String timestamp1, String timestamp2, long size2, String event, String cpu, long id) {
        Process source = this.processHashMap.get(pid + name);
        NetworkEntity sink = this.networkHashMap.get(srcIP + ":" + srcP + "->" + dstIp + ":" + dstP);
        PtoNEvent pn = new PtoNEvent("Process To Network", timestamp1, timestamp2, source, sink, event, id);
        pn.updateSize(size2);
        String key = pid + name + timestamp1 + timestamp2;
        this.pnmap.put(key, pn);
    }

    private void setProcessToNetworkEnd(PtoNEvent pn, String timestamp1, String timestamp2) {
        pn.setEndTime(timestamp1 + "." + timestamp2);
    }
}

