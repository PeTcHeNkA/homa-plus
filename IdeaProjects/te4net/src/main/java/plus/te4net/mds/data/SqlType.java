package plus.te4net.mds.data;

@Deprecated
public abstract class SqlType {
    public static String NULL     = "NULL";
    public static SqlType VARCHAR = of("VARCHAR");
    public static SqlType INT     = of("INT"    );
    public static SqlType TEXT    = of("TEXT"   );
    public static SqlType DATE    = of("DATE"   );
    public static SqlType TIME    = of("TIME"   );
    public static SqlType FLOAT   = of("FLOAT"  );
    public static SqlType REAL    = of("REAL"   );


    public static SqlType VARCHAR(int n){
        if(n==-1) {
            return new SqlType() {
                @Override
                String toStr() {
                    return "VARCHAR";
                }
            };
        } else {
            return new SqlType() {
                @Override
                String toStr() {
                    return "VARCHAR("+n+")";
                }
            };
        }
    }


    public static SqlType CAST(Object o, SqlType type){
        return new SqlType() {
            @Override
            String toStr() {
                return "CAST('"+o+"', AS"+type+")";
            }
        };
    }


    public static SqlType of(String var){
        return new SqlType() {
            @Override
            String toStr() {
                return var;
            }
        };
    }


    abstract String toStr();
    @Override
    public String toString() {
        return toStr();
    }
}