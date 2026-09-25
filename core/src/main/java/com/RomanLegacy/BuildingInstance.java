package com.RomanLegacy;

import com.RomanLegacy.BuildingType.PowerType;
import com.RomanLegacy.BuildingType.ServiceCategory;
import com.RomanLegacy.Connectable;
import com.RomanLegacy.BuildingType.ConsumptionServiceCategory;

/**
 * Rappresenta un singolo edificio piazzato sulla mappa.
 * rootX/rootY è la cella in basso-sinistra dell'edificio.
 *
 * PER LE CASE:
 * - type è mutabile (upgrade cambia la casa in-place).
 * - housePop cresce se i bisogni sono soddisfatti e la casa non è piena.
 * - Un tick di popolazione viene chiamato ogni secondo (~TICK_INTERVAL).
 * - canUpgrade() == true quando la casa è piena E tutti i bisogni sono ok.
 */
public class BuildingInstance implements Connectable {

    /** Tipo corrente — può cambiare in-place con upgradeHouse(). */
    public BuildingType type;

    public final int rootX;
    public final int rootY;

    // ── Produzione
    // ────────────────────────────────────────────────────────────────
    public float progress = 0f;
    public long lastUpdateMs = 0;
    public boolean paused = false;
    public boolean rotated = false;
    public boolean connected = false;
    public boolean hasSteam = false;
    public boolean hasElectricity = false;
    // ── Logistica (magazzini)
    // ───────────────────────────────────────────────────
    public boolean hasCarriageLogistics = false;
    public boolean hasTruckLogistics = false;
    public float workforceSatisfaction = 1f;
    public int productionCounter = 0; // per extra ogni 3 cicli
    // -- Monumenti
    // --------------------------------------------------------
    public boolean monumentUpgrading = false;
    public float monumentUpgradeProgress = 0f;
    // ── Popolazione (solo case)
    // ───────────────────────────────────────────────────
    /** Abitanti correnti nella casa [0 … houseCap]. */
    public float housePop = 0f;
    // ── Recinti ──
    public int ownerX = -1, ownerY = -1; // solo recinti: cella root dell'allevamento a cui appartengono
    public float penEfficiency = 1f; // solo allevamenti: recinti costruiti / recinti richiesti

    public BuildingInstance(BuildingType type, int rootX, int rootY) {
        this.type = type;
        this.rootX = rootX;
        this.rootY = rootY;
        this.lastUpdateMs = System.currentTimeMillis();
    }

    // ── Tick produzione
    // ───────────────────────────────────────────────────────────

    // In BuildingInstance.java - metodo tick()
    public boolean tick(ResourceManager resources, float delta) {
        // Se non ha produzione E non è consumableOnly, salta
        if (type.prod == null && !type.consumableOnly)
            return false;
        if (paused)
            return false;

        // Controlli connessioni, potenza...
        BuildingType.ConnectionType req = type.requiredConnection;
        if (req != null && req != BuildingType.ConnectionType.NONE && !connected)
            return false;
        if (type.requiredPower == PowerType.STEAM && !hasSteam)
            return false;
        if (type.requiredPower == PowerType.ELECTRICITY && !hasElectricity)
            return false;
        if (type.requiredPower == PowerType.BOTH && (!hasSteam || !hasElectricity))
            return false;

        float elapsed = Math.min(0.5f, delta);
        float cycle = type.cycleTime > 0 ? type.cycleTime : 1f;
        float speedMulti = 1.0f;

        if (hasCarriageLogistics && hasTruckLogistics) {
            speedMulti = 2.5f;
        } else if (hasTruckLogistics) {
            speedMulti = 2.0f;
        } else if (hasCarriageLogistics) {
            speedMulti = 1.5f;
        }

        float effWorkforce = 0.4f + 0.6f * workforceSatisfaction;

        // Il progresso avanza sempre, indipendentemente dalla disponibilità di
        // input: si ferma solo a progress=1 ("pronto, in attesa di risorse")
        // invece di congelarsi a ogni tick in cui manca anche solo momentaneamente
        // l'input richiesto.
        if (progress < 1f) {
            progress += (elapsed / cycle) * speedMulti * effWorkforce * penEfficiency;
            if (progress > 1f)
                progress = 1f;
        }

        if (progress >= 1f) {
            // Controllo input SOLO al completamento del ciclo
            if (type.input != null) {
                for (java.util.Map.Entry<String, Float> e : type.input.entrySet()) {
                    if (resources.get(e.getKey()) < e.getValue()) {
                        return false; // resta "pronto" al 100%, riprova al prossimo tick
                    }
                }
                for (java.util.Map.Entry<String, Float> e : type.input.entrySet())
                    resources.add(e.getKey(), -e.getValue());
            }

            progress = 0f;

            if (type.prod != null) {
                resources.add(type.prod, type.rate);

                if (hasCarriageLogistics && hasTruckLogistics) {
                    productionCounter++;
                    if (productionCounter >= 3) {
                        resources.add(type.prod, 1);
                        productionCounter = 0;
                    }
                }
            }

            return true;
        }
        return false;
    }

