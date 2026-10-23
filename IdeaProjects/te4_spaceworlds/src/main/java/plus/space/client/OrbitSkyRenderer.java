package plus.space.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.space.client.net.SolarMapClientProvider;
import plus.space.common.CommonCalculator;
import plus.space.common.GalName;
import java.util.Random;


@SideOnly(Side.CLIENT)
public class OrbitSkyRenderer extends IRenderHandler {
    //private static final ResourceLocation MOON_PHASES_TEXTURES = new ResourceLocation("textures/environment/moon_phases.png");
    //private static final ResourceLocation FORCEFIELD_TEXTURES = new ResourceLocation("textures/misc/forcefield.png");

    private static final ResourceLocation SUN_TEXTURES = new ResourceLocation("textures/environment/sun.png");
    private final TextureManager renderEngine;
    private final VertexFormat vertexBufferFormat;
    private VertexBuffer sky2VBO;
    private VertexBuffer starVBO;
    boolean vboEnabled = OpenGlHelper.useVbo();
    private int glSkyList2 = -1;
    private int starGLCallList = -1;
    private int pass = 1;

    private SolarMap.MapObj current;
    private SolarMap.Star curStar = null;
    private int curStarDistance = -1;
    private SolarMap.MapObj[] planets;


    public OrbitSkyRenderer(Minecraft mc){
        this.renderEngine = mc.getTextureManager();
        SolarMapClientProvider.getCurrent(mapObj -> {
            current = mapObj;

            GalName planet = current.name;
            GalName star = planet.getParent();
            GalName nl = star==null?null:star.getParent();

            while (nl != null){
                planet = planet.getParent();
                star = planet.getParent();
                nl = star.getParent();
            }
            GalName finalName = star;
            GalName finalPlanet = planet;
            SolarMap.INST.getSystems(stars -> {
                curStar = SolarMap.INST.getStar(finalName.getSelfName());

                SolarMap.INST.getPlanets(current.name, (mapObjTuple)->{
                    planets = mapObjTuple.B;

                    SolarMap.INST.getPlanets(finalPlanet,
                            mapObjTuple1 -> curStarDistance = ((SolarMap.Planet) mapObjTuple1.A).orbit
                    );

                });
            });

        });

        this.vertexBufferFormat = new VertexFormat();
        this.vertexBufferFormat.addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3));

        generateStars();
        generateSky2();
    }


    private void generateSky2() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        if (this.sky2VBO != null) {
            this.sky2VBO.deleteGlBuffers();
        }
        if (this.glSkyList2 >= 0) {
            GLAllocation.deleteDisplayLists(this.glSkyList2);
            this.glSkyList2 = -1;
        }
        if (this.vboEnabled) {
            this.sky2VBO = new VertexBuffer(this.vertexBufferFormat);
            this.renderSky(bufferbuilder, -16.0F, true);
            bufferbuilder.finishDrawing();
            bufferbuilder.reset();
            this.sky2VBO.bufferData(bufferbuilder.getByteBuffer());
        } else {
            this.glSkyList2 = GLAllocation.generateDisplayLists(1);
            GlStateManager.glNewList(this.glSkyList2, 4864);
            this.renderSky(bufferbuilder, -16.0F, true);
            tessellator.draw();
            GlStateManager.glEndList();
        }
    }


    private void renderSky(BufferBuilder bufferBuilderIn, float posY, boolean reverseX) {
        bufferBuilderIn.begin(7, DefaultVertexFormats.POSITION);
        for (int k = -384; k <= 384; k += 64) {
            for (int l = -384; l <= 384; l += 64) {
                float f = (float)k;
                float f1 = (float)(k + 64);
                if (reverseX) {
                    f1 = (float)k;
                    f = (float)(k + 64);
                }
                bufferBuilderIn.pos(f, posY, l).endVertex();
                bufferBuilderIn.pos(f1, posY, l).endVertex();
                bufferBuilderIn.pos(f1, posY, l + 64).endVertex();
                bufferBuilderIn.pos(f, posY, l + 64).endVertex();
            }
        }
    }


    private void generateStars() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        if (this.starVBO != null) {
            this.starVBO.deleteGlBuffers();
        }
        if (this.starGLCallList >= 0) {
            GLAllocation.deleteDisplayLists(this.starGLCallList);
            this.starGLCallList = -1;
        }
        if (this.vboEnabled) {
            this.starVBO = new VertexBuffer(this.vertexBufferFormat);
            this.renderStars(bufferbuilder);
            bufferbuilder.finishDrawing();
            bufferbuilder.reset();
            this.starVBO.bufferData(bufferbuilder.getByteBuffer());
        } else {
            this.starGLCallList = GLAllocation.generateDisplayLists(1);
            GlStateManager.pushMatrix();
            GlStateManager.glNewList(this.starGLCallList, 4864);
            this.renderStars(bufferbuilder);
            tessellator.draw();
            GlStateManager.glEndList();
            GlStateManager.popMatrix();
        }
    }


    private void renderStars(BufferBuilder bufferBuilderIn) {
        Random random = new Random(10842L);
        bufferBuilderIn.begin(7, DefaultVertexFormats.POSITION);

        for (int i = 0; i < 1500; ++i) {
            double d0 = random.nextFloat() * 2.0F - 1.0F;
            double d1 = random.nextFloat() * 2.0F - 1.0F;
            double d2 = random.nextFloat() * 2.0F - 1.0F;
            double d3 = 0.15F + random.nextFloat() * 0.1F;
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;

            if (d4 < 1.0D && d4 > 0.01D) {
                d4 = 1.0D / Math.sqrt(d4);
                d0 = d0 * d4;
                d1 = d1 * d4;
                d2 = d2 * d4;
                double d5 = d0 * 100.0D;
                double d6 = d1 * 100.0D;
                double d7 = d2 * 100.0D;
                double d8 = Math.atan2(d0, d2);
                double d9 = Math.sin(d8);
                double d10 = Math.cos(d8);
                double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
                double d12 = Math.sin(d11);
                double d13 = Math.cos(d11);
                double d14 = random.nextDouble() * Math.PI * 2.0D;
                double d15 = Math.sin(d14);
                double d16 = Math.cos(d14);

                for (int j = 0; j < 4; ++j) {
                    double d18 = (double)((j & 2) - 1) * d3;
                    double d19 = (double)((j + 1 & 2) - 1) * d3;
                    double d21 = d18 * d16 - d19 * d15;
                    double d22 = d19 * d16 + d18 * d15;
                    double d23 = d21 * d12 + 0.0D * d13;
                    double d24 = 0.0D * d12 - d21 * d13;
                    double d25 = d24 * d9 - d22 * d10;
                    double d26 = d22 * d9 + d24 * d10;
                    bufferBuilderIn.pos(d5 + d25, d6 + d23, d7 + d26).endVertex();
                }
            }
        }
    }


    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        Entity entity = mc.getRenderViewEntity();


        float rainColor = 1.0F - world.getRainStrength(partialTicks);
        float worldCelestialAngle;
        float starsColor;

        float infXzRotation = 75;
        float infYRotation = 50;

