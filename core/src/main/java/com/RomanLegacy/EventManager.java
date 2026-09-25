package com.RomanLegacy;

import com.RomanLegacy.GameState;
import com.badlogic.gdx.Gdx;
import java.util.*;

public class EventManager {

    private static final float MIN_CHECK_INTERVAL = 60f;
    private static final float MAX_CHECK_INTERVAL = 150f;

    private final Random rng;
    private float timeSinceLastCheck = 0f;
    private float nextCheckIn;

    private final Set<String> firedEvents = new HashSet<>();
    private final Map<String, Float> conditionTimers = new HashMap<>();
    public NarrativeEvent pendingEvent = null;

    public EventManager(long seed) {
        rng = new Random(seed);
        rollNextCheckDelay();
    }

    private void rollNextCheckDelay() {
        nextCheckIn = MIN_CHECK_INTERVAL + rng.nextFloat() * (MAX_CHECK_INTERVAL - MIN_CHECK_INTERVAL);
    }

    public void tick(float delta, GameState state) {
        if (pendingEvent != null)
            return;

        // Eventi TIMER: roll casuale periodico, invariato
        timeSinceLastCheck += delta;
        if (timeSinceLastCheck >= nextCheckIn) {
            timeSinceLastCheck = 0f;
            rollNextCheckDelay();
            rollTimerEvent(state);
            if (pendingEvent != null)
                return;
        }

        // Eventi a condizione: timer continuo dal momento in cui diventano eleggibili
        updateConditionEvents(delta, state);
    }

    private void rollTimerEvent(GameState state) {
        List<NarrativeEvent> eligible = new ArrayList<>();
        for (NarrativeEvent e : NarrativeEvent.getAllEvents()) {
            if (e.triggerType != NarrativeEvent.TriggerType.TIMER)
                continue;
            if (!e.triggerConfigured) // ← blocca i TIMER "per dimenticanza"
                continue;
            if (!e.repeatable && firedEvents.contains(e.id))
                continue;
            eligible.add(e);
        }
        if (eligible.isEmpty())
            return;

        pendingEvent = eligible.get(rng.nextInt(eligible.size()));
    }

    private void updateConditionEvents(float delta, GameState state) {
        for (NarrativeEvent e : NarrativeEvent.getAllEvents()) {
            if (e.triggerType == NarrativeEvent.TriggerType.TIMER)
                continue;
            if (!e.repeatable && firedEvents.contains(e.id))
                continue;

            boolean eligibleNow = isEligible(e, state);
            float elapsed = conditionTimers.getOrDefault(e.id, 0f);

            if (!eligibleNow) {
                if (elapsed != 0f)
                    conditionTimers.put(e.id, 0f);
                continue;
            }

            elapsed += delta;
            if (elapsed >= e.triggerDelay) {
                conditionTimers.put(e.id, 0f);
                pendingEvent = e;
                return;
            }
            conditionTimers.put(e.id, elapsed);
        }
    }

    private boolean isEligible(NarrativeEvent e, GameState state) {
        if (!e.triggerConfigured)
            return false;

        switch (e.triggerType) {
            case TIMER:
                return true;
            case POPULATION_THRESHOLD:
                return state.getClassPopulation(e.thresholdClass) >= e.thresholdAmount;
            case RESOURCE_THRESHOLD:
                return e.thresholdResource != null
                        && state.resources.get(e.thresholdResource) >= e.thresholdAmount;
            case FACTION_ARRIVED:
                return state.factionManager.isArrived(e.faction);
            case FACTION_RELATION:
                if (!state.factionManager.isArrived(e.faction))
                    return false;
                int rel = state.factionManager.getRelation(e.faction);
                return rel >= e.minRelation && rel <= e.maxRelation;
            case BUILDING_EXISTS:
                return state.countBuildingsOfType(e.requiredBuildingType) >= e.requiredBuildingCount;
            case NEW_WORLD_UNLOCKED:
                return state.newWorldUnlocked;
            default:
                return false;
        }
    }

    public void resolveChoice(int choiceIndex, GameState state) {
        if (pendingEvent == null)
            return;
        if (choiceIndex < 0 || choiceIndex >= pendingEvent.choices.length)
            return;

        NarrativeEvent.EventChoice choice = pendingEvent.choices[choiceIndex];
        for (NarrativeEvent.EventEffect eff : choice.effects) {
            applyEffect(eff, state);
        }

        String resolvedId = pendingEvent.id;
        firedEvents.add(resolvedId);
        notifyUnlockedBuildings(resolvedId, state);
        pendingEvent = null;
    }

    private void notifyUnlockedBuildings(String eventId, GameState state) {
        List<String> labels = new ArrayList<>();
        for (BuildingType bt : BuildingType.getAllBuildable()) {
            if (eventId.equals(bt.requiredEventId))
                labels.add(bt.label);
        }
    }

    public boolean hasFired(String eventId) {
        return eventId != null && firedEvents.contains(eventId);
    }

    public int getFiredEventCount() {
        return firedEvents.size();
    }

    private void applyEffect(NarrativeEvent.EventEffect eff, GameState state) {
        switch (eff.type) {
            case RESOURCE:
                state.resources.add(eff.resourceId, eff.resourceAmount);
                break;
            case IDEOLOGY:
                state.ideologyManager.applyShift(eff.ideologyShift);
                break;
            case FACTION_RELATION:
                state.factionManager.changeRelation(eff.targetFaction, eff.relationDelta);
                break;
            case DIFFICULTY:
                state.difficulty = eff.difficulty;
                break;
        }
    }

    // ── Save / Load ──────────────────────────────────────────────────────────────

    public List<String> serializeFiredEvents() {
        return new ArrayList<>(firedEvents);
    }

    public void deserializeFiredEvents(List<String> ids) {
        firedEvents.clear();
        if (ids != null)
            firedEvents.addAll(ids);
    }

    public String getPendingEventId() {
        return pendingEvent != null ? pendingEvent.id : null;
    }

    public void restorePendingEvent(String id) {
        if (id != null)
            pendingEvent = NarrativeEvent.byId(id);
    }
}