package advancedafk;

import java.util.ArrayList;
import java.util.List;

import me.edge209.afkTerminator.AfkDetect;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


public class AdvancedAFK extends JavaPlugin{
	
	//This Plugin
	public static AdvancedAFK plugin;
	
	//Permission
	public static IPermissionHandler permissions;
	
	//Boolean if AfkTerminator is installed
	public static boolean useAFKTerminator = false;
	
	//Default Messages before beneath
	public static String MESSAGE_BEFORE = "Start doing something else then ";
	
	//List of Messages which are send to player if hes going to be AFK
	//Can be set in config
	public static String[] messages = {"interacting",
											"interacting with entities",
											"changing your item in hand",
											"leaving the bed",
											"toggling sneak",
											"dropping items",
											"picking up items",
											"chatting",
											"moving",
											"clicking around in your inventory"};

	//List of Booleans if checking/logging an action is enabled
	//Can be set in config
	public static int[] MAX_TIMES = {90,
											90,
											20,
											10,
											30,
											30,
											30,
											30,
											100,
											100};
	
	//Just some kind of "enum". Each action has his own integer
	public static final int ACTION_INTERACT = 0;
	public static final int ACTION_INTERACT_ENTITY = 1;
	public static final int ACTION_ITEM_HELD_CHANGE = 2;
	public static final int ACTION_BED_LEAVE = 3;
	public static final int ACTION_TOGGLE_SNEAK = 4;
	public static final int ACTION_DROP_ITEM = 5;
	public static final int ACTION_PICKUP_ITEM = 6;
	public static final int ACTION_CHAT = 7;
	public static final int ACTION_MOVE = 8;
	public static final int ACTION_INVENTORY_CLICK = 8;
	
	//Default 
	//Can be set in config
	public static int MAX_LOGGED_LOCATIONS = 20;
	public static int MAX_LOGGED_ACTIONS = 20;

	public static int MAX_AFK_TIME_MESSAGE = 30;
	public static int MAX_AFK_TIME_KICK = 90;
	
	public static boolean AFK_ENABLED = true;
	public static boolean KICK_ENABLED = true;
	
	public static boolean ENABLE_MESSAGES = false;
	
	//End of Config Entries
	

	public AFK_API functions = new AFK_API(this);
	public Event_Listener pListener = new Event_Listener(functions);
	
	//A list which stores if a player is afk atm
	public List<Player> isAFK = new ArrayList<Player>();
	
	//A list which stores if a player is in Inventory
	public List<Player> inInventory = new ArrayList<Player>();
	
	//The plugin "onEnable" method, which is called when plugin loads
	@Override
	 public void onEnable() {
		 plugin = this;
		
		 getServer().getPluginManager().registerEvents(this.pListener, this);
		 
		 //If you update your config, this applies changes to users config
		 this.getConfig().options().copyDefaults(true);
		 this.saveConfig();
		 
		 reload();
		
	 }
	
	public void setPlayerInInv(Player p, boolean b){
		if(b && !inInventory.contains(p)){
			inInventory.add(p);
		}else if(inInventory.contains(p)){
			inInventory.remove(p);
		}
	}
	
