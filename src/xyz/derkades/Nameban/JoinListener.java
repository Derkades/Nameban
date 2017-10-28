package xyz.derkades.Nameban;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import xyz.derkades.derkutils.bukkit.Colors;

public class JoinListener implements Listener {
	
	@EventHandler
	public void onLogin(LoginEvent event){
		List<String> bannedNames;
		if (Nameban.INSTANCE.config.contains("banned")){
			bannedNames = Nameban.INSTANCE.config.getStringList("banned");
		} else {
			bannedNames = new ArrayList<>();
		}

		if (Nameban.isBanned(bannedNames, event.getConnection().getName())){
			Nameban.INSTANCE.getLogger().info(String.format("%s's connection has been cancelled, because their name is in the banned names list.", event.getConnection().getName()));
			String disconnectMessage = Colors.parseColors(Nameban.INSTANCE.config.getString("ban-message"));
			event.setCancelReason(TextComponent.fromLegacyText(disconnectMessage));
			event.setCancelled(true);
		}
	}

}
