package com.cte4.mac.core;

import java.lang.instrument.Instrumentation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.java.Log;

@SpringBootApplication
@Log
public class CoreAgent {
	public static boolean firstTime = true;

	public static void main(String[] args) {
		SpringApplication.run(CoreAgent.class, args);
	}


    /**
     * - 1. load annotation configuration file from classpath mac/mac-cfg.json
     * - 2. generate exposer scripts with context info
     * - 3. attach bundles
     * - 4. valid weaving
     * - 5. apply scripts
     */
    protected static void init() {
        InstallAgent.attach();
        log.info("mac-core agent is up");
    }

    /**
     * The entry mac-core through java-agent facade with following process steps
     * @param args
     * @param inst
     * @throws Exception
     */
	public static void premain(String args, Instrumentation inst) throws Exception {
        synchronized (CoreAgent.class) {
            if (firstTime) {
                firstTime = false;
            } else {
                return;
            }
        }
        CoreAgent.init();
    }

    public static void agentmain(String args, Instrumentation inst) throws Exception {
        premain(args, inst);
    }

}
