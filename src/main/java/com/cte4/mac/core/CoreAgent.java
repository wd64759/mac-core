package com.cte4.mac.core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cte4.mac.core.utils.AnnotationExposer;
import com.e4.mac.apt.processor.RuleCfgGenerator;

/**
 * - 1. load annotation configuration file from classpath mac/mac-cfg.json - 2.
 * generate exposer scripts with context info - 3. attach bundles - 4. valid
 * weaving - 5. apply scripts
 */
public class CoreAgent {

    static Logger log = LogManager.getLogger(CoreAgent.class);

    /**
     * The entry mac-core through java-agent facade with following process steps
     * 
     * @param args
     * @param inst
     * @throws Exception
     */
    public static void premain(String args, Instrumentation inst) throws Exception {
        log.info("mac-core agent is loaded");
        inst.addTransformer(new ClassFileTransformer() {
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                if (!AnnotationExposer.isInit()) {
                    InputStream cfgFile = loader.getResourceAsStream(RuleCfgGenerator.CFG_FILE);
                    if (cfgFile != null) {
                        try {
                            AnnotationExposer.init(cfgFile);
                        } catch (Exception e) {
                            log.error("fail to init mac framework", e);
                        }
                    }
                }
                return null;
            }
        });

    }

    public static void agentmain(String args, Instrumentation inst) throws Exception {
        premain(args, inst);
    }
}
