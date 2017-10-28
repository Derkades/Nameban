package xyz.derkades.Nameban;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import org.bstats.bungeecord.Metrics;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class Nameban extends Plugin {
	
	public static Nameban INSTANCE;
	
	public Configuration config;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		
		saveDefaultConfig();
		
		getProxy().getPluginManager().registerCommand(this, new BanCommand());
		getProxy().getPluginManager().registerListener(this, new JoinListener());
		
		getProxy().getScheduler().runAsync(this, () -> {
			loadConfig();
		});
		
		new Metrics(this);
	}
	
	public void saveConfig(){
		try {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "config.yml"));
		} catch (IOException e) {
			getLogger().severe("An error occured while trying to save config: " + e.getMessage());
		}
	}
	
	public void loadConfig(){
		try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
		} catch (IOException e){
			getLogger().severe("An error occured while trying to load config: " + e.getMessage());
		}
	}
	
	private void saveDefaultConfig(){
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}
	
	
	public static boolean isBanned(List<String> bannedNames, String name) {
		boolean contains = INSTANCE.config.getBoolean("contains", true);
		boolean caseSensitive = INSTANCE.config.getBoolean("case-sensitive", false);
		
		if (contains) {
			for (String bannedName : bannedNames) {
				if (caseSensitive && name.contains(bannedName)) {
					System.out.println(String.format("[nameban debug] %s is banned, name contains %s case sensitive", name, bannedName));
					return true;
				} else if (!caseSensitive && name.toLowerCase().contains(bannedName.toLowerCase())) {
					System.out.println(String.format("[nameban debug] %s is banned, name contains %s case insensitive", name, bannedName));
					return true;
				}
			}
		} else {
			for (String bannedName : bannedNames) {
				if (caseSensitive && name.equals(bannedName)) {
					System.out.println(String.format("[nameban debug] %s is banned, name equals %s case sensitive", name, bannedName));
					return true;
				} else if (!caseSensitive && name.equalsIgnoreCase(bannedName)) {
					System.out.println(String.format("[nameban debug] %s is banned, name equals %s case insensitive", name, bannedName));
					return true;
				}
			}
		}
		
		return false;
	}

}
