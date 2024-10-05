/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgParser {
    String argString;
    Map<String, String> arguments;

    public ArgParser(String[] args) {
        this.argString = String.join((CharSequence)" ", args);
    }

    public Map<String, String> parseArgs() {
        this.arguments = new HashMap<String, String>();
        Pattern pPath = Pattern.compile("-p ([^ ]+?)(?: |$)");
        Pattern pDetection = Pattern.compile("-d ([^ ]+?)(?: |$)");
        Pattern pHigh = Pattern.compile("-h (.+?)(?:(?: -)|$)");
        Pattern pNeutral = Pattern.compile("-n (.+?)(?:(?: -)|$)");
        Pattern pLow = Pattern.compile("-l (.+?)(?:(?: -)|$)");
        Pattern pRes = Pattern.compile("-r ([^ ]+?)(?: |$)");
        Pattern pSuffix = Pattern.compile("-s ([^ ]+?)(?: |$)");
        Pattern pThresh = Pattern.compile("-t ([^ ]+?)(?: |$)");
        Pattern pOrigin = Pattern.compile("-o");
        Matcher mPath = pPath.matcher(this.argString);
        Matcher mDetection = pDetection.matcher(this.argString);
        Matcher mHigh = pHigh.matcher(this.argString);
        Matcher mNeutral = pNeutral.matcher(this.argString);
        Matcher mLow = pLow.matcher(this.argString);
        Matcher mRes = pRes.matcher(this.argString);
        Matcher mSuffix = pSuffix.matcher(this.argString);
        Matcher mThresh = pThresh.matcher(this.argString);
        Matcher mOrigin = pOrigin.matcher(this.argString);
        if (!(mPath.find() && mDetection.find() && mThresh.find())) {
            this.prompt();
            System.exit(1);
        }
        String path = mPath.group(1);
        String detection = mDetection.group(1);
        String high = mHigh.find() ? mHigh.group(1) : null;
        String neutral = mNeutral.find() ? mNeutral.group(1) : null;
        String low = mLow.find() ? mLow.group(1) : null;
        String res = mRes.find() && !mRes.group(1).equals(".") ? mRes.group(1) : System.getProperty("user.dir");
        String suffix = mSuffix.find() ? mSuffix.group(1) : "";
        String thresh = mThresh.group(1);
        if (!(new File(path).exists() && new File(res).exists() && new File(res).isDirectory())) {
            System.out.println("No such file or directory!");
            System.exit(2);
        }
        this.arguments.put("path", path);
        this.arguments.put("detection", detection);
        this.arguments.put("high", high);
        this.arguments.put("neutral", neutral);
        this.arguments.put("low", low);
        this.arguments.put("res", res);
        this.arguments.put("suffix", suffix);
        this.arguments.put("thresh", thresh);
        if (mOrigin.find()) {
            this.arguments.put("origin", "");
        }
        return this.arguments;
    }

    private void test() {
        System.out.println(this.argString);
        for (String s2 : this.arguments.keySet()) {
            System.out.println(s2 + ": " + this.arguments.get(s2));
        }
    }

    private void prompt() {
        System.out.println("Usage:\nProcessOneLog\n-p <path to log file>\n-d <POI>\n-h [highRP,...]\n-n [neutralRP,...]\n-l [lowRP,...]\n-r [output dir]\n-s [suffix]\n-t <threshold>\n-o [track origin only?]");
    }
}

