package advancedafk;

import java.util.HashMap;
import java.util.HashSet;

import me.edge209.afkTerminator.AfkDetect;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AFK_Watcher implements Runnable{
	
	static HashMap<Player, Integer> time = new HashMap<Player, Integer>();
	public AFK_API functions;
	
	private int hookTimer = 0;
	private boolean loaded = false;
	
	public AFK_Watcher(AFK_API functions){
		this.functions = functions;
	}
	
	public static void reload(){
		
		time = new HashMap<Player, Integer>();
	}
	
	@Override
	public void run() {
		if(!loaded){
			hookTimer ++;
			if(hookTimer > 20){
				functions.owner.loadPluginConnection();
				loaded = true;
			}
		}

		HashSet<Player> h = new HashSet<Player>();
		h.addAll(time.keySet());
		for(Player p : h){
			if(!AdvancedAFK.permissions.has(p,"advancedafk.*")){
				int doubleIt = 1;
				if(AdvancedAFK.permissions.has(p,"advancedafk.lagging")){
					doubleIt = 2;
				}
				time.put(p, time.get(p) + 1);
				try{
					if(!AdvancedAFK.permissions.has(p,"advancedafk.exempt.afk") && time.get(p) == AdvancedAFK.MAX_AFK_TIME_MESSAGE * 20*doubleIt && AdvancedAFK.AFK_ENABLED){
						functions.setAfk(p, true);
					}else if(!AdvancedAFK.permissions.has(p,"advancedafk.exempt.kick") && time.get(p) == AdvancedAFK.MAX_AFK_TIME_KICK * 20*doubleIt && AdvancedAFK.KICK_ENABLED){
						functions.kick(p);
					}else{
						if(!AdvancedAFK.permissions.has(p,"advancedafk.exempt.afk") && getAFKMachineStartTime(p.getName()) >= AdvancedAFK.MAX_AFK_TIME_MESSAGE * 20*doubleIt && AdvancedAFK.AFK_ENABLED){
							//He is AFK
							functions.setAfk(p, true);
						}else if(!AdvancedAFK.permissions.has(p,"advancedafk.exempt.kick") && getAFKMachineStartTime(p.getName()) >= AdvancedAFK.MAX_AFK_TIME_KICK * 20*doubleIt && AdvancedAFK.KICK_ENABLED){
							//Hes to long AFK
							functions.kick(p);
						}
					}
				}catch (Exception e){
					AdvancedAFK.log("[AdvancedAFK] Error in Watcher");
					AdvancedAFK.log(e.getMessage());
				}
			}
		}
		if(AdvancedAFK.plugin.isEnabled()){
			Bukkit.getServer().getScheduler().runTaskLater(AdvancedAFK.plugin, this, 1);
		}
	}
	
	public static long getAFKMachineStartTime(String playerName) {
		try{
			return(AfkDetect.getAFKMachineStartTime(playerName));
		}catch(NoClassDefFoundError NCDE){
			return 0;
		}catch(NullPointerException NPE){
			return 0;
		}

	}
	
}
