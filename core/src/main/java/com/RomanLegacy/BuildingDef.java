package com.RomanLegacy;

import java.util.HashMap;

/**
 * Rappresentazione JSON di un BuildingType — mirror 1:1 dei metodi di
 * BuildingType.Builder. Ogni campo non presente nel JSON resta al suo
 * valore di default Java (vedi inizializzatori sotto), coerente con i
 * default del Builder originale.
 *
 * NOTA: "label" per ora resta testo letterale (non chiave di traduzione).
 * Verrà rinominato in "labelKey" quando integreremo I18NBundle, in un
 * passaggio successivo e separato da questa migrazione.
 */
public class BuildingDef {
    public String id;
    public String label;
    public String colorHex;
    public int w;
    public int h;

    public HashMap<String, Float> cost;
    public HashMap<String, Float> input;

    public String prod;
    public int rate;
    public int cycleTime;

    public int[] onlyTerrain;

    public boolean isHouse = false;
    public Integer houseLevel;
    public String[] houseNeeds;

    public boolean buildable = true;
    public String[] allowedRegions; // nomi di BuildingType.Region

    public String requiredConnection; // nome di BuildingType.ConnectionType
    public String requiredPower; // nome di BuildingType.PowerType

    public float maintenanceCost;

    public int influenceRadius;
    public boolean consumableOnly = false;

    public boolean isMonument = false;
    public String monumentChainId;
    public int monumentLevel;
    public HashMap<String, Float> upgradeCost;
    public float upgradeTime;

    public float axisShift;

    public String serviceCategory; // nome di BuildingType.ServiceCategory
    public int serviceCapacity;
    public String consumptionServiceCategory; // nome di BuildingType.ConsumptionServiceCategory

    public Integer workforceClass;
    public int workforceAmount;

    public Integer unlockClass;
    public int unlockAmount;

    public String requiredEventId;

    public String[] penIds;
    public String penOwnerId;

    public String fieldTypeId;
    public int fieldCap;
    public String fieldOwnerId;

    public boolean requiresDeposit = false;
}
