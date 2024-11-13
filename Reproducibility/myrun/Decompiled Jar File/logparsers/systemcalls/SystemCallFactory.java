/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package logparsers.systemcalls;

import java.util.ArrayList;
import java.util.List;
import logparsers.systemcalls.Action;
import logparsers.systemcalls.Fingerprint;
import logparsers.systemcalls.SystemCall;

public class SystemCallFactory {
    private static long id = 0L;
    private String type;
    private List<Action> onMatch;
    private Class cStart;
    private Class cEnd;

    public SystemCallFactory(String type, Class c1, Class c2) {
        this.type = type;
        this.onMatch = new ArrayList<Action>();
        this.cStart = c1;
        this.cEnd = c2;
    }

    public SystemCallFactory addAction(Action action) {
        this.onMatch.add(action);
        return this;
    }

    public SystemCall getSystemCall(String event) {
        SystemCall instance = new SystemCall("" + id++, this.type, new Fingerprint(event, this.cStart, this.cEnd));
        for (Action action : this.onMatch) {
            instance = instance.addAction(action);
        }
        return instance;
    }
}

