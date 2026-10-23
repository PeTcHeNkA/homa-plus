using ProtoBuf;
using ProtoBuf;
using ProtoBuf.Compiler;
using System.Collections.Generic;
using System.IO;
using System.Text;
using System.Text.Encodings.Web;
using System.Text.Json;
using System.Text.RegularExpressions;
using System.Windows.Shapes;

namespace GF2_LaunchHX.lib
{
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

    //[ProtoContract]
    //public class RawData 
    //{
    //    // Other known fields...
        
    //    private readonly List<KeyValuePair<int, object>> _unknownFields = new();

    //    // Method to access unknown fields
    //    public IEnumerable<KeyValuePair<int, object>> GetUnknownFields()
    //    {
    //        return _unknownFields;
    //    }
    //}
    ////proto end


    public class ELine
    {
        public int Id { get; set; }
        public string? Value { get; set; }
        public string Original { get; set; }


        private static Regex trs1 = new Regex("^(?:[^a-zA-Z]*|[^a-zA-Z]*[a-zA-Z][^a-zA-Z]*)$", RegexOptions.Compiled);
        private static Regex trs2 = new Regex("[A-Z][A-Z]-[1-9]-[1-9]", RegexOptions.Compiled);

        public static bool IsTranslatable(string? str)
        {
            if (String.IsNullOrWhiteSpace(str)) return false;
            if (str.Length == 1)
            {
                return false;
            }
            if (trs2.IsMatch(str))
            {
                return false;
            }
            return !trs1.IsMatch(str);
        }


        public ELine()
        {
            Original = "";
        }

        public ELine(int id, string value)
        {
            Id = id;
            Value = value;
            Original = value;
        }


        public string GetSaveKey(bool useId)
        {
            if (!useId || Id == 0) return Original;

            return "#ID:" + Id + "|" + Original;
        }


        public static uint GetIdFrom(string str)
        {
            if (str == null || str.Length <= 5) return 0;
            if (str.StartsWith("#ID:"))
            {
                int index = str.IndexOf('|', 4);
                if (index == -1) return 0;
                int num = 0;
                for (int i = 4; i < index; i++)
                {
                    num *= 10;
                    num += (str[i] - '0');
                }
                return (uint)num;
            }

            return 0;
        }


        public static string RemIdFrom(string src)
        {
            if (src.StartsWith("#ID:"))
            {
                int index = src.IndexOf("|");
                if (index == -1) return src;
                return src[(index + 1)..];
            }

            return src;
        }



        public string ActualValue()
        {
            if (Value != null) return Value;
            return Original;
        }

        public ELine Copy()
        {
            ELine copy = new ELine();
            copy.Id = Id;
            copy.Value = Value;
            copy.Original = Original;
            return copy;
        }
    }


    public class LangTable
    {
        public ELine[] lines { get; private set; }
        private int dist = 10;
        private bool sect = true;

        private Dictionary<int, ELine> idIndex = new Dictionary<int, ELine>();
        private Dictionary<string, List<ELine>> keyIndex = new Dictionary<string, List<ELine>>();


        //Table IO start
        public void Read(byte[] data)
        {
            Read(data, true);
        }


        public void Read(byte[] data, bool index)
        {
            int len = ReadInt(data);

            //byte[] chunk1 = Copy(data, 4, len);
            //byte[] chunk2 = Copy(data, len + 4, data.Length - (len + 4));
            ReadLines(data, len + 4);
            if (index)
            {
                DoIndex();
            }
        }



        public void DoIndex()
        {
            if(idIndex.Count == 0)
            {
                foreach (var line in lines)
                {
                    AddToIndex(line);
                }
            }
        }


        // "#ID:20433|English": "Английский",
        public string GetMagic(int magicId)
        {
            if(idIndex.Count != 0)
            {
                if(idIndex.ContainsKey(magicId))
                {
                    return idIndex[magicId].Original;
                }
            } 
            else
            {
                foreach (var line in lines)
                {
                    if (line.Id == magicId)
                    {
                        return line.Original;
                    }
                }
            }
            return null;
        }


        public void Inject(int id, string value)
        {
            DoIndex();

            if (idIndex.ContainsKey(id))
            {
                idIndex[id].Value = value;
            } 
            else
            {
                ELine line = new ELine();
                line.Id = id;
                line.Original = value;
                line.Value = value;

                List<ELine> list = lines.ToList();
                list.Append(line);
                list.Sort((l1, l2) => {
                    return l1.Id.CompareTo(l2.Id);
                });

                lines = list.ToArray();

                idIndex.Clear();
                DoIndex();
            }
        }


