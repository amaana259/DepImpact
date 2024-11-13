/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package logparsers.systemcalls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import logparsers.systemcalls.Action;
import logparsers.systemcalls.Fingerprint;
import pagerank.Entity;

public class SystemCall {
    public String name;
    public String type;
    public Fingerprint fingerPrint;
    public List<Action> onMatch;

    public SystemCall(String name, String type, Fingerprint fingerPrint) {
        this.name = name;
        this.type = type;
        this.fingerPrint = fingerPrint;
        this.onMatch = new ArrayList<Action>();
    }

    public SystemCall addAction(Action action) {
        this.onMatch.add(action);
        return this;
    }

    public void react(Map<String, String> mStart, Map<String, String> mEnd, Entity[] eStart, Entity[] eEnd) {
        for (Action action : this.onMatch) {
            action.apply(mStart, mEnd, eStart, eEnd);
        }
    }
}

