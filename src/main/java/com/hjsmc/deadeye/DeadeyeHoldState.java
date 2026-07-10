package com.hjsmc.deadeye;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

final class DeadeyeHoldState {
    private final Set<UUID> requested = new HashSet<>();
    private final Set<UUID> active = new HashSet<>();

    boolean isAnyActive() {
        return !active.isEmpty();
    }

    boolean isActive(UUID playerId) {
        return active.contains(playerId);
    }

    boolean isRequested(UUID playerId) {
        return requested.contains(playerId);
    }

    void setRequested(UUID playerId, boolean holding, boolean canActivate) {
        if (holding) {
            requested.add(playerId);
            if (canActivate) {
                active.add(playerId);
            } else {
                active.remove(playerId);
            }
        } else {
            requested.remove(playerId);
            active.remove(playerId);
        }
    }

    void suspend(UUID playerId) {
        active.remove(playerId);
    }

    void resumeIfRequested(UUID playerId) {
        if (requested.contains(playerId)) {
            active.add(playerId);
        }
    }

    void clear() {
        requested.clear();
        active.clear();
    }
}
