package com.RomanLegacy;

import java.util.Set;

/**
 * Interfaccia comune per BuildingInstance e MonumentInstance.
 * Permette di unificare la logica di connessione.
 */
public interface Connectable {
    
    BuildingType.ConnectionType getRequiredConnection();
    
    boolean isConnected();
    void setConnected(boolean connected);
    
    int getRootX();
    int getRootY();
    int getEffectiveW();
    int getEffectiveH();
    
    /**
     * Controlla se occupa una cella specifica.
     */
    default boolean occupies(int cx, int cy) {
        return cx >= getRootX() && cx < getRootX() + getEffectiveW()
                && cy >= getRootY() && cy < getRootY() + getEffectiveH();
    }
    
    /**
     * True se l'edificio tocca la rete (o è adiacente).
     */
    default boolean touchesNetwork(Set<String> network) {
        if (network.isEmpty()) return false;
        
        int[] ddx = {0, 1, 0, -1};
        int[] ddy = {1, 0, -1, 0};
        
        for (int dy = 0; dy < getEffectiveH(); dy++) {
            for (int dx = 0; dx < getEffectiveW(); dx++) {
                int cx = getRootX() + dx;
                int cy = getRootY() + dy;
                String key = cx + "," + cy;
                if (network.contains(key)) return true;
                for (int i = 0; i < 4; i++) {
                    if (network.contains((cx + ddx[i]) + "," + (cy + ddy[i])))
                        return true;
                }
            }
        }
        return false;
    }
}