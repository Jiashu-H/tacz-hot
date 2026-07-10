package com.hjsmc.deadeye;

import com.hjsmc.deadeye.client.DeadeyeConfigUiValuesTest;
import com.hjsmc.deadeye.client.DeadeyeHudRulesTest;

public final class DeadeyeLogicTestSuite {
    private DeadeyeLogicTestSuite() {
    }

    public static void main(String[] args) {
        DeadeyeConfigUiValuesTest.main(args);
        DeadeyeEnergyRulesTest.main(args);
        DeadeyeHoldStateTest.main(args);
        DeadeyeHudRulesTest.main(args);
    }
}
