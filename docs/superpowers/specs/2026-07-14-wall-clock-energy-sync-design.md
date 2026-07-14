# Wall-Clock Energy Sync Rate Design

## Goal

Make `energySyncRate` a real-time, server-authoritative target average and
upper bound for intermediate Deadeye energy updates, with every configured
value from 1 through 20 producing distinct behavior when the server provides
enough ticks.

## Existing Problem

The current implementation converts packets per second to a fixed integer
tick interval with `round(20 / rate)`. This creates two errors:

1. Values such as 6, 9, and 14 through 19 can exceed the configured cap at
   normal 20 TPS.
2. Deadeye deliberately lowers server TPS, so a tick-count interval also
   scales the wall-clock packet rate by the slowdown multiplier.

Twenty configured values collapse into only eight effective tick intervals.

## Behavioral Contract

- `energySyncRate` remains an integer from 1 through 20.
- The value controls intermediate energy updates using elapsed wall-clock
  time, not an assumed 20 TPS.
- With continuously changing energy and enough server ticks, the long-run
  average is the configured number of packets per wall-clock second, subject
  to at most one packet of rounding difference at observation boundaries.
- The limiter sends at most one fresh intermediate value per server tick.
  When server TPS is lower than the configured rate, it does not send
  duplicate stale values merely to reach a packet count.
- A slow server tick or pause must not create a catch-up burst afterward.
- Changes to or from 0% and 100% remain immediate and exempt from the rate
  limit.
- Login synchronization remains immediate.
- An immediate boundary or login synchronization resets the intermediate
  synchronization schedule.
- A backward or repeated time sample contributes no elapsed-time credit.

## Design

Introduce a package-private, zero-dependency `DeadeyeEnergySyncLimiter` owned
by each player's `DeadeyeEnergyManager.State`.

The limiter stores:

- the last observed monotonic timestamp in nanoseconds;
- a fractional packet credit below one packet during normal operation.

On each server tick, `DeadeyeEnergyManager` samples `System.nanoTime()` once
and supplies the same timestamp to every player limiter. A limiter adds:

```text
elapsedSeconds * configuredPacketsPerSecond
```

to its fractional credit. If intermediate energy changed and credit is at
least one, it permits one packet and subtracts one credit. A normal fractional
remainder below one packet is preserved. If a long stall leaves one or more
complete packets of backlog after the send, that backlog is discarded so the
next tick cannot replay it as a burst.

When energy has not changed, accumulated credit is capped at one packet. This
allows the next real change to synchronize promptly without banking an
unbounded burst during idle periods.

Boundary transitions bypass the credit check, permit an immediate packet,
and reset both credit and timestamp. Login performs the same schedule reset
after sending the authoritative energy value.

## Data Flow

1. The server reaches the end of a tick and samples monotonic wall-clock time.
2. `DeadeyeEnergyManager` updates each player's authoritative energy.
3. The player's limiter receives current energy, last synchronized energy,
   timestamp, and configured rate.
4. Boundary changes synchronize immediately; unchanged values do not send;
   intermediate changes send only when fractional credit permits.
5. A permitted send updates `lastSyncedEnergy` and transmits the existing
   `ClientboundDeadeyeEnergyPacket`.

No packet format or protocol version changes are required.

## Testing

The limiter remains independent of Minecraft and Forge APIs so the existing
zero-dependency logic suite can test it directly.

Tests will verify:

- every rate from 1 through 20 over a multi-second 20 TPS simulation;
- wall-clock accounting at 5 TPS: rates 1 through 5 retain their configured
  average, while rates 6 through 20 send once per available tick without
  accumulating catch-up credit;
- no rate overshoot or accumulated catch-up burst after a long pause;
- immediate synchronization at 0%, 100%, and when leaving a boundary;
- unchanged energy never synchronizes;
- login/reset behavior clears old timing credit;
- backward and repeated timestamps do not add credit;
- existing energy, hold-state, UI-value, and HUD-rule tests remain green.

## Documentation Changes

Update README and configuration comments to describe `energySyncRate` as a
wall-clock target average/upper bound for intermediate values. Document that
the server sends at most one fresh value per tick, so actual packet frequency
cannot exceed current server TPS. Keep the explicit 0%/100% immediate-sync
exception.

## Non-Goals

- Sending duplicate packets from a separate scheduler when server TPS is low.
- Moving authoritative energy calculation off the server tick thread.
- Client-side prediction or interpolation.
- Persisting energy across logout; logout reset is intentional behavior.
