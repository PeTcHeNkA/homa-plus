package plus.mixins;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import plus.client.camera.Camera;


@Mixin(MovementInputFromOptions.class)
public class MixinInput extends MovementInput {
    @Final
    @Shadow
    private GameSettings gameSettings;


    /**
     * @author Te4hno
     * @reason add Sp
     */
    @Overwrite
    public void updatePlayerMoveState() {
        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;

        if (this.gameSettings.keyBindForward.isKeyDown()) {
            ++this.moveForward;
            this.forwardKeyDown = true;
        } else {
            this.forwardKeyDown = false;
        }

        if (this.gameSettings.keyBindBack.isKeyDown()) {
            --this.moveForward;
            this.backKeyDown = true;
        } else {
            this.backKeyDown = false;
        }

        if (this.gameSettings.keyBindLeft.isKeyDown()) {
            ++this.moveStrafe;
            this.leftKeyDown = true;
        } else {
            this.leftKeyDown = false;
        }

        if (this.gameSettings.keyBindRight.isKeyDown()) {
            --this.moveStrafe;
            this.rightKeyDown = true;
        } else {
            this.rightKeyDown = false;
        }

        this.jump = this.gameSettings.keyBindJump.isKeyDown();
        this.sneak = this.gameSettings.keyBindSneak.isKeyDown();

        if (this.sneak && !Camera.isSelfSpshipActive) {
            this.moveStrafe = this.moveStrafe * 0.3f;
            this.moveForward = this.moveForward * 0.3f;
        }
    }
}