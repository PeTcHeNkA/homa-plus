package test1;

import plus.te4net.mds.html.vr.FastJsOptimizer;

public class TestJs {
    static String code;

static {
code = """
(()=>{
    let flag;

    /*document.addEventListener("mousemove",(e)=>{
        if(curPage!=1)return;
        s
        dx = e.clientX/window.innerWidth-1;
        let y = e.clientY/window.innerHeight-1;

        bg0.style.top = (3*y)+ "%";
        s
    })*/
    function run(mark0=mark){
    }
})();
        """;
}


    public static void main(String[] args) {

        System.out.println(new FastJsOptimizer(code).doScan());

    }
}
