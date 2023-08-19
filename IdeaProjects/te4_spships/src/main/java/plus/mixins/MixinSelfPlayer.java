package plus.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.client.ISpRSelf;
import plus.client.camera.Camera;
import plus.client.mashine.*;
import plus.common.player.PlayerSpship;
import plus.utls.MathUtils;

import static plus.client.camera.Camera.onEndSelfSpship;
import static plus.client.camera.Camera.onStartSelfSpship;


@Mixin(EntityPlayerSP.class)
public abstract class MixinSelfPlayer extends AbstractClientPlayer implements PlayerSpship, ISpRSelf {
    @Final
    @Shadow
    public NetHandlerPlayClient connection;
    @Shadow
    public int sprintingTicksLeft;
    @Shadow
    protected int sprintToggleTimer;
    @Shadow
    public float prevTimeInPortal;
    @Shadow
    public float timeInPortal;
    @Shadow
    protected Minecraft mc;
    @Shadow
    public MovementInput movementInput;
    @Shadow
    private int autoJumpTime;
    @Shadow
    private boolean wasFallFlying;
    @Shadow
    private int horseJumpPowerCounter;
    @Shadow
    private float horseJumpPower;

    @Shadow
    protected abstract boolean isCurrentViewEntity();
    @Shadow
    public abstract boolean isRidingHorse();
    @Shadow
    public abstract float getHorseJumpPower();
    @Shadow
    protected abstract void sendHorseJump();

    @Shadow private boolean handActive;
    private double speed = 0;
    private double prevSpeed = 0;
    private float realPith = 0;

    public MixinSelfPlayer(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }


    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(CallbackInfo ci){
        Camera.onEndSelfSpship(this);
        prevSpShield = getSpShield();
//        if(isSpshipMode()){
//            onStartSelfSpship(this);
//        } else {
//            //onEndSelfSpship(this);
//        }
    }


    private boolean prevMode;
    private float prevSpShield = 0;
    @Inject(method = "onLivingUpdate", at = @At("HEAD"), cancellable = true)
    public void onLivingUpdate(CallbackInfo ci) {
        ci.cancel();
        boolean spMode = isSpshipMode();
        if(spMode != prevMode){
            prevMode = spMode;
            if(spMode){
                onStartSelfSpship(this);
            } else {
                onEndSelfSpship(this);
            }
        }
        if(spMode){
            float curShieldSp = getSpShield();
            if(prevSpShield != curShieldSp) {
                prevSpShield = curShieldSp;
                Camera.overlay.onPostDamage(curShieldSp);
            }
        }

        ++this.sprintingTicksLeft;

        if (this.sprintToggleTimer > 0) {
            --this.sprintToggleTimer;
        }

        this.prevTimeInPortal = this.timeInPortal;

        if (this.inPortal) {
            if (this.mc.currentScreen != null && !this.mc.currentScreen.doesGuiPauseGame()) {
                if (this.mc.currentScreen instanceof GuiContainer) {
                    this.closeScreen();
                }

                this.mc.displayGuiScreen(null);
            }

            if (this.timeInPortal == 0.0F) {
                this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_PORTAL_TRIGGER, this.rand.nextFloat() * 0.4F + 0.8F));
            }

            this.timeInPortal += 0.0125F;

            if (this.timeInPortal >= 1.0F) {
                this.timeInPortal = 1.0F;
            }

