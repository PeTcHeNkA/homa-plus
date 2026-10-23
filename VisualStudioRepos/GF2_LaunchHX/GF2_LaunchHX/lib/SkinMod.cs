using ProtoBuf;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace GF2_LaunchHX.lib
{
    [ProtoContract]
    public class TRef
    {
        [ProtoMember(1)]
        long id;
    }

    [ProtoContract]
    public class ClothesU2
    {
        [ProtoMember(1)]
        List<ClotheUItem> item;
    }


    [ProtoContract]
    public class ClotheUItem
    {
        [ProtoMember(1)]
        int? id; 

        [ProtoMember(2)]
        int? l2; 

        [ProtoMember(3)]
        int? l3; 

        [ProtoMember(4)]
        int? l4;

        [ProtoMember(5)]
        int? modelId;

        [ProtoMember(6)]
        byte[] l6;

        [ProtoMember(7)]
        string gunCode;

        [ProtoMember(8)]
        string name;

        [ProtoMember(9)]
        TRef text1;  //from old

        [ProtoMember(10)]
        TRef text2; //from old

        [ProtoMember(11)]
        int? unlockType;

        [ProtoMember(12)]
        int? unlockArg;

        [ProtoMember(13)]
        int? unlockDesc;

        [ProtoMember(14)]
        List<int> l14;

        [ProtoMember(15)]

        List<int> l15;  // ???

        [ProtoMember(16)]
        TRef text3;  //from old

        [ProtoMember(17)]
        string flag;

        [ProtoMember(19)]
        int? l19;

        [ProtoMember(21)]
        int? l21;

        [ProtoMember(22)]
        int? l22;

        [ProtoMember(23)]
        int? l23;

        [ProtoMember(24)]
        string ski3;

        [ProtoMember(25)]
        string ski4;

        [ProtoMember(26)]
        string ski5;

        [ProtoMember(27)]
        string ski6;

        [ProtoMember(28)]
        string ski7;

        [ProtoMember(29)]
        string ski8;

        [ProtoMember(31)]
        List<byte[]> l31;

        [ProtoMember(32)]
        int? b31;

        [ProtoMember(33)]
        int? b32;
    }



    internal class SkinMod
    {
        public string dir;


        public void test()
        {
            //Serializer.
        }
    }
}
