package net.minecraft.world.gen;

import plus.fserver.utls.math.FastRandom;
import java.util.Random;


public class NoiseGeneratorImproved extends NoiseGenerator {
    private final int[] permutations;
    private final float xCoord, yCoord, zCoord;
    private static final int[] GRAD_X  = new int[] {1, -1, 1, -1, 1, -1, 1, -1, 0, 0, 0, 0, 1, 0, -1, 0};
    private static final int[] GRAD_Y  = new int[] {1, 1, -1, -1, 0, 0, 0, 0, 1, -1, 1, -1, 1, -1, 1,-1};
    private static final int[] GRAD_Z  = new int[] {0, 0, 0, 0, 1, 1, -1, -1, 1, 1, -1, -1, 0, 1, 0, -1};
    private static final int[] GRAD_2X = new int[] {1, -1, 1, -1, 1, -1, 1, -1, 0, 0, 0, 0, 1, 0, -1, 0};
    private static final int[] GRAD_2Z = new int[] {0, 0, 0, 0, 1, 1, -1, -1, 1, 1, -1, -1, 0, 1, 0, -1};

    public NoiseGeneratorImproved() {
        this(FastRandom.INST);
    }


    public NoiseGeneratorImproved(Random random) {
        int[] permutations = this.permutations = new int[512];
        this.xCoord = random.nextFloat() * 256;
        this.yCoord = random.nextFloat() * 256;
        this.zCoord = random.nextFloat() * 256;

        int i, j, k;
        for (i = 0; i < 256; permutations[i] = i++);

        for (i = 0; i < 256; ++i) {
            j = random.nextInt(256 - i) + i;
            k = permutations[i];
            permutations[i] = permutations[j];
            permutations[j] = k;
            permutations[i + 256] = permutations[i];
        }
    }


    public static float lerp(float d2, float d1, float d3) {
        return d1 + d2 * (d3 - d1);
    }


    public static float grad2(int num1, final float d1, final float d2) {
        return GRAD_2X[num1 &= 15] * d1 + GRAD_2Z[num1] * d2;
    }


    public static float grad(int num1, float d1, float d2, float d3) {
        return GRAD_X[num1 &= 15] * d1 + GRAD_Y[num1] * d2 + GRAD_Z[num1] * d3;
    }


    public final double lerp(double d2, double d1, double d3) {
        return d1 + d2 * (d3 - d1);
    }


    public final double grad2(int num1, final double d1, final double d2) {
        return GRAD_2X[num1 &= 15] * d1 + GRAD_2Z[num1] * d2;
    }


    public final double grad(int num1, double d1, double d2, double d3) {
        return GRAD_X[num1 &= 15] * d1 + GRAD_Y[num1] * d2 + GRAD_Z[num1] * d3;
    }


    public void populateNoiseArray(double[] noiseArray, double xOffset, double yOffset, double zOffset, int xSize, int ySize, int zSize, double xScale, double yScale, double zScale, double noiseScale) {
        int[] permutations = this.permutations;
        int i = 0, n1, n2, n3, n4, n5, n6, n7, n8;
        float d2, d1, d0 = 1 / (float)noiseScale, fl1;
        final float xCoord = this.xCoord, yCoord = this.yCoord, zCoord = this.zCoord;
        final float
                xOffsetF = (float) xOffset,
                xScaleF = (float) xScale,
                zOffsetF = (float) zOffset,
                zScaleF = (float) zScale;

        if (ySize == 1)
            for(int j2 = 0; j2 < xSize; ++j2) {
                d2 = j2 * xScaleF + xCoord + xOffsetF;
                n5 = (int)d2;

                if (d2 < n5) --n5;
                n6 = n5 & 255;

                d2 = d2 - n5;
                d1 = d2 * d2 * d2 * (d2 * (d2 * 6 - 15) + 10);

                for(int j6 = 0; j6 < zSize; ++j6) {
                    fl1 = zOffsetF + j6 * zScaleF + zCoord;
                    n7 = (int)fl1;

                    if (fl1 < n7) --n7;

                    n8 = n7 & 255;
                    fl1 = fl1 - n7;
                    n1 = permutations[n6];
                    n2 = permutations[n1] + n8;
                    n3 = permutations[n6 + 1];
                    n4 = permutations[n3] + n8;

                    int i7 = i++;
                    noiseArray[i7] += lerp(
                            fl1 * fl1 * fl1 * (fl1 * (fl1 * 6 - 15) + 10),
                            lerp(d1, grad2(permutations[n2], d2, fl1), grad(permutations[n4], d2 - 1, 0, fl1)),
                            lerp(d1, grad(permutations[n2 + 1], d2, 0, fl1 - 1),
                                    grad(permutations[n4 + 1], d2 - 1, 0, fl1 - 1))
                    ) * d0;
                }
            }
        else {
            final float yOffsetF = (float) yOffset, yScaleF = (float) yScale;

            float d1z = 0, d2z = 0, d3z = 0, d4z = 0, d3, d4, d5;

            for (int l2 = 0, k = -1, i4, i3, j3; l2 < xSize; ++l2) {
                d1 = l2 * xScaleF + xCoord + xOffsetF;
                i3 = (int)d1;

                if (d1 < i3) --i3;

                j3 = i3 & 255;
                d1 -= i3;
                d2 = d1 * d1 * d1 * (d1 * (d1 * 6 - 15) + 10);

                for (int k3 = 0; k3 < zSize; ++k3) {
                    d3 = zOffsetF + k3 * zScaleF + zCoord;
                    n7 = (int)d3;

                    if (d3 < n7) --n7;

                    i4 = n7 & 255;
                    d3 -= n7;
                    d4 = d3 * d3 * d3 * (d3 * (d3 * 6 - 15) + 10);

                    for (int j4 = 0; j4 < ySize; ++j4) {
                        d5 = yOffsetF + j4 * yScaleF + yCoord;
                        i3 = (int)d5;

                        if (d5 < i3) --i3;

                        n8 = i3 & 255;
                        d5 = d5 - i3;
                        fl1 = d5 * d5 * d5 * (d5 * (d5 * 6 - 15) + 10);

                        if (j4 == 0 || n8 != k) {
                            k = n8;
                            n1 = permutations[j3] + n8;
                            n2 = permutations[n1] + i4;
                            n3 = permutations[n1 + 1] + i4;
                            n4 = permutations[j3 + 1] + n8;
                            n5 = permutations[n4] + i4;
                            n6 = permutations[n4 + 1] + i4;
                            d1z = lerp(d2, grad(permutations[n2], d1, d5, d3), grad(permutations[n5], d1 - 1, d5, d3));
                            d2z = lerp(d2, grad(permutations[n3], d1, d5 - 1, d3), grad(permutations[n6], d1 - 1, d5 - 1, d3));
                            d3z = lerp(d2, grad(permutations[n2 + 1], d1, d5, d3 - 1), grad(permutations[n5 + 1], d1 - 1, d5, d3 - 1));
                            d4z = lerp(d2, grad(permutations[n3 + 1], d1, d5 - 1, d3 - 1), grad(permutations[n6 + 1], d1 - 1, d5 - 1, d3 - 1));
                        }
                        noiseArray[i++] += lerp(d4, lerp(fl1, d1z, d2z), lerp(fl1, d3z, d4z)) * d0;
                    }
                }
            }
        }
    }
}