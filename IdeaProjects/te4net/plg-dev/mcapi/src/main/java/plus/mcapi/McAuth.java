package plus.mcapi;

import io.netty.handler.codec.http.HttpHeaderNames;
import plus.apiutils.UUIDUtils;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.api.Var;
import plus.te4net.core.auth.AuthException;
import plus.te4net.core.auth.TokenInfo;
import plus.te4net.core.auth.UserData;
import plus.te4net.core.auth.UserMap;
import plus.te4net.core.data.SqlType;
import plus.te4net.core.node.HEWorker;
import plus.te4net.mds.proc.AuthRoute;
import plus.tson.TsonMap;
import plus.tson.utl.Tuple;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;


public class McAuth extends AuthRoute<McUser> {
    public static String basicApiRoute = "mcapi/";
    public static McAuth inst;
    private final byte[] dataAuthCli = "CLI".getBytes(StandardCharsets.UTF_8);

    public McAuth(HEWorker worker, @Var(key = "database", def = "mcusers.db")
        String database, TsonMap mailAuth) {
        super(worker, new MMap(worker.pool(), database), mailAuth);
        inst = this;
    }


    @Override
    protected void onNotfound(RequestSession session) throws AuthException {
        String sp0 = session.splat()[0];
        session.setHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        switch (sp0) {
            case "authclient" -> {
                Tuple<McUser, String> res = authNoKey(getLogin(session), session.queryParams("psw"), dataAuthCli);
                setToken(res.B, session);
                session.send(res.B);
                return;
            }
        }
        super.onNotfound(session);
    }


    @Override
    protected void onNotfoundPost(RequestSession session) {
        String sp0 = session.splat()[1];
        session.setHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        switch (sp0) {
            case "logouthclient" ->{
                String token = session.encodeBodyAsStr();
                session.send("");
                if (token.isEmpty()){
                    token = getToken(session);
                    if (token == null || token.isEmpty()) return;
                }
                logout(token, dataAuthCli);
                return;
            }
            case "username" -> {
                String token = session.encodeBodyAsStr();
                if (token.isEmpty()) {
                    token = getToken(session);
                    if (token == null || token.isEmpty()) {
                        session.send("NULL");
                        return;
                    }
                }
                McUser user = auth(token, dataAuthCli);
                if(user==null){
                    session.send("NULL");
                } else {
                    session.send(user.name+";"+user.uuid);
                }
                return;
            }
            case "canauth" -> {
                String token = session.encodeBodyAsStr();
                if (token.isEmpty()) {
                    token = getToken(session);
                    if (token == null || token.isEmpty()) {
                        session.send("FALSE");
                        return;
                    }
                }
                McUser usr = auth(token, dataAuthCli);
                session.send(usr == null ? "FALSE" : "TRUE");
                return;
            }
        }
        super.onNotfoundPost(session);
    }

    public McUser getByUUID(String uuid) {
        return ((MMap)userMap).getByUUID(uuid);
    }


    public ArrayList<McUser> getByName(String name) {
        return ((MMap)userMap).getByName(name);
    }


    static class MMap extends UserMap<McUser> {
        public MMap(ScheduledExecutorService service, String database) {
            super(service, new Data(database));
        }

        public McUser getByUUID(String uuid) {
            try {
                return ((Data)users).getByUUID(uuid);
            } catch (SQLException e) {
                return null;
            }
        }


        private ArrayList<McUser> getByName(String name){
            return ((Data)users).getUserFromNameI(name);
        }


        @Override
        public McUser put(TokenInfo key, McUser value) {
            return super.put(key, value);
        }


        @Override
        public boolean remove(Object key, Object value) {
            return super.remove(key, value);
        }


        @Override
        public void update(McUser user) {
            super.update(user);
        }
    }

    static class Data extends UserData<McUser> {
        public Data(String database) {
            super(database);
            try {
                addColumn("USERS", "UUID", SqlType.VARCHAR,        true);
            } catch (SQLException e) {}
        }


        protected ArrayList<McUser> getUserFromNameI(String name){
            String sqlCode = "SELECT LOGIN, PASSWORD, DATA FROM USERS WHERE LOWER(NAME) LIKE LOWER('" + name + "')";
            ArrayList<McUser> users = new ArrayList<>();
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


        @Override
        public McUser getUserFromName(String name) {
            String sqlCode = "SELECT LOGIN, PASSWORD, UUID, DATA FROM USERS WHERE NAME = '" + name + "'";
            AtomicReference<McUser> user = new AtomicReference<>();
            try {
                runSql(sqlCode, result -> {
                    if(!result.next())return;
                    user.set(new McUser(
                            result.getString(1),
                            result.getString(2),
                            name,
                            result.getString(3),
                            result.getBytes(4)
                    ));
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return user.get();
        }


        @Override
        public McUser getUserFromToken(String token, byte[] data) {
            String sqlCode = "SELECT LOGIN, PASSWORD, NAME, UUID, DATA FROM USERS WHERE LOGIN = (SELECT LOGIN FROM TOKENS WHERE TOKEN = '" + token + "' and DATA = ? and TIME > " + (int)(System.currentTimeMillis() >> 14) + ")";
            PreparedStatement ps = null;

            try {
                ps = this.connection.prepareStatement(sqlCode);
                ps.setBytes(1, data);
            } catch (SQLException var11) {
                throw new RuntimeException(var11);
            }

            ResultSet result = null;
            McUser user = null;
            try {
                result = ps.executeQuery();
                if (result.next()) {
                    user = new McUser(
                            result.getString(1),
                            result.getString(2),
                            result.getString(3),
                            result.getString(4),
                            result.getBytes(5)
                    );
                }
                result.close();
                ps.close();
            } catch (Exception var10) {
                try {
                    ps.close();
                    if (result != null) {
                        result.close();
                    }
                } catch (SQLException ignored) {}
            }
            return user;
        }


        @Override
        public McUser getUserFromLogin(String login) {
            String sqlCode = "SELECT PASSWORD, NAME, UUID, DATA FROM USERS WHERE LOGIN = '" + login + "';";
            AtomicReference<McUser> user = new AtomicReference<>();
            try {
                runSql(sqlCode, result -> {
                    if(!result.next())return;
                    user.set(new McUser(
                            login,
                            result.getString(1),
                            result.getString(2),
                            result.getString(3),
                            result.getBytes(4)
                    ));
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return user.get();
        }


        protected McUser getByUUID(String uuid) throws SQLException {
            String sqlCode = "SELECT LOGIN, PASSWORD, NAME, DATA FROM USERS WHERE UUID = '" + uuid + "'";
            AtomicReference<McUser> user = new AtomicReference<>();
            runSql(sqlCode, result -> {
                if(!result.next())return;
                user.set(new McUser(
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
        public McUser newUser(String login, String psw, String name, byte[] bytes) {
            return new McUser(login, psw, name, UUIDUtils.fromUUID(UUID.randomUUID()), bytes);
        }


        public void addUserInTable(McUser user) {
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
        public McUser editPassword(McUser user, String password) {
            return new McUser(user.login, password, user.name, user.uuid, user.getData());
        }
    }
}