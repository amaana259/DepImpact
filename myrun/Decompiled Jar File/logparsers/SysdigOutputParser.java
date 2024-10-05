/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package logparsers;

import java.io.IOException;
import java.util.Map;
import pagerank.FtoPEvent;
import pagerank.NtoPEvent;
import pagerank.PtoFEvent;
import pagerank.PtoNEvent;
import pagerank.PtoPEvent;

public interface SysdigOutputParser {
    public Map<String, PtoFEvent> getPfmap();

    public Map<String, PtoNEvent> getPnmap();

    public Map<String, PtoPEvent> getPpmap();

    public Map<String, NtoPEvent> getNpmap();

    public Map<String, FtoPEvent> getFpmap();

    public void getEntities() throws IOException;

    public void afterBuilding();

    public void setHostOrNot(boolean var1);
}