//        SpaseObjectsInf inf;
//        Biome biome = world.getBiome(entity.getPosition());
//        if(biome instanceof ISpaseObjectsInfFolder){
//            inf = ((ISpaseObjectsInfFolder) biome).getSpaceObjInf();
//        } else {
//            inf = new SpaseObjectsInf(50, 75, 0, 0, 0.7f, null);
//        }
//        if(inf.time>0) {
//            worldCelestialAngle = inf.time;
//        } else {
            worldCelestialAngle = getWorldCel(world.getWorldTime(), partialTicks);
//        }
//        if(inf.stars>0){
//            starsColor = inf.stars;
//        } else {
            starsColor = world.getStarBrightness(partialTicks) * rainColor;
//        }

        Vec3d vec3d = getSkyColor(world, entity, worldCelestialAngle, partialTicks);

        float skyR = (float) vec3d.x;
        float skyG = (float) vec3d.y;
        float skyB = (float) vec3d.z;

        if (pass != 2) {
            float f3 = (skyR * 30.0F + skyG * 59.0F + skyB * 11.0F) / 100.0F;
            float f4 = (skyR * 30.0F + skyG * 70.0F) / 100.0F;
            float f5 = (skyR * 30.0F + skyB * 70.0F) / 100.0F;
            skyR = f3;
            skyG = f4;
            skyB = f5;
        }

        GlStateManager.disableTexture2D();
        GlStateManager.color(skyR, skyG, skyB);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufBuilder = tessellator.getBuffer();
        GlStateManager.depthMask(false);
        GlStateManager.enableFog();
        GlStateManager.color(skyR, skyG, skyB);

        GlStateManager.disableFog();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderHelper.disableStandardItemLighting();

        //float worldCelestialAngle = 0.5f;// world.getCelestialAngle(partialTicks);

        float[] afloat = world.provider.calcSunriseSunsetColors(
                worldCelestialAngle, partialTicks);

        if (afloat != null) {
            if(curStar != null){
                float[] nw = StarTextureRepo.getStarColor(curStar.textureId);
                if(nw != null){
                    afloat[0] = (afloat[0] + nw[0]) / 2f;
                    afloat[1] = (afloat[1] + nw[1]) / 2f;
                    afloat[2] = (afloat[2] + nw[2]) / 2f;
                }
            }

            GlStateManager.disableTexture2D();
            GlStateManager.shadeModel(7425);
            GlStateManager.pushMatrix();
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(MathHelper.sin(worldCelestialAngle * ((float)Math.PI * 2F)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
            float f6 = afloat[0];
            float f7 = afloat[1];
            float f8 = afloat[2];

            if (pass != 2) {
                float f9 = (f6 * 30.0F + f7 * 59.0F + f8 * 11.0F) / 100.0F;
                float f10 = (f6 * 30.0F + f7 * 70.0F) / 100.0F;
                float f11 = (f6 * 30.0F + f8 * 70.0F) / 100.0F;
                f6 = f9;
                f7 = f10;
                f8 = f11;
            }

            bufBuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
            bufBuilder.pos(0.0D, 100.0D, 0.0D).color(f6, f7, f8, afloat[3]).endVertex();

            for (int j2 = 0; j2 <= 16; ++j2) {
                float f21 = (float) j2 * ((float) Math.PI * 2F) / 16.0F;
                float f12 = MathHelper.sin(f21);
                float f13 = MathHelper.cos(f21);
                bufBuilder.pos(f12 * 120.0F, f13 * 120.0F, -f13 * 40.0F * afloat[3]).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
            }

            tessellator.draw();
            GlStateManager.popMatrix();
            GlStateManager.shadeModel(7424);
        }

        GlStateManager.enableTexture2D();
        GlStateManager.pushMatrix();

        GlStateManager.color(1.0F, 1.0F, 1.0F, rainColor);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        GlStateManager.rotate(infXzRotation, 1.0F, 0.0F, 0.0F);
        float finalYrot = ((worldCelestialAngle * 360.0F)+infYRotation)%360;
        GlStateManager.rotate(finalYrot, 0.0F, 0.0F, 1.0F);
        float starSize = 50.0F;

        if(curStar != null){
            starSize *= curStar.scale;
            if(curStarDistance>-1){
                starSize = starSize * 3 / curStarDistance;
            }
            this.renderEngine.bindTexture(StarTextureRepo.getObjTexture(curStar.textureId));
        } else {
            this.renderEngine.bindTexture(SUN_TEXTURES);
        }

        bufBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufBuilder.pos(-starSize, 100.0D, -starSize).tex(0.0D, 0.0D).endVertex();
        bufBuilder.pos(starSize, 100.0D, -starSize).tex(1.0D, 0.0D).endVertex();
        bufBuilder.pos(starSize, 100.0D, starSize).tex(1.0D, 1.0D).endVertex();
        bufBuilder.pos(-starSize, 100.0D, starSize).tex(0.0D, 1.0D).endVertex();
        tessellator.draw();

        GlStateManager.rotate(-infXzRotation, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-finalYrot, 0.0F, 0.0F, 1.0F);


        if (starsColor > 0.0F) {
            GlStateManager.disableTexture2D();
            GlStateManager.color(starsColor, starsColor, starsColor, starsColor);

            if (this.vboEnabled) {
                this.starVBO.bindBuffer();
                GlStateManager.glEnableClientState(32884);
                GlStateManager.glVertexPointer(3, 5126, 12, 0);
                this.starVBO.drawArrays(7);
                this.starVBO.unbindBuffer();
                GlStateManager.glDisableClientState(32884);
            } else {
                GlStateManager.callList(this.starGLCallList);
            }
            GlStateManager.enableTexture2D();
        }


        //GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);


        if(planets != null && planets.length!=0){//TODO edit planets
            GlStateManager.disableBlend();
            for(SolarMap.MapObj planet: planets){
                if(!(planet instanceof SolarMap.Planet))continue;
                SolarMap.Planet planet1 = (SolarMap.Planet) planet;
                ResourceLocation texture = StarTextureRepo.getObjTexture(planet.textureId);
                if(texture==null)continue;
                this.renderEngine.bindTexture(texture);

                float planetXz = 50*planet1.orbit;
                float planetY = finalYrot+30*planet1.orbit;
                float size = planet.scale*10;


                GlStateManager.rotate(planetY, 1f, 0f, 0f);
                GlStateManager.rotate(planetXz, 1f, 0f, 0f);

                bufBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                bufBuilder.pos(-size, 100.0D, -size).tex(0.0D, 0.0D).endVertex();
                bufBuilder.pos(size, 100.0D, -size).tex(1.0D, 0.0D).endVertex();
                bufBuilder.pos(size, 100.0D, size).tex(1.0D, 1.0D).endVertex();
                bufBuilder.pos(-size, 100.0D, size).tex(0.0D, 1.0D).endVertex();
                tessellator.draw();

                GlStateManager.rotate(-planetY, 1f, 0f, 0f);
                GlStateManager.rotate(-planetXz, 1f, 0f, 0f);
            }
            GlStateManager.enableBlend();

            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableFog();
        GlStateManager.popMatrix();
        GlStateManager.disableTexture2D();
        //GlStateManager.color(0.0F, 0.0F, 0.0F);
        double horizon = mc.player.getPositionEyes(partialTicks).y - world.getHorizon();

        if (horizon < 0.0D) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 12.0F, 0.0F);

            if (this.vboEnabled) {
                this.sky2VBO.bindBuffer();
                GlStateManager.glEnableClientState(32884);
                GlStateManager.glVertexPointer(3, 5126, 12, 0);
                this.sky2VBO.drawArrays(7);
                this.sky2VBO.unbindBuffer();
                GlStateManager.glDisableClientState(32884);
            } else {
                GlStateManager.callList(this.glSkyList2);
            }

            GlStateManager.popMatrix();
            float f19 = -((float) (horizon + 65.0D));

            bufBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            bufBuilder.pos(-1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
            bufBuilder.pos(1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
            bufBuilder.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufBuilder.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufBuilder.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
            bufBuilder.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
            bufBuilder.pos(1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
            bufBuilder.pos(-1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
            bufBuilder.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
            bufBuilder.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufBuilder.pos(1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
            bufBuilder.pos(1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
            bufBuilder.pos(-1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
            bufBuilder.pos(-1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
            bufBuilder.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufBuilder.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
            bufBuilder.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
            bufBuilder.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufBuilder.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufBuilder.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
        }

        if (world.provider.isSkyColored()) {
            GlStateManager.color(skyR * 0.2F + 0.04F, skyG * 0.2F + 0.04F, skyB * 0.6F + 0.1F);
        } else {
            GlStateManager.color(skyR, skyG, skyB);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, -((float) (horizon - 16.0D)), 0.0F);
        GlStateManager.callList(this.glSkyList2);
        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
    }


    public Vec3d getSkyColor(World world, Entity entityIn, float worldCelestialAngle, float partialTicks) {
        float f1 = MathHelper.cos(worldCelestialAngle * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
        f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
        int i = MathHelper.floor(entityIn.posX);
        int j = MathHelper.floor(entityIn.posY);
        int k = MathHelper.floor(entityIn.posZ);
        BlockPos blockpos = new BlockPos(i, j, k);
        int l = net.minecraftforge.client.ForgeHooksClient.getSkyBlendColour(world, blockpos);
        float f3 = (float)(l >> 16 & 255) / 255.0F;
        float f4 = (float)(l >> 8 & 255) / 255.0F;
        float f5 = (float)(l & 255) / 255.0F;
        f3 = f3 * f1;
        f4 = f4 * f1;
        f5 = f5 * f1;
        float f6 = world.getRainStrength(partialTicks);

        if (f6 > 0.0F) {
            float f7 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.6F;
            float f8 = 1.0F - f6 * 0.75F;
            f3 = f3 * f8 + f7 * (1.0F - f8);
            f4 = f4 * f8 + f7 * (1.0F - f8);
            f5 = f5 * f8 + f7 * (1.0F - f8);
        }

        float f10 = world.getThunderStrength(partialTicks);

        if (f10 > 0.0F) {
            float f11 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.2F;
            float f9 = 1.0F - f10 * 0.75F;
            f3 = f3 * f9 + f11 * (1.0F - f9);
            f4 = f4 * f9 + f11 * (1.0F - f9);
            f5 = f5 * f9 + f11 * (1.0F - f9);
        }

        return new Vec3d(f3, f4, f5);
    }


    public float getWorldCel(float worldTime, float partialTicks){
        float daySize = 24000;
        if(curStar != null){
            if(curStarDistance>-1){
                daySize = CommonCalculator.calcWorldDay(curStarDistance);
            }
        }
        int i = (int)(worldTime % daySize);
        float f = ((float)i + partialTicks) / daySize - 0.25F;

        if (f < 0.0F) {
            ++f;
        }

        if (f > 1.0F) {
            --f;
        }
        float f1 = 1.0F - (float)((Math.cos((double)f * Math.PI) + 1.0D) / 2.0D);
        f = f + (f1 - f) / 3.0F;
        return f;
    }
}