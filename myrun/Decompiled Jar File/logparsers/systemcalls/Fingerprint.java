/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package logparsers.systemcalls;

import java.util.Map;
import pagerank.Entity;

public class Fingerprint {
    private String fingerprintString;

    public Fingerprint(String s2, Class c1, Class c2) {
        String s1 = c1 == null ? "null" : "" + c1.toString();
        String s22 = c2 == null ? "null" : "" + c2.toString();
        this.fingerprintString = s2 + s1 + s22;
    }

    public static Fingerprint toFingerPrint(Map<String, String> begin, Map<String, String> end, Entity[] beginEntity, Entity[] endEntity) {
        Class<?> c1 = beginEntity[1] == null ? null : beginEntity[1].getClass();
        Class<?> c2 = endEntity[1] == null ? null : endEntity[1].getClass();
        return new Fingerprint(begin.get("event"), c1, c2);
    }

    public String getFingerprintString() {
        return this.fingerprintString;
    }

    public int hashCode() {
        return this.fingerprintString.hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof Fingerprint && ((Fingerprint)o).getFingerprintString().equals(this.fingerprintString);
    }

    public String toString() {
        return this.fingerprintString;
    }
}