    // ── Tick popolazione
    public void tickHouse(ResourceManager resources, GameState state) {
        if (!type.isHouse)
            return;
        if (type.houseNeeds == null || type.houseNeeds.length == 0)
            return;

        int cap = type.houseCap();
        if (cap == 0)
            return;

        float graceCap = cap * 0.30f;
        boolean inGrace = (int) housePop < (int) graceCap;

        boolean needsSatisfied = true;
        for (String need : type.houseNeeds) {
            if (resources.get(need) < 0.05f) {
                needsSatisfied = false;
                break;
            }
        }

        if (needsSatisfied) {
            float consumptionMultiplier = state.getConsumptionMultiplier();
            for (String need : type.houseNeeds) {
                float consumption = (0.006f * (((type.houseLevel + 1) * 0.1f))) * consumptionMultiplier;
                resources.add(need, -consumption);
            }
        }

        if ((inGrace || needsSatisfied) && housePop < cap) {
            float growth = 0.2f;
            float target = inGrace ? graceCap : cap;
            housePop = Math.min(target, housePop + growth);
        } else if (!needsSatisfied && housePop > graceCap) {
            housePop = Math.max(graceCap, housePop - 0.1f);
        }

        housePop = Math.min(cap, Math.max(0f, housePop));
    }

    // ── Tasse e manutenzione ─────────────────────────────────────────────────

    /**
     * Calcola le tasse generate da questa casa al minuto.
     * Le tasse sono proporzionali alla popolazione e al livello della casa.
     */
    public float calculateTaxesPerMinute() {
        if (!type.isHouse)
            return 0f;
        int level = type.houseLevel != null ? type.houseLevel : 0;
        float baseTaxPerPerson = 2.5f + (level * 2.5f);
        return housePop * baseTaxPerPerson;
    }

    public float getMaintenancePerMinute() {
        if (type.isHouse)
            return 0f;
        if (type.prod == null && !type.consumableOnly)
            return 0f;
        if (type.input == null || type.input.isEmpty())
            return 0f;
        return type.maintenanceCost;
    }

    // ── Upgrade casa
    // ──────────────────────────────────────────────────────────────

    public boolean canUpgrade(ResourceManager resources) {
        if (!type.isHouse) {
            return false;
        }
        if (type.houseNeeds == null || type.houseNeeds.length == 0) {
            return false;
        }
        if (type.nextHouseLevel() == null) {
            return false;
        }

        int cap = type.houseCap();
        if (housePop < cap - 0.5f) {
            return false;
        }

        for (String need : type.houseNeeds) {
            if (resources.get(need) < 0.05f) {
                return false;
            }
        }
        return true;
    }

    public boolean upgradeHouse(ResourceManager resources) {
        if (!canUpgrade(resources))
            return false;
        BuildingType next = type.nextHouseLevel();
        if (next == null)
            return false;
        type = next;
        housePop = Math.min(housePop, type.houseCap());
        return true;
    }

    public boolean startMonumentUpgrade(ResourceManager resources, float costMultiplier) {
        if (!type.isMonument || type.isLastMonumentLevel())
            return false;
        BuildingType next = type.nextMonumentLevel();
        if (next == null)
            return false;

        java.util.Map<String, Float> scaledCost = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, Float> e : next.upgradeCost.entrySet()) {
            scaledCost.put(e.getKey(), e.getValue() * costMultiplier);
        }

        if (!resources.canAfford(scaledCost))
            return false;
        resources.spend(scaledCost);
        monumentUpgrading = true;
        monumentUpgradeProgress = 0f;
        return true;
    }

    public boolean tickMonumentUpgrade(float delta, float timeMultiplier) {
        if (!monumentUpgrading)
            return false;
        monumentUpgradeProgress += delta;
        float base = type.upgradeTime > 0 ? type.upgradeTime : 60f; // fallback
        float required = base * timeMultiplier;
        if (monumentUpgradeProgress >= required) {
            BuildingType next = type.nextMonumentLevel();
            if (next != null) {
                this.type = next;
            }
            monumentUpgrading = false;
            monumentUpgradeProgress = 0f;
            return true;
        }
        return false;
    }

    // ── Utilità
    // ───────────────────────────────────────────────────────────────────

    public boolean occupies(int cx, int cy) {
        int w = rotated ? type.h : type.w;
        int h = rotated ? type.w : type.h;
        return cx >= rootX && cx < rootX + w
                && cy >= rootY && cy < rootY + h;
    }

    public int effectiveW() {
        return rotated ? type.h : type.w;
    }

    public int effectiveH() {
        return rotated ? type.w : type.h;
    }

    @Override
    public int getRootX() {
        return rootX;
    }

    @Override
    public int getRootY() {
        return rootY;
    }

    @Override
    public int getEffectiveW() {
        return effectiveW();
    }

    @Override
    public int getEffectiveH() {
        return effectiveH();
    }

    @Override
    public BuildingType.ConnectionType getRequiredConnection() {
        return type.requiredConnection;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}