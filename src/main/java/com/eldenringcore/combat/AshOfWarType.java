package com.eldenringcore.combat;

public enum AshOfWarType {
    NONE(0.0f),
    CORPSE_PILER(18.0f),
    TRANSIENT_MOONLIGHT(15.0f),
    MOONLIGHT_WAVE(20.0f),
    WATERFOWL_DANCE(26.0f);

    private final float baseFpCost;

    AshOfWarType(float baseFpCost) {
        this.baseFpCost = baseFpCost;
    }

    public float getBaseFpCost() {
        return this.baseFpCost;
    }
}
