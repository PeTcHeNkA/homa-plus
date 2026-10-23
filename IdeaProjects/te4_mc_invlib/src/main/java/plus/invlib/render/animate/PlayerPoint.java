package plus.invlib.render.animate;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import plus.invlib.render.Updateable;


public class PlayerPoint extends ABCPoint<PlayerPoint, EntityPlayerSP>{
    public float translate1arg;
    public float translate2arg;
    public float translate3arg;

    public float scale1arg;
    public float scale2arg;
    public float scale3arg;

    public float renderYawOffset;
    public float rotationPitch;
    public float rotationYawHead;
    public float prevRotationYawHead;


    public PlayerPoint pow(float f) {
        PlayerPoint p = new PlayerPoint();
        p.scale1arg = this.scale1arg * f;
        p.scale2arg = this.scale2arg * f;
        p.scale3arg = this.scale3arg * f;
        p.translate1arg = this.translate1arg * f;
        p.translate2arg = this.translate2arg * f;
        p.translate3arg = this.translate3arg * f;
        p.renderYawOffset = this.renderYawOffset * f;
        p.rotationPitch = this.rotationPitch * f;
        p.rotationYawHead = this.rotationYawHead * f;
        p.prevRotationYawHead = this.prevRotationYawHead * f;
        return p;
    }


    public PlayerPoint copy(){
        PlayerPoint p = new PlayerPoint();
        p.scale1arg = this.scale1arg;
        p.scale2arg = this.scale2arg;
        p.scale3arg = this.scale3arg;
        p.translate1arg = this.translate1arg;
        p.translate2arg = this.translate2arg;
        p.translate3arg = this.translate3arg;
        p.renderYawOffset = this.renderYawOffset;
        p.rotationPitch = this.rotationPitch;
        p.rotationYawHead = this.rotationYawHead;
        p.prevRotationYawHead = this.prevRotationYawHead;
        return p;
    }


    public PlayerPoint add(PlayerPoint p) {
        PlayerPoint p1 = new PlayerPoint();
        p1.scale1arg = this.scale1arg + p.scale1arg;
        p1.scale2arg = this.scale2arg + p.scale2arg;
        p1.scale3arg = this.scale3arg + p.scale3arg;
        p1.translate1arg = this.translate1arg + p.translate1arg;
        p1.translate2arg = this.translate2arg + p.translate2arg;
        p1.translate3arg = this.translate3arg + p.translate3arg;
        p1.renderYawOffset = this.renderYawOffset + p.renderYawOffset;
        p1.rotationPitch = this.rotationPitch + p.rotationPitch;
        p1.rotationYawHead = this.rotationYawHead + p.rotationYawHead;
        p1.prevRotationYawHead = this.prevRotationYawHead + p.prevRotationYawHead;
        return p1;
    }


    public PlayerPoint range(PlayerPoint p) {
        PlayerPoint p1 = new PlayerPoint();
        p1.scale1arg = range(this.scale1arg, p.scale1arg);
        p1.scale2arg = range(this.scale2arg, p.scale2arg);
        p1.scale3arg = range(this.scale3arg, p.scale3arg);
        p1.translate1arg = range(this.translate1arg, p.translate1arg);
        p1.translate2arg = range(this.translate2arg, p.translate2arg);
        p1.translate3arg = range(this.translate3arg, p.translate3arg);
        p1.renderYawOffset = range(this.renderYawOffset, p.renderYawOffset);
        p1.rotationPitch = range(this.rotationPitch, p.rotationPitch);
        p1.rotationYawHead = range(this.rotationYawHead, p.rotationYawHead);
        p1.prevRotationYawHead = range(this.prevRotationYawHead, p.prevRotationYawHead);
        return p1;
    }


    public void render(EntityPlayerSP ent) {
        GlStateManager.translate(translate1arg, translate2arg, translate3arg);
        GlStateManager.scale(scale1arg, scale2arg, scale3arg);
        ent.renderYawOffset = renderYawOffset;
        ent.rotationPitch = rotationPitch;
        ent.rotationYawHead = rotationYawHead;
        ent.prevRotationYawHead = prevRotationYawHead;
    }


    public static class Builder extends PlayerPoint implements Updateable {
        public Builder(){
            update();
        }


        public void translate(float translate1arg, float translate2arg, float translate3arg){
            this.translate1arg = translate1arg;
            this.translate2arg = translate2arg;
            this.translate3arg = translate3arg;
        }
        public void scale(float scale1arg, float scale2arg, float scale3arg){
            this.scale1arg = scale1arg;
            this.scale2arg = scale2arg;
            this.scale3arg = scale3arg;
        }
    }
}