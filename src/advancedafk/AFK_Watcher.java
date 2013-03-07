package advancedafk;

import java.util.HashMap;

import me.edge209.afkTerminator.AfkDetect;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AFK_Watcher implements Runnable{
	
	static HashMap<Player, Integer> time = new HashMap<Player, Integer>();
	public AFK_Functions functions;
	
	public AFK_Watcher(AFK_Functions functions){
		this.functions = functions;
	}
	
	@Override
	public void run() {
		for(Player p : time.keySet()){
			if(!p.hasPermission("advancedafk.*")){
				int doubleIt = 1;
				if(p.hasPermission("advancedafk.lagging")){
					doubleIt = 2;
				}
				time.put(p, time.get(p) + 1);
				try{
					if(!p.hasPermission("advancedafk.exempt.afk") && time.get(p) == AdvancedAFK.MAX_AFK_TIME_MESSAGE * 20*doubleIt && AdvancedAFK.AFK_ENABLED){
						functions.afk(p, true);
					}else if(!p.hasPermission("advancedafk.exempt.kick") && time.get(p) == AdvancedAFK.MAX_AFK_TIME_KICK * 20*doubleIt && AdvancedAFK.KICK_ENABLED){
						functions.kick(p);
					}else{
						if(AdvancedAFK.useAFKTerminator){
							if(!p.hasPermission("advancedafk.exempt.afk") && getAFKMachineStartTime(p.getName()) >= AdvancedAFK.MAX_AFK_TIME_MESSAGE * 20*doubleIt && AdvancedAFK.AFK_ENABLED){
								//He is AFK
								functions.afk(p, true);
							}else if(!p.hasPermission("advancedafk.exempt.kick") && getAFKMachineStartTime(p.getName()) >= AdvancedAFK.MAX_AFK_TIME_KICK * 20*doubleIt && AdvancedAFK.KICK_ENABLED){
								//Hes to long AFK
								functions.kick(p);
							}
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
		}

	}
	
}
