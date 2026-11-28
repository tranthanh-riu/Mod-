package com.eldenringcore.nativecombat;

/**
 * Native Elden Ring Weapon Styles (independent of Epic Fight).
 * Each style defines its own combo cadence, reach, sweep angle, stamina cost, and playerAnimator clips.
 */
public enum EldenWeaponStyle {
    KATANA(
            "katana",
            new String[]{"katana_slash_1", "katana_slash_2", "katana_slash_3"},
            "katana_heavy_thrust",
            "katana_unsheathe",
            3.8, 125.0,
            14.0f, 24.0f, 20.0f,
            15.0f, 9, 14
    );

    public final String id;
    public final String[] comboAnims;
    public final String heavyAnim;
    public final String ashOfWarAnim;
    public final double reach;
    public final double arcDegrees;
    public final float lightStamina;
    public final float heavyStamina;
    public final float ashStamina;
    public final float ashFpCost;
    public final int lightCooldownTicks;
    public final int heavyCooldownTicks;

    EldenWeaponStyle(String id, String[] comboAnims, String heavyAnim, String ashOfWarAnim,
                     double reach, double arcDegrees,
                     float lightStamina, float heavyStamina, float ashStamina,
                     float ashFpCost, int lightCooldownTicks, int heavyCooldownTicks) {
        this.id = id;
        this.comboAnims = comboAnims;
        this.heavyAnim = heavyAnim;
        this.ashOfWarAnim = ashOfWarAnim;
        this.reach = reach;
        this.arcDegrees = arcDegrees;
        this.lightStamina = lightStamina;
        this.heavyStamina = heavyStamina;
        this.ashStamina = ashStamina;
        this.ashFpCost = ashFpCost;
        this.lightCooldownTicks = lightCooldownTicks;
        this.heavyCooldownTicks = heavyCooldownTicks;
    }
}
