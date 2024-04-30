package plus.fserver.utls.collections;

import net.minecraft.util.math.BlockPos;


public final class LeavesCache extends BlockPos.MutableBlockPos {
    private final byte[] updates = new byte[1093];

    public int get(int dx, int dy, int dz) {
        int cursor = ((dx + 4) << 7) | ((dy + 4) << 3) | ((dz + 4) >> 1);
        int result = updates[cursor];
        return ((dz & 1) == 1 ? (result >> 4) & 0xF : (result & 0xF)) - 3;
    }


    public static int cursor(int dx, int dy, int dz) {
        return ((dx + 4) << 8) | ((dy + 4) << 4) | (dz + 4);
    }


    public int get(int cursor) {
        int result = updates[cursor >> 1];
        return ((cursor & 1) == 1 ? (result >> 4) & 0xF : (result & 0xF)) - 3;
    }


    public void set(int cursor, int value) {
        value += 3;
        int mask, pre;
        if ((cursor & 1) == 1) {
            mask = 0x0F;
            pre = value << 4;
        } else {
            mask = 0xF0;
            pre = value;
        }
        cursor >>= 1;
        updates[cursor] = (byte) ((updates[cursor] & mask) | pre);
    }


    public void set(int dx, int dy, int dz, int value) {
        value += 3;
        int cursor = ((dx + 4) << 7) | ((dy + 4) << 3) | ((dz + 4) >> 1),
                mask, pre;

        if ((dz & 1) == 1) {
            mask = 0x0F;
            pre = value << 4;
        } else {
            mask = 0xF0;
            pre = value;
        }
        updates[cursor] = (byte) ((updates[cursor] & mask) | pre);
    }
}
