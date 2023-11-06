package plus.te4net.core.data;

import plus.te4net.core.api.PublicUsage;


@PublicUsage
public abstract class SqlType {
    public static final String NULL     = "NULL";
    public static final SqlType VARCHAR = of("VARCHAR");
    public static final SqlType INT     = of("INT"    );
    public static final SqlType TEXT    = of("TEXT"   );
    public static final SqlType DATE    = of("DATE"   );
    public static final SqlType TIME    = of("TIME"   );
    public static final SqlType FLOAT   = of("FLOAT"  );
    public static final SqlType REAL    = of("REAL"   );


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


    @Override
    public String toString() {
        return toStr();
    }


    abstract String toStr();
}