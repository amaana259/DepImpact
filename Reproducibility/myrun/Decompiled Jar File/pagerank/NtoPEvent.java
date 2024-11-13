/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import pagerank.Event;
import pagerank.NetworkEntity;
import pagerank.Process;
import pagerank.PtoNEvent;

public class NtoPEvent
extends Event {
    public static final String TYPE = "NtoP";
    private NetworkEntity source;
    private Process sink;
    private long size;
    private String event;

    public NtoPEvent() {
    }

    public NtoPEvent(String startS, String startMs, NetworkEntity source, Process sink, String event, long size2, long id) {
        super(TYPE, startS, startMs, id);
        this.source = source;
        this.sink = sink;
        this.size = size2;
        this.event = event;
    }

    public NtoPEvent(String type, String startS, String startMs, NetworkEntity source, Process sink, String event, long id) {
        super(type, startS, startMs, id);
        this.source = source;
        this.sink = sink;
        this.size = 0L;
        this.event = event;
    }

    public NtoPEvent(PtoNEvent a) {
        super(TYPE, a.getStart().split("\\.")[0], a.getStart().split("\\.")[1], a.getUniqID());
        this.source = a.getSink();
        this.sink = a.getSource();
        this.size = a.getSize();
        this.event = a.getEvent();
    }

    public NetworkEntity getSource() {
        return this.source;
    }

    public Process getSink() {
        return this.sink;
    }

    public long getSize() {
        return this.size;
    }

    public String getEvent() {
        return this.event;
    }

    public void updateSize(long i) {
        this.size += i;
    }

    public void setSize(long size2) {
        this.size = size2;
    }

    @Override
    public String toString() {
        return "NtoPEvent{source=" + this.source + ", sink=" + this.sink + ", size=" + this.size + ", event='" + this.event + '\'' + '}';
    }
}

