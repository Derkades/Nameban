package xyz.derkades.Nameban;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import xyz.derkades.derkutils.bukkit.Colors;

public class BanCommand extends Command {

	public BanCommand() {
		super("nameban");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length != 1){
			sender.sendMessage(new ComponentBuilder("Use /nameban [name] to ban a player name. If the name specified banned, it will be unbanned. Use /nameban list to get a list of banned names.").color(ChatColor.RED).create());
			return;
		}
		
		if (!sender.hasPermission("nameban.ban")){
			return;
		}
		
		List<String> bannedNames;
		if (Nameban.INSTANCE.config.contains("banned")){
			bannedNames = Nameban.INSTANCE.config.getStringList("banned");
		} else {
			bannedNames = new ArrayList<>();
		}
		
		if (args[0].equals("list")){
			for (String bannedName : bannedNames){
				sender.sendMessage(new TextComponent("- " + bannedName));
			}
		} else {
			String bannedName = args[0];
			
			if (Nameban.isBanned(bannedNames, bannedName)){
				//Name is banned, unban
				bannedNames.remove(bannedName);
				Nameban.INSTANCE.config.set("banned", bannedNames); //Update string list in config
				
				sender.sendMessage(new ComponentBuilder(bannedName + " has been removed from the banned names list.").color(ChatColor.GREEN).create());
				
				//Save config async
				Nameban.INSTANCE.getProxy().getScheduler().runAsync(Nameban.INSTANCE, () -> {
					Nameban.INSTANCE.saveConfig();
				});
			} else {
				//Name is not already banned, ban now
				bannedNames.add(bannedName);
				Nameban.INSTANCE.config.set("banned", bannedNames); //Update string list in config
				
				ProxiedPlayer player = ProxyServer.getInstance().getPlayer(bannedName);
				if (player != null){ //If a player with this name exists
					String disconnectMessage = Colors.parseColors(Nameban.INSTANCE.config.getString("ban-message"));
					player.disconnect(TextComponent.fromLegacyText(disconnectMessage));
				}
				
				sender.sendMessage(new ComponentBuilder("Players with the name " + bannedName + " can no longer join the server.").color(ChatColor.GOLD).create());
				
				//Save config async
				Nameban.INSTANCE.getProxy().getScheduler().runAsync(Nameban.INSTANCE, () -> {
					Nameban.INSTANCE.saveConfig();
				});
				
				BaseComponent[] notifyMessage = new ComponentBuilder("")
						.append(bannedName).color(ChatColor.AQUA).bold(true)
						.append(" has been namebanned by ").color(ChatColor.DARK_AQUA).bold(true)
						.append(sender.getName()).color(ChatColor.AQUA).bold(true)
						.append(".").color(ChatColor.DARK_AQUA).bold(true)
						.create();
				
				for (ProxiedPlayer online : ProxyServer.getInstance().getPlayers()) {
					if (
							( !(sender instanceof ProxiedPlayer) || !((ProxiedPlayer) sender).getUniqueId().equals(online.getUniqueId()) ) 
							&& online.hasPermission("nameban.notify"))
						
						online.sendMessage(notifyMessage);
				}
			}
		}
	}

}
