package vn.astrax.titles.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import vn.astrax.titles.model.*;

import java.io.File;
import java.util.*;

/** Đọc titles.yml. Mục lỗi bị bỏ qua kèm cảnh báo, không làm sập plugin. */
public final class TitleLoader {
    private final JavaPlugin plugin;
    public TitleLoader(JavaPlugin plugin) { this.plugin = plugin; }

    public void load(TitleRegistry reg) {
        reg.clear();
        File f = new File(plugin.getDataFolder(), "titles.yml");
        if (!f.exists()) plugin.saveResource("titles.yml", false);
        ConfigurationSection root = YamlConfiguration.loadConfiguration(f).getConfigurationSection("titles");
        if (root == null) { plugin.getLogger().warning("titles.yml không có mục 'titles'."); return; }
        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) continue;
            try {
                AchievementType type = AchievementType.parse(s.getString("type", "MANUAL"));
                if (type == null) throw new IllegalArgumentException("type không hợp lệ: " + s.getString("type"));
                Map<String, BuffValue> buffs = new LinkedHashMap<>();
                ConfigurationSection bs = s.getConfigurationSection("buffs");
                if (bs != null) for (String k : bs.getKeys(false)) {
                    try { buffs.put(normalize(k), BuffValue.parse(bs.get(k))); }
                    catch (RuntimeException e) { plugin.getLogger().warning("titles.yml: " + id + " buff " + k + " lỗi: " + e.getMessage()); }
                }
                reg.register(new TitleDef(id, s.getString("display", id), s.getString("description", ""), Rarity.parse(s.getString("rarity")),
                        s.getString("icon", "NAME_TAG"), type, s.getLong("target", 1), s.getString("filter"), buffs,
                        s.getBoolean("announce", false), s.getBoolean("hidden", false)));
            } catch (RuntimeException e) { plugin.getLogger().warning("titles.yml: bỏ qua '" + id + "': " + e.getMessage()); }
        }
    }
    /** "generic.max_health" / "MAX_HEALTH" / "max_health" -> "max_health". */
    static String normalize(String k) {
        String s = k.trim().toLowerCase(Locale.ROOT);
        if (s.startsWith("generic.")) s = s.substring(8);
        if (s.startsWith("minecraft:")) s = s.substring(10);
        return s;
    }
}
