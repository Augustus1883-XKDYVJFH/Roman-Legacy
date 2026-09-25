package com.RomanLegacy;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ResourceManager {

    public static class Resource {
        public final String id;
        public final String label;
        public final float[] color;
        public float amount;

        public Resource(String id, String label, float r, float g, float b) {
            this.id = id;
            this.label = label;
            this.color = new float[] { r, g, b };
            this.amount = 0f;
        }
    }

    private final Map<String, Resource> resources = new LinkedHashMap<>();

    public ResourceManager() {

        add("coin", "Coins", 0.78f, 0.66f, 0.13f); // fatto
        add("population", "Population", 0.80f, 0.50f, 0.30f); // no png

        // catene produttive
        // pane
        add("grain", "Grain", 0.85f, 0.70f, 0.20f); // fatto
        add("flour", "Flour", 0.90f, 0.85f, 0.70f); // fatto
        add("bread", "Bread", 0.85f, 0.65f, 0.40f); // fatto

        // acciaio e prodotti ferro
        add("iron", "Iron", 0.44f, 0.38f, 0.38f); // fatto
        add("tools", "Tools", 0.40f, 0.35f, 0.35f); // fatto da vedere
        add("steel", "Steel", 0.31f, 0.36f, 0.44f); // fatto

        // mattoni e prodotti argilla
        add("clay", "Clay", 0.69f, 0.44f, 0.25f); // fatto
        add("brick", "Brick", 0.69f, 0.36f, 0.19f); // fatto
        add("ceramics", "Ceramics", 0.69f, 0.36f, 0.19f); // fatto
        add("concrete", "Concrete", 0.50f, 0.53f, 0.53f); // fatto

        // finestre
        add("sand", "Sand", 0.91f, 0.85f, 0.44f); // fatto
        add("glass", "Glass", 0.44f, 0.66f, 0.69f); // fatto
        add("window", "Window", 0.44f, 0.66f, 0.69f);

        // toga
        add("wool", "Wool", 0.88f, 0.85f, 0.75f); // fatto
        add("flax", "Flax", 0.94f, 0.90f, 0.70f); // fatto
        add("fabric", "Fabric", 0.50f, 0.67f, 0.72f); // fatto da vedere
        add("toga", "Toga", 0.50f, 0.66f, 0.72f); // fatto

        // lampadine
        add("copper", "Copper", 0.75f, 0.44f, 0.25f); // fatto
        add("cable", "Cable", 0.25f, 0.32f, 0.38f); // fatto da vedere
        // lucerna electrica
        add("lightbulb", "Lightbulb", 0.95f, 0.92f, 0.60f); // fatto da vedere
        add("grapes", "Grapes", 0.50f, 0.25f, 0.50f); // fatto
        add("wine", "Wine", 0.55f, 0.15f, 0.35f); // fatto

        // plastica e carburante
        add("oil", "Oil", 0.10f, 0.10f, 0.12f); // fatto da rivedere
        add("fuel", "Fuel", 0.15f, 0.17f, 0.10f); // fatto
        add("plastic", "Plastic", 0.18f, 0.30f, 0.28f); // fatto

        // cibo in scatola
        add("fish", "Fish", 0.23f, 0.50f, 0.66f); // fatto
        add("cannedfood", "Canned food", 0.38f, 0.47f, 0.31f); // fatto

        // --- birra (NUOVA) ---
        add("hops", "Hops", 0.35f, 0.76f, 0.25f); // fatto
        add("beer", "Beer", 0.95f, 0.75f, 0.20f); // fatto

        // --- olio d'oliva (NUOVA) ---
        add("olive", "Olive", 0.15f, 0.45f, 0.10f); // fatto
        add("olive_oil", "Olive oil", 0.35f, 0.70f, 0.20f); // fatto

        // sapone e profumi
        add("lavander", "Lavander", 0.57f, 0.39f, 0.85f); // fatto ritoccare
        add("soap", "Soap", 0.85f, 0.80f, 0.60f); // fatto da rivedere
        add("saltpeter", "Saltpeter", 0.82f, 0.85f, 0.69f); // fatto
        add("perfume", "Perfume", 0f, 0f, 0f); // fatto

        // --- formaggi (NUOVA) ---
        add("milk", "Milk", 0.95f, 0.92f, 0.88f); // fatto da rivedere
        add("cheese", "CHeese", 0.88f, 0.75f, 0.35f); // fatto

        // occhiali
        add("zinc", "Zinc", 0.65f, 0.65f, 0.70f); // fatto
        add("brass", "Brass", 0.80f, 0.70f, 0.25f); // fatto
        // ocularia
        add("spectacles", "Spectacles", 0.40f, 0.55f, 0.70f); // fatto da vedere

        // --- marmo (NUOVA) ---
        add("marble", "Marble", 0.90f, 0.90f, 0.95f); // fatto

        // --- cuoio e pelletteria (NUOVA) ---
        add("leather", "Leather", 0.55f, 0.35f, 0.20f); // fatto
        add("purse", "Purse", 0.75f, 0.55f, 0.25f); // fatto

        // --- caffè (NUOVA) ---
        add("coffee_beans", "Coffee beans", 0.40f, 0.20f, 0.10f); // fatto
        add("coffee", "Coffee", 0.35f, 0.18f, 0.08f); // fatto da vedere/ritoccare

        // --- tabacco e sigari (NUOVA) ---
        add("tobacco", "Tobacco", 0.86f, 0.70f, 0.24f); // fatto
        add("cigars", "Cigars", 0.80f, 0.60f, 0.20f); // fatto da rivedere

        // --- zucchero e rum (NUOVA) ---
        add("sugar", "Sugar", 0.87f, 0.90f, 0.52f); // fatto da rifinire
        add("rum", "Rum", 0.70f, 0.35f, 0.20f); // fatto

        // paper
        add("paper", "Paper", 0f, 0f, 0f); // fatto

        // prodotti base con più scopi
        add("rubber", "Rubber", 0.25f, 0.30f, 0.18f); // fatto
        add("coal", "Coal", 0.16f, 0.16f, 0.16f); // fatto
        add("timber", "Timber", 0.29f, 0.41f, 0.21f); // fatto

        add("wood_log", "Wood log", 0f, 0f, 0f);
        add("fan", "Fan", 0f, 0f, 0f);
        add("fur_coats", "Fur coats", 0f, 0f, 0f);
        add("sewing_machine", "Sewing machine", 0f, 0f, 0f);

        // prodotti avanzati misti
        // motor
        add("motor", "Motor", 0.25f, 0.28f, 0.34f); // fatto da rivedere
        add("electricmotor", "Electric motor", 0.25f, 0.28f, 0.44f);
        // radius
        add("radio", "Radio", 0.30f, 0.35f, 0.40f);
        add("steam_carriage", "Steam carriage", 0.8f, 0.7f, 0.4f);
        add("truck", "Truck", 0.4f, 0.7f, 0.3f);

        // Valori iniziali — le monete arrivano dall'evento THE_NEW_CITY
        set("population", 0f);
    }

    public void add(String id, float amount) {
        Resource r = resources.get(id);
        if (r != null)
            r.amount = Math.max(0, r.amount + amount);
    }

    public void set(String id, float amount) {
        Resource r = resources.get(id);
        if (r != null)
            r.amount = Math.max(0, amount);
    }

    public float get(String id) {
        Resource r = resources.get(id);
        return r != null ? r.amount : 0f;
    }

    public boolean canAfford(Map<String, Float> cost) {
        for (Map.Entry<String, Float> e : cost.entrySet()) {
            if (get(e.getKey()) < e.getValue())
                return false;
        }
        return true;
    }

    public void spend(Map<String, Float> cost) {
        for (Map.Entry<String, Float> e : cost.entrySet()) {
            add(e.getKey(), -e.getValue());
        }
    }

    public void setTotalPopulation(float total) {
        set("population", total);
    }

    /**
     * Restituisce le risorse più importanti da mostrare nella top bar.
     * Ordine: monete, popolazione, poi le altre per quantità decrescente.
     */
    public Resource[] getTopBarResources(int n) {
        List<Resource> important = new ArrayList<>();

        Resource coin = resources.get("coin");
        Resource population = resources.get("population");

        if (coin != null)
            important.add(coin);

        // 3. Altre risorse ordinate per quantità (decrescente)
        List<Resource> others = new ArrayList<>();
        for (Resource r : resources.values()) {
            if (r != coin && r != population) {
                others.add(r);
            }
        }
        others.sort((a, b) -> Float.compare(b.amount, a.amount));

        // Aggiungi fino a n elementi
        important.addAll(others);

        int size = Math.min(n, important.size());
        return important.subList(0, size).toArray(new Resource[size]);
    }

    public Map<String, Resource> getAll() {
        return resources;
    }

    private void add(String id, String label, float r, float g, float b) {
        resources.put(id, new Resource(id, label, r, g, b));
    }
}