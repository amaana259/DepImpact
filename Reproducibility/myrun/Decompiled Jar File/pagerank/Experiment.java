/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import pagerank.MetaConfig;

public class Experiment {
    Properties config;
    File configFile;
    File log;
    public String POI;
    public String[] highRP;
    public String[] lowRP;
    public String[] midRP;
    double threshold;
    double detectionSize;
    boolean trackOrigin;
    String[] criticalEdges;
    public String[] criticalNodes;
    public String[] importantProcessStarts;
    public String[] importantFileStarts;
    public String[] importantIPStarts;
    public String[] entries;
    public String pathToDot;
    public String idAndNamePath;
    public String frequencyPath;
    public String nodozeTime;
    public Set<String> skipEntry = new HashSet<String>();
    public Set<String> mustEntry = new HashSet<String>();
    File dotFile;

    public Experiment(File logFile, File configFile) throws IOException {
        FileInputStream fi = new FileInputStream(configFile);
        this.log = logFile;
        this.configFile = configFile;
        this.config = new Properties();
        this.config.load(fi);
        this.digestConfig();
    }

    public Experiment(File configFile) throws IOException {
        FileInputStream fi = new FileInputStream(configFile);
        this.log = null;
        this.configFile = configFile;
        this.config = new Properties();
        this.config.load(fi);
        this.digestConfig();
    }

    public void setPathToDot(String s2) {
        this.pathToDot = s2;
        this.dotFile = new File(this.pathToDot);
    }

    private void digestConfig() {
        this.POI = this.config.getProperty("POI");
        String highRPString = this.config.getProperty("highRP", "");
        this.highRP = highRPString.split(",");
        String lowRPString = this.config.getProperty("lowRP", "");
        this.lowRP = lowRPString.split(",");
        String[] defaultMidRP = MetaConfig.midRP;
        String midRPString = this.config.getProperty("midRP", "");
        String[] additionalMidRP = midRPString.split(",");
        ArrayList<String> _ = new ArrayList<String>();
        _.addAll(Arrays.asList(defaultMidRP));
        _.addAll(Arrays.asList(additionalMidRP));
        this.midRP = _.toArray(new String[_.size()]);
        this.threshold = Double.parseDouble(this.config.getProperty("threshold", "0"));
        this.trackOrigin = Boolean.parseBoolean(this.config.getProperty("trackOrigin", "false"));
        this.detectionSize = Double.parseDouble(this.config.getProperty("detectionSize", "0"));
        this.criticalEdges = this.config.getProperty("criticalEdge", "").split(";");
        this.criticalNodes = this.config.getProperty("criticalNodes", "").split(",");
        this.importantFileStarts = this.config.getProperty("importantFileStarts", "").split(",");
        this.importantProcessStarts = this.config.getProperty("importantProcessStarts", "").split(",");
        this.importantIPStarts = this.config.getProperty("importantIPStarts", "").split(",");
        this.entries = this.config.getProperty("entry", "").split(",");
        this.idAndNamePath = this.config.getProperty("id_and_name");
        this.frequencyPath = this.config.getProperty("event_count");
        this.nodozeTime = this.config.getProperty("nodoze_time");
        this.updateMustEntry();
        this.updateSkipEntry();
    }

    private void updateMustEntry() {
        String[] mustValues = this.config.getProperty("must", "").split(",");
        if (mustValues == null || mustValues.length == 0) {
            return;
        }
        for (String s2 : mustValues) {
            this.mustEntry.add(s2);
        }
    }

    private void updateSkipEntry() {
        String[] skipValues = this.config.getProperty("skip", "").split(",");
        if (skipValues == null || skipValues.length == 0) {
            return;
        }
        for (String s2 : skipValues) {
            this.skipEntry.add(s2);
        }
    }

    public Set<String> getInitial() {
        HashSet<String> res = new HashSet<String>();
        for (String s2 : this.highRP) {
            res.add(s2);
        }
        for (String s2 : this.lowRP) {
            res.add(s2);
        }
        return res;
    }

    public List<String> getHighRP() {
        List<String> res = Arrays.asList(this.highRP);
        return res;
    }

    public List<String> getLowRP() {
        List<String> res = Arrays.asList(this.lowRP);
        return res;
    }

    public String[] getCriticalEdges() {
        String[] copyOfCriticalEdges = Arrays.copyOf(this.criticalEdges, this.criticalEdges.length);
        return copyOfCriticalEdges;
    }

    public String[] getEntries() {
        String[] res = new String[this.entries.length];
        for (int i = 0; i < res.length; ++i) {
            res[i] = this.entries[i].trim();
        }
        return res;
    }
}

