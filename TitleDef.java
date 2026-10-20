package vn.astrax.titles.model;

import java.util.Map;

/**
 * Một danh hiệu (titles.yml).
 * filter: giới hạn mục tiêu (vd ZOMBIE, DIAMOND_ORE) - null = bất kỳ.
 * buffs: tên attribute (vd max_health) -> giá trị.
 */
public record TitleDef(String id, String display, String description, Rarity rarity, String icon, AchievementType type,
                       long target, String filter, Map<String, BuffValue> buffs, boolean announce, boolean hidden) {
    public TitleDef { buffs = buffs == null ? Map.of() : Map.copyOf(buffs); target = Math.max(1, target); }
    public boolean matches(String key) { return filter == null || filter.isBlank() || filter.equalsIgnoreCase(key); }
}
