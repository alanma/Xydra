package org.xydra.log.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.xydra.annotations.RunsInGWT;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


/**
 * Can render exceptions as a nicely readable HTML.
 * 
 * @author xamde
 */
@RunsInGWT(true)
public class SharedExceptionUtils_GwtEmul {
    
    private static final Logger log = LoggerFactory.getLogger(SharedExceptionUtils_GwtEmul.class);
    
    /**
     * Assume a and b come from the same program and have a common caller
     * hierarchy. Display the stack traces in three parts: Just A, just B, and
     * the common ancestor of both A and B.
     * 
     * @param aname headline for a
     * @param a a stack trace with newlines in it
     * @param bname headline for b
     * @param b another stack trace with newlines in it
     * @param linesToSkip how many lines in the beginning to skip
     */
    public static void dumpWhereStacktracesAreDifferent(String aname, String a, String bname,
            String b, int linesToSkip) {
        List<String> alist = linesAsList(a);
        alist = alist.subList(linesToSkip, alist.size());
        List<String> blist = linesAsList(b);
        blist = blist.subList(linesToSkip, blist.size());
        List<String> common = new ArrayList<String>();
        /* list have not always the same length */
        String commonLine = "none";
        while(alist.size() > 0 && blist.size() > 0
                && alist.get(alist.size() - 1).equals(blist.get(blist.size() - 1))) {
            commonLine = alist.get(alist.size() - 1);
            common.add(0, commonLine);
            alist = alist.subList(0, alist.size() - 1);
            blist = blist.subList(0, blist.size() - 1);
        }
        log.warn(" " + aname + " ==== ");
        for(String s : alist) {
            log.warn(s);
        }
        log.warn(" " + bname + " ==== ");
        for(String s : blist) {
            log.warn(s);
        }
        log.warn(" Common start of both stack traces ==== ");
        for(String s : common) {
            log.warn(s);
        }
    }
    
    /**
     * See also DebugUtils#dumpStacktrace() in Xydra.Core
     * 
     * @return the stack trace as a multi-line StringBuffer
     */
    public static StringBuffer getStacktraceAsString() {
        try {
            throw new RuntimeException("CALLER");
        } catch(Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            pw.close();
            sw.flush();
            return sw.getBuffer();
        }
    }
    
    private static List<String> linesAsList(String s) {
        StringReader sr = new StringReader(s);
        BufferedReader br = new BufferedReader(sr);
        List<String> list = new ArrayList<String>();
        try {
            for(String line = br.readLine(); line != null; line = br.readLine()) {
                list.add(line);
            }
        } catch(IOException e) {
            assert false;
        }
        return list;
    }
}
