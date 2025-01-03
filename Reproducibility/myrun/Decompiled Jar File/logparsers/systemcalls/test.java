/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package logparsers.systemcalls;

import java.util.HashMap;
import logparsers.systemcalls.Fingerprint;
import logparsers.systemcalls.SystemCall;
import pagerank.FileEntity;

public class test {
    public static void main(String[] args) {
        String e1_start = "12753 1537174223.380364423 2 InputThread (6171) > read cwd=/home/lcl/ fd=59(<f>/dev/input/event13) size=6144  latency=0";
        String e1_end = "12755 1537174223.380369051 2 InputThread (6171) < read cwd=/home/lcl/ res=48 data=.........I.......................I..............  latency=4628";
        String e2_start = "116064 1537174224.655175304 1 gnome-terminal- (24732) > write cwd=/home/lcl/ fd=14(<f>/dev/ptmx) size=3  latency=0";
        String e2_end = "116065 1537174224.655190866 1 gnome-terminal- (24732) < write cwd=/home/lcl/ res=3 data=.[A  latency=15562";
        HashMap<Fingerprint, SystemCall> table = new HashMap<Fingerprint, SystemCall>();
        SystemCall read = new SystemCall("read", "ftop", new Fingerprint("read", FileEntity.class, null)).addAction((x, y, z, w) -> System.out.println("read!"));
        SystemCall write = new SystemCall("write", "ptof", new Fingerprint("write", FileEntity.class, null)).addAction((x, y, z, w) -> System.out.println("read!"));
        table.put(read.fingerPrint, read);
        table.put(write.fingerPrint, write);
        System.out.println(FileEntity.class.hashCode());
        FileEntity e = new FileEntity(1.0, 1L, 1, "1", "1", null, null, null, 1L);
        System.out.println(e.getClass().toString());
    }
}

