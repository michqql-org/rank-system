package me.michqql.ranksystem.commands.ranks;

import me.michqql.ranksystem.players.PlayerData;
import me.michqql.ranksystem.players.PlayerManager;
import me.michqql.ranksystem.ranks.Rank;
import me.michqql.ranksystem.ranks.RankManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand implements CommandExecutor {

    private final RankManager rankManager;
    private final PlayerManager playerManager;

    public RankCommand(RankManager rankManager, PlayerManager playerManager) {
        this.rankManager = rankManager;
        this.playerManager = playerManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            // Send list of players ranks
            if(sender instanceof Player player) {
                PlayerData data = playerManager.getPlayerData(player.getUniqueId());
                if(data == null) {
                    player.sendMessage(ChatColor.RED + "You have no player data!");
                } else {
                    player.sendMessage(ChatColor.DARK_GREEN + "Listing player ranks:");
                    data.getPlayerRanks().forEach(playerRank ->
                            player.sendMessage(ChatColor.GREEN + "> " + playerRank.getRank().getRankId()));
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You must be a player to have ranks!");
            }
            return true;
        }

        String subCommand = args[0];
        if(subCommand.equalsIgnoreCase("create")) {
            if(args.length <= 1) {
                sender.sendMessage(ChatColor.RED + "Please specify an id");
            } else {
                String id = args[1];
                if(rankManager.doesRankExist(id)) {
                    sender.sendMessage(ChatColor.RED + "A rank with id " + id + " already exists!");
                } else {
                    rankManager.createRank(id);
                    sender.sendMessage(ChatColor.GREEN + "Created a new rank with id " + id);
                }
            }
            return true;
        }

        if(subCommand.equalsIgnoreCase("perms")) {
            if(args.length <= 1) {
                sender.sendMessage(ChatColor.RED + "Please specify a rank id");
            } else {
                String id = args[1];
                Rank rank = rankManager.getRankById(id);
                if(rank == null) {
                    sender.sendMessage(ChatColor.RED + "A rank with id " + id + " doesn't exist!");
                } else {
                    sender.sendMessage(ChatColor.DARK_GREEN + "Listing " + rank.getRankId() + "'s permissions:");
                    rank.getPermissions().forEach(s ->
                            sender.sendMessage(ChatColor.GREEN + "> " + s));
                }
            }
            return true;
        }

        if(subCommand.equalsIgnoreCase("test")) {
            if(sender instanceof Player player) {
                if(args.length <= 1) {
                    sender.sendMessage(ChatColor.RED + "Please specify a permission to test");
                } else {
                    String permission = args[1];
                    PlayerData data = playerManager.getPlayerData(player.getUniqueId());

                    if(data == null) {
                        player.sendMessage(ChatColor.RED + "You have no player data!");
                    } else {
                        boolean has = player.hasPermission(permission);
                        player.sendMessage(ChatColor.GRAY + "Has permission: " + (has ? ChatColor.GREEN : ChatColor.RED) + has);
                    }
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You must be a player to have ranks!");
            }
            return true;
        }
        return false;
    }

    public void createRank(CommandSender sender, String identifier) {
        // Check if the command already exists
        if(rankManager.doesRankExist(identifier)) {
            sender.sendMessage(ChatColor.RED + "A rank with id " + identifier + " already exists!");
            return;
        }

        // Otherwise, create the command and save it
        rankManager.createRank(identifier);
        sender.sendMessage(ChatColor.GREEN + "Created a new rank with id " + identifier);
    }

    public void testPermission(Player player, String permission) {
        boolean has = player.hasPermission(permission);
        player.sendMessage(ChatColor.GRAY + "Has permission: " + (has ? ChatColor.GREEN : ChatColor.RED) + has);
    }
}
