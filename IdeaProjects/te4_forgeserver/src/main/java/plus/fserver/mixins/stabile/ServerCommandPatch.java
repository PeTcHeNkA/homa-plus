package plus.fserver.mixins.stabile;

import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.fserver.api.ServerCommand;


@Mixin(ServerCommandManager.class)
public abstract class ServerCommandPatch extends CommandHandler implements ICommandListener {
    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(MinecraftServer serverIn, CallbackInfo ci){
        for(ServerCommand cmd: ServerCommand.commands){
            registerCommand(cmd);
        }
    }
}