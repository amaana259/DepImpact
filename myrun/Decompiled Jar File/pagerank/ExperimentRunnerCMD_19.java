/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.jgrapht.graph.DirectedPseudograph;
import org.json.simple.JSONObject;
import pagerank.EntityNode;
import pagerank.EventEdge;
import pagerank.Experiment;
import pagerank.GetGraph;
import pagerank.MetaConfig;
import pagerank.ProcessOneLogCMD_19;
import theia_exp.GraphReader;

public class ExperimentRunnerCMD_19 {
    DirectedPseudograph<EntityNode, EventEdge> graphFromLog;
    String PathToLogs;
    String PathToRes;
    FilenameFilter logNameFilter;
    String backWardDotPath;

    public static void main(String[] args) {
        for (String arg : args) {
            System.out.println(arg);
        }
        try {
            String resPath = "C:\\Users\\fang2\\OneDrive\\Desktop\\reptracker\\reptracker\\input_11\\vpnfilter-multi\\host1";
            String pathToLogs = "C:\\Users\\fang2\\OneDrive\\Desktop\\reptracker\\reptracker\\input_11\\vpnfilter-multi\\host1";
            String[] log = new String[]{"vpnfilterhost1.log"};
            ExperimentRunnerCMD_19 er = new ExperimentRunnerCMD_19(pathToLogs, resPath, log);
            er.run2("clusterall", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ExperimentRunnerCMD_19(String pathToLogs, String pathToRes, FilenameFilter logNameFilter) {
        this.PathToLogs = pathToLogs;
        this.PathToRes = pathToRes;
        this.logNameFilter = logNameFilter;
    }

    public ExperimentRunnerCMD_19(String pathTologs, String pathToRes, String[] lognames) {
        this.PathToLogs = pathTologs;
        this.PathToRes = pathToRes;
        HashSet<String> logset = new HashSet<String>();
        for (String s2 : lognames) {
            logset.add(s2);
        }
        this.logNameFilter = (dir, name) -> logset.contains(name);
    }

    public ExperimentRunnerCMD_19(String pathToLogs, String pathToRes, FilenameFilter logNameFilter, String[] exclusion) {
        this.PathToLogs = pathToLogs;
        this.PathToRes = pathToRes;
        HashSet<String> exclusionSet = new HashSet<String>();
        for (String s2 : exclusion) {
            exclusionSet.add(s2);
        }
        this.logNameFilter = (dir, name) -> logNameFilter.accept(dir, name) && !exclusionSet.contains(name);
    }

    public ExperimentRunnerCMD_19(String pathToRes, String pathToLogs, String dotPath) {
        this.PathToRes = pathToRes;
        this.backWardDotPath = dotPath;
        this.PathToLogs = pathToLogs;
    }

    public void run_with_backwardgraph(String mode) throws FileNotFoundException {
        File resDir = this.makeResDir(this.PathToRes);
        ArrayList<Experiment> experiments_backward = new ArrayList<Experiment>();
        try {
            List<File> propertyFiles = this.getPropertyFilesWithoutLog(this.PathToLogs);
            for (File f : propertyFiles) {
                if (f.getName().indexOf("backward") == -1) continue;
                Experiment exp = new Experiment(f);
                exp.setPathToDot(this.backWardDotPath);
                experiments_backward.add(exp);
            }
            for (Experiment e : experiments_backward) {
                File oneRes = new File(resDir + "/" + e.configFile.getParentFile().getName() + "-" + e.configFile.getName().split("_")[1]);
                if (!oneRes.exists()) {
                    oneRes.mkdir();
                }
                System.out.println(e.pathToDot);
                System.out.println(e.dotFile.getName());
                File logFile = new File(oneRes.getAbsolutePath() + "/" + e.dotFile.getName().split("\\.")[0] + ".log");
                PrintStream logStream = new PrintStream(new FileOutputStream(logFile, true));
                this.logging_without_parsing(e, logFile);
                LogStream ls = new LogStream(System.out, logStream);
                LogStream lse = new LogStream(System.err, logStream);
                System.setOut(ls);
                System.setErr(lse);
                JSONObject jsonLog = new JSONObject();
                jsonLog.put("Case", e.dotFile.getName());
                jsonLog.put("Mode", mode);
                GraphReader graphReader = new GraphReader();
                DirectedPseudograph<EntityNode, EventEdge> backtrack = graphReader.readGraph(this.backWardDotPath);
                ProcessOneLogCMD_19.run_exp_backward_without_backtrack(backtrack, oneRes.getAbsolutePath() + "/", "", e.threshold, e.trackOrigin, e.dotFile.getAbsolutePath(), MetaConfig.localIP, e.POI, e.highRP, e.midRP, e.lowRP, e.dotFile.getName().split("\\.")[0], e.detectionSize, e.getInitial(), e.criticalEdges, mode, jsonLog, e.getEntries(), e);
                logStream.close();
                Timestamp currentTimestamp = ExperimentRunnerCMD_19.getTimeStamp();
                jsonLog.put("Timestamp", currentTimestamp.toString());
                File entryPointsJsonFile = new File(oneRes.getAbsolutePath() + "/" + e.configFile.getName().split("\\.")[0] + "_json_log.json");
                FileWriter jsonWriter = new FileWriter(entryPointsJsonFile);
                jsonWriter.write(jsonLog.toJSONString());
                jsonWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run2(String mode, boolean host) throws FileNotFoundException {
        File resDir = this.makeResDir(this.PathToRes);
        File[] logs = this.getLogs(this.PathToLogs);
        File log = logs[0];
        ArrayList<Experiment> experiments_backward = new ArrayList<Experiment>();
        try (FileOutputStream fo2 = new FileOutputStream(this.PathToRes + "/build_time.log");){
            long start = System.currentTimeMillis();
            GetGraph generator = new GetGraph(logs[0].getPath(), MetaConfig.localIP, host);
            long end = System.currentTimeMillis();
            fo2.write(("parsing time:" + (double)(end - start) / 1000.0).getBytes());
            start = System.currentTimeMillis();
            generator.GenerateGraph();
            end = System.currentTimeMillis();
            fo2.write(("building time:" + (double)(end - start) / 1000.0).getBytes());
            this.graphFromLog = generator.getJg();
            for (EntityNode e : this.graphFromLog.vertexSet()) {
                if (e.isFileNode()) {
                    System.out.println(e.getF().getLocation());
                    continue;
                }
                if (e.isProcessNode()) {
                    e.getP().getLocation();
                    continue;
                }
                e.getN().getLocation();
            }
        } catch (IOException fo2) {
            // empty catch block
        }
        try {
            List<File> propertyFiles = this.getPropertyFiles(log);
            for (File propertyFile : propertyFiles) {
                String propertyName = propertyFile.getName();
                if (propertyName.indexOf("backward") == -1) continue;
                experiments_backward.add(new Experiment(log, propertyFile));
            }
            System.out.println("Nmber of exp: " + String.valueOf(experiments_backward.size()));
            for (Experiment e : experiments_backward) {
                File oneRes = new File(resDir + "/" + e.configFile.getParentFile().getName() + "-" + e.configFile.getName().split("_")[1]);
                if (!oneRes.exists()) {
                    oneRes.mkdir();
                }
                File logFile = new File(oneRes.getAbsolutePath() + "/" + e.log.getName().split("\\.")[0] + ".log");
                PrintStream logStream = new PrintStream(new FileOutputStream(logFile, true));
                this.logging(e, logFile);
                LogStream ls = new LogStream(System.out, logStream);
                LogStream lse = new LogStream(System.err, logStream);
                System.setOut(ls);
                System.setErr(lse);
                JSONObject jsonLog = new JSONObject();
                jsonLog.put("Case", e.log.getName());
                jsonLog.put("Mode", mode);
                ProcessOneLogCMD_19.run_exp_backward(this.graphFromLog, oneRes.getAbsolutePath() + "/", "", e.threshold, e.trackOrigin, e.log.getAbsolutePath(), MetaConfig.localIP, e.POI, e.highRP, e.midRP, e.lowRP, e.log.getName().split("\\.")[0], e.detectionSize, e.getInitial(), e.criticalEdges, mode, jsonLog, e.getEntries(), e);
                logStream.close();
                Timestamp currentTimestamp = ExperimentRunnerCMD_19.getTimeStamp();
                jsonLog.put("Timestamp", currentTimestamp.toString());
                File entryPointsJsonFile = new File(oneRes.getAbsolutePath() + "/" + e.configFile.getName().split("\\.")[0] + "_json_log.json");
                FileWriter jsonWriter = new FileWriter(entryPointsJsonFile);
                jsonWriter.write(jsonLog.toJSONString());
                jsonWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logging(Experiment e, File logFile) throws IOException {
        Logger logger = Logger.getLogger(e.configFile.getName());
        FileHandler fileHandler = new FileHandler(logFile.getAbsolutePath());
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(new LogFormatter());
        logger.addHandler(fileHandler);
        logger.info("#############Configurations#############");
        logger.info("Log File: " + e.log.getAbsolutePath());
        logger.info("Config File: " + e.configFile.getAbsolutePath());
        logger.info("POI: " + e.POI);
        logger.info("Local IP: " + this.formatArray(MetaConfig.localIP));
        logger.info("High RP: " + this.formatArray(e.highRP));
        logger.info("Low RP: " + this.formatArray(e.lowRP));
        logger.info("Threshold: " + String.valueOf(e.threshold));
        logger.info("Track Origin: " + String.valueOf(e.trackOrigin));
        logger.info("Detection size: " + String.valueOf(e.detectionSize));
        logger.info("Seeds: " + this.formatArray(e.getInitial().toArray(new String[e.getInitial().size()])));
        logger.info("##############Sysdig Parser#############");
        logger.info("P2P: " + this.formatArray(MetaConfig.ptopSystemCall));
        logger.info("P2F: " + this.formatArray(MetaConfig.ptofSystemCall));
        logger.info("F2P: " + this.formatArray(MetaConfig.ftopSystemCall));
        logger.info("P2N: " + this.formatArray(MetaConfig.ptonSystemCall));
        logger.info("N2P: " + this.formatArray(MetaConfig.ntopSystemCall));
        logger.info("########################################");
        logger.info("Mid RP: " + this.formatArray(e.midRP));
        logger.info("########################################");
    }

    private void logging_without_parsing(Experiment e, File logFile) throws IOException {
        Logger logger = Logger.getLogger(e.configFile.getName());
        FileHandler fileHandler = new FileHandler(logFile.getAbsolutePath());
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(new LogFormatter());
        logger.addHandler(fileHandler);
        logger.info("#############Configurations#############");
        logger.info("Config File: " + e.configFile.getAbsolutePath());
        logger.info("POI: " + e.POI);
        logger.info("Local IP: " + this.formatArray(MetaConfig.localIP));
        logger.info("High RP: " + this.formatArray(e.highRP));
        logger.info("Low RP: " + this.formatArray(e.lowRP));
        logger.info("Threshold: " + String.valueOf(e.threshold));
        logger.info("Track Origin: " + String.valueOf(e.trackOrigin));
        logger.info("Detection size: " + String.valueOf(e.detectionSize));
        logger.info("Seeds: " + this.formatArray(e.getInitial().toArray(new String[e.getInitial().size()])));
        logger.info("##############Sysdig Parser#############");
        logger.info("P2P: " + this.formatArray(MetaConfig.ptopSystemCall));
        logger.info("P2F: " + this.formatArray(MetaConfig.ptofSystemCall));
        logger.info("F2P: " + this.formatArray(MetaConfig.ftopSystemCall));
        logger.info("P2N: " + this.formatArray(MetaConfig.ptonSystemCall));
        logger.info("N2P: " + this.formatArray(MetaConfig.ntopSystemCall));
        logger.info("########################################");
        logger.info("Mid RP: " + this.formatArray(e.midRP));
        logger.info("########################################");
    }

    private String formatArray(String[] array) {
        return String.join((CharSequence)",", array);
    }

    private File makeResDir(String pathToRes) {
        File resDir = new File(this.PathToRes);
        if (!resDir.exists() || !resDir.isDirectory()) {
            resDir.mkdir();
        } else {
            System.out.println("Result Directory " + pathToRes + " already exists, overwriting!");
        }
        return resDir;
    }

    private File[] getLogs(String pathToLogs) throws FileNotFoundException {
        File logDir = new File(pathToLogs);
        if (!logDir.exists() || !logDir.isDirectory()) {
            throw new FileNotFoundException("Invalid directory: " + logDir);
        }
        return logDir.listFiles(this.logNameFilter);
    }

    public static Timestamp getTimeStamp() {
        Calendar calendar = Calendar.getInstance();
        Timestamp currentTimestamp = new Timestamp(calendar.getTime().getTime());
        return currentTimestamp;
    }

    public List<File> getPropertyFiles(File logFile) {
        System.out.println(logFile.getName());
        File[] files = logFile.getParentFile().listFiles();
        ArrayList<File> propertyFiles = new ArrayList<File>();
        String caseName = logFile.getName().split("\\.")[0];
        for (File f : files) {
            String name = f.getName();
            if (name.indexOf(caseName) == -1 || name.indexOf("property") == -1) continue;
            propertyFiles.add(f);
        }
        return propertyFiles;
    }

    public List<File> getPropertyFilesWithoutLog(String pathToLogs) {
        ArrayList<File> propertyFiles = new ArrayList<File>();
        try {
            File[] files;
            File logFolder = new File(pathToLogs);
            for (File f : files = logFolder.listFiles()) {
                String name = f.getName();
                if (name.indexOf("property") == -1) continue;
                propertyFiles.add(f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return propertyFiles;
    }

    public static String[] build_res_path(String[] log_paths, String mode) {
        String[] res = new String[log_paths.length];
        int idx = 0;
        for (String l : log_paths) {
            String resPath;
            File log_folder = new File(l);
            String name = log_folder.getName();
            String resName = name + "-" + mode + "-res";
            String pareName = log_folder.getParent();
            res[idx] = resPath = pareName + "/" + resName;
            File resFolder = new File(pareName + "/" + resName);
            if (!resFolder.exists()) {
                resFolder.mkdir();
            }
            ++idx;
        }
        return res;
    }

    public static String[] getDotPath(String[] log_paths) {
        String[] res = new String[log_paths.length];
        for (int i = 0; i < log_paths.length; ++i) {
            String logFolderPath = log_paths[i];
            File logFolder = new File(logFolderPath);
            for (File f : logFolder.listFiles()) {
                if (f.getName().indexOf("dot") == -1) continue;
                res[i] = f.getAbsolutePath();
            }
        }
        return res;
    }

    public static String[] findLog(String folder, int size2) {
        String[] res = new String[size2];
        int idx = 0;
        File folderPath = new File(folder);
        for (File f : folderPath.listFiles()) {
            if (!f.getName().endsWith("log")) continue;
            res[idx++] = f.getName();
        }
        return res;
    }

    class LogStream
    extends PrintStream {
        PrintStream out;

        public LogStream(PrintStream out1, PrintStream out2) {
            super(out1);
            this.out = out2;
        }

        @Override
        public void write(byte[] buf, int off, int len) {
            try {
                super.write(buf, off, len);
                this.out.write(buf, off, len);
            } catch (Exception exception) {
                // empty catch block
            }
        }

        @Override
        public void flush() {
            super.flush();
            this.out.flush();
        }
    }

    class LogFormatter
    extends Formatter {
        private DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

        LogFormatter() {
        }

        @Override
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder();
            builder.append(this.df.format(new Date(record.getMillis()))).append(" - ");
            builder.append(this.formatMessage(record));
            builder.append("\n");
            return builder.toString();
        }
    }
}

