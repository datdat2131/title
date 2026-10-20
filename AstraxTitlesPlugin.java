package vn.astrax.titles;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import vn.astrax.titles.buff.BuffManager;
import vn.astrax.titles.commands.TitleAdminCommand;
import vn.astrax.titles.commands.TitleCommand;
import vn.astrax.titles.config.TitleLoader;
import vn.astrax.titles.gui.TitleGui;
import vn.astrax.titles.hook.PapiHook;
import vn.astrax.titles.listener.ChatListener;
import vn.astrax.titles.listener.ProgressListener;
import vn.astrax.titles.model.*;
import vn.astrax.titles.storage.DataStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class AstraxTitlesPlugin extends JavaPlugin {
    public static final LegacyComponentSerializer AMP = LegacyComponentSerializer.legacyAmpersand();
    private final TitleRegistry registry = new TitleRegistry();
    private final Map<UUID, PlayerTitleData> cache = new ConcurrentHashMap<>();
    private DataStore store; private BuffManager buffs; private TitleGui gui;
    private int autosaveTask = -1, playtimeTask = -1;

    @Override public void onEnable() {
        saveDefaultConfig();
        store = new DataStore(getDataFolder(), getLogger());
        buffs = new BuffManager(this); gui = new TitleGui(this);
        reloadAll();
        var pm = getServer().getPluginManager();
        pm.registerEvents(new ProgressListener(this), this); pm.registerEvents(new ChatListener(this), this); pm.registerEvents(gui, this);
        var tc = new TitleCommand(this); var ac = new TitleAdminCommand(this);
        Objects.requireNonNull(getCommand("title")).setExecutor(tc); getCommand("title").setTabCompleter(tc);
        Objects.requireNonNull(getCommand("titleadmin")).setExecutor(ac); getCommand("titleadmin").setTabCompleter(ac);
        for (Player p : Bukkit.getOnlinePlayers()) loadBlocking(p);
        long every = Math.max(1, getConfig().getInt("autosave-minutes", 5)) * 1200L;
        autosaveTask = Bukkit.getScheduler().runTaskTimer(this, () -> { for (PlayerTitleData d : cache.values()) if (d.dirty()) store.saveAsync(d); }, every, every).getTaskId();
        playtimeTask = Bukkit.getScheduler().runTaskTimer(this, this::tickPlaytime, 1200L, 1200L).getTaskId();
        if (pm.isPluginEnabled("PlaceholderAPI")) { new PapiHook(this).register(); getLogger().info("Đã đăng ký PlaceholderAPI (%astraxtitles_*%)."); }
        getLogger().info("AstraxTitles bật: " + registry.size() + " danh hiệu.");
    }

    @Override public void onDisable() {
        if (autosaveTask != -1) Bukkit.getScheduler().cancelTask(autosaveTask);
        if (playtimeTask != -1) Bukkit.getScheduler().cancelTask(playtimeTask);
        for (Player p : Bukkit.getOnlinePlayers()) buffs.remove(p);
        for (PlayerTitleData d : cache.values()) store.saveAsync(d);
        cache.clear(); store.close();
    }

    public void reloadAll() {
        reloadConfig(); new TitleLoader(this).load(registry);
        for (TitleDef t : registry.all()) for (String a : t.buffs().keySet())
            if (!buffs.validAttribute(a)) getLogger().warning("titles.yml: '" + t.id() + "' dùng attribute không tồn tại: " + a);
        for (Player p : Bukkit.getOnlinePlayers()) { PlayerTitleData d = cache.get(p.getUniqueId()); if (d != null && d.selected() != null && registry.get(d.selected()) == null) d.select(null); buffs.apply(p); refreshTab(p); }
    }

    /** Playtime: +1 phút cho mỗi người chơi online. */
    private void tickPlaytime() {
        for (Player p : Bukkit.getOnlinePlayers()) progress(p, AchievementType.PLAYTIME_MINUTES, "MINUTE", 1);
    }

    public TitleRegistry titles() { return registry; }
    public BuffManager buffs() { return buffs; }
    public DataStore store() { return store; }
    public TitleGui gui() { return gui; }
    public PlayerTitleData data(UUID id) { return cache.get(id); }
    public void putData(PlayerTitleData d) { cache.put(d.owner(), d); }
    public PlayerTitleData removeData(UUID id) { return cache.remove(id); }

    private void loadBlocking(Player p) {
        try { PlayerTitleData d = store.loadAsync(p.getUniqueId()).get(10, TimeUnit.SECONDS); cache.put(p.getUniqueId(), d == null ? new PlayerTitleData(p.getUniqueId()) : d); buffs.apply(p); }
        catch (Exception e) { getLogger().severe("Không tải được dữ liệu " + p.getName() + ": " + e); }
    }

    /** Ghi nhận tiến độ + thông báo mở khóa. Gọi trên main thread. */
    public void progress(Player p, AchievementType type, String key, long amount) {
        PlayerTitleData d = cache.get(p.getUniqueId()); if (d == null) return;
        for (TitleDef t : registry.progress(d, type, key, amount)) onUnlocked(p, t);
    }

    public void onUnlocked(Player p, TitleDef t) {
        Component name = AMP.deserialize(t.display());
        p.sendMessage(Component.text("§a✦ Mở khóa danh hiệu: ").append(name).append(Component.text(" §7(/title để đeo)")));
        p.showTitle(Title.title(Component.text("§6§lTHÀNH TỰU"), name));
        p.playSound(p.getLocation(), t.rarity().atLeast(Rarity.LEGENDARY) ? Sound.UI_TOAST_CHALLENGE_COMPLETE : Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        Rarity min = Rarity.parse(getConfig().getString("announce-min-rarity", "LEGENDARY"));
        if (t.announce() || t.rarity().atLeast(min))
            Bukkit.broadcast(Component.text("§e" + p.getName() + " §7vừa đạt danh hiệu ").append(name).append(Component.text("§7!")));
        if (d(p) != null && d(p).selected() == null && getConfig().getBoolean("auto-equip-first", true)) equip(p, t.id());
    }
    private PlayerTitleData d(Player p) { return cache.get(p.getUniqueId()); }

    public boolean equip(Player p, String id) {
        PlayerTitleData d = cache.get(p.getUniqueId()); if (d == null || !d.select(id)) return false;
        buffs.apply(p); refreshTab(p); return true;
    }
    public void unequip(Player p) { PlayerTitleData d = cache.get(p.getUniqueId()); if (d != null) { d.select(null); buffs.apply(p); refreshTab(p); } }

    /** Prefix dạng "[Title] " (Component rỗng nếu không đeo). */
    public Component prefix(UUID id) {
        PlayerTitleData d = cache.get(id); TitleDef t = d == null ? null : registry.get(d.selected());
        if (t == null) return Component.empty();
        return AMP.deserialize(getConfig().getString("chat.title-format", "&8[%title%&8] ").replace("%title%", t.display()));
    }
    public void refreshTab(Player p) {
        if (!getConfig().getBoolean("tab.enabled", false)) return;
        p.playerListName(prefix(p.getUniqueId()).append(Component.text(p.getName())));
    }
}
