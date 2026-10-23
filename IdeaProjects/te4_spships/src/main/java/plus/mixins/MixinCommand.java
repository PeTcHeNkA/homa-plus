package plus.mixins;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.common.etc.SCommand;
import plus.common.spship.entity.OffSpShip;
import plus.common.spship.item.ItemModule;


@Mixin(ServerCommandManager.class)
public abstract class MixinCommand extends CommandHandler implements ICommandListener {

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(MinecraftServer serverIn, CallbackInfo ci){


        registerCommand(new SCommand(0, "sp_spawn") {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
                EntityPlayerMP pl = getConsumer(sender);

                pl.world.spawnEntity(new OffSpShip(pl));

            }
        });

        registerCommand(new SCommand(0, "spm_cr") {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args){
                try {
                    EntityPlayerMP pl = getConsumer(sender);
                    int[] params = new int[args.length-1];
                    for(int i=1;i<args.length;i++){
                        params[i-1] = Integer.parseInt(args[i]);
                    }
                    pl.addItemStackToInventory(ItemModule.create(params));
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }


    private EntityPlayerMP getConsumer(ICommandSender sender){
        return (EntityPlayerMP) sender.getCommandSenderEntity();
    }
}