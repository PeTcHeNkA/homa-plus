package plus.valcirias.data.coder;

import plus.valcirias.avatar.AvatarFloader;
import plus.valcirias.data.loader.genetaror.ValkiriaGenerator;
import plus.valcirias.Valkiria;
import java.util.ArrayList;
import java.util.Arrays;


public abstract class BasicCoder {

    private final ComponentCoder[] coders;
    public final ValkiriaGenerator g;
    protected final ItemCoder c;

    protected BasicCoder(ItemCoder c, ValkiriaGenerator generator) {
        this.c = c;
        this.g = generator;
        CoderList coders = new CoderList();
        coders.add(
                new ComponentCoder() {
                    @Override
                    void encode(Valkiria v, String data) {
                        v.setShield(Double.parseDouble(data));
                    }

                    @Override
                    Object code(Valkiria v) {
                        return v.getShield();
                    }
                },
                new ComponentCoder() {
                    @Override
                    void encode(Valkiria v, String data) {
                        v.setAttackPoints(Integer.parseInt(data));
                    }

                    @Override
                    Object code(Valkiria v) {
                        return v.getAttackPoints();
                    }
                },
                new ComponentCoder() {
                    @Override
                    void encode(Valkiria v, String data) {
                        v.setShieldPoints(Integer.parseInt(data));
                    }

                    @Override
                    Object code(Valkiria v) {
                        return v.getShieldPoints();
                    }
                },
                new ComponentCoder() {
                    @Override
                    void encode(Valkiria v, String data) {
                        v.setCriticalPoints(Integer.parseInt(data));
                    }

                    @Override
                    Object code(Valkiria v) {
                        return v.getCriticalPoints();
                    }
                },
                new ComponentCoder() {
                    @Override
                    void encode(Valkiria v, String data) {
                        v.setEnergyPoints(Integer.parseInt(data));
                    }

                    @Override
                    Object code(Valkiria v) {
                        return v.getEnergyPoints();
                    }
                },
                new ComponentCoder() {
                    @Override
                    void encode(Valkiria v, String data) {
                        v.setAvailablePoints(Integer.parseInt(data));
                    }

                    @Override
                    Object code(Valkiria v) {
                        return v.getAvailablePoints();
                    }
                },
                new ComponentCoder() {
                    @Override
                    void encode(Valkiria v, String data) {
                        v.setCombatMode(data.equals("true"));
                    }

                    @Override
                    Object code(Valkiria v) {
                        return v.CombatMode() ? "true" : "false";
                    }
                },
                new ComponentCoder() {
                    @Override
                    void encode(Valkiria v, String data) {
                        v.setExperience(Integer.parseInt(data));
                    }

                    @Override
                    Object code(Valkiria v) {
                        return v.getExperience();
                    }
                },
                new ComponentCoder() {
                    @Override
                    void encode(Valkiria v, String data) {
                        v.setWeapon(c.encode(data));
                    }

                    @Override
                    Object code(Valkiria v) {
                        return c.code(v.getWeapon());
                    }
                },
                new ComponentCoder() {
                    @Override
                    void encode(Valkiria v, String data) {
                        if (!data.equals("null")) {
                            String[] rawAvatar = data.split(":");
                            v.setAvatar(AvatarFloader.getAvatarSet(rawAvatar[0]).getAvatar(Integer.parseInt(rawAvatar[1])));
                        }
                    }

                    @Override
                    Object code(Valkiria v) {
                        return v.getAvatar().toString();
                    }
                },
                new ComponentCoder() {
                    @Override
                    void encode(Valkiria v, String data) {
                        v.setColor(data);
                    }

                    @Override
                    Object code(Valkiria v) {
                        return v.getColor();
                    }
                },
                new ComponentCoder() {
                    @Override
                    void encode(Valkiria v, String data) {
                        v.setQuestSession(data);
                    }

                    @Override
                    Object code(Valkiria v) {
                        if (v.getQuestSession() != null) {
                            return v.getQuestSession().toString();
                        } else {
                            return "null";
                        }
                    }
                },
                new ComponentCoder() {
                    @Override
                    void encode(Valkiria v, String data) {
                        v.setPremium(data.equals("true"));
                    }

                    @Override
                    Object code(Valkiria v) {
                        return v.isPremium() ? "true" : "false";
                    }
                },
                new ComponentCoder() {
                    @Override
                    void encode(Valkiria v, String data) {
                        v.setBattlePassData(data);
                    }

                    @Override
                    Object code(Valkiria v) {
                        return v.getBattlePassData();
                    }
                }
        );

        this.coders = coders.toArray(new ComponentCoder[0]);
    }


    public ValkiriaGenerator getGenerator(){
        return g;
    }


    protected Valkiria encodeV(String rawData) {
        if(rawData==null)return g.ofString(null);
        String[] args = rawData.split(";");
        Valkiria v = g.ofString(args[0]);
        int i = 1;
        for(ComponentCoder c:coders){
            if(i>=args.length){
                System.out.printf("Loaded legacy data! Args < [%s] is default%n", i);
                break;
            }
            try {
                c.encode(v, args[i]);
            } catch (Exception e){
                System.out.printf("Critical error in [%s] coder!%n", i);
                e.printStackTrace();
            }
            ++i;
        }
        return v;
    }


    protected String codeV(Valkiria valkiria) {
        StringBuilder CodedSt = new StringBuilder();
        CodedSt.append(valkiria.ClassName()).append(";");
        for(ComponentCoder c:coders){
            CodedSt.append(c.code(valkiria)).append(";");
        }
        return CodedSt.toString();
    }
}


class CoderList extends ArrayList<ComponentCoder>{
    public void add(ComponentCoder... coders){
        this.addAll(Arrays.asList(coders));
    }
}


abstract class ComponentCoder{
    abstract void encode(Valkiria v,String data);
    abstract Object code(Valkiria v);
}