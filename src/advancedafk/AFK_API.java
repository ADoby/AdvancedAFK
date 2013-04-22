package advancedafk;

import org.bukkit.ChatColor;
//import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class AFK_API {
	
	public AdvancedAFK owner;
	
	//Two variables which store the Strings of config entries which have to been checked
	//if they are correct format
	String[] isInt = {"Afk.MAX_AFK_TIME_MESSAGE",
			"Kick.MAX_AFK_TIME_KICK",
			"Logging.MAX_LOGGED_LOCATIONS",
			"Logging.MAX_LOGGED_ACTIONS",
			"Logging.MAX_INTERACT",
			"Logging.MAX_INTERACT_ENTITY", 
			"Logging.MAX_ITEM_HELD_CHANGE",
			"Logging.MAX_BED_LEAVE",
			"Logging.MAX_TOGGLE_SNEAK",
			"Logging.MAX_DROP_ITEM",
			"Logging.MAX_PICKUP_ITEM",
			"Logging.MAX_CHAT",
			"Logging.MAX_MOVE",
			"Logging.MAX_INVENTORY_CLICK"};
	
	String[] isBool = {"Afk.Enabled",
			"Kick.Enabled"};
	
	public AFK_API(AdvancedAFK owner){
		this.owner = owner;
	}
	
	//This method sets/unsets a player to afk
	public void setAfk(Player player, boolean set){
		if(set){
			owner.isAFK.add(player);
			AFK_Watcher.time.put(player, AdvancedAFK.MAX_AFK_TIME_MESSAGE * 20);
			if(AdvancedAFK.plugin.getConfig().getBoolean("Afk.ENABLE_MESSAGE")){
				player.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', AdvancedAFK.plugin.getConfig().getString("Afk.IsAfk").replace("%PLAYERNAME%", player.getName()).replace("%PLAYERDISP%", player.getDisplayName())));
			}
		}else{
			owner.isAFK.remove(player);
			if(AdvancedAFK.plugin.getConfig().getBoolean("Afk.ENABLE_MESSAGE")){
				player.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', AdvancedAFK.plugin.getConfig().getString("Afk.NoLongerAfk").replace("%PLAYERNAME%", player.getName()).replace("%PLAYERDISP%", player.getDisplayName())));
			}
		}
	}
	
	//Checks config entries if they have correct format like Integers or Booleans
	//Strings do not have to be checked
	public String CheckConfig(){
		for(int i=0;i<isInt.length;i++){
			if(!isInt(AdvancedAFK.plugin.getConfig().getString(isInt[i]))){
				return "ERROR: Error in config! Error at " + isInt[i] + ": " + AdvancedAFK.plugin.getConfig().getString(isInt[i]) + " is no number";
			}
		}
		for(int i=0;i<isBool.length;i++){
			if(!isBoolean(AdvancedAFK.plugin.getConfig().getString(isBool[i]))){
				return "ERROR: Error in config! Error at " + isBool[i] + ": " + AdvancedAFK.plugin.getConfig().getString(isBool[i]) + " is no boolean (true/false)";
			}
		}
		
		return "GOOD";
	}
	
	private boolean isBoolean(String input){
		try{
			Boolean.valueOf(input);
			return true;
		}catch(ClassCastException cE){
			return false;
		}
	}
	
	private boolean isInt(String input){
		try {
			Integer.parseInt(input);
			return true;
		} catch(NumberFormatException nFE) {
			return false;
		}
	}
	
	//Kicks the player and resets his loggs etc.
	public void kick(Player p){
		p.kickPlayer(ChatColor.translateAlternateColorCodes('&', AdvancedAFK.plugin.getConfig().getString("Kick.KickReason").replace("%PLAYERNAME%", p.getName()).replace("%PLAYERDISP%", p.getDisplayName())));
		owner.isAFK.remove(p);
		AFK_Watcher.time.remove(p);
		if(AdvancedAFK.plugin.getConfig().getBoolean("Afk.ENABLE_MESSAGE")){
			p.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', AdvancedAFK.plugin.getConfig().getString("Kick.KickReason").replace("%PLAYERNAME%", p.getName()).replace("%PLAYERDISP%", p.getDisplayName())));
		}
	}
	
	//Returns if a player is in a inventory atm
	public static boolean isInInventory(Player p){
		return AdvancedAFK.plugin.inInventory.contains(p);
	}
	
	//Returns if a player is afk atm
	public static boolean isAfk(Player p){
		if(AdvancedAFK.plugin.isAFK.contains(p)){
			return true;
		}else{
			return false;
		}
	}
     
	//Test method to try if plugin is installed, If you wanna use this
	//Same effect: getServer().getPluginManager().isPluginEnabled("Advanced AFK")
	public static boolean isInstalled(){
		return true;
	}
	 
}
