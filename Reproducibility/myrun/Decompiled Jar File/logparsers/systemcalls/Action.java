/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package logparsers.systemcalls;

import java.util.Map;
import pagerank.Entity;

public interface Action {
    public void apply(Map<String, String> var1, Map<String, String> var2, Entity[] var3, Entity[] var4);
}

