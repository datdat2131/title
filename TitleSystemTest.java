package vn.astrax.titles;

import org.junit.jupiter.api.Test;
import vn.astrax.titles.model.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TitleSystemTest {
    static TitleDef def(String id, AchievementType t, long target, String filter, Rarity r) {
        return new TitleDef(id, "&a" + id, "", r, "NAME_TAG", t, target, filter, Map.of("max_health", new BuffValue(2, false)), false, false);
    }
    static TitleRegistry reg() {
        TitleRegistry r = new TitleRegistry();
        r.register(def("zombie", AchievementType.KILL_MOB, 3, "ZOMBIE", Rarity.COMMON));
        r.register(def("anymob", AchievementType.KILL_MOB, 5, null, Rarity.RARE));
        r.register(def("miner", AchievementType.BREAK_BLOCK, 2, null, Rarity.COMMON));
        r.register(def("vip", AchievementType.PERMISSION, 1, null, Rarity.EPIC));
        return r;
    }
    @Test void buffValueParsing() {
        assertEquals(4.0, BuffValue.parse("4").amount()); assertFalse(BuffValue.parse(4).percent());
        BuffValue p = BuffValue.parse("5%"); assertTrue(p.percent()); assertEquals(0.05, p.amount(), 1e-9);
        assertEquals("+5%", p.describe()); assertEquals("+4", BuffValue.parse("4").describe()); assertEquals("-2", BuffValue.parse("-2").describe());
        assertThrows(IllegalArgumentException.class, () -> BuffValue.parse("abc")); assertThrows(IllegalArgumentException.class, () -> BuffValue.parse(null));
    }
    @Test void typeAndRarityParsing() {
        assertEquals(AchievementType.KILL_MOB, AchievementType.parse("kill_mob")); assertNull(AchievementType.parse("nope")); assertEquals(AchievementType.MANUAL, AchievementType.parse(null));
        assertEquals(Rarity.COMMON, Rarity.parse("???")); assertTrue(Rarity.MYTHIC.atLeast(Rarity.LEGENDARY));
        assertTrue(AchievementType.FISH.counted()); assertFalse(AchievementType.MANUAL.counted());
    }
    @Test void filterMatching() { TitleDef z = def("z", AchievementType.KILL_MOB, 1, "zombie", Rarity.COMMON); assertTrue(z.matches("ZOMBIE")); assertFalse(z.matches("SPIDER")); assertTrue(def("a", AchievementType.KILL_MOB, 1, null, Rarity.COMMON).matches("X")); }
    @Test void progressUnlocksAtTargetOnlyOnce() {
        TitleRegistry r = reg(); PlayerTitleData d = new PlayerTitleData(UUID.randomUUID());
        assertTrue(r.progress(d, AchievementType.KILL_MOB, "ZOMBIE", 1).isEmpty()); assertTrue(r.progress(d, AchievementType.KILL_MOB, "ZOMBIE", 1).isEmpty());
        var got = r.progress(d, AchievementType.KILL_MOB, "ZOMBIE", 1);
        assertEquals(1, got.size()); assertEquals("zombie", got.get(0).id()); assertTrue(d.has("ZOMBIE"));
        assertTrue(r.progress(d, AchievementType.KILL_MOB, "ZOMBIE", 1).stream().noneMatch(t -> t.id().equals("zombie"))); // không mở lại
        assertEquals(4, d.progressOf("anymob")); // tất cả mob đều tính cho anymob
    }
    @Test void filterIgnoresOtherTargets() {
        TitleRegistry r = reg(); PlayerTitleData d = new PlayerTitleData(UUID.randomUUID());
        for (int i = 0; i < 10; i++) r.progress(d, AchievementType.KILL_MOB, "SPIDER", 1);
        assertEquals(0, d.progressOf("zombie")); assertTrue(d.has("anymob")); assertFalse(d.has("zombie"));
    }
    @Test void unlockReportedExactlyOnceAcrossCalls() {
        TitleRegistry r = reg(); PlayerTitleData d = new PlayerTitleData(UUID.randomUUID());
        int total = r.progress(d, AchievementType.BREAK_BLOCK, "STONE", 1).size() + r.progress(d, AchievementType.BREAK_BLOCK, "STONE", 1).size() + r.progress(d, AchievementType.BREAK_BLOCK, "STONE", 1).size();
        assertEquals(1, total); assertTrue(d.has("miner"));
    }
    @Test void selectOnlyUnlocked() {
        PlayerTitleData d = new PlayerTitleData(UUID.randomUUID());
        assertFalse(d.select("vip")); d.unlock("vip"); assertTrue(d.select("VIP")); assertEquals("vip", d.selected());
        assertTrue(d.lock("vip")); assertNull(d.selected()); // gỡ khóa thì tự bỏ chọn
        d.unlock("a"); d.select("a"); assertTrue(d.select(null)); assertNull(d.selected());
    }
    @Test void restoreIsCleanAndDropsInvalidSelection() {
        PlayerTitleData d = new PlayerTitleData(UUID.randomUUID());
        d.restore(List.of("A", "B"), Map.of("A", 5L), "C"); assertNull(d.selected()); assertFalse(d.dirty()); assertTrue(d.has("a")); assertEquals(5, d.progressOf("a"));
        d.restore(List.of("A"), Map.of(), "a"); assertEquals("a", d.selected());
    }
    @Test void copyIsDeepAndProgressNeverOverflows() {
        PlayerTitleData d = new PlayerTitleData(UUID.randomUUID()); d.unlock("x"); d.addProgress("x", 3); PlayerTitleData c = d.copy(); d.addProgress("x", 10); d.unlock("y");
        assertEquals(3, c.progressOf("x")); assertFalse(c.has("y"));
        d.addProgress("big", Long.MAX_VALUE); d.addProgress("big", 5); assertEquals(Long.MAX_VALUE, d.progressOf("big"));
        assertEquals(d.progressOf("x"), d.addProgress("x", -5)); // số âm bị bỏ qua
    }
    @Test void dirtyFlag() { PlayerTitleData d = new PlayerTitleData(UUID.randomUUID()); assertFalse(d.dirty()); d.unlock("a"); assertTrue(d.dirty()); d.clearDirty(); assertFalse(d.dirty()); }
}
