package test1;

import plus.te4net.mds.html.vr.FastJsOptimizer;

public class TestJs {
    static String code;

static {
code =
"""
                let prevLogin, prevResult, cryptokey = "", flag = false, prvN, prvR;
                const _nickMsg = "_nickMsg", _mlMsg = "_mlMsg"
                let _V=(a,e)=>{getEl(a).style.display=e?"none":"block"};

                function inputMailCode(){
                    reanet.lock().set("_lgcfm",true).unlock().set("_loginBtn", reanet.model["_loginmode"]==2?"Обновить":"Создать аккаунт")
                }

                function getGoTo(){
                    let urlParams = new URLSearchParams(window.location.search);
                    let goto = urlParams.get('goto');
                    if(goto==""||goto==null)return "/";
                   \s
                    function ofMini(link){
                        console.log(link);
                        if(link==null || link=="")return null;
                        function ofMin0(raw){
                            return raw.replaceAll("->","/").replaceAll("-e>","=")
                                    .replaceAll("-q>","?").replaceAll("-n>","&");
                        }
                        switch (link.charAt(0)) {
                            case 'H':return "https://" + ofMin0(link.substring(1));
                            case 'S':return "/";
                            case 'I':return '/'+ ofMin0(link.substring(1));
                            default:return "http://" + ofMin0(link);
                        };
                    }
                    return ofMini(goto);
                }

                function sendMail(){
                    let exist = reanet.model["_loginmode"] == 2
                    let login = getLoginM(exist);
                    if(!login)return;
                    let password = _validPsw();
                    if(!password)return;
                    let nick = getNameM(exist)
                    if(!nick)return
                   \s
                    post("/auth/api/mail-"+(exist?"restore":"newuser"), login+";"+nick+";"+password+";"+cryptokey,(answ)=>{
                        if(answ.startsWith("ERROR")){
                            let code = answ.split(" ")[1];
                            if(code == "NO_CAP4A"){
                                cryptokey = "";
                                _showCap4a();
                            } else if(code == "READY_EXIST_NAME"){
                                //TODO
                            }
                            console.log(code);
                        } else{
                            inputMailCode();
                        }
                    });
                }
                function _isValidLogin0 (login){
                    return /[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$/.test(login)?login:false;
                }
                function _isValidPsw0 (psw){
                    return /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!#$%?@-_+=^:;.<>*])(?=\\S+$).{8,24}$/.test(psw)?psw:false;
                }
                function _validNick0 (nick){
                    return (nick.length>3 && nick.length < 16 && /^\\w+$/.test(nick))?nick:false
                }

                function getLoginM(exist=true){
                    let valid = _isValidLogin()
                    if(!valid)return valid
                    if(_existLogin(valid)){
                        if(!exist){
                            reanet.set(_mlMsg,"Аккаунт уже зарегистрирован")
                            return false
                        }
                    } else {
                        if(exist){
                            reanet.set(_mlMsg,"Аккаунт не найден")
                            return false
                        }
                    }
                    return valid
                }
                function getNameM(exist=true){
                    let valid = _isValidNick()
                    if(!valid)return valid
                    if(_existName(valid)){
                        if(!exist){
                            reanet.set(_nickMsg, "Никнейм уже занят")
                            return false
                        }
                    }
                    return valid
                }

                window._isValidLogin = ()=>{
                    let el = getEl("_lg-mail")
                    if(!el || !el.value)return true
                    let res = _isValidLogin0(el.value)
                    if(!res){
                        reanet.model[_mlMsg] = "Некорректный адрес почты!"
                    }
                    return res
                }
                window._isValidNick = ()=>{
                    let el = getEl("_lg-nick")
                    if(!el || !el.value)return true
                    let nick = el.value
                    let valid = _validNick0(nick)

                    let setMsg = (msg)=>{
                        reanet.model[_nickMsg] = msg
                    }

                    if(valid){
                        return valid
                    }
                    if(nick.length<4){
                        setMsg("Минимальная длина никнейма - 4 символа!")
                    } else if(nick.length>15){
                        setMsg("Максимальная длина никнейма - 15 символов!")
                    } else {
                        setMsg("Никнейм содержит недопустимые символы!")
                    }
                    return false
                }
                window._validPsw = ()=>{
                    let el = getEl("_lg-psw")
                    if(!el || !el.value)return true
                    let psw = el.value
                    let valid = _isValidPsw0(psw)
                    if(valid)return valid
                    let setMsg = (msg)=>{
                        reanet.model["_pswMsg"] = msg
                    }
                    if(psw.length<8){
                        setMsg("Минимальная длина - 8 символов!");
                    } else if(psw.length>24){
                        setMsg("Максимальная длина - 24 символа!");
                    } else if(!/(?=.*[0-9])/.test(psw)){
                        setMsg("Пароль должен содержать не менее 1 цифры!");
                    } else if(!/(?=.*[a-z])/.test(psw)){
                        setMsg("Пароль должен содержать не менее 1 прописной буквы!");
                    } else if(!/(?=.*[A-Z])/.test(psw)){
                        setMsg("Пароль должен содержать не менее 1 заглавной буквы!");
                    } else {
                        setMsg("Пароль содержит недопустимые символы! ");
                    }
                    return false
                }
                window._isValidDoublePsw = (strong=false)=>{
                    let el = getEl("_lg-psw")
                    if(!el)return true
                    let val2 = getEl("_lg-psw-db").value
                    if(!val2)return !strong
                    return el.value == val2
                }
                window._setLgnMode = (mode)=>{
                    let msg, hdr
                    let tg1 = "Восстановление"
                    let tg2 = "Регистрация"
                    let tg3 = "Авторизация"
                    let th1 = "пароль"
                    if(mode==0){
                        msg = "Войти"
                        hdr = tg3
                    } if(mode==1){
                        msg = "Создать аккаунт"
                        hdr = tg2
                        tg2 = tg3
                    } else if(mode==2){
                        msg = "Сбросить пароль"
                        hdr = tg1
                        tg1 = tg3
                        th1 = "новый пароль"
                    }
                    reanet.model["_loginmode"] = mode
                    reanet.model["_loginBtn"]  = msg
                    reanet.model["_loginHdr"]  = hdr
                    reanet.model["_loginBf1"]  = tg1
                    reanet.model["_loginBf2"]  = tg2
                    reanet.model["_pswph"]     = th1
                }
                window._toggleMode = (left)=>{
                    let mode = reanet.model["_loginmode"]
                    reanet.model["_wgPsw"] = false
                    if(mode==0){
                        _setLgnMode(left?2:1)
                    } else if(mode==1){
                        _setLgnMode(left?2:0)\s
                    } else if(mode==2){
                        _setLgnMode(left?0:1)
                    }
                    reanet.update()
                }
                window._getCap4a = (login=getEl("_lg-mail").value)=>{
                    get("/auth/?cap4a-mtd=html&login="+login, (data)=>{
                        getEl("_cap4a").innerHTML = data;
                    });
                    getEl("_cap4a_answer").value = "";
                    getEl("_cap4a_module").style.visibility="visible";
                    _V("_cap4a_view",false);_V("_cap4a_view_success",true);
                }
                window._showCap4a = (s=false)=>{

                    if(cryptokey == ""){
                        let login = _isValidLogin()
                        if(!login || login==true)return;
                        flag = true;
                        _getCap4a(login);
                        _V("_cap4a_view",s);_V("_cap4a_view_success",!s);
                    } else{
                        flag = true;
                        _V("_cap4a_view",!s);_V("_cap4a_view_success",!s);
                    }
                }

                function _existLogin(login){
                    if(prevLogin==login && prevResult){
                        return prevResult;
                    }
                    prevLogin = login;
                    let res = syncGet("/auth/check_mail?login="+login+"&crypt-key="+cryptokey);
                    if(res=="TRUE")prevResult = true;
                    else if(res=="FALSE")prevResult = false;
                    else{\s
                        prevResult = prevLogin = null;
                        console.log(res);
                        let code = res.split(" ")[1];
                        if(code=="NO_CAP4A" || code=="INVALID_KEY"){
                            _getCap4a();
                        }
                    }
                    return prevResult;
                }

                function _existName(nick){
                    if(prvN==nick)return prvR
                    prvN = nick
                    let res = syncGet("/auth/chick_name?name="+nick+"&crypt-key="+cryptokey);
                    if(res=="TRUE")prvR = true;
                    else if(res=="FALSE")prvR = false;
                    else{\s
                        prvN = prvR = null;
                        console.log(res);
                        let code = res.split(" ")[1];
                        if(code=="NO_CAP4A" || code=="INVALID_KEY"){
                            _getCap4a();
                        }
                    }
                    return prvR;
                }

                window._checkCap4a =()=>{
                    let answer = getEl("_cap4a_answer").value;
                    let cap4aid = getEl("cap4aid").innerHTML;
                    get("/auth/?cap4a-mtd=answer&login="+getEl("_lg-mail").value+"&cap4a-id="+cap4aid+"&cap4a-answer="+answer,(data)=>{
                        if(data.startsWith("ERROR")){
                            let code = data.split(" ")[1];
                            if(code=="NO_CAP4A"){
                                _getCap4a();
                            }
                            return;
                        }
                        cryptokey = data;
                        console.log("KEY: "+cryptokey);
                        _V("_cap4a_view",true);_V("_cap4a_view_success",false);
                        if(reanet.model["_loginmode"]==2){
                            getEl("_lg-psw").style.display="inline-block";
                        }
                    });
                }
                window._onLoginClick =()=>{
                    let curMode = reanet.model["_loginmode"]
                    console.log(curMode);
                    if(reanet.model["_lgcfm"]){
                        if(curMode==1 || curMode==2){
                            let login = getEl("_lg-mail").value
                            let key = getEl("_lg-cfm").value;
                            get("/auth/confirm_"+(curMode==1?"create":"update")+"?api=1&login="+login+"&crypt-key="+key,(res)=>{
                                if(res.startsWith("SUCCESS")){
                                    window.location = getGoTo();
                                } else {
                                    reanet.model["_lgcfm"] = false
                                    // let code = data.split(" ")[1];
                                    // if(code=="NO_CAP4A"){
                                    //     _getCap4a();
                                    //     return
                                    // }
                                }
                                console.log(res);
                            })
                        }
                    } else {
                        if(cryptokey == ""){
                            if(!flag){
                                _showCap4a();
                            } else{
                                let login = getLoginM(curMode==1);
                                if(login)getCap4a(login);
                            }
                            return;
                        } else{
                            if(curMode==1 || curMode==2){
                                sendMail();
                            }
                            if(curMode==0){
                                auth();
                            }
                        }
                    }
                }

                function auth(){
                    let login = getLoginM(true);
                    if(!login)return;
                    let password = getEl("_lg-psw").value
                    if(!password)return;\s
                    post("/auth/api/login", login+";"+password+";"+cryptokey,(answ)=>{
                        //reanet.model["_pswMsg"] = msg
                        if(answ.startsWith("ERROR")){
                            let code = answ.split(" ")[1];
                            if(code == "NO_CAP4A"){
                                cryptokey = "";
                                _showCap4a();
                            } else if(code == "WRONG_PASSWORD"){
                                reanet.lock().set("_wgPsw",true).unlock().set("_pswMsg", "Неправильный пароль")
                                //setMsg("Неправильный пароль",false);
                            }
                            console.log(answ);
                        } else{
                            setTimeout(()=>{window.location = getGoTo()},200);
                        }
                    });
                }
        """;
}


    public static void main(String[] args) {

        System.out.println(new FastJsOptimizer(code).doScan());

    }
}
