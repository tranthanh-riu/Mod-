package com.eldenringcore.combat;

public enum AshOfWarType {
    NONE(0.0f),
    CORPSE_PILER(18.0f),
    TRANSIENT_MOONLIGHT(15.0f),
    MOONLIGHT_WAVE(20.0f),
    WATERFOWL_DANCE(26.0f),
    TAKERS_FLAMES(24.0f),
    STARCALLER_CRY(26.0f),
    DESTINED_DEATH(30.0f),
    BLOODHOUNDS_FINESSE(16.0f),
    GOLDEN_VOW(22.0f),
    ANCIENT_LIGHTNING_SPEAR(24.0f);

    private final float baseFpCost;

    AshOfWarType(float baseFpCost) {
        this.baseFpCost = baseFpCost;
    }

    public float getBaseFpCost() {
        return this.baseFpCost;
    }
}
