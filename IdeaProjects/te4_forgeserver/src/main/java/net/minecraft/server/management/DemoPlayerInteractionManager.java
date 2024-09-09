package net.minecraft.server.management;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;


public class DemoPlayerInteractionManager extends PlayerInteractionManager {
    private boolean displayedIntro;
    private boolean demoTimeExpired;
    private int demoEndedReminder;
    private int gameModeTicks;

    public DemoPlayerInteractionManager(World p_i1513_1_) {
        super(p_i1513_1_);
    }


    public void updateBlockRemoving() {
        super.updateBlockRemoving();
        ++this.gameModeTicks;
        long lvt_1_1_ = player.world.getTotalWorldTime();
        long lvt_3_1_ = lvt_1_1_ / 24000L + 1L;
        if (!this.displayedIntro && this.gameModeTicks > 20) {
            this.displayedIntro = true;
            this.player.connection.sendPacket(new SPacketChangeGameState(5, 0.0F));
        }

        this.demoTimeExpired = lvt_1_1_ > 120500L;
        if (this.demoTimeExpired) {
            ++this.demoEndedReminder;
        }

        if (lvt_1_1_ % 24000L == 500L) {
            if (lvt_3_1_ <= 6L) {
                this.player.sendMessage(new TextComponentTranslation("demo.day." + lvt_3_1_, new Object[0]));
            }
        } else if (lvt_3_1_ == 1L) {
            if (lvt_1_1_ == 100L) {
                this.player.connection.sendPacket(new SPacketChangeGameState(5, 101.0F));
            } else if (lvt_1_1_ == 175L) {
                this.player.connection.sendPacket(new SPacketChangeGameState(5, 102.0F));
            } else if (lvt_1_1_ == 250L) {
                this.player.connection.sendPacket(new SPacketChangeGameState(5, 103.0F));
            }
        } else if (lvt_3_1_ == 5L && lvt_1_1_ % 24000L == 22000L) {
            this.player.sendMessage(new TextComponentTranslation("demo.day.warning", new Object[0]));
        }

    }


    private void sendDemoReminder() {
        if (this.demoEndedReminder > 100) {
            this.player.sendMessage(new TextComponentTranslation("demo.reminder", new Object[0]));
            this.demoEndedReminder = 0;
        }
    }


    public void onBlockClicked(BlockPos p_180784_1_, EnumFacing p_180784_2_) {
        if (this.demoTimeExpired) {
            this.sendDemoReminder();
        } else {
            super.onBlockClicked(p_180784_1_, p_180784_2_);
        }
    }


    public void blockRemoving(BlockPos p_180785_1_) {
        if (!this.demoTimeExpired) {
            super.blockRemoving(p_180785_1_);
        }
    }


    public boolean tryHarvestBlock(BlockPos p_180237_1_) {
        return this.demoTimeExpired ? false : super.tryHarvestBlock(p_180237_1_);
    }


    public EnumActionResult processRightClick(EntityPlayer p_187250_1_, World p_187250_2_, ItemStack p_187250_3_, EnumHand p_187250_4_) {
        if (this.demoTimeExpired) {
            this.sendDemoReminder();
            return EnumActionResult.PASS;
        } else {
            return super.processRightClick(p_187250_1_, p_187250_2_, p_187250_3_, p_187250_4_);
        }
    }


    public EnumActionResult processRightClickBlock(EntityPlayer p_187251_1_, World p_187251_2_, ItemStack p_187251_3_, EnumHand p_187251_4_, BlockPos p_187251_5_, EnumFacing p_187251_6_, float p_187251_7_, float p_187251_8_, float p_187251_9_) {
        if (this.demoTimeExpired) {
            this.sendDemoReminder();
            return EnumActionResult.PASS;
        } else {
            return super.processRightClickBlock(p_187251_1_, p_187251_2_, p_187251_3_, p_187251_4_, p_187251_5_, p_187251_6_, p_187251_7_, p_187251_8_, p_187251_9_);
        }
    }
}
