/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import pagerank.Event;
import pagerank.FileEntity;
import pagerank.Process;

public class PtoFEvent
extends Event {
    public static final String TYPE = "PtoF";
    private Process source;
    private FileEntity sink;
    private String event;
    private long size;

    public PtoFEvent() {
    }

    public PtoFEvent(String startS, String startMs, Process source, FileEntity sink, String event, long amount, long id) {
        super(TYPE, startS, startMs, id);
        this.source = source;
        this.sink = sink;
        this.event = event;
        this.size = amount;
    }

    public PtoFEvent(String type, String startS, String startMs, Process source, FileEntity sink, String event, long amount, long id) {
        super(type, startS, startMs, id);
        this.source = source;
        this.sink = sink;
        this.event = event;
        this.size = amount;
    }

    public void updateAmount(int i) {
        this.size += (long)i;
    }

    public String getEvent() {
        return this.event;
    }

    public Process getSource() {
        return this.source;
    }

    public FileEntity getSink() {
        return this.sink;
    }

    public long getSize() {
        return this.size;
    }
}

