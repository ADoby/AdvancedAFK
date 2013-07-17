package advancedafk;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerData {
    private Player player;
    private LinkedHashSet<Location> lastLocations = new LinkedHashSet<Location>();
    private Location lastLocation;
    private int lastAction = 0;
    private int stepCounts = 0;
    private int actionCounts = 0;
    private int time = 0;
    private boolean isAfk = false;

    public PlayerData(Player player) {
        this.player = player;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    // The last locations/blocks the player walked by
    public Set<Location> getLastLocations() {
        return lastLocations;
    }
    
    public void addLastLocations(Location loc) {
        //If loggedLocations is higher then MAX_LOGGED_LOCATIONS delete first
        Iterator<Location> iter = null;
        while (lastLocations.size() > AdvancedAFK.MAX_LOGGED_LOCATIONS && (iter == null || iter.hasNext())) {
            if (iter == null) {
                iter = lastLocations.iterator();
            }
            iter.next();
            iter.remove();
        }
        lastLocations.add(loc);
    }
    
    public boolean touchLastLocations(Location loc) {
        if (!lastLocations.remove(loc))
            return false;
        
        lastLocations.add(loc);
        return true;
    }

    // The last location the player was
    public Location getLastLocation() {
        return lastLocation;
    }
    
    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    // The last action the player did (Like chatting, interacting, building etc.)
    public int getLastAction() {
        return lastAction;
    }
    
    public void setLastAction(int action) {
        this.lastAction = action;
    }

    // How many times the player walks over the same locations
    public int getStepCounts() {
        return stepCounts;
    }

    public void setStepCounts(int stepCounts) {
        this.stepCounts = stepCounts;
    }

    public int getActionCounts() {
        return actionCounts;
    }

    public void setActionCounts(int actionCounts) {
        this.actionCounts = actionCounts;
    }
    
    // The time the player did not do anything/something different, his afk-time
    public int getTime() {
        return time;
    }
    
    public void setTime(int time) {
        this.time = time;
    }
    
    public boolean isAfk() {
        return isAfk;
    }
    
    public void setAfk(boolean value) {
        this.isAfk = value;
    }
}
