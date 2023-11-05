
import plus.te4net.mds.html.vr.FastJsOptimizer;


public class Test {
    //public static RouteMap<String, RouteKey> map = new RouteMap<>();

    public static void main(String[] args) throws Exception {


        String code = "                        console.log(code);\n" +
                "                    } else{\n" +
                "                        inputMailCode();\n" +
                "                    }\n" +
                "                    });\n" +
                "                }\n" +
                "            function _isValidLogin0 (login){\n" +
                "                return /[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$/.test(login)?login:false;\n" +
                "            }\n" +
                "            function _isValidPsw0 (psw){\n" +
                "                return /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!#$%?@-_+=^:;.<>*])(?=\\S+$).{8,24}$/.test(psw)?psw:false;\n" +
                "            }\n" +
                "            function _validNick0 (nick){\n" +
                "                return (nick.length>3 && nick.length < 16 && /^\\w+$/.test(nick))?nick:false\n" +
                "            }\n" +
                "\n" +
                "            function getLoginM(exist=true){\n" +
                "                let valid = _isValidLogin()\n" +
                "                if(_existLogin(valid)){\n" +
                "                    if(!exist){\n" +
                "                        setMsg(\"Аккаунт не найден\")\n" +
                "                        return false\n" +
                "                    }";

        String code2 = "            let prevLogin, prevResult, cfmMode, cryptokey\n" +
                "            let _V=(a,e)=>{getEl(a).style.display=e?\"none\":\"block\"};\n" +
                "\n" +
                "            function sendMail(){";

        System.out.println();

        System.out.println(new FastJsOptimizer(code2).doScan());

    }


}
