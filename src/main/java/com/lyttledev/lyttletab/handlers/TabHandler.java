package com.lyttledev.lyttletab.handlers;

import com.lyttledev.lyttletab.LyttleTab;
import com.lyttledev.lyttletab.types.Configs;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lyttledev.lyttleutils.utils.entity.Player.getDisplayName;

public class TabHandler implements Listener {
    public static LyttleTab plugin;

    private List<String> animatedHeaders;
    private List<String> animatedFooters;
    private int tabListRefreshInterval;
    private int tabListAnimationInterval;
    private AtomicInteger headerIndex = new AtomicInteger(0);
    private AtomicInteger footerIndex = new AtomicInteger(0);

    private int headerTaskId = -1;
    private int footerTaskId = -1;
    private int refreshTaskId = -1;

    private boolean sortingEnabled = true;
    private SortEngine sortEngine = new SortEngine();

    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    public TabHandler(LyttleTab plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        TabHandler.plugin = plugin;
        loadConfig();
        startTasks();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        refreshTabList();
    }

    public void refreshTabList() {
        Collection<? extends Player> online = plugin.getServer().getOnlinePlayers();
        if (online.isEmpty()) return;

        List<Player> players = new ArrayList<>(online);

        if (sortingEnabled && !sortEngine.rules.isEmpty()) {
            sortEngine.prepare(players);
            players.sort(sortEngine.comparator());
        }

        String header = animatedHeaders.isEmpty() ? "" : animatedHeaders.get(headerIndex.get());
        String footer = animatedFooters.isEmpty() ? "" : animatedFooters.get(footerIndex.get());

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            applyTabVisuals(player, header, footer);
            player.setPlayerListOrder(i);
        }
    }

    private void advanceHeader() {
        if (animatedHeaders.isEmpty()) return;
        headerIndex.updateAndGet(i -> (i + 1) % animatedHeaders.size());
    }

    private void advanceFooter() {
        if (animatedFooters.isEmpty()) return;
        footerIndex.updateAndGet(i -> (i + 1) % animatedFooters.size());
    }

    private void applyTabVisuals(Player player, String header, String footer) {
        Replacements replacements = new Replacements.Builder()
                .add("<NAME>", getDisplayName(player))
                .build();

        String playerName = (String) plugin.config.tab.get("tab_player_name");
        player.playerListName(plugin.message.getMessageRaw(playerName, replacements, player));

        if (!header.isEmpty()) {
            player.sendPlayerListHeader(plugin.message.getMessageRaw(header, player));
        }
        if (!footer.isEmpty()) {
            player.sendPlayerListFooter(plugin.message.getMessageRaw(footer, player));
        }
    }

    public void reload() {
        stopTasks();
        loadConfig();
        resetIndexes();
        startTasks();
        refreshTabList();
    }

    private void loadConfig() {
        Configs config = plugin.config;

        List<String> loadedHeaders = config.tab.getStringList("tab_list_header");
        List<String> loadedFooters = config.tab.getStringList("tab_list_footer");

        this.animatedHeaders = (loadedHeaders != null) ? new ArrayList<>(loadedHeaders) : new ArrayList<>();
        this.animatedFooters = (loadedFooters != null) ? new ArrayList<>(loadedFooters) : new ArrayList<>();

        this.tabListRefreshInterval = config.tab.getInt("tab_list_refresh_interval");
        this.tabListAnimationInterval = config.tab.getInt("tab_list_animation_interval");

        this.sortingEnabled = getBooleanSafe(config, "sorting.enabled", true);
        this.sortEngine = new SortEngine();

        List<Map<?, ?>> ruleMaps = config.tab.getMapList("sorting.rules");
        if (ruleMaps != null) {
            for (Map<?, ?> rawRule : ruleMaps) {
                if (rawRule == null) continue;
                String type = toStringSafe(rawRule.get("type")).toLowerCase(Locale.ROOT).trim();
                switch (type) {
                    case "placeholder": {
                        String placeholder = toStringSafe(rawRule.get("placeholder"));
                        List<String> order = toStringList(rawRule.get("order")).reversed();;
                        if (!placeholder.isEmpty()) {
                            sortEngine.rules.add(new PlaceholderOrderRule(placeholder, order));
                        }
                        break;
                    }
                    case "group": {
                        List<String> order = toStringList(rawRule.get("order")).reversed();;
                        sortEngine.rules.add(new GroupOrderRule(order));
                        break;
                    }
                    case "placeholder_a_to_z": {
                        String placeholder = toStringSafe(rawRule.get("placeholder"));
                        String direction = toStringSafe(rawRule.get("order"));
                        boolean asc = !"desc".equalsIgnoreCase(direction);
                        if (!placeholder.isEmpty()) {
                            sortEngine.rules.add(new PlaceholderAlphaRule(placeholder, asc));
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    }

    private void startTasks() {
        if (!animatedHeaders.isEmpty()) {
            headerTaskId = Bukkit.getScheduler().runTaskTimer(
                    plugin, this::advanceHeader, 0L, Math.max(1, tabListAnimationInterval) * 20L
            ).getTaskId();
        }
        if (!animatedFooters.isEmpty()) {
            footerTaskId = Bukkit.getScheduler().runTaskTimer(
                    plugin, this::advanceFooter, 0L, Math.max(1, tabListAnimationInterval) * 20L
            ).getTaskId();
        }
        refreshTaskId = Bukkit.getScheduler().runTaskTimer(
                plugin, this::refreshTabList, 0L, Math.max(1, tabListRefreshInterval) * 20L
        ).getTaskId();
    }

    private void stopTasks() {
        if (headerTaskId != -1) Bukkit.getScheduler().cancelTask(headerTaskId);
        if (footerTaskId != -1) Bukkit.getScheduler().cancelTask(footerTaskId);
        if (refreshTaskId != -1) Bukkit.getScheduler().cancelTask(refreshTaskId);
        headerTaskId = -1;
        footerTaskId = -1;
        refreshTaskId = -1;
    }

    private void resetIndexes() {
        headerIndex.set(0);
        footerIndex.set(0);
    }

    private static boolean getBooleanSafe(Configs config, String path, boolean def) {
        try {
            Object v = config.tab.get(path);
            return (v instanceof Boolean) ? (Boolean) v : def;
        } catch (Exception e) {
            return def;
        }
    }

    private static String toStringSafe(Object o) {
        return (o == null) ? "" : String.valueOf(o);
    }

    @SuppressWarnings("unchecked")
    private static List<String> toStringList(Object o) {
        if (o == null) return Collections.emptyList();
        if (o instanceof List<?>) {
            List<?> list = (List<?>) o;
            List<String> out = new ArrayList<>(list.size());
            for (Object e : list) {
                if (e != null) out.add(String.valueOf(e));
            }
            return out;
        }
        return Collections.singletonList(String.valueOf(o));
    }

    // --- Sorting engine (extensible by rule type) ---
    private static final class SortEngine {
        private final List<SortRule> rules = new ArrayList<>();

        void prepare(List<Player> players) {
            for (SortRule rule : rules) {
                rule.prepare(players);
            }
        }

        Comparator<Player> comparator() {
            if (rules.isEmpty())
                return Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER);
            return (a, b) -> {
                for (SortRule rule : rules) {
                    int c = rule.compare(a, b);
                    if (c != 0) return c;
                }
                return String.CASE_INSENSITIVE_ORDER.compare(a.getName().toLowerCase(Locale.ROOT), b.getName().toLowerCase(Locale.ROOT));
            };
        }
    }

    private interface SortRule {
        void prepare(List<Player> players);

        int compare(Player a, Player b);

        String describe();
    }

    /**
     * Explicit order: first in the order list is highest priority (lowest index).
     * Players matching the first entry are sorted to the top.
     * All value and placeholder comparisons are case-insensitive.
     */
    private final class PlaceholderOrderRule implements SortRule {
        private final String placeholder;
        private final List<String> explicitOrder;
        private final Map<String, Integer> orderIndex;
        private final Map<UUID, String> values = new HashMap<>();

        private PlaceholderOrderRule(String placeholder, List<String> order) {
            this.placeholder = placeholder.toLowerCase(Locale.ROOT); // force lower case
            this.explicitOrder = (order == null) ? Collections.emptyList() : new ArrayList<>();
            this.orderIndex = new HashMap<>();
            if (order != null) {
                for (int i = 0; i < order.size(); i++) {
                    String val = order.get(i) == null ? "" : order.get(i).toLowerCase(Locale.ROOT);
                    this.explicitOrder.add(val);
                    orderIndex.put(val, i);
                }
            }
        }

        @Override
        public void prepare(List<Player> players) {
            values.clear();
            for (Player p : players) {
                String v = resolvePlaceholderAsString(placeholder, p);
                values.put(p.getUniqueId(), v == null ? "" : v.toLowerCase(Locale.ROOT));
            }
        }

        @Override
        public int compare(Player a, Player b) {
            String va = values.getOrDefault(a.getUniqueId(), "");
            String vb = values.getOrDefault(b.getUniqueId(), "");

            Integer ia = lookupIndex(va);
            Integer ib = lookupIndex(vb);

            if (!ia.equals(ib)) {
                // Lower index = higher priority/top
                return Integer.compare(ia, ib);
            }
            return va.compareTo(vb);
        }

        private Integer lookupIndex(String v) {
            if (v == null) return explicitOrder.size();
            Integer idx = orderIndex.get(v);
            return (idx != null) ? idx : explicitOrder.size();
        }

        @Override
        public String describe() {
            return "placeholder(type=order, placeholder=" + placeholder + ", order=" + explicitOrder + ")";
        }
    }

    private static final class GroupOrderRule implements SortRule {
        private final List<String> explicitOrder;
        private final Map<String, Integer> orderIndex;
        private final Map<UUID, List<String>> playerGroups = new HashMap<>();

        private GroupOrderRule(List<String> order) {
            this.explicitOrder = (order == null) ? Collections.emptyList() : new ArrayList<>();
            this.orderIndex = new HashMap<>();
            if (order != null) {
                for (int i = 0; i < order.size(); i++) {
                    String val = order.get(i) == null ? "" : order.get(i).toLowerCase(Locale.ROOT);
                    this.explicitOrder.add(val);
                    orderIndex.put(val, i);
                }
            }
        }

        @Override
        public void prepare(List<Player> players) {
            playerGroups.clear();
            for (Player p : players) {
                List<String> groups = extractGroups(p);
                List<String> lowerGroups = new ArrayList<>(groups.size());
                for (String g : groups) {
                    lowerGroups.add(g.toLowerCase(Locale.ROOT));
                }
                playerGroups.put(p.getUniqueId(), lowerGroups);
            }
        }

        @Override
        public int compare(Player a, Player b) {
            int ia = firstMatchIndex(playerGroups.getOrDefault(a.getUniqueId(), Collections.emptyList()));
            int ib = firstMatchIndex(playerGroups.getOrDefault(b.getUniqueId(), Collections.emptyList()));
            if (ia != ib) return Integer.compare(ia, ib);

            String ga = firstGroupName(playerGroups.get(a.getUniqueId()));
            String gb = firstGroupName(playerGroups.get(b.getUniqueId()));
            return ga.compareTo(gb);
        }

        private int firstMatchIndex(List<String> groups) {
            if (groups == null || groups.isEmpty()) return explicitOrder.size();
            for (String g : groups) {
                Integer idx = orderIndex.get(g);
                if (idx != null) return idx;
            }
            return explicitOrder.size();
        }

        private static String firstGroupName(List<String> groups) {
            if (groups == null || groups.isEmpty()) return "";
            return groups.get(0);
        }

        private static List<String> extractGroups(Player p) {
            Set<String> groups = new LinkedHashSet<>();
            for (PermissionAttachmentInfo pai : p.getEffectivePermissions()) {
                String perm = pai.getPermission();
                if (perm == null) continue;
                String lower = perm.toLowerCase(Locale.ROOT);
                if (lower.startsWith("group.") && lower.length() > 6) {
                    groups.add(lower.substring("group.".length()));
                } else if (lower.startsWith("luckperms.group.") && lower.length() > "luckperms.group.".length()) {
                    groups.add(lower.substring("luckperms.group.".length()));
                }
            }
            return new ArrayList<>(groups);
        }

        @Override
        public String describe() {
            return "group(order=" + explicitOrder + ")";
        }
    }

    private final class PlaceholderAlphaRule implements SortRule {
        private final String placeholder;
        private final boolean asc;
        private final Map<UUID, String> values = new HashMap<>();

        private PlaceholderAlphaRule(String placeholder, boolean asc) {
            this.placeholder = placeholder.toLowerCase(Locale.ROOT);
            this.asc = asc;
        }

        @Override
        public void prepare(List<Player> players) {
            values.clear();
            for (Player p : players) {
                String v;
                if ("%player%".equalsIgnoreCase(placeholder.trim())) {
                    v = p.getName().toLowerCase(Locale.ROOT);
                } else {
                    v = resolvePlaceholderAsString(placeholder, p);
                    if (v != null && v.trim().equalsIgnoreCase(placeholder.trim())) {
                        v = "";
                    } else if (v != null) {
                        v = v.toLowerCase(Locale.ROOT);
                    }
                }
                values.put(p.getUniqueId(), v == null ? "" : v);
            }
        }

        @Override
        public int compare(Player a, Player b) {
            String va = values.getOrDefault(a.getUniqueId(), "");
            String vb = values.getOrDefault(b.getUniqueId(), "");
            boolean aEmpty = va == null || va.isEmpty();
            boolean bEmpty = vb == null || vb.isEmpty();
            if (aEmpty && bEmpty) return 0;
            if (aEmpty != bEmpty) return aEmpty ? 1 : -1;
            int cmp = va.compareTo(vb);
            return asc ? cmp : -cmp;
        }

        @Override
        public String describe() {
            return "placeholder_a_to_z(placeholder=" + placeholder + ", order=" + (asc ? "asc" : "desc") + ")";
        }
    }

    /**
     * Resolves a PAPI placeholder for a player, returns plain text, never null.
     * Always processes in lower case for case-insensitive comparison.
     */
    private String resolvePlaceholderAsString(String placeholder, Player player) {
        try {
            Component papiValue = plugin.message.getMessageRaw(placeholder, player);
            if (papiValue == null) return "";
            String out = PLAIN_SERIALIZER.serialize(papiValue).trim();
            if (out.equalsIgnoreCase(placeholder)) {
                return "";
            }
            return out.toLowerCase(Locale.ROOT);
        } catch (Exception e) {
            return "";
        }
    }
}