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
import java.util.Date;
import java.util.HashSet;
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
import pagerank.ExperimentRunnerCMD_19;
import pagerank.GetGraph;
import pagerank.MetaConfig;
import pagerank.ProcessOneLogCMD_19;
import pagerank.ProcessOneLogCmd;

public class ExperimentRunnerCmd {
    public static String mode;
    public static String do_split;
    DirectedPseudograph<EntityNode, EventEdge> graphFromLog;
    String PathToLogs;
    String PathToRes;
    FilenameFilter logNameFilter;

    public static void main(String[] args) {
        for (String arg : args) {
            System.out.println(arg);
        }
        if (args.length != 4) {
            System.err.println("Usage: ExperimentRunnerCmd <log path> <result path> <log names,...> <mode>");
            System.exit(-1);
        }
        try {
            String logPath = args[0];
            String resPath = args[1];
            String[] logs = args[2].split(";");
            mode = args[3];
            ExperimentRunnerCmd er = new ExperimentRunnerCmd(logPath, resPath, logs);
            er.run2();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ExperimentRunnerCmd(String pathToLogs, String pathToRes, FilenameFilter logNameFilter) {
        this.PathToLogs = pathToLogs;
        this.PathToRes = pathToRes;
        this.logNameFilter = logNameFilter;
    }

    public ExperimentRunnerCmd(String pathTologs, String pathToRes, String[] lognames) {
        this.PathToLogs = pathTologs;
        this.PathToRes = pathToRes;
        HashSet<String> logset = new HashSet<String>();
        for (String s2 : lognames) {
            logset.add(s2);
        }
        this.logNameFilter = (dir, name) -> logset.contains(name);
    }

    public ExperimentRunnerCmd(String pathToLogs, String pathToRes, FilenameFilter logNameFilter, String[] exclusion) {
        this.PathToLogs = pathToLogs;
        this.PathToRes = pathToRes;
        HashSet<String> exclusionSet = new HashSet<String>();
        for (String s2 : exclusion) {
            exclusionSet.add(s2);
        }
        this.logNameFilter = (dir, name) -> logNameFilter.accept(dir, name) && !exclusionSet.contains(name);
    }

    public void run() throws FileNotFoundException {
        File resDir = this.makeResDir(this.PathToRes);
        File[] logs = this.getLogs(this.PathToLogs);
        ArrayList<Experiment> experiments = new ArrayList<Experiment>();
        try {
            for (File log : logs) {
                File[] propertyFiles;
                for (File propertyFile : propertyFiles = log.getParentFile().listFiles((dir, name) -> (name.split("\\.")[0].equals(log.getName().split("\\.")[0]) || name.startsWith(log.getName().split("\\.")[0] + "-")) && name.endsWith(".property"))) {
                    experiments.add(new Experiment(log, propertyFile));
                }
            }
            for (Experiment e : experiments) {
                File oneRes = new File(resDir + "/" + e.configFile.getParentFile().getName() + "-" + e.configFile.getName().split("\\.")[0]);
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
                ProcessOneLogCmd.process(oneRes.getAbsolutePath() + "/", "", e.threshold, e.trackOrigin, e.log.getAbsolutePath(), MetaConfig.localIP, e.POI, e.highRP, e.midRP, e.lowRP, e.log.getName().split("\\.")[0], e.detectionSize, e.getInitial(), e.criticalEdges, mode, do_split);
                logStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run2() throws FileNotFoundException {
        File resDir = this.makeResDir(this.PathToRes);
        File[] logs = this.getLogs(this.PathToLogs);
        File log = logs[0];
        ArrayList experiments = new ArrayList();
        ArrayList<Experiment> experiments_backward = new ArrayList<Experiment>();
        try (FileOutputStream fo2 = new FileOutputStream(this.PathToRes + "/build_time.log");){
            long start = System.currentTimeMillis();
            GetGraph generator = new GetGraph(logs[0].getPath(), MetaConfig.localIP);
            long end = System.currentTimeMillis();
            fo2.write(("parsing time:" + (double)(end - start) / 1000.0).getBytes());
            start = System.currentTimeMillis();
            generator.GenerateGraph();
            end = System.currentTimeMillis();
            fo2.write(("building time:" + (double)(end - start) / 1000.0).getBytes());
            this.graphFromLog = generator.getJg();
        } catch (IOException fo2) {
            // empty catch block
        }
        try {
            File[] propertyFiles = log.getParentFile().listFiles((dir, name) -> (name.split("\\.")[0].equals(log.getName().split("\\.")[0]) || name.startsWith(log.getName().split("\\.")[0] + ":")) && name.endsWith(".property"));
            for (File propertyFile : propertyFiles) {
                String propertyName = propertyFile.getName();
                if (propertyName.indexOf("backward") == -1) continue;
                experiments_backward.add(new Experiment(log, propertyFile));
            }
            for (Experiment e : experiments_backward) {
                File oneRes = new File(resDir + "/" + e.configFile.getParentFile().getName() + "-" + e.configFile.getName().split("\\.")[0]);
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

