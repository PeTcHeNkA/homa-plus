
using System.IO;
using ProtoBuf;
using ProtoBuf.Serializers;

namespace GF2_RU_Launch
{
    //proto
    [ProtoContract]
    public class TIndexItem
    {
        [ProtoMember(1)]
        public uint beginOff;
        [ProtoMember(2)]
        public uint length;
    }
    [ProtoContract]
    public class TIndex
    {
        [ProtoMember(1)]
        public int dist;
        [ProtoMember(2)]
        public bool sect;
        [ProtoMember(3)]
        public Dictionary<uint, TIndexItem> index;
    }
    [ProtoContract]
    public class TItem
    {
        [ProtoMember(1)]
        public long id;
        [ProtoMember(2)]
        public string name;
    }
    [ProtoContract]
    public class Translations
    {
        [ProtoMember(1)]
        public List<TItem> data;
    }
    //proto end

    public class LangEditCS
    {
        public ELine[] lines {  get; private set; }
        private int dist = 10;
        private bool sect = true;

        private Dictionary<int, ELine> idIndex = new Dictionary<int, ELine>();
        private Dictionary<string, List<ELine>> keyIndex = new Dictionary<string, List<ELine>>();


        //Table IO start
        public void Read(byte[] data)
        {
            int len = ReadInt(data);

            //byte[] chunk1 = Copy(data, 4, len);
            byte[] chunk2 = Copy(data, len + 4, data.Length - (len + 4));
            readLines(chunk2);

            foreach (var line in lines) {
                addToIndex(line);
            }
        }


        private void readLines(byte[] data1)
        {
            var trs = Serializer.Deserialize<Translations>(new MemoryStream(data1));
            if (trs == null) return;

            lines = new ELine[trs.data.Count];

            var data = trs.data;

            for (int i = 0; i < lines.Length; i++)
            {
                var trs0 = data.ElementAt(i);
                ELine line = new ELine();
                line.Id = (int)trs0.id;
                line.Original = trs0.name;
                lines[i] = line;
            }
        }


        public byte[] Write()
        {
            WriteCallback callback = new WriteCallback();
            var tableOut = new MemoryStream();

            var index = new TIndex();
            index.dist = dist;
            index.sect = sect;
            index.index = new Dictionary<uint, TIndexItem>();

            int cursor = 0;

            while (cursor < lines.Length)
            {
                writeNext10(cursor, lines, tableOut, callback);

                cursor += callback.count;

                TIndexItem item = new TIndexItem();
                item.beginOff = (uint)callback.startOff;
                item.length   = (uint)callback.bLen;

                index.index.Add((uint)callback.index, item);
            }

            var indexOut = new MemoryStream();
            Serializer.Serialize(indexOut, index);

            byte[] indexBytes = indexOut.ToArray();
            byte[] tableBytes = tableOut.ToArray();

            byte[] all = new byte[indexBytes.Length + tableBytes.Length + 4];
            BitConverter.GetBytes(indexBytes.Length).CopyTo(all, 0);

            Array.Copy(indexBytes, 0, all, 4, indexBytes.Length);
            Array.Copy(tableBytes, 0, all, 4 + indexBytes.Length, tableBytes.Length);

            return all;
        }


        private static void writeNext10(int from, ELine[] lines, Stream outs, WriteCallback c)
        {
            c.startOff = outs.Length;

            int indexId = lines[from].Id / 10;
            int count = 0;

            Translations trsTemp = new Translations();
            trsTemp.data = new List<TItem>();

            for (int i = from; i < from + 10 && i < lines.Length; i++)
            {
                ELine line = lines[i];
                if (line.Id / 10 != indexId) break;

                TItem item = new TItem();
                item.id = line.Id;
                item.name = line.actualValue();

                trsTemp.data.Add(item);
                count += 1;
            }

            Serializer.Serialize(outs, trsTemp);

            c.index    = indexId;
            c.count    = count;

            c.bLen = outs.Length - c.startOff;
        }


        private class WriteCallback
        {
            public long index;
            public int count = 0;
            public long bLen = 0;
            public long startOff = 0;
        }


        public static byte[] Copy(byte[] data, int from, int len)
        {
            byte[] result = new byte[len];
            Array.Copy(data, from, result, 0, len);
            return result;
        }


        public static void WriteInt(byte[] buf, int data)
        {
            buf[3] = (byte)((data >> 24) & 0xff);
            buf[2] = (byte)((data >> 16) & 0xff);
            buf[1] = (byte)((data >> 8) & 0xff);
            buf[0] = (byte)(data & 0xff);
        }


        public static int ReadInt(byte[] data)
        {
            return (data[3] & 0xff) << 24
                    | (data[2] & 0xff) << 16
                    | (data[1] & 0xff) << 8
                    | (data[0] & 0xff);
        }
        //Table IO end


        private void addToIndex(ELine line)
        {
            if (line.Id != 0)
            {
                if (!idIndex.ContainsKey(line.Id))
                {
                    idIndex.Add(line.Id, line);
                }
                else
                {
                    if (line.Value != null)
                    {
                        idIndex[line.Id].Value = line.Value;
                    }
                }

            }
            if (line.Original == null) return;
            if (keyIndex.ContainsKey(line.Original))
            {
                var list = keyIndex[line.Original];
                bool needAdd = true;
                foreach (var kv in list)
                {
                    if (kv.Id == line.Id)
                    {
                        needAdd = false;
                        if (line.Value != null)
                        {
                            kv.Value = line.Value;
                        }
                    }
                }
                if (needAdd)
                {
                    list.Add(line);
                }
            }
            else
            {
                keyIndex[line.Original] = new List<ELine> { line };
            }
        }
    
    
        public void modifyLine(ELine line)
        {
            if(line.Id != 0)
            {
                if (idIndex.ContainsKey(line.Id))
                {
                    idIndex[line.Id].Value = line.actualValue();
                    return;
                }
            }
            if (keyIndex.ContainsKey(line.Original))
            {
                foreach(ELine l in keyIndex[line.Original])
                {
                    l.Value = line.actualValue();
                }
            }
        }


        public void mergeAll(List<ELine> lines)
        {
            foreach (var line in lines)
            {
                modifyLine(line);
            }
        }


        public List<ELine> getNotTranslated(bool onlyNotExists)
        {
            List<ELine> lines1 = new List<ELine>();

            foreach(ELine line in lines)
            {
                if(line.Original != null)
                {
                    if (((line.Value == line.Original && !onlyNotExists) || line.Value == null) 
                        && Editor.isTranslatable(line.Original))
                    {
                        lines1.Add(line);
                    }

                }
            }
            return lines1;
        }
    }
}