        private void ReadLines(byte[] data1, int pos)
        {
            var ms = new MemoryStream(data1);
            ms.Position = pos;
            var trs = Serializer.Deserialize<Translations>(ms);
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
                WriteNext10(cursor, lines, tableOut, callback);

                cursor += callback.count;

                TIndexItem item = new TIndexItem();
                item.beginOff = (uint)callback.startOff;
                item.length = (uint)callback.bLen;

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


        private static void WriteNext10(int from, ELine[] lines, Stream outs, WriteCallback c)
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
                item.name = line.ActualValue();

                trsTemp.data.Add(item);
                count += 1;
            }

            Serializer.Serialize(outs, trsTemp);

            c.index = indexId;
            c.count = count;

            c.bLen = outs.Length - c.startOff;
        }


        private class WriteCallback
        {
            public long index;
            public int count = 0;
            public long bLen = 0;
            public long startOff = 0;
        }


        private static byte[] Copy(byte[] data, int from, int len)
        {
            byte[] result = new byte[len];
            Array.Copy(data, from, result, 0, len);
            return result;
        }


        private static void WriteInt(byte[] buf, int data)
        {
            buf[3] = (byte)((data >> 24) & 0xff);
            buf[2] = (byte)((data >> 16) & 0xff);
            buf[1] = (byte)((data >> 8) & 0xff);
            buf[0] = (byte)(data & 0xff);
        }


        private static int ReadInt(byte[] data)
        {
            return (data[3] & 0xff) << 24
                    | (data[2] & 0xff) << 16
                    | (data[1] & 0xff) << 8
                    | (data[0] & 0xff);
        }
        //Table IO end


        private void AddToIndex(ELine line)
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


        public void ModifyLine(ELine line)
        {
            if (line.Id != 0)
            {
                if (idIndex.ContainsKey(line.Id))
                {
                    idIndex[line.Id].Value = line.ActualValue();
                    return;
                }
            }
            if (keyIndex.ContainsKey(line.Original))
            {
                foreach (ELine l in keyIndex[line.Original])
                {
                    l.Value = line.ActualValue();
                }
            }
        }


        public List<ELine>? GetLines(ELine key)
        {
            if (key.Id != 0)
            {
                if (idIndex.ContainsKey(key.Id))
                {
                    return new List<ELine>{idIndex[key.Id]};
                }
            }
            if (keyIndex.ContainsKey(key.Original))
            {
                return keyIndex[key.Original];
            }
            return null;
        }


        public void MergeAll(List<ELine> lines)
        {
            //maybe sync?
            lines.AsParallel().ForAll(ModifyLine);
        }


        public List<ELine> GetNotTranslated(bool onlyNotExists)
        {
            List<ELine> lines1 = new List<ELine>();

            foreach (ELine line in lines)
            {
                if (line.Original != null)
                {
                    if (((line.Value == line.Original && !onlyNotExists) || line.Value == null)
                        && ELine.IsTranslatable(line.Original))
                    {
                        lines1.Add(line);
                    }

                }
            }
            return lines1;
        }


        public static List<ELine> FromJsonBytes(byte[] data)
        {
            
            var map = JsonSerializer.Deserialize<Dictionary<string, string>>(UTF8Encoding.UTF8.GetString(data));
            return FromMap(map);
        }


        //public static T des(T t)
        //{
        //    return JsonSerializer.Deserialize<T>(UTF8Encoding.UTF8.GetString(data));
        //}


        public static List<ELine> FromMap(Dictionary<string, string> map)
        {
            List<ELine> list = new List<ELine>();
            if (map == null) return list;

            foreach (var item in map)
            {
                var id = ELine.GetIdFrom(item.Key);

                var orig = ELine.RemIdFrom(item.Key);
                ELine eLine = new ELine((int)id, orig);
                eLine.Value = item.Value;
                list.Add(eLine);
            }
            return list;
        }


        public static JsonSerializerOptions UNSAFE_OPTIONS = new JsonSerializerOptions
        {
            Encoder = JavaScriptEncoder.UnsafeRelaxedJsonEscaping,
            WriteIndented = true,
        };


        public static byte[] ToJsonBytesFast(List<ELine> list, bool useId)
        {
            var map = list.Select(l => { return new { k = l.GetSaveKey(useId), v = l.Value }; }).ToDictionary(x => x, x => x);

            var result = JsonSerializer.Serialize(map, UNSAFE_OPTIONS);
            return UTF8Encoding.UTF8.GetBytes(result);
        }


        //???
        public static byte[] ToJsonBytes(List<ELine> list, bool useId, IComparer<string> comparer)
        {
            var map = list.Select(l => { return new { key = l.GetSaveKey(useId), value = l.Value }; }).OrderBy(l =>
            {
                if (l.key.StartsWith("#ID:"))
                {
                    int index = l.key.IndexOf("|");
                    if (index == -1) return l.key;
                    return l.key.Substring(index + 1);
                }

                return l.key;
            }, comparer).DistinctBy(x => x.key).ToDictionary(x => x.key, x => x.value);

            var result = JsonSerializer.Serialize(map, UNSAFE_OPTIONS);
            return UTF8Encoding.UTF8.GetBytes(result);
        }
    }
}