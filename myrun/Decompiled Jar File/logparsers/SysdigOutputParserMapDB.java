/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package logparsers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
import logparsers.Utils;
import logparsers.exceptions.EventStartUnseenException;
import logparsers.exceptions.UnknownEventException;
import logparsers.systemcalls.Fingerprint;
import logparsers.systemcalls.SystemCall;
import logparsers.systemcalls.SystemCallFactory;
import org.jetbrains.annotations.NotNull;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;
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

public class SysdigOutputParserMapDB
implements SysdigOutputParser {
    private DB db = DBMaker.memoryDB().allocateStartSize(0x40000000L).allocateIncrement(0x20000000L).make();
    private File log;
    private Set<String> localIP;
    private long UID;
    private double repu = 0.0;
    private int hops = 0;
    private Map<String, FileEntity> files;
    private Map<String, Process> processes;
    private Map<String, NetworkEntity> networks;
    private Map<String, PtoFEvent> pfEvent;
    private Map<String, PtoNEvent> pnEvent;
    private Map<String, PtoPEvent> ppEvent;
    private Map<String, NtoPEvent> npEvent;
    private Map<String, FtoPEvent> fpEvent;
    private Map<Fingerprint, SystemCall> answering;
    private Map<String, Map<String, String>> incompleteEvents;
    private Map<String, PtoPEvent> backFlow;
    private Map<String, PtoPEvent> forwardFlow;
    public boolean hostOrNot;
    private static final Pattern pParent = Pattern.compile("ptid=(?<parentPID>\\d+)\\((?<parent>.+?)\\)");

    public static void main(String[] args) {
        try {
            SysdigOutputParserMapDB parser = new SysdigOutputParserMapDB("input/attacks_bad/command_injection_step3.txt", MetaConfig.localIP);
            parser.getEntities();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public SysdigOutputParserMapDB(String pathToLog, String[] localIP) {
        Kryo kryo = new Kryo();
        kryo.register(FileEntity.class);
        kryo.register(Process.class);
        kryo.register(NetworkEntity.class);
        kryo.register(PtoFEvent.class);
        kryo.register(FtoPEvent.class);
        kryo.register(PtoNEvent.class);
        kryo.register(NtoPEvent.class);
        kryo.register(PtoPEvent.class);
        KryoSerializer serializer = new KryoSerializer(kryo);
        this.hostOrNot = false;
        this.files = this.db.hashMap("file:" + pathToLog, Serializer.STRING, serializer).createOrOpen();
        this.processes = this.db.hashMap("processes" + pathToLog, Serializer.STRING, serializer).createOrOpen();
        this.networks = this.db.hashMap("networks" + pathToLog, Serializer.STRING, serializer).createOrOpen();
        this.pfEvent = this.db.hashMap("pfEvent" + pathToLog, Serializer.STRING, serializer).createOrOpen();
        this.pnEvent = this.db.hashMap("pnEvent" + pathToLog, Serializer.STRING, serializer).createOrOpen();
        this.ppEvent = this.db.hashMap("ppEvent" + pathToLog, Serializer.STRING, serializer).createOrOpen();
        this.npEvent = this.db.hashMap("npEvent" + pathToLog, Serializer.STRING, serializer).createOrOpen();
        this.fpEvent = this.db.hashMap("fpEvent" + pathToLog, Serializer.STRING, serializer).createOrOpen();
        this.incompleteEvents = new HashMap<String, Map<String, String>>();
        this.backFlow = this.db.hashMap("backFlow" + pathToLog, Serializer.STRING, serializer).createOrOpen();
        this.forwardFlow = this.db.hashMap("forwardFlow" + pathToLog, Serializer.STRING, serializer).createOrOpen();
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
            Map<String, String> matcher = Utils.parseEntry(currentLine);
            if (matcher.isEmpty()) continue;
            if (matcher.get("direction").equals(">")) {
                this.incompleteEvents.put(matcher.get("timestamp") + ":" + matcher.get("event") + ":" + matcher.get("cwd"), matcher);
                continue;
            }
            try {
                this.processEvent(matcher);
            } catch (Exception exception) {}
        }
        long end = System.currentTimeMillis();
        System.out.println("Parsing(in parser) time Cost:" + (double)(end - start) / 1000.0);
    }

    @Override
    public void afterBuilding() {
        this.db.close();
    }

    private void processEvent(Map<String, String> end) throws EventStartUnseenException, UnknownEventException {
        Map<String, String> start;
        String startTimestamp = new BigDecimal(end.get("timestamp")).subtract(new BigDecimal(end.get("latency")).scaleByPowerOfTen(-9)).toString();
        String key = startTimestamp + ":" + end.get("event") + ":" + end.get("cwd");
        if (!this.incompleteEvents.containsKey(key)) {
            String dummyEntry = String.format("%s %s %s %s (%s) %s %s cwd=%s !dummy!  latency=%s", "0", startTimestamp, end.get("cpu"), end.get("process"), end.get("pid"), ">", end.get("event"), end.get("cwd"), end.get("latency"));
            start = Utils.parseEntry(dummyEntry);
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
            System.out.println("Event enter point not seen: " + end.get("raw"));
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
        long size2 = Utils.extractSize(args);
        if (size2 != -1L) {
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
        long size2 = Utils.extractSize(args);
        if (size2 != -1L) {
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
        long size2 = Utils.extractSize(args);
        if (size2 != -1L) {
            PtoNEvent pn = new PtoNEvent(timestampsStart[0], timestampsStart[1], p, n, mStart.get("event"), size2, 0L);
            pn.setEndTime(timestampEnd);
            this.pnEvent.put(key, pn);
        }
    }

    private void addN2PEvent(Map<String, String> mStart, Map<String, String> mEnd, Entity[] entitiesStart, Entity[] entitiesEnd) {
        String args = mEnd.get("args");
        Process p = (Process)entitiesStart[0];
        NetworkEntity n = (NetworkEntity)entitiesStart[1];
        String timestampStart = mStart.get("timestamp");
        String event = mStart.get("event");
        String cwd = mStart.get("cwd");
        String key = timestampStart + ":" + event + ":" + cwd;
        String[] timestampsStart = timestampStart.split("\\.");
        String timestampEnd = mEnd.get("timestamp");
        long size2 = Utils.extractSize(args);
        if (size2 != -1L) {
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
        Map<String, String> file_socket = Utils.extractFileandSocket(args);
        String process_file = Utils.extractProcessFile(args);
        if (file_socket.containsKey("path")) {
            String path = file_socket.get("path");
            res[1] = this.files.computeIfAbsent(path, key -> new FileEntity(this.repu, id, this.hops, timestamp[0], timestamp[1], null, null, path, this.UID++));
        } else if (process_file != null) {
            res[1] = this.files.computeIfAbsent(process_file, key -> new FileEntity(this.repu, id, this.hops, timestamp[0], timestamp[1], null, null, process_file, this.UID++));
        } else if (file_socket.containsKey("sip") && file_socket.containsKey("sport") && file_socket.containsKey("dip") && file_socket.containsKey("dport")) {
            String sourceIP = file_socket.get("sip");
            String sourcePort = file_socket.get("sport");
            String desIP = file_socket.get("dip");
            String desPort = file_socket.get("dport");
            res[1] = this.networks.computeIfAbsent(sourceIP + ":" + sourcePort + "->" + desIP + ":" + desPort, key -> new NetworkEntity(this.repu, id, this.hops, timestamp[0], timestamp[1], sourceIP, desIP, sourcePort, desPort, this.UID++));
        } else {
            res[1] = null;
        }
        return res;
    }

    @Override
    public Map<String, PtoFEvent> getPfmap() {
        return this.pfEvent;
    }

    @Override
    public Map<String, PtoNEvent> getPnmap() {
        return this.pnEvent;
    }

    @Override
    public Map<String, PtoPEvent> getPpmap() {
        return this.ppEvent;
    }

    @Override
    public Map<String, NtoPEvent> getNpmap() {
        return this.npEvent;
    }

    @Override
    public Map<String, FtoPEvent> getFpmap() {
        return this.fpEvent;
    }

    class KryoSerializer
    implements Serializer {
        Kryo kryo;

        KryoSerializer(Kryo kryo) {
            this.kryo = kryo;
        }

        public void serialize(@NotNull DataOutput2 dataOutput2, @NotNull Object o) throws IOException {
            ByteArrayOutputStream objStream = new ByteArrayOutputStream();
            Output output = new Output(objStream);
            this.kryo.writeClassAndObject(output, o);
            output.close();
            dataOutput2.write(objStream.toByteArray());
            dataOutput2.close();
        }

        public Object deserialize(@NotNull DataInput2 dataInput2, int i) throws IOException {
            int pos = dataInput2.getPos();
            return this.kryo.readClassAndObject(new Input(Arrays.copyOfRange(dataInput2.internalByteArray(), pos, pos + i)));
        }
    }
}

