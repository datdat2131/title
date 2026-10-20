package vn.astrax.titles.model;

import java.util.Locale;

/**
 * Loại nhiệm vụ.
 * Đếm tiến độ: KILL_MOB, KILL_PLAYER, BREAK_BLOCK, FISH, PLAYTIME_MINUTES, BOSS_KILL.
 * Trạng thái (kiểm tra định kỳ): ELEMENTER_UNLOCK (plugin AstraxElementer), NGUYENTO_UNLOCK (plugin NguyenTo), PERMISSION.
 * MANUAL: không có nhiệm vụ, admin cấp tay.
 */
public enum AchievementType {
    KILL_MOB, KILL_PLAYER, BREAK_BLOCK, FISH, PLAYTIME_MINUTES, BOSS_KILL, ELEMENTER_UNLOCK, NGUYENTO_UNLOCK, PERMISSION, MANUAL;

    public static AchievementType parse(String s) {
        if (s == null) return null;
        try { return valueOf(s.trim().toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException e) { return null; }
    }
    public boolean counted() { return this == KILL_MOB || this == KILL_PLAYER || this == BREAK_BLOCK || this == FISH || this == PLAYTIME_MINUTES || this == BOSS_KILL; }
    public boolean state() { return this == ELEMENTER_UNLOCK || this == NGUYENTO_UNLOCK || this == PERMISSION; }
}
