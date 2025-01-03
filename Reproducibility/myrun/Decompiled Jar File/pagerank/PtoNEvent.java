/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import pagerank.Event;
import pagerank.NetworkEntity;
import pagerank.NtoPEvent;
import pagerank.Process;

public class PtoNEvent
extends Event {
    public static final String TYPE = "PtoN";
    private Process source;
    private NetworkEntity sink;
    private long size;
    private String event;

    public Process getSource() {
        return this.source;
    }

    public NetworkEntity getSink() {
        return this.sink;
    }

    public long getSize() {
        return this.size;
    }

    public String getEvent() {
        return this.event;
    }

    public PtoNEvent() {
    }

    public PtoNEvent(String startS, String startMs, Process source, NetworkEntity sink, String event, long size2, long id) {
        super(TYPE, startS, startMs, id);
        this.source = source;
        this.sink = sink;
        this.size = size2;
        this.event = event;
    }

    public PtoNEvent(String type, String startS, String startMs, Process source, NetworkEntity sink, String event, long id) {
        super(type, startS, startMs, id);
        this.source = source;
        this.sink = sink;
        this.size = 0L;
        this.event = event;
    }

    public PtoNEvent(NtoPEvent a) {
        super(TYPE, a.getStart().split("\\.")[0], a.getStart().split("\\.")[1], a.getUniqID());
        this.source = a.getSink();
        this.sink = a.getSource();
        this.size = a.getSize();
        this.event = a.getEvent();
    }

    public void updateSize(long i) {
        this.size += i;
    }
}

