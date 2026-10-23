
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

import org.bukkit.event.player.PlayerJoinEvent
import plus.tsonspy.TsonPlugin

/**
 * Templates for scripts
 */

def onEnable(TsonPlugin inst){
    println "Hello, Tson Spy :3"
}

def onDisable(TsonPlugin inst){
    println inst.name + " Disabled"
}

def exampleEvent(PlayerJoinEvent event){

}

import org.bukkit.boss.DragonBattle
import org.bukkit.entity.EnderDragon
import org.bukkit.event.entity.EntitySpawnEvent

def onRespawn(EntitySpawnEvent event){

    if(event.entity instanceof EnderDragon){
        def battle = event.entity.world.enderDragonBattle
        if(battle != null && battle.hasBeenPreviouslyKilled()){
            event.setCancelled(true)
            battle.setRespawnPhase(DragonBattle.RespawnPhase.NONE)
            battle.generateEndPortal(false)
        }
    }
}

def onCommand(TsonPlugin inst, CommandSender sender, Command command, String label, String[] args){
    println "Try call command " + String.join(" ", args)
}