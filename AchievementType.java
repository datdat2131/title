package vn.astrax.titles.model;

/** Loại thành tựu. MANUAL = chỉ admin cấp; PERMISSION = có quyền astraxtitles.title.<id> là mở. */
public enum AchievementType {
    KILL_MOB, KILL_PLAYER, BREAK_BLOCK, FISH, PLAYTIME_MINUTES, MANUAL, PERMISSION;

    public static AchievementType parse(String s) {
        if (s == null) return MANUAL;
        try { return valueOf(s.trim().toUpperCase(java.util.Locale.ROOT)); } catch (IllegalArgumentException e) { return null; }
    }
    /** Loại có bộ đếm tiến độ. */
    public boolean counted() { return this != MANUAL && this != PERMISSION; }
}
