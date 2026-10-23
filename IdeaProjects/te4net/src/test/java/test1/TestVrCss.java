package test1;

import plus.te4net.mds.html.vr.VrCss;

public class TestVrCss {
    static String code;
    static {
code = """
                        .login-form{
                            width: 18em;
                            --color-f: #FFFFE0;
                            color: var(--color-f);
                            font-size: 150%;
                            font-family: 'Exo', sans-serif;
                            test: a
                        }
        """;
}
//--bwgt: rgba(121, 121, 121, 0.439);
    //calc((100vw - var(--bwgt))/2)

    public static void main(String[] args) {
        //todo исправить ошибку экранирования
        System.out.println("STARTED");
        VrCss vrcss = new VrCss(code);

        System.out.println(vrcss);


        System.out.println("_");

    }
}







