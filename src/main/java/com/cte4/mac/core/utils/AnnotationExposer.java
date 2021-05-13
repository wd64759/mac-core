package com.cte4.mac.core.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.e4.mac.apt.processor.RuleCfgGenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnnotationExposer {

    static Logger log = LogManager.getLogger(AnnotationExposer.class);
    static AtomicBoolean init = new AtomicBoolean(false);
    static final String MAC_CFG_DIR = System.getProperty("mac.files.dir",
            System.getProperty("java. io. tmpdir", "/tmp"));
    static final String MAC_AGENT_CMD = System.getProperty("mac.agent","empty");

    /**
     * Load annotation configuration file
     * 
     * @param cfgStream
     */
    public static void init(InputStream cfgStream) {
        if (init.compareAndSet(false, true)) {
            Thread t = new Thread(() -> {
                try {
                    processCfg(cfgStream);
                    startAgent();
                } catch (MacInitiatialException e) {
                    e.printStackTrace();
                }
            });
            t.start();
        }
    }

    /**
     * Build new accessable configuration file with content from the input stream
     * 
     * @param cfgStream
     * @throws MacInitiatialException
     */
    protected static void processCfg(InputStream cfgStream) throws MacInitiatialException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(cfgStream));
        StringBuffer sbuf = new StringBuffer();
        String line;
        try {
            line = reader.readLine();
            while (line != null) {
                sbuf.append(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            log.error("fail to load MAC configuration file");
        }
        buildCfg(sbuf.toString());
    }

    /**
     * To load mac agent with current PID
     */
    protected static void startAgent() throws MacInitiatialException {
        String pid = getPID();
        if (pid != null) {
            try {
                String cmd = String.format("%s %s", MAC_AGENT_CMD, pid);
                System.out.println("exec - " + cmd);
                Process agentProcess = Runtime.getRuntime().exec(cmd);
                System.out.println("agent ID: " + agentProcess.pid());
                System.out.println("exec return code - " + agentProcess.exitValue());
            } catch (IOException e) {
                throw new MacInitiatialException("fail to run mac agent", e);
            }
        }
    }

    private static String getPID() {
        String processName = ManagementFactory.getRuntimeMXBean().getName();
        System.out.println(processName);
        Pattern p = Pattern.compile("(\\d+)@\\w+");
        Matcher m = p.matcher(processName);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(getPID());
    }

    /**
     * Move configuration file to accessable dir
     * 
     * @param cfgCnt
     */
    protected static void buildCfg(String cfgCnt) throws MacInitiatialException {
        String cfgFile = RuleCfgGenerator.CFG_FILE;
        try {
            File cfg = new File(new File(MAC_CFG_DIR), cfgFile);
            File loc = new File(cfg.getParentFile().getAbsolutePath());
            if (!loc.exists()) {
                log.info("target folder for mac file is missing.. try to re-build it");
                if (!loc.mkdirs()) {
                    String errMsg = "fail to create folder of - " + loc;
                    throw new MacInitiatialException(errMsg);
                }
            }
            try (FileOutputStream fo = new FileOutputStream(cfg)) {
                System.out.println("output mac configuration file to: " + cfg.getCanonicalPath());
                OutputStreamWriter out = new OutputStreamWriter(fo);
                out.write(cfgCnt);
            }
        } catch (Exception e) {
            throw new MacInitiatialException(e);
        }
    }

    public static boolean isInit() {
        return init.get();
    }
}
