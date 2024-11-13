/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import pagerank.ProcessOneLogDemo;

public class Artifact {
    DirectedPseudograph<EntityNode, EventEdge> graphFromLog;

    public static void main(String[] args) {
        for (String arg : args) {
            System.out.println(arg);
        }
        try {
            String pathToLog = args[0];
            String resPath = args[1];
            String inputParameter = args[2];
            Artifact runner = new Artifact();
            runner.run2(pathToLog, resPath, inputParameter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run2(String logPath, String resPath, String parameterFile) throws FileNotFoundException {
        File resDir = this.makeResDir(resPath);
        File log = new File(logPath);
        GetGraph generator = new GetGraph(logPath, new String[0], true);
        generator.GenerateGraph();
        DirectedPseudograph<EntityNode, EventEdge> graphFromLog = generator.getJg();
        try {
            File propertyFile = new File(parameterFile);
            Experiment exp = new Experiment(log, propertyFile);
            JSONObject jsonLog = new JSONObject();
            jsonLog.put("Case", exp.log.getName());
            ProcessOneLogDemo.run_exp_backward(graphFromLog, resPath + "/", "", exp.threshold, exp.trackOrigin, exp.log.getAbsolutePath(), MetaConfig.localIP, exp.POI, exp.highRP, exp.midRP, exp.lowRP, exp.log.getName().split("\\.")[0], exp.detectionSize, exp.getInitial(), exp.criticalEdges, "clusterall", jsonLog, exp.getEntries(), exp);
        } catch (Exception e) {
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
        logger.info("Detection size: " + String.valueOf(e.detectionSize));
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
        logger.info("Detection size: " + String.valueOf(e.detectionSize));
        logger.info("########################################");
    }

    private String formatArray(String[] array) {
        return String.join((CharSequence)",", array);
    }

    private File makeResDir(String pathToRes) {
        File resDir = new File(pathToRes);
        if (!resDir.exists() || !resDir.isDirectory()) {
            resDir.mkdir();
        } else {
            System.out.println("Result Directory " + pathToRes + " already exists, overwriting!");
        }
        return resDir;
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

