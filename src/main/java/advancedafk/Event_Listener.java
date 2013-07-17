package advancedafk;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class Event_Listener implements Listener{

	public AFK_API functions;
	
	public static HashMap<Player, PlayerData> playerData = new HashMap<Player, PlayerData>();
	
	public Event_Listener(AFK_API functions){
		this.functions = functions;
	}
	
	//#############################################################
	//If an action is performed this method is called
	//If logging for this action is enabled it will log this action
	//If the player did not make to much actions of this type set his afk time to 0 (Hes NOT AFK)
	//#############################################################
	private void logAction(Player p, int action){
		if(AdvancedAFK.MAX_TIMES[action] >= AdvancedAFK.MAX_LOGGED_ACTIONS || AdvancedAFK.MAX_TIMES[action] < 0){
			//Logging for this action is disabled
			return;
		}
		
		PlayerData data = playerData.get(p);
		
		//Not complicated logging means if a player does something new hes NOT AFK
		//so if lastAction is not this action, hes not afk
		//But if he does one action more then "MAX" (see config) times, he IS AFK
		if (data.getLastAction() != action){
			//Hes not AFK, different action
			data.setLastAction(action);
				
			//Set his afk-time to 0
			data.setTime(0);
			if(data.isAfk()){
				//If hes afk, set him to NOT afk
				functions.setAfk(data, false);
			}
			
			//Set his actionCounter to 0 because he did this action the first time
			data.setActionCounts(0);
		}else{
			//He is doing this action more then 1 time now
			
			//actionCount can not be higher then MAX_LOGGED_ACTIONS
			//This makes it possible to let an Event reset the AFK-Time but not be counted as AFK-Action
			//e.g. MOVING around
			if(data.getActionCounts() < AdvancedAFK.MAX_LOGGED_ACTIONS){
				//count times he is doing this action without doing something else
			    data.setActionCounts(data.getActionCounts() + 1);
			}
		
			if(data.getActionCounts() > AdvancedAFK.MAX_TIMES[action]){
				//He did this action more then "MAX" (see config) times he could be afk
			    data.setActionCounts(0);
				if(AdvancedAFK.AFK_ENABLED && AdvancedAFK.ENABLE_MESSAGES){
					p.sendMessage("[Advanced-AFK] " + AdvancedAFK.MESSAGE_BEFORE + AdvancedAFK.messages[action]);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
        AdvancedAFK.handleLogin(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e){
		//If player leaves server delete him from lists
        playerData.remove(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent e){
		//Seems like this could be if player leaves because of kick or something
        playerData.remove(e.getPlayer());
	}
	
	//#############################################################################
	//This method logs movement
	//It saves the last locations the player was and if he walks the same way
	//over and over, it will warn and not reset his afk-time
	//#############################################################################
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e){		

		if(e == null){
			Bukkit.getLogger().info("Event Null");
			return;
		}
		if(e.getPlayer() == null){
			Bukkit.getLogger().info("Player Null");
			return;
		}
        
        PlayerData data = playerData.get(e.getPlayer());
		//Get Block Location beneath player
		Location blockLoc = e.getPlayer().getLocation().getBlock().getLocation();
		
		if(!data.getLastLocation().equals(blockLoc)){
			//Its another location then the last time checking
		    data.setLastLocation(blockLoc);
			//new last Location

			//If this location has not been logged yet save it
			if (!data.touchLastLocations(blockLoc)) {
				data.addLastLocations(blockLoc);
				
				//Log action, hes moving :P
				logAction(e.getPlayer(),AdvancedAFK.ACTION_MOVE);
				
				//This location has not been logged yet = hes NOT AFK
				data.setTime(0);
				if(data.isAfk()){
					functions.setAfk(data, false);
				}
				
				//Set his stepCount to 0
				data.setStepCounts(0);
			} else {
				//Player did walk over this block in the last "MAX_LOGGED_LOCATIONS"
				//but this could be false alarm, because hey, you do walk over the same blocks some times, don't you :D
				//So set his stepCount +1, if he now walks "MAX_LOGGED_LOCATIONS"-times over the same blocks again, hes probably afk
				data.setStepCounts(data.getStepCounts() + 1);
				if(data.getStepCounts() >= AdvancedAFK.MAX_LOGGED_LOCATIONS){
					//He may be afk, talk to the player :P
				    data.setStepCounts(0);
					if(AdvancedAFK.AFK_ENABLED && AdvancedAFK.ENABLE_MESSAGES){
						e.getPlayer().sendMessage("[Advanced-AFK] " + AdvancedAFK.MESSAGE_BEFORE + AdvancedAFK.messages[AdvancedAFK.ACTION_MOVE]);
					}
				}
			}
		}

	}
	
	//Events that will be called by bukkit
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e){
		logAction(e.getPlayer(),AdvancedAFK.ACTION_CHAT);		
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e){
		if(e.getWhoClicked() instanceof Player){
			//Player clicked something in Inventory
			logAction((Player)e.getWhoClicked(),AdvancedAFK.ACTION_INVENTORY_CLICK);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e){
		logAction(e.getPlayer(),AdvancedAFK.ACTION_INTERACT);
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent e){
		logAction(e.getPlayer(),AdvancedAFK.ACTION_INTERACT_ENTITY);
	}
	
	@EventHandler
	public void onItemHeldChange(PlayerItemHeldEvent e){
		logAction(e.getPlayer(),AdvancedAFK.ACTION_ITEM_HELD_CHANGE);
	}
	
	@EventHandler
	public void onPlayerBedLeave(PlayerBedLeaveEvent e){
		logAction(e.getPlayer(),AdvancedAFK.ACTION_BED_LEAVE);
	}
	
	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent e){
		logAction(e.getPlayer(),AdvancedAFK.ACTION_TOGGLE_SNEAK);
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e){
		logAction(e.getPlayer(),AdvancedAFK.ACTION_DROP_ITEM);
	}
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent e){
		logAction(e.getPlayer(),AdvancedAFK.ACTION_PICKUP_ITEM);
	}
	
}