            this.inPortal = false;
        }
        else if (this.isPotionActive(MobEffects.NAUSEA) && this.getActivePotionEffect(MobEffects.NAUSEA).getDuration() > 60) {
            this.timeInPortal += 0.006666667F;

            if (this.timeInPortal > 1.0F) {
                this.timeInPortal = 1.0F;
            }
        } else {
            if (this.timeInPortal > 0.0F) {
                this.timeInPortal -= 0.05F;
            }
            if (this.timeInPortal < 0.0F) {
                this.timeInPortal = 0.0F;
            }
        }
        if (this.timeUntilPortal > 0) {
            --this.timeUntilPortal;
        }

        boolean flag = this.movementInput.jump;
        boolean flag1 = this.movementInput.sneak;
        float f = 0.8F;
        boolean flag2 = this.movementInput.moveForward >= 0.8F;
        this.movementInput.updatePlayerMoveState();
        net.minecraftforge.client.ForgeHooksClient.onInputUpdate(this, this.movementInput);
        this.mc.getTutorial().handleMovement(this.movementInput);

        if (this.isHandActive() && !this.isRiding()) {
            this.movementInput.moveStrafe *= 0.2F;
            this.movementInput.moveForward *= 0.2F;
            this.sprintToggleTimer = 0;
        }

        boolean flag3 = false;

        if (this.autoJumpTime > 0) {
            --this.autoJumpTime;
            flag3 = true;
            this.movementInput.jump = true;
        }

        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent event = new net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent(this, axisalignedbb);
        if(!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) { axisalignedbb = event.getEntityBoundingBox();
            this.pushOutOfBlocks(this.posX - (double)this.width * 0.35D, axisalignedbb.minY + 0.5D, this.posZ + (double)this.width * 0.35D);
            this.pushOutOfBlocks(this.posX - (double)this.width * 0.35D, axisalignedbb.minY + 0.5D, this.posZ - (double)this.width * 0.35D);
            this.pushOutOfBlocks(this.posX + (double)this.width * 0.35D, axisalignedbb.minY + 0.5D, this.posZ - (double)this.width * 0.35D);
            this.pushOutOfBlocks(this.posX + (double)this.width * 0.35D, axisalignedbb.minY + 0.5D, this.posZ + (double)this.width * 0.35D);
        }
        boolean flag4 = (float)this.getFoodStats().getFoodLevel() > 6.0F || this.capabilities.allowFlying;

        if (this.onGround && !flag1 && !flag2 && this.movementInput.moveForward >= 0.8F && !this.isSprinting() && flag4 && !this.isHandActive() && !this.isPotionActive(MobEffects.BLINDNESS)) {
            if (this.sprintToggleTimer <= 0 && !this.mc.gameSettings.keyBindSprint.isKeyDown()) {
                this.sprintToggleTimer = 7;
            } else {
                this.setSprinting(true);
            }
        }

        if (!this.isSprinting() && this.movementInput.moveForward >= 0.8F && flag4 && !this.isHandActive() && !this.isPotionActive(MobEffects.BLINDNESS) && this.mc.gameSettings.keyBindSprint.isKeyDown()) {
            this.setSprinting(true);
        }

        if (this.isSprinting() && (this.movementInput.moveForward < 0.8F || this.collidedHorizontally || !flag4)) {
            this.setSprinting(false);
        }

        if (!spMode && this.capabilities.allowFlying) {
            if (this.mc.playerController.isSpectatorMode()) {
                if (!this.capabilities.isFlying) {
                    this.capabilities.isFlying = true;
                    this.sendPlayerAbilities();
                }
            } else if (!flag && this.movementInput.jump && !flag3) {
                if (this.flyToggleTimer == 0) {
                    this.flyToggleTimer = 7;
                } else {
                    this.capabilities.isFlying = !this.capabilities.isFlying;
                    this.sendPlayerAbilities();
                    this.flyToggleTimer = 0;
                }
            }
        }

        if (this.movementInput.jump && !flag && !this.onGround && this.motionY < 0.0D && !this.isElytraFlying() && !this.capabilities.isFlying) {
            ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            if (itemstack.getItem() == Items.ELYTRA && ItemElytra.isUsable(itemstack)) {
                this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_FALL_FLYING));
            }
        }

        this.wasFallFlying = this.isElytraFlying();

        if (this.capabilities.isFlying && this.isCurrentViewEntity()) {
            if(spMode){
                float boost = this.isSprinting() ? 4.5f:3f;
                if(this.movementInput.sneak){
                    this.motionY -= this.capabilities.getFlySpeed() * boost;
                }
                if (this.movementInput.jump) {
                    this.motionY += this.capabilities.getFlySpeed() * boost;
                }
            } else {
                if (this.movementInput.sneak) {
                    this.movementInput.moveStrafe = (float)((double)this.movementInput.moveStrafe / 0.3D);
                    this.movementInput.moveForward = (float) ((double) this.movementInput.moveForward / 0.3D);
                    this.motionY -= this.capabilities.getFlySpeed() * 3.0F;
                }
                if (this.movementInput.jump) {
                    this.motionY += this.capabilities.getFlySpeed() * 3.0F;
                }
            }
        }

        if (this.isRidingHorse()) {
            IJumpingMount ijumpingmount = (IJumpingMount)this.getRidingEntity();

            if (this.horseJumpPowerCounter < 0) {
                ++this.horseJumpPowerCounter;

                if (this.horseJumpPowerCounter == 0) {
                    this.horseJumpPower = 0.0F;
                }
            }

            if (flag && !this.movementInput.jump) {
                this.horseJumpPowerCounter = -10;
                ijumpingmount.setJumpPower(MathHelper.floor(this.getHorseJumpPower() * 100.0F));
                this.sendHorseJump();
            } else if (!flag && this.movementInput.jump) {
                this.horseJumpPowerCounter = 0;
                this.horseJumpPower = 0.0F;
            } else if (flag) {
                ++this.horseJumpPowerCounter;

                if (this.horseJumpPowerCounter < 10) {
                    this.horseJumpPower = (float)this.horseJumpPowerCounter * 0.1F;
                } else {
                    this.horseJumpPower = 0.8F + 2.0F / (float)(this.horseJumpPowerCounter - 9) * 0.1F;
                }
            }
        } else {
            this.horseJumpPower = 0.0F;
        }

        super.onLivingUpdate();
        prevSpeed = speed;
        speed = MathUtils.calcEntitySpeed(this);
        realPith = rotationPitch;

        if (this.onGround && this.capabilities.isFlying &&
                !this.mc.playerController.isSpectatorMode() && !spMode) {
            this.capabilities.isFlying = false;
            this.sendPlayerAbilities();
        }
    }


    @Inject(method = "displayGUIChest", at = @At("HEAD"), cancellable = true)
    public void displayGUIChest(IInventory chestInventory, CallbackInfo ci){
        if(chestInventory instanceof IInteractionObject) {
            String s = ((IInteractionObject) chestInventory).getGuiID();
            switch (s) {
                case "minecraft:sp_craft_gui":
                    this.mc.displayGuiScreen(new GuiSpCraft(this.inventory, chestInventory));
                    ci.cancel();
                    break;
                case "minecraft:sp_furnace_gui":
                    this.mc.displayGuiScreen(new GuiSpFurnace(this.inventory, chestInventory));
                    ci.cancel();
                    break;
                case "minecraft:aether_react":
                    this.mc.displayGuiScreen(new GuiSpReact(this.inventory, chestInventory));
                    ci.cancel();
                    break;
                case "minecraft:sp_press_gui":
                    this.mc.displayGuiScreen(new GuiSpPress(this.inventory, chestInventory));
                    ci.cancel();
                    break;
                case "minecraft:sp_eng_cap":
                    this.mc.displayGuiScreen(new GuiSpCapacitor(this.inventory, chestInventory));
                    ci.cancel();
                    break;
            }
        }
    }


    @Override
    public void turn(float yaw, float pitch) {
        if(isSpshipMode() && !Camera.stabilePos){
            pitch = pitch*0.1f;
        }

        float f = this.rotationPitch;
        float f1 = this.rotationYaw;
        this.rotationYaw = (float)((double)this.rotationYaw + (double)yaw * 0.15D);
        this.rotationPitch = (float)((double)this.rotationPitch - (double)pitch * 0.15D);
        this.rotationPitch = MathHelper.clamp(this.rotationPitch, -90.0F, 90.0F);
        this.prevRotationPitch += this.rotationPitch - f;
        this.prevRotationYaw += this.rotationYaw - f1;

        if (this.getRidingEntity() != null) {
            this.getRidingEntity().applyOrientationToEntity(this);
        }
    }


//    @Inject(method = "isHandActive", at = @At("HEAD"), cancellable = true)
//    public void isHandActive(CallbackInfoReturnable<Boolean> cir) {
//        cir.setReturnValue(this.handActive && ! prevMode);
//    }



    @Override
    public double getSpeed() {
        return speed;
    }


    @Override
    public double getPrevSpeed() {
        return prevSpeed;
    }


    @Override
    public float getRealPith() {
        return realPith;
    }


    //@Inject(method = "attackEntityFrom", at = @At("HEAD"))
//    @Override
//    public void onSpAttack(DamageSource source, float amount){
//        Camera.overlay.onPostDamage();
//    }
}