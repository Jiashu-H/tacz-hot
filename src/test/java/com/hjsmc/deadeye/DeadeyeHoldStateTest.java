package com.hjsmc.deadeye;

import java.util.UUID;

public final class DeadeyeHoldStateTest {
    private DeadeyeHoldStateTest() {
    }

    public static void main(String[] args) {
        exhaustionSuspendsWithoutForgettingThePhysicalHold();
        infiniteEnergyCanResumeARequestedHold();
        releasingTheKeyClearsRequestedAndActiveState();
    }

    private static void exhaustionSuspendsWithoutForgettingThePhysicalHold() {
        DeadeyeHoldState state = new DeadeyeHoldState();
        UUID playerId = UUID.randomUUID();

        state.setRequested(playerId, true, true);
        state.suspend(playerId);

        assertTrue(state.isRequested(playerId));
        assertFalse(state.isActive(playerId));
    }

    private static void infiniteEnergyCanResumeARequestedHold() {
        DeadeyeHoldState state = new DeadeyeHoldState();
        UUID playerId = UUID.randomUUID();

        state.setRequested(playerId, true, false);
        state.resumeIfRequested(playerId);

        assertTrue(state.isActive(playerId));
        assertTrue(state.isAnyActive());
    }

    private static void releasingTheKeyClearsRequestedAndActiveState() {
        DeadeyeHoldState state = new DeadeyeHoldState();
        UUID playerId = UUID.randomUUID();

        state.setRequested(playerId, true, true);
        state.setRequested(playerId, false, false);
        state.resumeIfRequested(playerId);

        assertFalse(state.isRequested(playerId));
        assertFalse(state.isActive(playerId));
        assertFalse(state.isAnyActive());
    }

    private static void assertTrue(boolean value) {
        if (!value) {
            throw new AssertionError("Expected true");
        }
    }

    private static void assertFalse(boolean value) {
        if (value) {
            throw new AssertionError("Expected false");
        }
    }
}
