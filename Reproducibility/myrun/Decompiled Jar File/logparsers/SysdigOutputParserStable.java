/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package logparsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import logparsers.SysdigOutputParser;
import logparsers.exceptions.UnknownEventException;
import logparsers.systemcalls.Fingerprint;
import logparsers.systemcalls.SystemCall;
import logparsers.systemcalls.SystemCallFactory;
import pagerank.Entity;
import pagerank.FileEntity;
import pagerank.FtoPEvent;
import pagerank.MetaConfig;
import pagerank.NetworkEntity;
import pagerank.NtoPEvent;
import pagerank.Process;
import pagerank.PtoFEvent;
import pagerank.PtoNEvent;
import pagerank.PtoPEvent;

public class SysdigOutputParserStable
implements SysdigOutputParser {
    private File log;
    private Set<String> localIP;
    private long UID;
    private double repu = 0.0;
    private int hops = 0;
    private boolean hostOrNot = false;
    private HashMap<String, FileEntity> files = new HashMap();
    private HashMap<String, Process> processes = new HashMap();
    private HashMap<String, NetworkEntity> networks = new HashMap();
    private HashMap<String, PtoFEvent> pfEvent = new HashMap();
    private HashMap<String, PtoNEvent> pnEvent = new HashMap();
    private HashMap<String, PtoPEvent> ppEvent = new HashMap();
    private HashMap<String, NtoPEvent> npEvent = new HashMap();
    private HashMap<String, FtoPEvent> fpEvent = new HashMap();
    private Map<Fingerprint, SystemCall> answering;
    private Map<String, Map<String, String>> incompleteEvents = new HashMap<String, Map<String, String>>();
    private Map<String, PtoPEvent> backFlow = new HashMap<String, PtoPEvent>();
    private Map<String, PtoPEvent> forwardFlow = new HashMap<String, PtoPEvent>();
    private static final Pattern pSysdigEntry = Pattern.compile("(?<timestamp>\\d+\\.\\d+) (?<cpu>\\d+) (?<process>.+?) \\((?<pid>\\d+)\\) (?<direction>[><]) (?<event>.+?) cwd=(?<cwd>.+?) (?<args>.*?) latency=(?<latency>\\d+)");
    private static final Pattern pFile = Pattern.compile("fd=(?<fd>\\d+)\\(<f>(?<path>.+?)\\)");
    private static final Pattern pProcessFile = Pattern.compile("filename=(?<path>[^ ]+)");
    private static final Pattern pSocket = Pattern.compile("(?:(?:fd)|(?:res))=(?<fd>\\d+)\\((?:(?:<4t>)|(?:<4u>))(?<sourceIP>\\d+\\.\\d+\\.\\d+\\.\\d+):(?<sourcePort>\\d+)->(?<desIP>\\d+\\.\\d+\\.\\d+\\.\\d+):(?<desPort>\\d+)\\)");
    private static final Pattern pSize = Pattern.compile("res=(?<size>\\d+)");
    private static final Pattern pParent = Pattern.compile("ptid=(?<parentPID>\\d+)\\((?<parent>.+?)\\)");

    public static void main(String[] args) {
        try {
            SysdigOutputParserStable parser = new SysdigOutputParserStable("input/test_logs/rename.txt", MetaConfig.localIP);
            parser.getEntities();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public SysdigOutputParserStable(String pathToLog, String[] localIP) {
        this.answering = new HashMap<Fingerprint, SystemCall>();
        this.log = new File(pathToLog);
        this.UID = 0L;
        this.localIP = new HashSet<String>();
        this.localIP.addAll(Arrays.asList(localIP));
        this.registerSystemCalls();
    }

    @Override
    public void setHostOrNot(boolean i) {
        this.hostOrNot = i;
    }

    private void registerSystemCalls() {
        SystemCallFactory f2pSystemCall = new SystemCallFactory("FtoP", FileEntity.class, null).addAction(this::updateP2PLinks).addAction(this::addF2PEvent);
        for (String s2 : MetaConfig.ftopSystemCall) {
            SystemCall systemCall = f2pSystemCall.getSystemCall(s2);
            this.answering.put(systemCall.fingerPrint, systemCall);
        }
        SystemCallFactory p2fSystemCall = new SystemCallFactory("PtoF", FileEntity.class, null).addAction(this::updateP2PLinks).addAction(this::addP2FEvent);
        for (String s3 : MetaConfig.ptofSystemCall) {
            SystemCall systemCall = p2fSystemCall.getSystemCall(s3);
            this.answering.put(systemCall.fingerPrint, systemCall);
        }
        SystemCallFactory n2pSystemCall = new SystemCallFactory("NtoP", NetworkEntity.class, null).addAction(this::updateP2PLinks).addAction(this::addN2PEvent);
        for (String s4 : MetaConfig.ntopSystemCall) {
            SystemCall systemCall = n2pSystemCall.getSystemCall(s4);
            this.answering.put(systemCall.fingerPrint, systemCall);
        }
        SystemCallFactory p2nSystemCall = new SystemCallFactory("PtoN", NetworkEntity.class, null).addAction(this::updateP2PLinks).addAction(this::addP2NEvent);
        for (String s5 : MetaConfig.ptonSystemCall) {
            SystemCall systemCall = p2nSystemCall.getSystemCall(s5);
            this.answering.put(systemCall.fingerPrint, systemCall);
        }
        SystemCall execve = new SystemCall("execve", "PtoP", new Fingerprint("execve", FileEntity.class, null)).addAction((mStart, mEnd, eStart, eEnd) -> {
            Matcher mParent;
            Process pStart = (Process)eStart[0];
            Process pEnd = (Process)eEnd[0];
            FileEntity f = (FileEntity)eStart[1];
            String timestampStart = (String)mStart.get("timestamp");
            String timestampEnd = (String)mEnd.get("timestamp");
            String cwd = (String)mStart.get("cwd");
            String key = timestampStart + ":execve:" + cwd;
            String[] timestampsStart = timestampStart.split("\\.");
            String[] timestampsEnd = timestampEnd.split("\\.");
            String args = (String)mEnd.get("args");
            if (!args.contains("res=0")) {
                return;
            }
            if (!f.getPath().equals("<NA>")) {
                FtoPEvent fp = new FtoPEvent(timestampsStart[0], timestampsStart[1], f, pEnd, "execve", 0L, 0L);
                fp.setEndTime(timestampStart);
                this.fpEvent.put(key, fp);
            }
            if ((mParent = pParent.matcher(args)).find()) {
                String pidParent = mParent.group("parentPID");
                String nameParent = mParent.group("parent");
                String keyParent = pidParent + nameParent;
                Process parent = this.processes.computeIfAbsent(keyParent, k -> new Process(this.repu, -1L, this.hops, pidParent, null, null, null, timestampsStart[0], timestampsStart[1], nameParent, this.UID++));
                PtoPEvent forwardLink = new PtoPEvent(timestampsStart[0], timestampsStart[1], parent, pEnd, "execve", 0L);
                forwardLink.setEndTime(timestampEnd);
                this.ppEvent.put(key, forwardLink);
                this.forwardFlow.put(keyParent, forwardLink);
                PtoPEvent backLink = new PtoPEvent(timestampsStart[0], timestampsStart[1], pEnd, parent, "execve", 0L);
                backLink.setEndTime(timestampEnd);
                this.backFlow.put((String)mEnd.get("pid") + (String)mEnd.get("process"), backLink);
                this.ppEvent.put(key + "back", backLink);
            }
        });
        this.answering.put(execve.fingerPrint, execve);
        SystemCall accept = new SystemCall("accept", "NtoP", new Fingerprint("accept", null, NetworkEntity.class)).addAction((mStart, mEnd, entitiesStart, entitiesEnd) -> {
            Process p = (Process)entitiesEnd[0];
            NetworkEntity n = (NetworkEntity)entitiesEnd[1];
            String timestamp = (String)mEnd.get("timestamp");
            String cwd = (String)mEnd.get("cwd");
            String key = timestamp + ":accept:" + cwd;
            String[] timestamps = timestamp.split("\\.");
            NtoPEvent np = new NtoPEvent(timestamps[0], timestamps[1], n, p, (String)mEnd.get("event"), 0L, 0L);
            np.setEndTime(timestamp);
            PtoNEvent pn = new PtoNEvent(timestamps[0], timestamps[1], p, n, (String)mEnd.get("event"), 0L, 0L);
            pn.setEndTime(timestamp);
            this.npEvent.put(key, np);
            this.pnEvent.put(key, pn);
        });
        this.answering.put(accept.fingerPrint, accept);
        SystemCall fcntl = new SystemCall("fcntl", "NtoP", new Fingerprint("fcntl", null, NetworkEntity.class)).addAction((mStart, mEnd, entitiesStart, entitiesEnd) -> {
            String timestampStart = (String)mStart.get("timestamp");
            String event = (String)mStart.get("event");
            String cwd = (String)mStart.get("cwd");
            String key = timestampStart + ":" + event + ":" + cwd;
            String[] timestampsStart = timestampStart.split("\\.");
            String timestampEnd = (String)mEnd.get("timestamp");
            Process p = (Process)entitiesEnd[0];
            NetworkEntity n = (NetworkEntity)entitiesEnd[1];
            NtoPEvent np = new NtoPEvent(timestampsStart[0], timestampsStart[1], n, p, (String)mStart.get("event"), 0L, 0L);
            np.setEndTime(timestampEnd);
            this.npEvent.put(key, np);
        });
        this.answering.put(fcntl.fingerPrint, fcntl);
        SystemCall rename = new SystemCall("rename", "PtoF", new Fingerprint("rename", null, null)).addAction((mStart, mEnd, entitiesStart, entitiesEnd) -> {
            String timestampStart = (String)mStart.get("timestamp");
            String timestampEnd = (String)mEnd.get("timestamp");
            String[] timestampsStart = timestampStart.split("\\.");
            String event = (String)mEnd.get("event");
            String args = (String)mEnd.get("args");
            String cwd = (String)mEnd.get("cwd");
            String oldPath = args.substring(args.indexOf("oldpath=") + 8, args.lastIndexOf(" newpath"));
            String newPath = args.substring(args.indexOf("newpath=") + 8, args.lastIndexOf(" "));
            if (oldPath.endsWith(")")) {
                oldPath = oldPath.substring(oldPath.indexOf("(") + 1, oldPath.length() - 1);
            }
            if (newPath.endsWith(")")) {
                newPath = newPath.substring(newPath.indexOf("(") + 1, newPath.length() - 1);
            }
            String realOldPath = oldPath;
            String realNewPath = newPath;
            Process p = (Process)entitiesStart[0];
            String key = timestampStart + ":" + event + ":" + cwd;
            FileEntity oldFile = this.files.computeIfAbsent(oldPath, k -> new FileEntity(this.repu, 0L, this.hops, timestampsStart[0], timestampsStart[1], null, null, realOldPath, this.UID++));
            FtoPEvent fp = new FtoPEvent(timestampsStart[0], timestampsStart[1], oldFile, p, (String)mStart.get("event"), 0L, 0L);
            fp.setEndTime(timestampEnd);
            this.fpEvent.put(key, fp);
            FileEntity newFile = this.files.computeIfAbsent(newPath, k -> new FileEntity(this.repu, 0L, this.hops, timestampsStart[0], timestampsStart[1], null, null, realNewPath, this.UID++));
            PtoFEvent pf = new PtoFEvent(timestampsStart[0], timestampsStart[1], p, newFile, (String)mStart.get("event"), 0L, 0L);
            pf.setEndTime(timestampEnd);
            this.pfEvent.put(key, pf);
        });
        this.answering.put(rename.fingerPrint, rename);
    }

    @Override
    public void getEntities() throws IOException {
        String currentLine;
        System.out.println("Parsing...");
        long start = System.currentTimeMillis();
        BufferedReader logReader = new BufferedReader(new FileReader(this.log), 0x100000);
        while ((currentLine = logReader.readLine()) != null) {
            Map<String, String> fields = this.matchEntry(currentLine);
            if (fields.isEmpty()) continue;
            if (fields.get("direction").equals(">")) {
                this.incompleteEvents.put(fields.get("timestamp") + ":" + fields.get("event") + ":" + fields.get("cwd"), fields);
                continue;
            }
            try {
                this.processEvent(fields);
            } catch (Exception exception) {}
        }
        long end = System.currentTimeMillis();
        System.out.println("Parsing(in parser) time Cost:" + (double)(end - start) / 1000.0);
    }

    @Override
    public void afterBuilding() {
    }

    private void processEvent(Map<String, String> end) throws UnknownEventException {
        Map<String, String> start;
        String startTimestamp = new BigDecimal(end.get("timestamp")).subtract(new BigDecimal(end.get("latency")).scaleByPowerOfTen(-9)).toString();
        String key = startTimestamp + ":" + end.get("event") + ":" + end.get("cwd");
        if (!this.incompleteEvents.containsKey(key)) {
            String dummyEntry = String.format("%s %s %s %s (%s) %s %s cwd=%s !dummy! latency=%s", "0", startTimestamp, end.get("cpu"), end.get("process"), end.get("pid"), ">", end.get("event"), end.get("cwd"), end.get("latency"));
            start = this.matchEntry(dummyEntry);
        } else {
            start = this.incompleteEvents.remove(key);
        }
        Entity[] startEntites = this.extractEntities(start);
        Entity[] endEntities = this.extractEntities(end);
        Fingerprint f = Fingerprint.toFingerPrint(start, end, startEntites, endEntities);
        SystemCall systemCall = this.answering.getOrDefault(f, null);
        if (systemCall == null) {
            throw new UnknownEventException("Unknown event: " + start.get("event"));
        }
        systemCall.react(start, end, startEntites, endEntities);
        if (start.get("args").equals("!dummy!")) {
            System.out.println("Event enter point not seen: " + start.get("raw"));
        }
    }

    public void updateP2PLinks(Map<String, String> mStart, Map<String, String> mEnd, Entity[] entitiesStart, Entity[] entitiesEnd) {
        PtoPEvent ff;
        PtoPEvent bf;
        String endTime = mEnd.get("timestamp");
        if (this.backFlow.containsKey(mEnd.get("pid") + mEnd.get("process")) && new BigDecimal((bf = this.backFlow.get(mEnd.get("pid") + mEnd.get("process"))).getEnd()).compareTo(new BigDecimal(endTime)) < 0) {
            bf.setEndTime(endTime);
        }
        if (this.forwardFlow.containsKey(mEnd.get("pid") + mEnd.get("process")) && new BigDecimal((ff = this.forwardFlow.get(mEnd.get("pid") + mEnd.get("process"))).getEnd()).compareTo(new BigDecimal(endTime)) < 0) {
            ff.setEndTime(endTime);
        }
    }

    private void addP2FEvent(Map<String, String> mStart, Map<String, String> mEnd, Entity[] entitiesStart, Entity[] entitiesEnd) {
        Process p = (Process)entitiesStart[0];
        FileEntity f = (FileEntity)entitiesStart[1];
        String timestampStart = mStart.get("timestamp");
        String event = mStart.get("event");
        String cwd = mStart.get("cwd");
        String key = timestampStart + ":" + event + ":" + cwd;
        String[] timestampsStart = timestampStart.split("\\.");
        String timestampEnd = mEnd.get("timestamp");
        String args = mEnd.get("args");
        Matcher mSize = pSize.matcher(args);
        if (mSize.find()) {
            long size2 = Long.parseLong(mSize.group("size"));
            PtoFEvent pf = new PtoFEvent(timestampsStart[0], timestampsStart[1], p, f, mStart.get("event"), size2, 0L);
            pf.setEndTime(timestampEnd);
            this.pfEvent.put(key, pf);
        }
    }

    private void addF2PEvent(Map<String, String> mStart, Map<String, String> mEnd, Entity[] entitiesStart, Entity[] entitiesEnd) {
        Process p = (Process)entitiesStart[0];
        FileEntity f = (FileEntity)entitiesStart[1];
        String timestampStart = mStart.get("timestamp");
        String event = mStart.get("event");
        String cwd = mStart.get("cwd");
        String key = timestampStart + ":" + event + ":" + cwd;
        String[] timestampsStart = timestampStart.split("\\.");
        String timestampEnd = mEnd.get("timestamp");
        String args = mEnd.get("args");
        Matcher mSize = pSize.matcher(args);
        if (mSize.find()) {
            long size2 = Long.parseLong(mSize.group("size"));
            FtoPEvent fp = new FtoPEvent(timestampsStart[0], timestampsStart[1], f, p, mStart.get("event"), size2, 0L);
            fp.setEndTime(timestampEnd);
            this.fpEvent.put(key, fp);
        }
    }

    private void addP2NEvent(Map<String, String> mStart, Map<String, String> mEnd, Entity[] entitiesStart, Entity[] entitiesEnd) {
        Process p = (Process)entitiesStart[0];
        NetworkEntity n = (NetworkEntity)entitiesStart[1];
        String timestampStart = mStart.get("timestamp");
        String event = mStart.get("event");
        String cwd = mStart.get("cwd");
        String key = timestampStart + ":" + event + ":" + cwd;
        String[] timestampsStart = timestampStart.split("\\.");
        String timestampEnd = mEnd.get("timestamp");
        String args = mEnd.get("args");
        Matcher mSize = pSize.matcher(args);
        if (mSize.find()) {
            long size2 = Long.parseLong(mSize.group("size"));
            PtoNEvent pn = new PtoNEvent(timestampsStart[0], timestampsStart[1], p, n, mStart.get("event"), size2, 0L);
            pn.setEndTime(timestampEnd);
            this.pnEvent.put(key, pn);
        }
    }

    private void addN2PEvent(Map<String, String> mStart, Map<String, String> mEnd, Entity[] entitiesStart, Entity[] entitiesEnd) {
        String args = mEnd.get("args");
        Matcher mSize = pSize.matcher(args);
        Process p = (Process)entitiesStart[0];
        NetworkEntity n = (NetworkEntity)entitiesStart[1];
        String timestampStart = mStart.get("timestamp");
        String event = mStart.get("event");
        String cwd = mStart.get("cwd");
        String key = timestampStart + ":" + event + ":" + cwd;
        String[] timestampsStart = timestampStart.split("\\.");
        String timestampEnd = mEnd.get("timestamp");
        if (mSize.find()) {
            long size2 = Long.parseLong(mSize.group("size"));
            NtoPEvent np = new NtoPEvent(timestampsStart[0], timestampsStart[1], n, p, mStart.get("event"), size2, 0L);
            np.setEndTime(timestampEnd);
            this.npEvent.put(key, np);
        }
    }

    private Entity[] extractEntities(Map<String, String> m3) {
        Entity[] res = new Entity[2];
        long id = 0L;
        String pid = m3.get("pid");
        String process = m3.get("process");
        String processKey = pid + process;
        String[] timestamp = m3.get("timestamp").split("\\.");
        res[0] = this.processes.computeIfAbsent(processKey, key -> new Process(this.repu, id, this.hops, pid, null, null, null, timestamp[0], timestamp[1], process, this.UID++));
        String args = m3.get("args");
        Matcher mFile = pFile.matcher(args);
        Matcher mProcessFile = pProcessFile.matcher(args);
        Matcher mSocket = pSocket.matcher(args);
        if (mFile.find()) {
            String path = mFile.group("path");
            if (path.indexOf("(") != -1) {
                path = path.substring(path.indexOf("(") + 1, path.length() - 1);
            }
            String path2 = path;
            res[1] = this.files.computeIfAbsent(path2, key -> new FileEntity(this.repu, id, this.hops, timestamp[0], timestamp[1], null, null, path2, this.UID++));
        } else if (mProcessFile.find()) {
            String path = mProcessFile.group("path");
            if (path.indexOf("(") != -1) {
                path = path.substring(path.indexOf("(") + 1, path.length() - 1);
            }
            String path2 = path;
            res[1] = this.files.computeIfAbsent(path2, key -> new FileEntity(this.repu, id, this.hops, timestamp[0], timestamp[1], null, null, path2, this.UID++));
        } else if (mSocket.find()) {
            String sourceIP = mSocket.group("sourceIP");
            String sourcePort = mSocket.group("sourcePort");
            String desIP = mSocket.group("desIP");
            String desPort = mSocket.group("desPort");
            res[1] = this.networks.computeIfAbsent(sourceIP + ":" + sourcePort + "->" + desIP + ":" + desPort, key -> new NetworkEntity(this.repu, id, this.hops, timestamp[0], timestamp[1], sourceIP, desIP, sourcePort, desPort, this.UID++));
        } else {
            res[1] = null;
        }
        return res;
    }

    private Map<String, String> matchEntry(String entry) {
        HashMap<String, String> res = new HashMap<String, String>();
        Matcher m3 = pSysdigEntry.matcher(entry);
        if (m3.find()) {
            res.put("raw", entry);
            res.put("timestamp", m3.group("timestamp"));
            res.put("cpu", m3.group("cpu"));
            res.put("process", m3.group("process"));
            res.put("pid", m3.group("pid"));
            res.put("direction", m3.group("direction"));
            res.put("event", m3.group("event"));
            res.put("cwd", m3.group("cwd"));
            res.put("latency", m3.group("latency"));
            res.put("args", m3.group("args"));
        }
        return res;
    }

    public HashMap<String, PtoFEvent> getPfmap() {
        return this.pfEvent;
    }

    public HashMap<String, PtoNEvent> getPnmap() {
        return this.pnEvent;
    }

    public HashMap<String, PtoPEvent> getPpmap() {
        return this.ppEvent;
    }

    public HashMap<String, NtoPEvent> getNpmap() {
        return this.npEvent;
    }

    public HashMap<String, FtoPEvent> getFpmap() {
        return this.fpEvent;
    }
}
