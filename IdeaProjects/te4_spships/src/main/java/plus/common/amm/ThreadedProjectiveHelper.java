package plus.common.amm;

import io.netty.util.internal.ConcurrentSet;


public class ThreadedProjectiveHelper extends Thread{
    private static final ThreadedProjectiveHelper INST = new ThreadedProjectiveHelper();

    public ConcurrentSet<SpBullet> data = new ConcurrentSet<>();
    public boolean isActive = false;

    private ThreadedProjectiveHelper(){
        setName("Projective Thread");
        setDaemon(true);
        start();
    }

    public static void listen(SpBullet bullet){
        INST.data.add(bullet);
        synchronized (INST) {
            if (!INST.isActive) {
                INST.notify();
            }
        }
    }

    @Override
    public void run() {
        long prevTick = System.currentTimeMillis();
        while (true){
            boolean flag = true;
            for(SpBullet bullet:data){
                if(bullet.isDead){
                    data.remove(bullet);
                } else {
//                    bullet.raytraceresult = ProjectileHelper.
//                            forwardsRaycast(bullet, true,
//                                    bullet.ticksInAir > 26, bullet.thrower);
//                    if(bullet.raytraceresult != null){
//                        data.remove(bullet);
//                    }
                }
                if(flag){
                    flag = false;
                }
            }
            if(flag) {
                long curTick = System.currentTimeMillis();
                int range = (int) (curTick - prevTick);
                prevTick = curTick;
                if (range < 20) {
                    try {
                        Thread.sleep(20 - range);
                    } catch (InterruptedException ignored) {}
                }
            } else {
                try {
                    synchronized (this){
                        isActive = false;
                        wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
