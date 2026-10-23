package plus.Character;

import org.bukkit.Location;

public interface Character {
    public int spawn(Location loc);
    public void playAnimation(String animation);
    public void playAnimation(String animation, double speed);
    public void remove();
}
