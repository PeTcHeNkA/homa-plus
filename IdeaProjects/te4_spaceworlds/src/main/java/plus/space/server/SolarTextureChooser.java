package plus.space.server;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import plus.fserver.api.BiomeIndexes;
import plus.fserver.utls.math.FastRandom;
import plus.fserver.world.generator.biome.BiomeLib;
import plus.fserver.world.generator.biome.layers.EGenLayerBiomePlus;
import java.util.function.Supplier;


public class SolarTextureChooser {
    public static int choiceStationId(SolarRepository.Race race){
        switch (race){
            case IMPERIAL: return or(32, 33, 34, 35);
            case PIRACY:   return or(24, 25, 26, 27);
            case NEUTRAL:  return or(20, 21, 22, 23);
            case VOIDS:    return or(28, 29, 30, 31);
        }
        return 20;
    }


    interface Checker{
        boolean hasWater();
        boolean hasSand();
        boolean isClean();
        boolean isCold();
        default int calcPlanetTextureId(){
            return 100 + choosePlanetId(this);
        }
    }


    private static final class BChecker implements Checker{
        private Biome biome;
        @Override
        public boolean hasWater() {
            return biome.canRain() || biome.getTempCategory() == Biome.TempCategory.OCEAN || biome.getBaseHeight() < 0.1;
        }
        @Override
        public boolean hasSand() {
            return !biome.canRain() || biome.getTempCategory() == Biome.TempCategory.WARM;
        }
        @Override
        public boolean isClean() {
            Block tb = biome.topBlock.getBlock();
            return (tb == Blocks.GLASS || tb == Blocks.SAND) && biome.getHeightVariation() < 0.4;
        }
        @Override
        public boolean isCold() {
            return biome.isSnowyBiome();
        }
    }


    private static final class BMCecker implements Checker{
        private final Biome[] biomes;
        private BMCecker(Biome[] biomes) {
            this.biomes = biomes;
        }

        private boolean analytic(BChecker bc, Supplier<Boolean> func){
            int summ = 0;
            for (Biome biome : biomes) {
                bc.biome = biome;
                summ += (func.get() ? 1 : -1);
            }
            if(summ > -2 && summ < 2) return FastRandom.INST.nextBoolean();
            return summ>0;
        }


        @Override
        public boolean hasWater() {
            BChecker bc = new BChecker();
            return analytic(bc, bc::hasWater);
        }


        @Override
        public boolean hasSand() {
            BChecker bc = new BChecker();
            return analytic(bc, bc::hasSand);
        }

        @Override
        public boolean isClean() {
            BChecker bc = new BChecker();
            return analytic(bc, bc::isClean);
        }


        @Override
        public boolean isCold() {
            BChecker bc = new BChecker();
            return analytic(bc, bc::isCold);
        }
    }


    private static final class CChecker implements Checker{
        private final boolean hasWater, hasSand, isClean, isCold;

        private CChecker(Checker parent) {
            hasWater = parent.hasWater();
            hasSand  = parent.hasSand();
            isClean = parent.isClean();
            isCold = parent.isCold();;
        }

        @Override
        public boolean hasWater() {
            return hasWater;
        }

        @Override
        public boolean hasSand() {
            return hasSand;
        }

        @Override
        public boolean isClean() {
            return isClean;
        }

        @Override
        public boolean isCold() {
            return isCold;
        }
    }


    public static Checker choicePlanetId(BiomeLib lib){
        Biome[] biomes = new Biome[
                lib.biomes[0].size()+
                lib.biomes[1].size()+
                lib.biomes[2].size()+
                lib.biomes[3].size()
        ];
        int cursor = 0;
        for (int i=0;i<lib.biomes.length;i++){
            for(EGenLayerBiomePlus.BiomeEntry entry:lib.biomes[i]){
                biomes[cursor] = BiomeIndexes.get(entry.id);
                ++cursor;
            }
        }
        return new CChecker(new BMCecker(biomes));
    }


    public static int chooseStarId(){
        return FastRandom.INST.nextInt(8)+1;
    }


    public static int chooseMetBeltID(){
        return FastRandom.INST.nextInt(24) + 51;
    }


    public static int chooceMoonID(){
        return FastRandom.INST.nextInt(20) + 81;
    }


    private static int choosePlanetId(Checker checker){
        int[] clean = {0, 1, 2, 3};
        int[] dirt = {2, 3, 4, 5};
        int rand = FastRandom.INST.nextInt(121);
        if(rand>90){
            if(rand>110) return rand+1;
            int[] temp = clean;
            clean = dirt;
            dirt = temp;
        }

        if(checker.hasWater()){
            if(checker.isCold()){
                if(checker.isClean()){
                    if(checker.hasSand()){
                        return or(15, 16) + (or(clean)*20);
                    } else {
                        return or(5, 6) + (or(clean)*20);
                    }
                } else {
                    if(!checker.hasSand()){
                        return or(3, 4) + (or(dirt)*20);
                    } else {
                        return or(7, 8) + (or(dirt)*20);
                    }
                }
            } else {
                if(checker.isClean()){
                    if(checker.hasSand()){
                        return or(11, 12) + (or(clean)*20);
                    } else {
                        return or(1, 2, 19, 20) + (or(clean)*20);
                    }
                } else {
                    if(checker.hasSand()){
                        return or(13, 14) + (or(dirt)*20);
                    } else {
                        return or(3, 4, 17, 18) + (or(dirt)*20);
                    }
                }
            }
        } else {
            if(checker.isCold()){
                if(checker.isClean()){
                    return or(5, 6) + (or(clean)*20);
                } else {
                    return or(7, 8) + (or(dirt)*20);
                }
            } else {
                if(checker.isClean()){
                    return or(9, 10) + (or(clean)*20);
                } else {
                    return or(13, 14) + (or(dirt)*20);
                }
            }
        }
    }


    private static int or(int...ids){
        return ids[FastRandom.INST.nextInt(ids.length)];
    }


    private static int or(int id1, int id2){
        return (FastRandom.INST.nextBoolean()?id1:id2);
    }
}