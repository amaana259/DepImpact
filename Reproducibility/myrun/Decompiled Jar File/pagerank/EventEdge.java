/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.math.BigDecimal;
import pagerank.EntityNode;
import pagerank.FtoPEvent;
import pagerank.NtoPEvent;
import pagerank.PtoFEvent;
import pagerank.PtoNEvent;
import pagerank.PtoPEvent;

public class EventEdge {
    private EntityNode source;
    private EntityNode sink;
    public long id;
    public BigDecimal startTime;
    public BigDecimal endTime;
    private String type;
    private String event;
    private long size;
    public double weight;
    public double timeWeight;
    public double amountWeight;
    public double structureWeight;

    EventEdge(PtoFEvent pf) {
        this.source = new EntityNode(pf.getSource());
        this.sink = new EntityNode(pf.getSink());
        this.id = pf.getUniqID();
        this.startTime = new BigDecimal(pf.getStart());
        this.endTime = new BigDecimal(pf.getEnd());
        this.type = pf.getType();
        this.event = pf.getEvent();
        this.size = pf.getSize();
    }

    EventEdge(PtoPEvent pp) {
        this.source = new EntityNode(pp.getSource());
        this.sink = new EntityNode(pp.getSink());
        this.id = pp.getUniqID();
        this.startTime = new BigDecimal(pp.getStart());
        this.endTime = new BigDecimal(pp.getEnd());
        this.type = pp.getType();
        this.event = pp.getEvent();
        this.size = 0L;
    }

    EventEdge(FtoPEvent fp) {
        this.source = new EntityNode(fp.getSource());
        this.sink = new EntityNode(fp.getSink());
        this.id = fp.getUniqID();
        this.startTime = new BigDecimal(fp.getStart());
        try {
            this.endTime = new BigDecimal(fp.getEnd());
        } catch (Exception e) {
            System.out.println(fp.getUniqID());
            System.out.println(fp.getSource());
            System.out.println(fp.getSink());
        }
        this.type = fp.getType();
        this.event = fp.getEvent();
        this.size = fp.getSize();
    }

    EventEdge(NtoPEvent np) {
        this.source = new EntityNode(np.getSource());
        this.sink = new EntityNode(np.getSink());
        this.id = np.getUniqID();
        this.startTime = new BigDecimal(np.getStart());
        try {
            this.endTime = new BigDecimal(np.getEnd());
        } catch (Exception e) {
            System.out.println(np.getSource());
            System.out.println(np.getSink());
            System.out.println(np.getUniqID());
        }
        this.type = np.getType();
        this.event = np.getEvent();
        this.size = np.getSize();
    }

    EventEdge(PtoNEvent pn) {
        this.source = new EntityNode(pn.getSource());
        this.sink = new EntityNode(pn.getSink());
        this.id = pn.getUniqID();
        this.startTime = new BigDecimal(pn.getStart());
        try {
            this.endTime = new BigDecimal(pn.getEnd());
        } catch (Exception e) {
            System.out.println(pn.getUniqID());
            System.out.println(pn.getSource());
            System.out.println(pn.getEvent());
            System.out.println(pn.getSink());
        }
        this.type = pn.getType();
        this.event = pn.getEvent();
        this.size = pn.getSize();
    }

    public EventEdge(EventEdge edge) {
        this.source = edge.getSource();
        this.sink = edge.getSink();
        this.id = edge.getID();
        this.startTime = edge.getStartTime();
        this.endTime = edge.getEndTime();
        this.type = edge.getType();
        this.size = edge.getSize();
        this.weight = edge.weight;
    }

    public EventEdge(String type, BigDecimal starttime, BigDecimal endtime, long amount, EntityNode from, EntityNode to, long id) {
        this.source = from;
        this.sink = to;
        this.type = type;
        this.size = amount;
        this.startTime = starttime;
        this.endTime = endtime;
        this.id = id;
    }

    @Deprecated
    public EventEdge(EventEdge edge, long id) {
        this.source = edge.getSource();
        this.sink = edge.getSink();
        this.id = id;
        this.startTime = edge.getStartTime();
        this.endTime = edge.getEndTime();
        this.type = edge.getType();
        this.size = edge.getSize();
    }

    public EventEdge(EventEdge edge, EntityNode from, EntityNode to, long id) {
        this.source = from;
        this.sink = to;
        this.id = id;
        this.startTime = edge.getStartTime();
        this.endTime = edge.getEndTime();
        this.type = edge.getType();
        this.size = edge.getSize();
        this.event = edge.getEvent();
    }

    public EventEdge merge(EventEdge e2) {
        this.endTime = e2.endTime;
        this.size += e2.size;
        return this;
    }

    public void printInfo() {
        System.out.println("id: " + this.id + " Source:" + this.source.getSignature() + " Target:" + this.getSink().getSignature() + " End time:" + this.endTime.toString() + " Size:" + this.size);
    }

    public void setEdgeEvent(String event) {
        this.event = event;
    }

    long getID() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    EntityNode getSource() {
        return this.source;
    }

    EntityNode getSink() {
        return this.sink;
    }

    BigDecimal getStartTime() {
        return this.startTime;
    }

    BigDecimal getEndTime() {
        return this.endTime;
    }

    BigDecimal[] getInterval() {
        BigDecimal[] res = new BigDecimal[]{this.startTime, this.endTime};
        return res;
    }

    String getType() {
        return this.type;
    }

    String getEvent() {
        return this.event;
    }

    public boolean eventIsNull() {
        return this.event == null;
    }

    long getSize() {
        return this.size;
    }

    public BigDecimal getDuration() {
        return this.endTime.subtract(this.startTime);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventEdge)) {
            return false;
        }
        EventEdge eventEdge = (EventEdge)o;
        if (this.id != eventEdge.id) {
            return false;
        }
        if (!this.source.equals(eventEdge.source)) {
            return false;
        }
        if (!this.sink.equals(eventEdge.sink)) {
            return false;
        }
        if (!this.startTime.equals(eventEdge.startTime)) {
            return false;
        }
        return this.endTime.equals(eventEdge.endTime);
    }

    public int hashCode() {
        int result = this.source.hashCode();
        result = 31 * result + this.sink.hashCode();
        result = 31 * result + this.startTime.hashCode();
        return result;
    }

    public String toString() {
        return "EventEdge{source=" + this.source.getSignature() + ", sink=" + this.sink.getSignature() + ", id=" + this.id + ", startTime=" + this.startTime + ", endTime=" + this.endTime + ", type='" + this.type + '\'' + ", event='" + this.event + '\'' + ", size=" + this.size + ", weight=" + this.weight + ", timeWeight=" + this.timeWeight + ", amountWeight=" + this.amountWeight + ", structureWeight=" + this.structureWeight + '}';
    }
}

