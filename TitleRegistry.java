package vn.astrax.titles.model;

import java.util.*;

/** Danh sách danh hiệu + chỉ mục theo loại thành tựu (để xử lý sự kiện nhanh, không lặp toàn bộ). */
public final class TitleRegistry {
    private final Map<String, TitleDef> byId = new LinkedHashMap<>();
    private final Map<AchievementType, List<TitleDef>> byType = new EnumMap<>(AchievementType.class);

    public void clear() { byId.clear(); byType.clear(); }
    public void register(TitleDef d) {
        byId.put(d.id().toLowerCase(Locale.ROOT), d);
        byType.computeIfAbsent(d.type(), k -> new ArrayList<>()).add(d);
    }
    public TitleDef get(String id) { return id == null ? null : byId.get(id.toLowerCase(Locale.ROOT)); }
    public Collection<TitleDef> all() { return Collections.unmodifiableCollection(byId.values()); }
    public List<TitleDef> ofType(AchievementType t) { return byType.getOrDefault(t, List.of()); }
    public int size() { return byId.size(); }

    /**
     * Ghi nhận tiến độ cho sự kiện (type,key). Trả về các danh hiệu vừa mở khóa.
     */
    public List<TitleDef> progress(PlayerTitleData data, AchievementType type, String key, long amount) {
        List<TitleDef> list = ofType(type);
        if (list.isEmpty()) return List.of();
        List<TitleDef> unlockedNow = null;
        for (TitleDef d : list) {
            if (data.has(d.id()) || !d.matches(key)) continue;
            long total = data.addProgress(d.id(), amount);
            if (total >= d.target() && data.unlock(d.id())) {
                if (unlockedNow == null) unlockedNow = new ArrayList<>();
                unlockedNow.add(d);
            }
        }
        return unlockedNow == null ? List.of() : unlockedNow;
    }
}
