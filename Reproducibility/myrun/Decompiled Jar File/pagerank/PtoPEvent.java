/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import pagerank.Event;
import pagerank.Process;

public class PtoPEvent
extends Event {
    public static final String TYPE = "PtoP";
    private Process source;
    private Process sink;
    private String event;

    public PtoPEvent() {
    }

    public PtoPEvent(String startS, String startMs, Process source, Process sink, String event, long id) {
        super(TYPE, startS, startMs, id);
        this.source = source;
        this.sink = sink;
        this.event = event;
    }

    public PtoPEvent(String type, String startS, String startMs, Process source, Process sink, String event, long id) {
        super(type, startS, startMs, id);
        this.source = source;
        this.sink = sink;
        this.event = event;
    }

    public Process getSource() {
        return this.source;
    }

    public Process getSink() {
        return this.sink;
    }

    public String getEvent() {
        return this.event;
    }

    public void setSink(Process sink) {
        this.sink = sink;
    }

    public void setSource(Process source) {
        this.source = source;
    }
}

