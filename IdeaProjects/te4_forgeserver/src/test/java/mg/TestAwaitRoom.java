package mg;

import plus.fserver.world.addon.pvpworld.MinigameAwaitRoom;
import java.util.HashMap;
import java.util.UUID;


public class TestAwaitRoom {
    private static final HashMap<UUID,User> users = new HashMap<>();

    public static User[] all = new User[]{
            new User(10),
            new User(11),
            new User(17), //3
            new User(10),
            new User(11),
            new User(20), //6
            new User(16),
            new User(17),
            new User(19), //9
    };


    public static void main(String[] args) {
        MinigameAwaitRoom room = new MinigameAwaitRoom(3, 3){
            @Override
            protected int getScore(UUID uuid) {
                User user = users.get(uuid);
                if(user == null)return 0;
                return user.score;
            }
        };

        for (User user:all){
            room.tryAdd(user.uuid);
        }

        print(room.calcScores());

        room.reBalance();
        System.out.println("-----");

        print(room.calcScores());
    }


    private static void print(int[][] scores){
        for (int i = 0; i < scores.length;i++){
            System.out.println(i+"| sum: "+MinigameAwaitRoom.sum(scores[i]));
        }
    }



    public static class User{
        final UUID uuid = UUID.randomUUID();
        final int score;

        public User(int score) {
            this.score = score;
            users.put(uuid, this);
        }
    }
}
