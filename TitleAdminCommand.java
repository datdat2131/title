package vn.astrax.titles.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import vn.astrax.titles.AstraxTitlesPlugin;
import vn.astrax.titles.model.*;

import java.util.*;
import java.util.function.Consumer;

public final class TitleAdminCommand implements CommandExecutor, TabCompleter {
    private final AstraxTitlesPlugin plugin;
    public TitleAdminCommand(AstraxTitlesPlugin plugin) { this.plugin = plugin; }

    @Override public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!s.hasPermission("astraxtitles.admin")) { s.sendMessage("§cBạn không có quyền."); return true; }
        if (a.length == 0) { s.sendMessage("§e/titleadmin §7reload | list | give <p> <id> | take <p> <id> | progress <p> <id> <số>"); return true; }
        switch (a[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> { plugin.reloadAll(); s.sendMessage("§aĐã tải lại: " + plugin.titles().size() + " danh hiệu."); }
            case "list" -> s.sendMessage("§7Danh hiệu: §f" + String.join(", ", plugin.titles().all().stream().map(TitleDef::id).toList()));
            case "give" -> { if (a.length < 3) return usage(s, "give <player> <id>"); TitleDef t = need(s, a[2]); if (t == null) return true;
                edit(s, a[1], d -> d.unlock(t.id()), p -> plugin.onUnlocked(p, t), "Đã cấp " + t.id()); }
            case "take" -> { if (a.length < 3) return usage(s, "take <player> <id>"); TitleDef t = need(s, a[2]); if (t == null) return true;
                edit(s, a[1], d -> d.lock(t.id()), p -> { plugin.buffs().apply(p); plugin.refreshTab(p); }, "Đã thu hồi " + t.id()); }
            case "progress" -> { if (a.length < 4) return usage(s, "progress <player> <id> <số>"); TitleDef t = need(s, a[2]); if (t == null) return true;
                long n; try { n = Long.parseLong(a[3]); } catch (NumberFormatException e) { s.sendMessage("§cSố không hợp lệ."); return true; }
                edit(s, a[1], d -> { d.setProgress(t.id(), n); if (n >= t.target()) d.unlock(t.id()); }, null, "Đã đặt tiến độ " + t.id() + " = " + n); }
            default -> s.sendMessage("§cLệnh không hợp lệ.");
        }
        return true;
    }
    private boolean usage(CommandSender s, String u) { s.sendMessage("§eDùng: /titleadmin " + u); return true; }
    private TitleDef need(CommandSender s, String id) { TitleDef t = plugin.titles().get(id); if (t == null) s.sendMessage("§cKhông có danh hiệu '" + id + "'."); return t; }

    /** Online: sửa cache; offline: đọc-sửa-ghi trên thread IO. */
    private void edit(CommandSender s, String name, Consumer<PlayerTitleData> change, Consumer<Player> afterOnline, String ok) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) { PlayerTitleData d = plugin.data(online.getUniqueId()); if (d == null) { s.sendMessage("§cDữ liệu chưa tải."); return; }
            change.accept(d); d.markDirty(); plugin.store().saveAsync(d); if (afterOnline != null) afterOnline.accept(online); s.sendMessage("§a" + ok + " cho " + name); return; }
        OfflinePlayer off = Bukkit.getOfflinePlayerIfCached(name);
        if (off == null) { s.sendMessage("§cKhông tìm thấy người chơi."); return; }
        plugin.store().modifyOffline(off.getUniqueId(), change).thenAccept(r -> Bukkit.getScheduler().runTask(plugin, () -> s.sendMessage(r ? "§a" + ok + " cho " + name + " §7(offline)" : "§cNgười chơi chưa có dữ liệu.")));
    }
    @Override public List<String> onTabComplete(CommandSender s, Command c, String l, String[] a) {
        if (a.length == 1) return List.of("reload", "list", "give", "take", "progress");
        if (a.length == 2 && a[0].matches("(?i)give|take|progress")) return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        if (a.length == 3) return plugin.titles().all().stream().map(TitleDef::id).toList();
        return List.of();
    }
}
