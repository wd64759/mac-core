package com.cte4.mac.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InstallAgentTests {

    @Test
    public void attach() {
        InstallAgent agent = InstallAgent.attach();
        Assertions.assertNotNull(agent);
    }
    
}
