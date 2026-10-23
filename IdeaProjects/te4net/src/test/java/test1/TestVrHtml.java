package test1;

import plus.te4net.mds.html.vr.VrHtml;

public class TestVrHtml {
    static String code;

    static {
        code = "<div no-slim    fix='a' foo></div>";



    }


    public static void main(String[] args) {
        VrHtml vrhtml = new VrHtml(code).uptCss().uptJs(null);

        System.out.println(vrhtml);
    }
}
