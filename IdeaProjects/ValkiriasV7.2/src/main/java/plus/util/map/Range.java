package plus.util.map;


public class Range {

    public final int min;
    public final int max;

    public Range(int... range){
        if(range.length>1) {
            int min = range[0];
            int max = range[1];
            if (min < max) {
                this.min = min;
                this.max = max;
            } else {
                this.min = max;
                this.max = min;
            }
        } else {
            this.min = range[0];
            this.max = min;
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Integer i) {
            return (i >= min) && (i <= max);
        }
        if (obj instanceof Double i) {
            return (i >= min) && (i <= max);
        }
        if (obj instanceof Range t) {
            return (this.min <= t.min) && (this.max >= t.max);
        }
        return false;
    }


    public boolean lower(double d){
        return d<min;
    }


    public boolean bigger(double d){
        return d>max;
    }
}