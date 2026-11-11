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
        public final int price; // monete per unità esportata; 0 = non vendibile
        public float amount;

        public Resource(String id, String label, float r, float g, float b, int price) {
            this.id = id;
            this.label = label;
            this.color = new float[] { r, g, b };
            this.price = price;
            this.amount = 0f;
        }

        public boolean isSellable() {
            return price > 0;
        }

        public String getLabel() {
            return Localization.getOr("resource." + id, label);
        }
    }

    private final Map<String, Resource> resources = new LinkedHashMap<>();

    public ResourceManager() {

        add("coin", "Coins", 0.85f, 0.72f, 0.20f);
        add("population", "Population", 0.80f, 0.50f, 0.30f);

        add("beer", "Beer", 0.90f, 0.65f, 0.15f, 42);
        add("brass", "Brass", 0.85f, 0.72f, 0.30f, 58);
        add("bread", "Bread", 0.80f, 0.55f, 0.28f, 64);
        add("brick", "Brick", 0.70f, 0.30f, 0.20f, 20);
        add("cable", "Cable", 0.20f, 0.20f, 0.22f, 66);
        add("cannedfood", "Canned food", 0.55f, 0.35f, 0.25f, 57);
        add("ceramics", "Ceramics", 0.88f, 0.85f, 0.80f, 29);
        add("cheese", "Cheese", 0.95f, 0.80f, 0.30f, 29);
        add("cigars", "Cigars", 0.45f, 0.25f, 0.15f, 122);
        add("clay", "Clay", 0.72f, 0.48f, 0.30f, 11);
        add("coal", "Coal", 0.15f, 0.15f, 0.16f, 8);
        add("coffee", "Coffee", 0.25f, 0.14f, 0.08f, 75);
        add("coffee_beans", "Coffee beans", 0.35f, 0.20f, 0.12f, 24);
        add("concrete", "Concrete", 0.62f, 0.62f, 0.60f, 166);
        add("copper", "Copper", 0.80f, 0.50f, 0.30f, 8);
        add("cotton", "Cotton", 0.95f, 0.94f, 0.90f, 9);
        add("electricmotor", "Electric motor", 0.30f, 0.35f, 0.50f, 1588);
        add("fabric", "Fabric", 0.45f, 0.60f, 0.72f, 28);
        add("fan", "Fan", 0.70f, 0.70f, 0.72f, 3975);
        add("fish", "Fish", 0.35f, 0.55f, 0.70f, 18);
        add("flax", "Flax", 0.85f, 0.80f, 0.55f, 9);
        add("flour", "Flour", 0.95f, 0.93f, 0.88f, 27);
        add("fuel", "Fuel", 0.25f, 0.22f, 0.15f, 26);
        add("fur_coats", "Fur coats", 0.55f, 0.38f, 0.22f, 264);
        add("glass", "Glass", 0.75f, 0.90f, 0.92f, 29);
        add("grain", "Grain", 0.85f, 0.70f, 0.20f, 9);
        add("grapes", "Grapes", 0.45f, 0.20f, 0.45f, 9);
        add("hops", "Hops", 0.45f, 0.65f, 0.25f, 9);
        add("iron", "Iron", 0.55f, 0.57f, 0.60f, 8);
        add("lavander", "Lavander", 0.60f, 0.45f, 0.85f, 9);
        add("leather", "Leather", 0.50f, 0.30f, 0.18f, 9);
        add("lightbulb", "Lightbulb", 0.98f, 0.95f, 0.70f, 315);
        add("marble", "Marble", 0.93f, 0.93f, 0.96f, 16);
        add("milk", "Milk", 0.97f, 0.96f, 0.92f, 9);
        add("motor", "Motor", 0.35f, 0.38f, 0.42f, 309);
        add("oil", "Oil", 0.12f, 0.11f, 0.10f, 6);
        add("olive", "Olive", 0.30f, 0.40f, 0.15f, 9);
        add("olive_oil", "Olive oil", 0.55f, 0.65f, 0.20f, 31);
        add("paper", "Paper", 0.92f, 0.90f, 0.85f, 37);
        add("perfume", "Perfume", 0.70f, 0.40f, 0.75f, 124);
        add("plastic", "Plastic", 0.20f, 0.45f, 0.55f, 28);
        add("purse", "Purse", 0.70f, 0.50f, 0.30f, 101);
        add("radio", "Radio", 0.40f, 0.30f, 0.20f, 674);
        add("rubber", "Rubber", 0.20f, 0.20f, 0.20f, 9);
        add("rum", "Rum", 0.75f, 0.45f, 0.15f, 107);
        add("saltpeter", "Saltpeter", 0.88f, 0.88f, 0.82f, 18);
        add("sand", "Sand", 0.93f, 0.87f, 0.60f, 11);
        add("sewing_machine", "Sewing machine", 0.35f, 0.35f, 0.38f, 88);
        add("soap", "Soap", 0.90f, 0.88f, 0.75f, 71);
        add("spectacles", "Spectacles", 0.35f, 0.45f, 0.60f, 215);
        add("steam_carriage", "Steam carriage", 0.55f, 0.35f, 0.25f, 1416);
        add("steel", "Steel", 0.60f, 0.63f, 0.68f, 50);
        add("sugar", "Sugar", 0.95f, 0.95f, 0.92f, 24);
        add("timber", "Timber", 0.55f, 0.38f, 0.20f, 11);
        add("tobacco", "Tobacco", 0.75f, 0.60f, 0.25f, 24);
        add("toga", "Toga", 0.90f, 0.88f, 0.82f, 57);
        add("tools", "Tools", 0.45f, 0.47f, 0.50f, 54);
        add("truck", "Truck", 0.45f, 0.55f, 0.35f, 3749);
        add("window", "Window", 0.60f, 0.78f, 0.85f, 91);
        add("wine", "Wine", 0.50f, 0.10f, 0.25f, 53);
        add("wood_log", "Wood log", 0.45f, 0.30f, 0.18f, 9);
        add("wool", "Wool", 0.92f, 0.90f, 0.85f, 9);
        add("zinc", "Zinc", 0.70f, 0.72f, 0.75f, 8);

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
        add(id, label, r, g, b, 0);
    }

    private void add(String id, String label, float r, float g, float b, int price) {
        resources.put(id, new Resource(id, label, r, g, b, price));
    }
}