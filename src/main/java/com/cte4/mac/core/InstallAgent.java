package com.cte4.mac.core;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ServerSocketFactory;

import org.jboss.byteman.agent.install.Install;
import org.jboss.byteman.agent.submit.Submit;

import lombok.extern.java.Log;

@Log
public class InstallAgent {

    private final Random random = new Random(System.nanoTime());
    private int port;
    private String processId;
    private String host;
    private boolean addtoBoot = false;
    private List<CheckPoint> checkpoints = new ArrayList<>();

    private InstallAgent() {
        String processName = ManagementFactory.getRuntimeMXBean().getName();
        Pattern pattern = Pattern.compile("(\\d+)@(.*)");
        Matcher matcher = pattern.matcher(processName);
        if (matcher.matches()) {
            this.processId = matcher.group(1);
            this.host = matcher.group(2);
        }
    }

    public static InstallAgent attach() {
        InstallAgent agent = new InstallAgent();
        try {
            if (!Install.isAgentAttached(agent.processId)) {
                agent.findAvaliablePort();
                System.setProperty("jdk.attach.allowAttachSelf", "true");
                Install.install(agent.processId, agent.addtoBoot, agent.host, agent.port, agent.props());
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "fail to attach agent", e);
        }
        return agent;
    }

    /**
     * to find avaiable port
     * 
     * @return
     */
    protected int findAvaliablePort() {
        this.port = Submit.DEFAULT_PORT;
        int retryTime = 10;
        while (retryTime-- > 0 && !isPortAvailable(this.port)) {
            this.port += random.nextInt(10);
        }
        return port;
    }

    /**
     * to check if TCP port is available
     * 
     * @param port
     * @return
     */
    protected boolean isPortAvailable(int port) {
        try {
            ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(port, 1,
                    InetAddress.getByName("localhost"));
            serverSocket.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private String[] props() {
        return new String[] { 
            "org.jboss.byteman.dump.generated.classes=true",
            "org.jboss.byteman.dump.generated.classes.directory=/tmp/dump",
            "org.jboss.byteman.mac.agentport=" + this.port, 
            "org.jboss.byteman.mac.pid=" + this.processId,
            "org.jboss.byteman.debug", 
            "org.jboss.byteman.verbose" };
    }

    @Override
    public String toString() {
        return String.format("host:%s,pid:%s,agentPort:%s", host, processId, port);
    }

}
