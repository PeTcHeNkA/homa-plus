package plus.te4net.mds.img;


public interface ImgSize {
    int calc(int i);
    String suffix();

    ImgSize P100 = new ImgSize() {
        @Override
        public int calc(int i) {
            return i;
        }

        @Override
        public String suffix() {
            return "P100";
        }
    };
    ImgSize P75 = new ImgSize() {
        @Override
        public int calc(int i) {
            return (i / 4) * 3;
        }

        @Override
        public String suffix() {
            return "P75";
        }
    };
    ImgSize P50 = new ImgSize() {
        @Override
        public int calc(int i) {
            return i / 2;
        }

        @Override
        public String suffix() {
            return "P50";
        }
    };
    ImgSize ORIG = new ImgSize() {
        @Override
        public int calc(int i) {
            return i;
        }

        @Override
        public String suffix() {
            return "ORIG";
        }
    };


    static ImgSize getDef(String suffix){
        char[] chars = suffix.toCharArray();
        if(chars[0]=='P'){
            if(chars[1]=='1' && chars[2]=='0' && chars[3]=='0')return P100;
            if(chars[1]=='7' && chars[2]=='5')return P75;
            if(chars[1]=='5' && chars[2]=='0')return P50;
        }
        return null;
    }


    static String suffix(ImgSize size){
        if(size==null)return ORIG.suffix();
        return size.suffix();
    }


    default int calcHs(){
        return suffix().hashCode()+(31*calc(100));
    }
}