	private void reload(){
		
		//Try to hook vault
		RegisteredServiceProvider<Permission> permissionsPlugin = null;  
        
        if (getServer().getPluginManager().isPluginEnabled("Vault"))
        {
            log("Vault detected. Using Vault.");
            
            permissionsPlugin = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        
            permissions = (IPermissionHandler)new PermissionHandlerWrapper(permissionsPlugin.getProvider());
        }
        else 
        {
            log("Vault not detected for permissions, defaulting to Bukkit Permissions");
            
            permissions = (IPermissionHandler)new MockPermissionHandler();
        }
		
		
		//Save default configs entries
		 
		 //Let the functions check this config for me
		 log("Checking Config..");
		 String result = functions.CheckConfig();
		 if(!result.equalsIgnoreCase("GOOD")){
			 //Problem in config, can't load plugin without it
			 log(result);
			 this.getPluginLoader().disablePlugin(this);
		 }else{
			 log("Config Okay!");
		 }
		 //Config is okay lets load things
		 
		 log("Loading Config..");
		 //Load Config Things
		 MAX_LOGGED_LOCATIONS = AdvancedAFK.plugin.getConfig().getInt("Logging.MAX_LOGGED_LOCATIONS");
		 MAX_LOGGED_ACTIONS = AdvancedAFK.plugin.getConfig().getInt("Logging.MAX_LOGGED_ACTIONS");
		 
		 //Load Enabled Logs
		 MAX_TIMES[ACTION_INTERACT] = AdvancedAFK.plugin.getConfig().getInt("Logging.MAX_INTERACT");
		 MAX_TIMES[ACTION_INTERACT_ENTITY] = AdvancedAFK.plugin.getConfig().getInt("Logging.MAX_INTERACT_ENTITY");
		 MAX_TIMES[ACTION_ITEM_HELD_CHANGE] = AdvancedAFK.plugin.getConfig().getInt("Logging.MAX_ITEM_HELD_CHANGE");
		 MAX_TIMES[ACTION_BED_LEAVE] = AdvancedAFK.plugin.getConfig().getInt("Logging.MAX_BED_LEAVE");
		 MAX_TIMES[ACTION_TOGGLE_SNEAK] = AdvancedAFK.plugin.getConfig().getInt("Logging.MAX_TOGGLE_SNEAK");
		 MAX_TIMES[ACTION_DROP_ITEM] = AdvancedAFK.plugin.getConfig().getInt("Logging.MAX_DROP_ITEM");
		 MAX_TIMES[ACTION_PICKUP_ITEM] = AdvancedAFK.plugin.getConfig().getInt("Logging.MAX_PICKUP_ITEM");
		 MAX_TIMES[ACTION_CHAT] = AdvancedAFK.plugin.getConfig().getInt("Logging.MAX_CHAT");
		 MAX_TIMES[ACTION_MOVE] = AdvancedAFK.plugin.getConfig().getInt("Logging.MAX_MOVE");
		 MAX_TIMES[ACTION_INVENTORY_CLICK] = AdvancedAFK.plugin.getConfig().getInt("Logging.MAX_INVENTORY_CLICK");
		 
		 MAX_AFK_TIME_MESSAGE = AdvancedAFK.plugin.getConfig().getInt("Afk.MAX_AFK_TIME_MESSAGE");
		 MAX_AFK_TIME_KICK = AdvancedAFK.plugin.getConfig().getInt("Kick.MAX_AFK_TIME_KICK");
		 
		 AFK_ENABLED = AdvancedAFK.plugin.getConfig().getBoolean("Afk.Enabled");
		 KICK_ENABLED = AdvancedAFK.plugin.getConfig().getBoolean("Kick.Enabled");
		 
		 ENABLE_MESSAGES = AdvancedAFK.plugin.getConfig().getBoolean("Logging.ENABLE_MESSAGES");
		 
		 //Load Message
		 MESSAGE_BEFORE = AdvancedAFK.plugin.getConfig().getString("Logging.MESSAGE_BEFORE") + " ";
		 
		//Load Messages
		 messages[ACTION_INTERACT] = AdvancedAFK.plugin.getConfig().getString("Logging.MESSAGE_INTERACT");
		 messages[ACTION_INTERACT_ENTITY] = AdvancedAFK.plugin.getConfig().getString("Logging.MESSAGE_INTERACT_ENTITY");
		 messages[ACTION_ITEM_HELD_CHANGE] = AdvancedAFK.plugin.getConfig().getString("Logging.MESSAGE_ITEM_HELD_CHANGE");
		 messages[ACTION_BED_LEAVE] = AdvancedAFK.plugin.getConfig().getString("Logging.MESSAGE_BED_LEAVE");
		 messages[ACTION_TOGGLE_SNEAK] = AdvancedAFK.plugin.getConfig().getString("Logging.MESSAGE_TOGGLE_SNEAK");
		 messages[ACTION_DROP_ITEM] = AdvancedAFK.plugin.getConfig().getString("Logging.MESSAGE_DROP_ITEM");
		 messages[ACTION_PICKUP_ITEM] = AdvancedAFK.plugin.getConfig().getString("Logging.MESSAGE_PICKUP_ITEM");
		 messages[ACTION_CHAT] = AdvancedAFK.plugin.getConfig().getString("Logging.MESSAGE_CHAT");
		 messages[ACTION_MOVE] = AdvancedAFK.plugin.getConfig().getString("Logging.MESSAGE_MOVE");
		 messages[ACTION_INVENTORY_CLICK] = AdvancedAFK.plugin.getConfig().getString("Logging.MESSAGE_INVENTORY_CLICK");
		 
		 //Lets look if AFK-Terminator is installed
		 try{
			AfkDetect.getAFKMachineStartTime("");
			useAFKTerminator = true;
			AdvancedAFK.log("AfkTerminator is installed, hooked");
		 }catch(NoClassDefFoundError NCDE){
			useAFKTerminator = false;
			AdvancedAFK.log("AfkTerminator not installed, please install to get Anti-AFK-Machines");
		 }catch(NullPointerException NPE){
			useAFKTerminator = false;
			AdvancedAFK.log("AfkTerminator not installed, please install to get Anti-AFK-Machines");
		 }
		 
		 //Start AFK Watcher which adds +1 to afk-time every tick
		 //Read AFK_Watcher comments
		 Bukkit.getServer().getScheduler().runTaskLater(plugin, new AFK_Watcher(functions), 1);
	}
	
