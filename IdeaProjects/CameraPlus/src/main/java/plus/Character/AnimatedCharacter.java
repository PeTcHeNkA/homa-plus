package plus.Character;

import org.bukkit.Location;

public class AnimatedCharacter implements Character{

    final private Character character;


    public AnimatedCharacter(String model, String name, boolean noDamage){
        try {
            character = new ModelEngineCharacter(model, name, noDamage);
        } catch (Exception e){
            e.printStackTrace();
            throw new NullPointerException("Model engine plugin is not exist!");
        }
    }

    @Override
    public int spawn(Location loc) {
        return character.spawn(loc);
    }

    @Override
    public void playAnimation(String animation) {
        character.playAnimation(animation);
    }

    @Override
    public void playAnimation(String animation, double speed) {
        character.playAnimation(animation, speed);
    }

    @Override
    public void remove() {
        character.remove();
    }
}
