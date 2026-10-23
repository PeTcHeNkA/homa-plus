package org.example;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class Main {
    private static final String data = "1 8fe041 a 69bdb8fe 1a a88c52b6 12 de255 1d cefb5bf d 5918315 1b 2861 11 2e7275 5 8a2f886a b 79e1e18 18 7245f 1a 7c473555 8 9cd1 6 10053d b 3c448225 10 ba760 11 9320 15 6d5d57e 0 61dea 9 62a3 a 2ab838 9 0641665 d 49ce2 12 d8138 11 14c0f 5 aef46917 7 71c9e0b9 5 a33f4 a 587b947 9 49a4 3 c14e254 17 2c5951 11 ba9fa11 15 87ce8e b bf4fc4 5 209e d ca643 a 51298 4 898b 8 0565 5 0b866b8 15 34d1 1d 4edb13 18 baf1d 14 3f5c9e9 6 21ae0 6 0a6b 15 32e8112 19 a4fb5ca 1 86192 18 28f9b44 1 8be691 11 bdf2e6 11 d6527a1 9 8612b b a758 8 08cbe 8 eaf9ec7 6 778c3d09 12 d03d 13 8035f 2 78f04 13 21d9 d 77fc1d 1 58a42 1d 9a382c f bca8 10 550908 16 49d068 1 33db4 18 5bd2 1b 80085aa a 0db6 17 a20f88bd 1b decff17b d 62fd 6 46518f a 4c62 c f414d4c 6 181b62e 9 e934eb0 1 983f a dddbe970 19 253396cc 2 2051 4 2542 9 ad8ee6cf 2 b6538 1a c479 13 69f648b6 6 b3b41489 2 5f650549 15 26d547a1 13 7d44398e 3 7c251eb5 3 63d93f 0 80dd8c 18 e13ee 6 2405b 1d 73cb88ea 8 196366 5 4700 12 14b0 8 1e11b 18 071c c 58f5 14 676391 c 872574c b 1b0ffb 6 2d5a 16 870542 d a25fd8b 1d 048c 11 94b399d 10 53968 17 f2ad 1c 9a3a9af 17 953e0 1c 26450d44 18 89ab0 13 aa796f7 4 907fd 1d 9ad32 c 9142b70 0 5c45 15 f9eda 0 8106bd62 b 53598e d a74a8 b 43ef 5 15d5 19 1d319e 1a 8274 f f515 14 57d9b 0 1b6990 1 590a0 1a 1792891 13 414b a b14b661 3 0955566 14 ee5561 1b c0c3 d d2f203 4 0fbb216 2 86fcc9c6 18 30bb5022 7 9a01d8a 1 0d5ec3a 19 17c12 9 0803 19 6f978b98 3 5bf0 18 5c1b4 c 4a8660fc 15 46bf0a a f428b84 15 1881 1b 5cb656 1 603c9e4 14 2fef044d 18 e8fc6e45 5 c870c0e d 387b 1a 44ca 1d 928a 13 0d7b40c 3 d4e2 8 9bfc 1c bea8 c aea2 15 cce96 10 9ede606 18 c600e c e249 11 d126d 19 96604 b f90d0 6 18697 18 7854debc 7 37cf3929 4 ea71 15 385e84 5 d20dfc8 11 1f1d 9 4d9ef2 b e29dde 1a c646070 6 b9395 14 6b4041b b ad960a 5 d25b18 f 377533 3 68fd09 2 fb85fd 12 8ed976d9 17 b5a0 4 5edf8 13 b59c297b 1d 3ce7 3 6f4cee8 16 64c2653 14 ba0e38e 18 1ff51242 19 d2fa16 19 62ecfa 6 da04a b c1a1 6 6310 6 9ad3 18 cc9bf 11 0a19c 17 7d5b5 8 86f94 17 ec3f 4 2c19a318 a a035 1b 2db0ccf d 78a3e5d2 19 3d3f8 16 f8afa6b0 10 c33f e 780bcf6 a 22202 d 6e412 16 71e52 10 f27a9c 9 f3106a10 5 dd42e b 59f7 19 6c5a35f 7 1f78 15 831a a f7aea9 15 cd4f2c07 8 17bd 3 3dcb 1a a0db9fbc 2 9053e 7 97cf4677 1b ca651ff1 d 2b7e8 2 5f19a92 d b707580 2 866afb14 8 d6014 4 53fe0ab 13 4561 12 a52f0 1a 7bc64 c 0acece 1b 01fc 0 a7bd6b0e c bb85 9 b4c5f92 2 a02177a 13 88a1a3 9 3c438 d a5ed74e1 8 73378 14 093ad 12 9fad1e 1d b493b32 f 5e6d 17 1dc64c 8 7ddeac3 2 1d13e805 9 570995f 14 3f7ad15f 1 1fed 7 9ef9f 1d 6cbc7 1 94760 b a27c26 8 20e07 c 0dc2 0 d680 2 bfad9488 7 bf42 4 3f9a79 a 35f06 c a2a2c9 b 102cb b 52f6d2 1c 43fae5 1b 5687 19 638655 5 31858 9 7e329d 17 ea4d5 13 f293c5 9 f79869 5 addb55 0 d04206 c 44bc5e6a d a5c93f9f 9 f5a35 8 b1b8 17 8b7f 12 1b7580 1c 9334909 4 5d431925 16 b11e4 5 866069 d fb813 a 3fa6 a dfbcd18 b dd16c1b8 b 08931 17 66989 9 28e5f7a 7 da39 1 1a0fa030 18 5f915 1a 73fc1 3 b591e2d4 16 800e470 0 5e01db 15 0db9 18 ade575 10 4fb9 c 3a3f 14 a8a40 11 e023b d 3865c 1d 84c7f746 6 82ce80 1c 96ce4 6 3c5bb48e 3 281ea1 14 9e6e 1a 31ae78 4 ad01 1a 03afb 10 f284 6 ecf735 1d 994784c5 8 b0231e80 10 4614bb 15 ba6f8cbc 6 5c86285e 10 0dbfaf 15 6deccc 11 a07a 3 a8a625 c 1a9d3c61 5 2357a7 10 1a58 c 2e29e4 2 f2f19 1b a634 11 8923a7 d 9ede5ae 10 be1fc28e 18 e9f49 f 4f7e46 10 b2b63fc 1b ff3d2f77 1 ca0ec 12 1b1f416 c 50ff0e 1 789096 1 bcca 1 6a9f 16 4bb7980 19 83908 6 ebf8 2 a6704 19 f42b b 7f8cb 2 b5e9 16 2dd0e24 13 bd48bd3f 17 c37fb91 19 2cbe6b0 12 c4ed12 16 0b49e 1a 2dc4 1a 6e3077 15 71b115b8 18 760417b 12 53da 9 e22d43f 0 f5bc7 14 1534 1 a93fdb 1 3e6adda 8 37fbde2 c 376c d 24cfd130 12 43f26 7 547f470b 18 5176d c d188aaae 2 968b e 4f44e a b9ebd 10 cae3 1b ea2cb8 17 3a956aac 6 293b80ff d 72a0 e d95d159a 1d 894a c 4450c 7 de05c7 8 849209d 1c 23db 17 c352 8 bcefd257 d c427 17 c33d714b 16 4156c 18 e9b5 0 70f052 8 d4faad 19 3303 7 b3783d f 24e9 b 071667 4 b8b7e5 6 93f470a 2 66a5 13 6b13b9d 8 e38f63 0 c3d75402 f efefa9 1c 4f6ab9db 15 c80f0 12 ac25 3 db73b 4 71a106 11 3d9f8 16 f416 8 962cc5 1d e585 12 cbb0fe 17 ab34aea 10 4d5c20d c 0cac 13 7267df9 10 ee7331 0 720b0 18 aed69 0 03f0 19 d06e44 1d 2426e4 13 5132 1 19ec7d4 17 75f5ce 15 076f 1c 8812e 1a b909e 6 7f9c 14 59ee28e2 b 30a10ba2 f aca9 17 1b7a9 10 2512a01b 1b 394d9f 18 ff852 d 779c65 19 b07a61fb f 958ceb 12 611e1ce d a946 12 38948864 3 aae2d 3 016e64c e bb92 14 f4676 4 551d35c 18 444b 18 939d6b b e724680a d a16e6a 18 62df3e c aa631c f 42786f b 942c 13 905e 13 f06a3 10 e745ec64 3 79b8d 1c 4760cf 1 493ed7f 1 3f6eb56 1b 3a7754f4 c 0e68f6 2 44bc b ef9d93db 1a cd665f3 9 511a 14 b25f3c 14 d7d76e c 8fc09531 16 6af3a 14 9caa8248 c 31496d f 82129a";

    public static void main(String[] args) throws IOException {
        args = data.split(" ");


        //args = "key1 value1 key2 value2 key1 valueData".split(" ");
        //        DataInputStream in = new DataInputStream(System.in);
//        args = in.readLine().split(" ");

        HashMap<String, AtomicInteger> map = new HashMap<>();
        int max = 0;


        for (int i = 0, l = args.length; i < l; i += 2){
            AtomicInteger prev = map.get(args[i]);
            int num = 0;
            if(prev != null)num = prev.get();
            else {
                map.put(args[i], prev = new AtomicInteger());
            }
            int summ = num + args[i+1].length();
            prev.set(summ);

            if(summ > max)max = summ;
        }

        System.out.println(max);
    }
}