	public static void log(String s){
		Bukkit.getLogger().info("["+plugin.getName()+"] " + s);
	}
	
	 @Override
	 public void onDisable(){
		 //Cancel our AFK_Watcher
		 Bukkit.getScheduler().cancelAllTasks();
	 }
	 
	 
	 
	 //This method is called if a player/or console uses a command
	 @Override
	 public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		 
		 //If command is /afk
		 if(commandLabel.equalsIgnoreCase("afk")){
			 //If command executer if a Player
			 if(sender instanceof Player){
				 Player player = (Player) sender;
				if(args.length == 1){
					if(args[0].equalsIgnoreCase("reload")){
						if(permissions.has(sender,"advancedafk.reload") || permissions.has(sender,"advancedafk.*")){
							 log("Reloading Plugin");
							 //Cancel our AFK_Watcher
							 Bukkit.getScheduler().cancelAllTasks();
							 reload();
						}
					}

					
					//If he tries to afk another player
					if(permissions.has(sender,"advancedafk.afk.other") || permissions.has(sender,"advancedafk.*")){
						for(Player p : getServer().getOnlinePlayers()){
							if(p.getName().contains(args[0]) || p.getDisplayName().contains(args[0])){
								if(AFK_API.isAfk(p)){
									player.sendMessage(ChatColor.RED + p.getDisplayName() + " is already set to AFK.");
							 	}else{
							 		if(!permissions.has(sender,"advancedafk.exempt.afk") || !permissions.has(sender,"advancedafk.*")){
							 			functions.setAfk(p, true);
							 			player.sendMessage(ChatColor.YELLOW + p.getDisplayName() + ChatColor.GREEN + " set to AFK");
							 		}else{
							 			player.sendMessage(ChatColor.YELLOW + p.getDisplayName() + ChatColor.RED + "Is exempt from SimpleAFK");
							 		}
							 	}
							}
						}
					}else{
						player.sendMessage(ChatColor.RED + "You don't have permission to do this.");
					}
				}else if(args.length > 1){
					player.sendMessage(ChatColor.RED + "Usage: /afk [player]");
				}else{
					//He wants to set himself to afk
					if(!permissions.has(sender,"advancedafk.exempt.afk") || !permissions.has(sender,"advancedafk.*")){
						if(permissions.has(sender,"advancedafk.afk.self") || permissions.has(sender,"advancedafk.*")){
							if(AFK_API.isAfk(player)){
								player.sendMessage(ChatColor.RED + "You are already set to AFK.");
							}else{
								functions.setAfk(player, true);
							}
						}else{
							player.sendMessage(ChatColor.RED + "You don't have permission to do this.");
						}
					}
				}
			 
				 	
			 }else{
				 if(args.length == 1){
					 if( args[0].equalsIgnoreCase("reload")){
						 log("Reloading Plugin");
						 //Cancel our AFK_Watcher
						 Bukkit.getScheduler().cancelAllTasks();
						 reload();
					 }
				 }
				 log("This command must be used in game.");
			 }
		 }
		 return true;
	 }
}
