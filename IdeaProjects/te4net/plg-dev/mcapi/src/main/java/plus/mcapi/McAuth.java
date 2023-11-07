package plus.mcapi;

import plus.apiutils.UUIDUtils;
import plus.te4net.core.api.Var;
import plus.te4net.core.auth.TokenInfo;
import plus.te4net.core.auth.UserData;
import plus.te4net.core.auth.UserMap;
import plus.te4net.core.data.SqlType;
import plus.te4net.core.node.HEWorker;
import plus.te4net.mds.proc.AuthRoute;
import plus.tson.TsonMap;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;


public class McAuth extends AuthRoute<MUser> {
    public static String basicApiRoute = "mcapi/";

    public static McAuth inst;
    public McAuth(HEWorker worker, @Var(key = "database", def = "mcusers.db")
        String database, TsonMap mailAuth) {
        super(worker, new MMap(database), mailAuth);
        inst = this;
    }


    public MUser getByUUID(String uuid) {
        return ((MMap)userMap).getByUUID(uuid);
    }


    public ArrayList<MUser> getByName(String name) {
        return ((MMap)userMap).getByName(name);
    }


    static class MMap extends UserMap<MUser> {
        public MMap(String database) {
            super(new Data(database));
        }

        public MUser getByUUID(String uuid) {
            try {
                return ((Data)users).getByUUID(uuid);
            } catch (SQLException e) {
                return null;
            }
        }


        private ArrayList<MUser> getByName(String name){
            return ((Data)users).getUserFromNameI(name);
        }


        @Override
        public MUser put(TokenInfo key, MUser value) {
            return super.put(key, value);
        }


        @Override
        public boolean remove(Object key, Object value) {
            return super.remove(key, value);
        }


        @Override
        public void update(MUser user) {
            super.update(user);
        }
    }

    static class Data extends UserData<MUser> {
        public Data(String database) {
            super(database);
            try {
                addColumn("USERS", "UUID", SqlType.VARCHAR,        true);
            } catch (SQLException e) {}
        }


        protected ArrayList<MUser> getUserFromNameI(String name){
            String sqlCode = "SELECT LOGIN, PASSWORD, DATA FROM USERS WHERE LOWER(NAME) LIKE LOWER('" + name + "')";
            ArrayList<MUser> users = new ArrayList<>();
            try {
                runSql(sqlCode, result -> {
                    if(!result.next())return;
                    users.add(newUser(
                            result.getString(1),
                            result.getString(2),
                            name,
                            result.getBytes(3)
                    ));
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return users;
        }


        protected MUser getByUUID(String uuid) throws SQLException {
            String sqlCode = "SELECT LOGIN, PASSWORD, NAME, DATA FROM USERS WHERE UUID = '" + uuid + "'";
            AtomicReference<MUser> user = new AtomicReference<>();
            runSql(sqlCode, result -> {
                if(!result.next())return;
                user.set(new MUser(
                        result.getString(1),
                        result.getString(2),
                        result.getString(3),
                        uuid,
                        result.getBytes(4)
                ));
            });
            return user.get();
        }


        @Override
        public MUser newUser(String login, String psw, String name, byte[] bytes) {
            return new MUser(login, psw, name, UUIDUtils.fromUUID(UUID.randomUUID()), bytes);
        }


        public void addUserInTable(MUser user) {
            String sqlCode = "INSERT INTO USERS (LOGIN, PASSWORD, NAME, UUID, DATA) VALUES ('"
             + user.login +"', '"+ user.password +"', '"+ user.name +"', '"+ user.uuid +"', ?)";

            try {
                this.runSqlWithBytes(sqlCode, user.getData());
            } catch (SQLException var4) {
                System.err.println("ERROR CREATING USER [" + user.login + ", " + user.name + "]");
                throw new RuntimeException(var4);
            }
        }


        @Override
        public MUser editPassword(MUser user, String password) {
            return new MUser(user.login, password, user.name, user.uuid, user.getData());
        }
    }
}