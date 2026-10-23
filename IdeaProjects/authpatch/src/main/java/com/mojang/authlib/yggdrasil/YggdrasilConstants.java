package com.mojang.authlib.yggdrasil;

public class YggdrasilConstants {
    //static final String BASE_URL = "https://authserver.mojang.com/";
    static final String BASE_SESSION_URL = "http://lochost.ru/mcapi/";


    static final String JOIN         = BASE_SESSION_URL + "join";
    static final String JOINED       = BASE_SESSION_URL + "hasJoined";
    static final String REFRESH      = BASE_SESSION_URL + "refresh";
    static final String VALIDATE     = BASE_SESSION_URL + "validate";

    static final String UUID_PROFILE = BASE_SESSION_URL + "profile/";
    static final String USER_PROFILE = BASE_SESSION_URL + "users/";

    static{
        System.out.println("CONSTANTS CLASS LOADED");
    }
}