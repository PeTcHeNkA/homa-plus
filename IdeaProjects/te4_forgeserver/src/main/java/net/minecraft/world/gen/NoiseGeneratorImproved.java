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
        this.permutations = new int[512];
        this.xCoord = random.nextFloat() * 256;
        this.yCoord = random.nextFloat() * 256;
        this.zCoord = random.nextFloat() * 256;

        int i, j, k;
        for (i = 0; i < 256; this.permutations[i] = i++);

        for (i = 0; i < 256; ++i) {
            j = random.nextInt(256 - i) + i;
            k = this.permutations[i];
            this.permutations[i] = this.permutations[j];
            this.permutations[j] = k;
            this.permutations[i + 256] = this.permutations[i];
        }
    }


    public final float lerp(float d2, float d1, float d3) {
        return d1 + d2 * (d3 - d1);
    }


    public final float grad2(int num1, final float d1, final float d2) {
        return GRAD_2X[num1 &= 15] * d1 + GRAD_2Z[num1] * d2;
    }


    public final float grad(int num1, float d1, float d2, float d3) {
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
        int i = 0, n1, n2, n3, n4, n5, n6, n7, n8;
        float d2, d1, d0 = 1 / (float)noiseScale, fl1;
        if (ySize == 1)

            for(int j2 = 0; j2 < xSize; ++j2) {
                d2 = j2 * (float)xScale + this.xCoord + (float)xOffset;
                n5 = (int)d2;

                if (d2 < n5) --n5;
                n6 = n5 & 255;

                d2 = d2 - n5;
                d1 = d2 * d2 * d2 * (d2 * (d2 * 6 - 15) + 10);

                for(int j6 = 0; j6 < zSize; ++j6) {
                    fl1 = (float) zOffset + j6 * (float)zScale + this.zCoord;
                    n7 = (int)fl1;

                    if (fl1 < n7) --n7;

                    n8 = n7 & 255;
                    fl1 = fl1 - n7;
                    n1 = this.permutations[n6];
                    n2 = this.permutations[n1] + n8;
                    n3 = this.permutations[n6 + 1];
                    n4 = this.permutations[n3] + n8;

                    int i7 = i++;
                    noiseArray[i7] += this.lerp(
                            fl1 * fl1 * fl1 * (fl1 * (fl1 * 6 - 15) + 10),
                            this.lerp(d1, this.grad2(this.permutations[n2], d2, fl1), this.grad(this.permutations[n4], d2 - 1, 0, fl1)),
                            this.lerp(d1, this.grad(this.permutations[n2 + 1], d2, 0, fl1 - 1),
                                    this.grad(this.permutations[n4 + 1], d2 - 1, 0, fl1 - 1))
                    ) * d0;
                }
            }
        else {
            float d1z = 0, d2z = 0, d3z = 0, d4z = 0, d3, d4, d5;

            for (int l2 = 0, k = -1, i4, i3, j3; l2 < xSize; ++l2) {
                d1 = l2 * (float)xScale + this.xCoord + (float)xOffset;
                i3 = (int)d1;

                if (d1 < i3) --i3;

                j3 = i3 & 255;
                d1 -= i3;
                d2 = d1 * d1 * d1 * (d1 * (d1 * 6 - 15) + 10);

                for (int k3 = 0; k3 < zSize; ++k3) {
                    d3 = (float)zOffset + k3 * (float)zScale + this.zCoord;
                    n7 = (int)d3;

                    if (d3 < n7) --n7;

                    i4 = n7 & 255;
                    d3 -= n7;
                    d4 = d3 * d3 * d3 * (d3 * (d3 * 6 - 15) + 10);

                    for (int j4 = 0; j4 < ySize; ++j4) {
                        d5 = (float)yOffset + j4 * (float)yScale + this.yCoord;
                        i3 = (int)d5;

                        if (d5 < i3) --i3;

                        n8 = i3 & 255;
                        d5 = d5 - i3;
                        fl1 = d5 * d5 * d5 * (d5 * (d5 * 6 - 15) + 10);

                        if (j4 == 0 || n8 != k) {
                            k = n8;
                            n1 = this.permutations[j3] + n8;
                            n2 = this.permutations[n1] + i4;
                            n3 = this.permutations[n1 + 1] + i4;
                            n4 = this.permutations[j3 + 1] + n8;
                            n5 = this.permutations[n4] + i4;
                            n6 = this.permutations[n4 + 1] + i4;
                            d1z = this.lerp(d2, this.grad(this.permutations[n2], d1, d5, d3), this.grad(this.permutations[n5], d1 - 1, d5, d3));
                            d2z = this.lerp(d2, this.grad(this.permutations[n3], d1, d5 - 1, d3), this.grad(this.permutations[n6], d1 - 1, d5 - 1, d3));
                            d3z = this.lerp(d2, this.grad(this.permutations[n2 + 1], d1, d5, d3 - 1), this.grad(this.permutations[n5 + 1], d1 - 1, d5, d3 - 1));
                            d4z = this.lerp(d2, this.grad(this.permutations[n3 + 1], d1, d5 - 1, d3 - 1), this.grad(this.permutations[n6 + 1], d1 - 1, d5 - 1, d3 - 1));
                        }
                        noiseArray[i++] += this.lerp(d4, this.lerp(fl1, d1z, d2z), this.lerp(fl1, d3z, d4z)) * d0;
                    }
                }
            }
        }
    }
}