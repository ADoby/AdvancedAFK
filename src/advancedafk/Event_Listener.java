package advancedafk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
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

	public AFK_Functions functions;
	
	static HashMap<Player, List<Location>> lastLocations = new HashMap<Player, List<Location>>();
	static HashMap<Player,Integer> lastAction = new HashMap<Player,Integer>();
	static HashMap<Player,Integer> stepCounts = new HashMap<Player, Integer>();
	static HashMap<Player,Integer> actionCounts = new HashMap<Player, Integer>();
	static HashMap<Player,Location> lastLocation = new HashMap<Player, Location>();
	
	public Event_Listener(AFK_Functions functions){
		this.functions = functions;
	}
	
	//#############################
	//Variable Description
	//AFK_Watcher.time.get(Player): The time the player did not do anything/something different, his afk-time
	//lastLocations.get(Player): The last locations/blocks the player walked by
	//lastAction.get(Player): The last action the player did (Like chatting, interacting, building etc.)
	//stepCounts.get(Player): How many times the player walks over the same locations
	//lastLocation.get(Player): The last location the player was
	//#############################
	
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
		
		//Not complicated logging means if a player does something new hes NOT AFK
		//so if lastAction is not this action, hes not afk
		//But if he does one action more then "MAX" (see config) times, he IS AFK
		if(lastAction.get(p) != action){
			//Hes not AFK, different action
			lastAction.put(p, action);
				
			//Set his afk-time to 0
			AFK_Watcher.time.put(p, 0);
			if(AFK_Functions.isAfk(p)){
				//If hes afk, set him to NOT afk
				functions.afk(p, false);
			}
			
			//Set his actionCounter to 0 because he did this action the first time
			actionCounts.put(p, 0);
		}else{
			//He is doing this action more then 1 time now
			
			//actionCount can not be higher then MAX_LOGGED_ACTIONS
			//This makes it possible to let an Event reset the AFK-Time but not be counted as AFK-Action
			//e.g. MOVING around
			if(actionCounts.get(p) < AdvancedAFK.MAX_LOGGED_ACTIONS){
				//count times he is doing this action without doing something else
				actionCounts.put(p, actionCounts.get(p)+1);
			}
		
			if(actionCounts.get(p) > AdvancedAFK.MAX_TIMES[action]){
				//He did this action more then "MAX" (see config) times he could be afk
				actionCounts.put(p, 0);
				if(AdvancedAFK.AFK_ENABLED && AdvancedAFK.ENABLE_MESSAGES){
					p.sendMessage("[Advanced-AFK] " + AdvancedAFK.MESSAGE_BEFORE + AdvancedAFK.messages[action]);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		//If player joining the server set default entries and add Lists to Maps
		//If you do not do this, you will get null-pointer or it just won't work
		AFK_Watcher.time.put(e.getPlayer(), 0);
		lastLocations.put(e.getPlayer(),new ArrayList<Location>());
		stepCounts.put(e.getPlayer(),0);
		lastAction.put(e.getPlayer(),0);
		actionCounts.put(e.getPlayer(),0);
		lastLocation.put(e.getPlayer(),e.getPlayer().getLocation());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e){
		//If player leaves server delete him from lists
		AFK_Watcher.time.remove(e.getPlayer());
		lastLocations.remove(e.getPlayer());
		stepCounts.remove(e.getPlayer());
		lastAction.remove(e.getPlayer());
		actionCounts.remove(e.getPlayer());
		lastLocation.remove(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave2(PlayerKickEvent e){
		//Seems like this could be if player leaves because of kick or something
		AFK_Watcher.time.remove(e.getPlayer());
		lastLocations.remove(e.getPlayer());
		stepCounts.remove(e.getPlayer());
		lastAction.remove(e.getPlayer());
		actionCounts.remove(e.getPlayer());
		lastLocation.remove(e.getPlayer());
	}
	
	//#############################################################################
	//This method logs movement
	//It saves the last locations the player was and if he walks the same way
	//over and over, it will warn and not reset his afk-time
	//#############################################################################
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e){		

		//Get Block Location beneath player
		Location blockLoc = e.getPlayer().getLocation().getBlock().getLocation();
		
		if(!lastLocation.get(e.getPlayer()).equals(blockLoc)){
			//Its another location then the last time checking
			lastLocation.put(e.getPlayer(), blockLoc);
			//new last Location

			//If this location has not been logged yet save it
			if(!lastLocations.get(e.getPlayer()).contains(blockLoc)){
				lastLocations.get(e.getPlayer()).add(blockLoc);
				
				//Log action, hes moving :P
				logAction(e.getPlayer(),AdvancedAFK.ACTION_MOVE);
				
				//This location has not been logged yet = hes NOT AFK
				AFK_Watcher.time.put(e.getPlayer(), 0);
				if(AFK_Functions.isAfk(e.getPlayer())){
					functions.afk(e.getPlayer(), false);
				}
				
				//Set his stepCount to 0
				stepCounts.put(e.getPlayer(),0);
			}else{
				//Player did walk over this block in the last "MAX_LOGGED_LOCATIONS"
				//but this could be false alarm, because hey, you do walk over the same blocks some times, don't you :D
				//So set his stepCount +1, if he now walks "MAX_LOGGED_LOCATIONS"-times over the same blocks again, hes probably afk
				stepCounts.put(e.getPlayer(),stepCounts.get(e.getPlayer())+1);
				
				if(stepCounts.get(e.getPlayer()) >= AdvancedAFK.MAX_LOGGED_LOCATIONS){
					//He may be afk, talk to the player :P
					stepCounts.put(e.getPlayer(),0);
					if(AdvancedAFK.AFK_ENABLED && AdvancedAFK.ENABLE_MESSAGES){
						e.getPlayer().sendMessage("[Advanced-AFK] " + AdvancedAFK.MESSAGE_BEFORE + AdvancedAFK.messages[AdvancedAFK.ACTION_MOVE]);
					}
				}
			}
			
			//If loggedLocations is higher then MAX_LOGGED_LOCATIONS delete first
			if(lastLocations.get(e.getPlayer()).size() > AdvancedAFK.MAX_LOGGED_LOCATIONS){
				lastLocations.get(e.getPlayer()).remove(0);
			}
		}

	}
	
	//Events that will be called by bukkit
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e){
		logAction(e.getPlayer(),AdvancedAFK.ACTION_CHAT);		
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e){
		AdvancedAFK.setPlayerInInv((Player)e.getPlayer(),true);
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e){
		AdvancedAFK.setPlayerInInv((Player)e.getPlayer(),false);
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
