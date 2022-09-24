package me.michqql.ranksystem.guis;

import me.michqql.ranksystem.permissions.PermissionsManager;
import me.michqql.servercoreutils.gui.Gui;
import me.michqql.servercoreutils.gui.GuiHandler;
import me.michqql.servercoreutils.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class PermissionFinderGui extends Gui {

    private final static int LEFT_SLOT = 48;
    private final static int FILTER_SLOT = 49;
    private final static int RIGHT_SLOT = 50;

    private final Consumer<String> permissionConsumer;

    // Filter variables
    private final HashMap<Filter, InventoryPane> filterPanes = new HashMap<>();
    private Filter currentFilter = Filter.ALPHABETICAL;
    private final StringPrompt searchPrompt;

    // Page variables
    private int pageNumber;

    public PermissionFinderGui(GuiHandler guiHandler, Player player, Consumer<String> permissionConsumer) {
        super(guiHandler, player);
        this.permissionConsumer = permissionConsumer;
        this.searchPrompt = new StringPrompt() {
            @NotNull
            @Override
            public String getPromptText(@NotNull ConversationContext context) {
                return ChatColor.GRAY + "Type " + ChatColor.RED + "\"-stop\"" + ChatColor.GRAY + " to cancel search";
            }

            @Nullable
            @Override
            public Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
                InventoryPane pane = filterPanes.get(currentFilter);
                if(pane instanceof SearchInventoryPane searchInventoryPane) {
                    searchInventoryPane.setSearchInput(input);
                    pane.updatePane(0);
                }
                guiHandler.openSavedGuis(player);
                return null;
            }
        };
        build("&2Find permission", 6);
    }

    @Override
    protected void createInventory() {
        this.filterPanes.put(Filter.SEARCH, new SearchInventoryPane(inventory));
        this.filterPanes.put(Filter.PLUGIN, new PluginInventoryPane(inventory));
        this.filterPanes.put(Filter.ALPHABETICAL, new AlphabeticInventoryPane(inventory));
        updateInventory();
    }

    @Override
    protected void updateInventory() {
        clearSlots();

        // Set filter item
        this.inventory.setItem(FILTER_SLOT, new ItemBuilder(Material.HOPPER)
                .displayName("&7Filter by &f" + currentFilter.title).lore(currentFilter.description).getItem());

        InventoryPane pane = filterPanes.get(currentFilter);
        if(pane != null) {
            int page = 0;
            if(pane.requiresPages()) {
                page = this.pageNumber;

                if(pageNumber > 0) {
                    this.inventory.setItem(LEFT_SLOT, new ItemBuilder(Material.ARROW)
                            .displayName("&7Page left").getItem());
                }

                if(pageNumber < pane.getRequiredPages() - 1) {
                    this.inventory.setItem(RIGHT_SLOT, new ItemBuilder(Material.ARROW)
                            .displayName("&7Page right").getItem());
                }
            }

            pane.updatePane(page);
        }
    }

    @Override
    protected void onClose() {

    }

    @Override
    protected boolean onClickEvent(int slot, ClickType clickType) {
        InventoryPane pane = filterPanes.get(currentFilter);
        if(pane == null)
            return true;

        if(slot == FILTER_SLOT) {
            // Special case - search
            if(clickType == ClickType.RIGHT && currentFilter == Filter.SEARCH) {
                // Start search
                player.sendMessage(ChatColor.GRAY + "Please enter a search query");

                Conversation conversation = new ConversationFactory(plugin)
                        .withEscapeSequence("-stop")
                        .addConversationAbandonedListener(abandonedEvent -> {
                            if(!abandonedEvent.gracefulExit()) {
                                player.sendMessage(ChatColor.RED + "Search query input cancelled");
                                guiHandler.openSavedGuis(player);
                            }
                        }).withLocalEcho(false)
                        .withFirstPrompt(searchPrompt)
                        .buildConversation(player);

                guiHandler.saveGuisAndClose(player);
                conversation.begin();
                return true;
            }

            this.currentFilter = switch (currentFilter) {
                case ALPHABETICAL -> Filter.PLUGIN;
                case PLUGIN -> Filter.SEARCH;
                case SEARCH -> Filter.ALPHABETICAL;
            };
            updateInventory();
            return true;
        } else if(slot == LEFT_SLOT) {
            if(pane.requiresPages() && pageNumber > 0) {
                pageNumber--;
                updateInventory();
            }
            return true;
        } else if(slot == RIGHT_SLOT) {
            if(pane.requiresPages() && pageNumber < pane.getRequiredPages() - 1) {
                pageNumber++;
                updateInventory();
            }
            return true;
        }


        pane.onClick(0, slot, clickType);
        return true;
    }

    @Override
    protected boolean onPlayerInventoryClickEvent(int i, ClickType clickType) {
        return true;
    }

    private enum Filter {
        SEARCH("&2Search", List.of("&7Search for specific permissions", "&eRight-Click &7to input search")),
        PLUGIN("&2Plugin", List.of("&7Lists the permissions by plugin")),
        ALPHABETICAL("&2Alphabetical", List.of("&7Lists the permissions in alphabetical order")),
        ;

        private final String title;
        private final List<String> description;

        Filter(String title, List<String> description) {
            this.title = title;
            this.description = description;
        }
    }

    private abstract static class InventoryPane {
        protected final static int MIN = 0, MAX = 44, PER_PAGE = 45;

        private final Inventory inventory;

        public InventoryPane(Inventory inventory) {
            this.inventory = inventory;
        }

        public void setItem(int slot, ItemStack item) {
            if(slot < MIN || slot > MAX)
                return;

            this.inventory.setItem(slot, item);
        }

        public abstract void updatePane(int page);
        public abstract void onClick(int page, int slot, ClickType type);
        public abstract boolean requiresPages();
        public abstract int getRequiredPages();
    }

    private class SearchInventoryPane extends InventoryPane {
        private static final int ERROR_SLOT = 22;

        private String searchInput;
        private List<PermissionsManager.PermissionInfo> searchResult;

        public SearchInventoryPane(Inventory inventory) {
            super(inventory);
        }

        public void setSearchInput(String searchInput) {
            this.searchInput = searchInput;
            this.searchResult = new ArrayList<>(PermissionsManager.getPermissionsBySearch(searchInput));
        }

        public boolean hasSearchInput() {
            return searchInput != null && !searchInput.isBlank();
        }

        public boolean hasSearchResults() {
            return searchResult != null && !searchResult.isEmpty();
        }

        @Override
        public void updatePane(int page) {
            if(!hasSearchInput()) {
                setItem(ERROR_SLOT, new ItemBuilder(Material.BARRIER)
                        .displayName("&cNo search input").getItem());
                return;
            }

            if(!hasSearchResults()) {
                setItem(ERROR_SLOT, new ItemBuilder(Material.BARRIER)
                        .displayName("&cNo results found").getItem());
                return;
            }

            int start = page * PER_PAGE;
            for(int i = 0; i < PER_PAGE; i++) {
                int index = start + i;
                if(index >= searchResult.size())
                    break;

                PermissionsManager.PermissionInfo info = searchResult.get(index);
                setItem(i, new ItemBuilder(Material.PAPER)
                        .displayName("&a" + info.getPermission()).getItem());
            }
        }

        @Override
        public void onClick(int page, int slot, ClickType type) {
            if(hasSearchInput() && hasSearchResults()) {
                int index = page * PER_PAGE + slot;
                if(index >= searchResult.size())
                    return;

                PermissionsManager.PermissionInfo info = searchResult.get(index);
                permissionConsumer.accept(info.getPermission());
            }
        }

        @Override
        public boolean requiresPages() {
            return searchResult != null && searchResult.size() > PER_PAGE;
        }

        @Override
        public int getRequiredPages() {
            return (int) Math.ceil((double) searchResult.size() / PER_PAGE);
        }
    }

    private class PluginInventoryPane extends InventoryPane {
        private static final int ERROR_SLOT = 22;
        private static final int CURRENT_PLUGIN_SLOT = 4;
        private static final int START_SLOT = 9;
        private static final int PER_PERM_PAGE = PER_PAGE - START_SLOT;

        private final List<Plugin> plugins;
        private Plugin currentPlugin;
        private List<PermissionsManager.PermissionInfo> pluginPermissions;

        public PluginInventoryPane(Inventory inventory) {
            super(inventory);

            // Get a list of all active plugins on the server
            this.plugins = Arrays.asList(Bukkit.getPluginManager().getPlugins());
            // Sort the plugins into alphabetical order
            plugins.sort(Comparator.comparing(Plugin::getName));

        }

        public void setCurrentPlugin(Plugin currentPlugin) {
            this.currentPlugin = currentPlugin;
            if(currentPlugin == null) {
                this.pluginPermissions = null;
            } else {
                this.pluginPermissions = new ArrayList<>(PermissionsManager.getPermissionsByPlugin(currentPlugin));
                this.pluginPermissions.sort(Comparator.comparing(PermissionsManager.PermissionInfo::getPermission));
            }
        }

        @Override
        public void updatePane(int page) {
            if(currentPlugin == null) {
                // Display a list of all plugins
                int start = page * PER_PAGE;
                for(int i = 0; i < PER_PAGE; i++) {
                    int index = start + i;
                    if(index >= plugins.size())
                        break;

                    Plugin plugin = plugins.get(index);
                    setItem(i, new ItemBuilder(Material.GRASS_BLOCK)
                            .displayName("&a" + plugin.getName()).getItem());
                }
            } else {
                setItem(CURRENT_PLUGIN_SLOT, new ItemBuilder(Material.GRASS_BLOCK)
                        .displayName("&2" + currentPlugin.getName())
                        .lore("&eClick &7to go back to all plugins").getItem());

                if(pluginPermissions.isEmpty()) {
                    setItem(ERROR_SLOT, new ItemBuilder(Material.BARRIER)
                            .displayName("&cNo permissions found").getItem());
                } else {
                    // Display a list of all the plugin's permissions
                    int start = page * PER_PERM_PAGE;
                    for (int i = 0; i < PER_PERM_PAGE; i++) {
                        int index = start + i;
                        if (index >= pluginPermissions.size())
                            break;

                        PermissionsManager.PermissionInfo info = pluginPermissions.get(index);
                        setItem(START_SLOT + i, new ItemBuilder(Material.PAPER)
                                .displayName("&a" + info.getPermission()).getItem());
                    }
                }
            }
        }

        @Override
        public void onClick(int page, int slot, ClickType type) {
            if(currentPlugin == null) {
                int index = page * PER_PAGE + slot;
                if(index >= plugins.size())
                    return;

                setCurrentPlugin(plugins.get(index));
                updateInventory();
            } else {
                // Display a list of all the plugin's permissions
                if(slot == CURRENT_PLUGIN_SLOT) {
                    setCurrentPlugin(null);
                    updateInventory();
                    return;
                }

                int index = (page * PER_PERM_PAGE) + (slot - START_SLOT);
                if(index >= pluginPermissions.size())
                    return;

                PermissionsManager.PermissionInfo info = pluginPermissions.get(index);
                permissionConsumer.accept(info.getPermission());
            }
        }

        @Override
        public boolean requiresPages() {
            if(currentPlugin == null) {
                return plugins.size() > PER_PAGE;
            } else {
                return pluginPermissions.size() > PER_PERM_PAGE;
            }
        }

        @Override
        public int getRequiredPages() {
            if(currentPlugin == null) {
                return (int) Math.ceil((double) plugins.size() / PER_PAGE);
            } else {
                return (int) Math.ceil((double) pluginPermissions.size() / PER_PERM_PAGE);
            }
        }
    }

    private class AlphabeticInventoryPane extends InventoryPane {
        private static final int ERROR_SLOT = 22;

        private final List<PermissionsManager.PermissionInfo> permissionInfo;

        public AlphabeticInventoryPane(Inventory inventory) {
            super(inventory);
            this.permissionInfo = new ArrayList<>(PermissionsManager.getAllPermissionInfo());
            // Sort alphabetically
            permissionInfo.sort(Comparator.comparing(PermissionsManager.PermissionInfo::getPermission));
        }

        @Override
        public void updatePane(int page) {
            if(permissionInfo.isEmpty()) {
                setItem(ERROR_SLOT, new ItemBuilder(Material.BARRIER)
                        .displayName("&cCould not find any permissions").getItem());
                return;
            }

            int start = page * PER_PAGE;
            for(int i = 0; i < PER_PAGE; i++) {
                int index = start + i;
                if(index >= permissionInfo.size())
                    break;

                PermissionsManager.PermissionInfo info = permissionInfo.get(index);
                List<String> lore = new ArrayList<>();
                lore.add("&7Possible source plugins:");
                if(info.hasPossibleSourcePlugins()) {
                    for (Plugin plugin : info.getPossibleSourcePlugins()) {
                        lore.add("&7 - " + plugin.getName());
                    }
                } else {
                    lore.add("&c - No plugin matches found");
                }
                setItem(i, new ItemBuilder(Material.PAPER).displayName("&a" + info.getPermission())
                        .lore(lore).getItem());
            }
        }

        @Override
        public void onClick(int page, int slot, ClickType type) {
            if(!permissionInfo.isEmpty()) {
                int index = page * PER_PAGE + slot;
                if(index >= permissionInfo.size())
                    return;

                PermissionsManager.PermissionInfo info = permissionInfo.get(index);
                permissionConsumer.accept(info.getPermission());
            }
        }

        @Override
        public boolean requiresPages() {
            return permissionInfo.size() > PER_PAGE;
        }

        @Override
        public int getRequiredPages() {
            return (int) Math.ceil((double) permissionInfo.size() / PER_PAGE);
        }
    }
}
