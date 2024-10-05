/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import pagerank.Event;
import pagerank.FileEntity;
import pagerank.Process;

public class FtoPEvent
extends Event {
    public static final String TYPE = "FtoP";
    private FileEntity source;
    private Process sink;
    private long size;
    private String event;

    public FtoPEvent() {
    }

    public FtoPEvent(String startS, String startMs, FileEntity source, Process sink, String event, long size2, long id) {
        super(TYPE, startS, startMs, id);
        this.source = source;
        this.sink = sink;
        this.size = size2;
        this.event = event;
    }

    public FtoPEvent(String type, String startS, String startMs, FileEntity source, Process sink, String event, long id) {
        super(type, startS, startMs, id);
        this.source = source;
        this.sink = sink;
        this.size = 0L;
        this.event = event;
    }

    public FileEntity getSource() {
        return this.source;
    }

    public Process getSink() {
        return this.sink;
    }

    public String getEvent() {
        return this.event;
    }

    public void updateSize(long i) {
        this.size += i;
    }

    public long getSize() {
        return this.size;
    }
}

