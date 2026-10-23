using ArcApp;
using System.IO;
using System.Net;
using System.Security.Policy;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using static GF2_LaunchHX.lib.RepoLoader;


namespace GF2_LaunchHX.lib
{
    internal class RepoLoader
    {
        private Config cfg;

        public RepoLoader(Config cfg0) {
            cfg = cfg0;
        }


        public static Tuple<GameType, List<PackInf>> GetSelectedPacks(Config cfg)
        {
            var gameType = MainWindow.GetGameType(cfg);

            List<PackInf> infs = new();

            foreach(var up in gameType.packs)
            {
                var name = up.Key;
                up.Value.name = name;

                if(cfg.packs == null)
                {
                    cfg.packs = gameType.default_info;
                    cfg.shouldSave = true;
                }
                foreach(var sp in cfg.packs)
                {
                    if(sp.name == name)
                    {
                        up.Value.uInfo = sp;
                        infs.Add(up.Value);
                        break;
                    }
                }
            }
            infs.Sort((pi1, pi2) => {
                return pi1.uInfo.priority > pi2.uInfo.priority ? 1 : -1;
            });
            return new Tuple<GameType, List<PackInf>>(gameType, infs);
        }


        public void Translate(LangTable tb, TranslationCallback cb, Action<TranslationCallback, List<ELine>> translateFunc)
        {
            var pks = GetSelectedPacksInfs(cfg);
            TranslateA1(pks, tb, cb, translateFunc);
        }


        //todo
        public static void TranslateA1(Tuple<GameType, List<PackInf>, string> pks, LangTable tb, TranslationCallback cb, Action<TranslationCallback, List<ELine>> translateFunc)
        {
            PackLoader loader = new PackLoader(pks.Item2);

            var lr = loader.Load(cb);

            cb.Reset();
            cb.status = "Слияние";

            tb.MergeAll(lr.GetLines());

            if (translateFunc != null)
            {
                cb.Reset();
                cb.status = "Перевод";
                var ls = tb.GetNotTranslated(false);
                cb.maxNum = ls.Count;
                translateFunc.Invoke(cb, ls);
            }

            cb.Reset();
            cb.status = "Иньекция";

            tb.Inject(pks.Item1.magic_version.id, pks.Item3);

            cb.Reset();
        }


        public static Tuple<GameType, List<PackInf>, string> GetSelectedPacksInfs(Config cfg)
        {
            var pks = GetSelectedPacks(cfg);
            var gameType = pks.Item1;

            List<PackInf> infs0 = pks.Item2;

            string version0 = gameType.prefix + "|";
            foreach (var inf0 in infs0)
            {
                version0 += (inf0.version + ";");
            }

            if (version0.Length > 20)
            {
                version0 = version0.Substring(0, 20);
            }

            return Tuple.Create(gameType, infs0, version0);
        }
    }


    public class UPacksInfo
    {
        public string name { get; set; }
        public string version { get; set; }
        public int? priority { get; set; }
        public int selected_branch { get; set; }
    }


    public class MagicVersion
    {
        public int id { get; set; }
        public string key { get; set; }
    }


    public class PackInf
    {
        private static string MAGIC_NULL = "NULL";
        //[JsonIgnore]
        //public int priority { get; set; }
        [JsonIgnore]
        public string name { get; set; }
        public string source_url { get; set; }
        public string version_url{ get; set; }
        public string cache_file { get; set; }
        public int def_branch { get; set; }
        [JsonIgnore]
        public string version { get; set; }
        public List<string> branches { get; set; }
        [JsonIgnore]
        private string? remoteVersion { get; set; }
        [JsonIgnore]
        private string? remoteTxt { get; set; }
        [JsonIgnore]
        public UPacksInfo uInfo { get; set; }

        public string GetSRCURL()
        {
            return ReplBranch(source_url);
        }


        public string GetVersionURL()
        {
            return ReplBranch(version_url);
        }


        private string ReplBranch(string str)
        {
            if (branches == null || branches.Count == 0)
            {
                return str;
            }
            var branch = uInfo.selected_branch;
            if (branch == -1)
            {
                branch = def_branch;
            }
            var br = branches[branch];
            return str.Replace("%__BRANCH%__%", br);
        }


        public string GetRemoteTxt()
        {
            if (remoteTxt == null)
            {
                var txt = ArcUtils.LoadStr(GetVersionURL());
                if (txt == null)
                {
                    txt = MAGIC_NULL;
                }
                remoteTxt = txt;
            }

            if (remoteTxt == MAGIC_NULL) return null;

            return remoteTxt;

            //string firstLine = log.Split("\n")[0];
        }


        public string GetRemoteVersion()
        {
            if (remoteVersion == null){
                var txt0 = GetRemoteTxt();
                if (txt0 != null)
                {
                    remoteVersion = txt0.Split("\n")[0];
                } 
                else
                {
                    remoteVersion = MAGIC_NULL;
                }
            }
            if (remoteVersion == MAGIC_NULL) return null;
            return remoteVersion;
        }


        public string GetCacheFileName()
        {
            //var pre = "./data/cache/";
            //var name0 = name;
            //if (name.Length > 20) {
            //    name0 = name.Substring(20);
            //}
            return MainWindow.TRANSLATION_CACHE_PATH + cache_file + ".json";
        }


        private static void AddC(TranslationCallback cb, ref int n, int count)
        {
            if(n <= 0)
            {
                return;
            }
            if(count > n)
            {
                count = n;
            }
            n -= count;
            cb.Add(count);
        }
        

        public PackResult GetData(TranslationCallback cb)
        {
            PackResult res = new PackResult();
            res.inf = this;

            var srcUrl = GetSRCURL();
            int n = 10;

            if (!string.IsNullOrEmpty(srcUrl))
            {
                try
                {
                    var isPack = false;
                    if ((isPack = srcUrl.StartsWith("pack:")) || srcUrl.StartsWith("http"))
                    {
                        n -= 1;
                        cb.Add(1);
                        if (File.Exists(cache_file))
                        {
                            if (GetRemoteVersion() == version)
                            {
                                res.data = File.ReadAllBytes(GetCacheFileName());
                                goto l1;
                            }
                        }
                        res.data = ArcUtils.LoadBytes(srcUrl.Replace("pack:","https:"));
                        AddC(cb, ref n, 1);

                        if (res.data != null)
                        {
                            cb.status = "Загрузка " + res.inf.name;
                            if (isPack)
                            {
                                var res10 = FilesInfo.Read(res.data);
                                var res1 = res10.LoadLines();
                                n -= 2;
                                cb.Add(2);

                                res.data = res1.Item2;
                                res.lines = res1.Item1;

                                res.updated = true;
                                var ch = GetCacheFileName();
                                ArcUtils.CreatePathIfNeed(ch);
                                File.WriteAllBytes(ch, res.data);
                            }
                            else
                            {
                                File.WriteAllBytes(GetCacheFileName(), res.data);
                                AddC(cb, ref n, 2);
                            }
                        }
                    }
                    else if (srcUrl.StartsWith("file:"))
                    {
                        var path = srcUrl.Substring(5);
                        if (File.Exists(path))
                        {
                            AddC(cb, ref n, 2);
                            res.data = File.ReadAllBytes(path);
                        }
                    }

                    l1: if (res.data != null && res.lines == null)
                    {
                        res.lines = LangTable.FromJsonBytes(res.data);
                    }
                }
                catch (Exception ex)
                {
                    AddC(cb, ref n, 10);
                    return null;
                }
            }
            if (res.lines == null)
            {
                res.lines = new List<ELine>();
            }
            AddC(cb, ref n, 10);
            return res;
        }
    
    
        public static List<PackInf>? ReadFrom(string str)
        {
            return JsonSerializer.Deserialize<List<PackInf>>(str);
        }
    }


    public class LoadingResult
    {
        List<ELine>? lines;
        public List<string>? wrongHosts;
        public List<PackResult> results;

        public LoadingResult(List<PackResult> results, List<string>? wrongHosts)
        {
            this.results = results;
            this.wrongHosts = wrongHosts;
        }

        public BData Data()
        {
            return new BData(this);
        }


        public void CleanCache()
        {
            lines = null;
        }


        public List<ELine> GetLines()
        {
            if(lines == null)
            {
                lines = new PackLoader.PackMerger().Merge(results);
            }
            return lines;
        }


        public List<PackResult> GetUpdated()
        {
            return results.Where(r => r.updated).ToList();
        }


        public class BData {
            public LoadingResult parent { get; }
            public byte[] data { get; }

            public BData(LoadingResult result) 
            {
                data = LangTable.ToJsonBytes(result.GetLines(), true, new GroupComparator());
            }

            public void SaveTo(string path)
            {
                File.WriteAllBytes(path, data);
            }
        }
    }


    public class PackResult
    {
        public PackInf inf;
        public byte[]? data;
        public List<ELine> lines;
        public bool updated;
    }


    public class PackLoader
    {
        private List<PackInf> packs;

        public PackLoader(List<PackInf> packs)
        {
            this.packs = packs;
        }


        public LoadingResult Load(TranslationCallback cb)
        {
            cb.Reset();
            cb.status = "Загрузка";
            cb.maxNum = packs.Count * 10;
            List<string> errorHosts = new List<string>();

            List<PackResult> results = packs.AsParallel()
                .Select(inf =>inf.GetData(cb)).ToList()
                .FindAll(inf => inf.lines != null)
                .OrderBy(inf => inf.inf.uInfo.priority).ToList();

            return new LoadingResult(results, errorHosts);
        }


        public class PackMerger
        {
            private Dictionary<int, ELine> idIndex = new Dictionary<int, ELine>();
            private Dictionary<string, List<ELine>> keyIndex = new Dictionary<string, List<ELine>>();

            public List<ELine> Merge(List<PackResult> packs)
            {
                packs.Sort((r, r2) => {
                    if (r.inf.uInfo.priority > r2.inf.uInfo.priority) return 1;
                    return -1;
                });
                List<ELine> result = new List<ELine>();
                if (packs.Count == 0) return result;
                result.AddRange(packs[0].lines.Select(line => line.Copy()));

                foreach (ELine line in result)
                {
                    AddToIndex(line);
                }

                int count = packs.Count;
                for (int i = 1; i < count; i++)
                {
                    foreach (ELine line in packs[i].lines)
                    {
                        AddToIndex(line);
                    }
                }
                return result;
            }


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
        }
    }


    public class GroupComparator : IComparer<string>
    {
        public static IComparer<string> EMPTY = new Empty();

        private class Empty : IComparer<string>
        {
            public int Compare(string o1r, string o2r)
            {
                return CompareDef(o1r, o2r);
            }
        }

        private Group defaultGroup = new Group("", 0, "default");
        private Group[] groups = new Group[0];
        private Group[][] mappingByFirst = new Group[128][];
        private Group[] noMapped = new Group[0];
        public Dictionary<string, Group> cache = new();

        public static GroupComparator fromTable(string data)
        {
            string[] lines = data.Replace("\r", "").Split("\n");
            GroupComparator comparator = new GroupComparator();

            foreach (string line in lines)
            {
                if (String.IsNullOrWhiteSpace(line)) continue;
                var chr = line[0];
                int index = line.IndexOf('|', 2);
                if (index == -1) continue;
                int num = 0;
                bool invert = line[2] == '-';
                int i = 2;
                if (invert) i = 3;

                for (; i < index; i++)
                {
                    var chr2 = line[i];
                    num *= 10;
                    num += (chr2 - '0');
                }
                if (invert)
                {
                    num = -num;
                }

                int index2 = line.IndexOf('|', index + 1);

                if (index2 == -1) index2 = line.Length;

                string groupName = line.Substring(index + 1, index2 - index - 1);

                string display = "";
                if (index2 != line.Length)
                {
                    display = line.Substring(index2 + 1, line.Length - index2 - 1).Trim();
                }
                else
                {
                    display = "group_" + num;
                }

                if (chr == 's')
                {
                    comparator.Add(groupName, num, display);
                }
                else if (chr == 'c')
                {
                    comparator.AddB(groupName, num, display);
                }
            }
            comparator.Complete();
            return comparator;
        }


        public void Add(string prefix, int sortIndex, string display)
        {
            groups = Add0(groups, new Group(prefix, sortIndex, display));
        }


        public void AddB(string prefix, int sortIndex, string display)
        {
            groups = Add0(groups, new GroupB(prefix, sortIndex, display));
        }


        private static Group[] Add0(Group[] groups, Group group)
        {
            if (groups == null) return new Group[] { group };
            Array.Resize(ref groups, groups.Length + 1);
            groups[groups.Length - 1] = group;
            return groups;
        }


        public void Complete()
        {
            Array.Sort(groups, (o1, o2) => o1.SortIndex.CompareTo(o2.SortIndex)); ;

            foreach (var group in groups)
            {
                if (group.GetType() != typeof(GroupB))
                {
                    int index = Index(group.Prefix);
                    mappingByFirst[index] = Add0(mappingByFirst[index], group);
                }
                else
                {
                    noMapped = Add0(noMapped, group);
                }
            }
        }


        private static int Index(string str)
        {
            if (string.IsNullOrEmpty(str)) return 0;
            return str[0] & 127;
        }


        public void SetDefaultGroup(string prefix, int sortIndex, string dsiplay)
        {
            defaultGroup = new Group(prefix, sortIndex, dsiplay);
        }


        public Group FindGroup(string key)
        {
            var byIndex = mappingByFirst[Index(key)];
            if (byIndex != null)
            {
                foreach (var group in byIndex)
                    if (group.IsSomeGroup(key)) return group;
            }
            foreach (var group in noMapped)
            {
                if (group.IsSomeGroup(key)) return group;
            }
            return defaultGroup;
        }


        public int Compare(string o1r, string o2r)
        {
            string o1 = (o1r);
            string o2 = (o2r);
            Group group1 = cache.TryGetValue(o1, out var groupFromCahce1) ? groupFromCahce1 : FindGroup(o1);
            Group group2 = cache.TryGetValue(o2, out var groupFromCahce2) ? groupFromCahce2 : FindGroup(o2); //fallback codefault group
            if (group1 != group2)
            {
                int result = group1.SortIndex.CompareTo(group2.SortIndex);
                if (result != 0)
                    return result;
            }
            int res = o1.Length.CompareTo(o2.Length);
            if (res != 0) return res;
            if (o1 == o1r && o2 == o2r)
                return string.Compare(o1, o2);
            else
            {
                int res2 = string.Compare(o1, o2);
                if (res2 == 0)
                {
                    uint id1 = ELine.GetIdFrom(o1r);
                    uint id2 = ELine.GetIdFrom(o2r);
                    return id1.CompareTo(id2);
                }
                return res2;
            }
        }


        private static int CompareDef(string o1, string o2)
        {
            int res2 = string.Compare(o1, o2);
            if (res2 == 0)
            {
                uint id1 = ELine.GetIdFrom(o1);
                uint id2 = ELine.GetIdFrom(o2);
                return id1.CompareTo(id2);
            }
            return res2;
        }


        public GroupComparator CleanCache()
        {
            cache = new();
            return this;
        }


        public GroupComparator Copy()
        {
            GroupComparator copy = new GroupComparator();
            copy.defaultGroup = defaultGroup;
            copy.groups = groups;
            copy.mappingByFirst = mappingByFirst;
            copy.noMapped = noMapped;

            return copy;
        }

        public GroupComparator MakeCache(List<ELine> list)
        {
            cache = buildGroupsDictionary(list, this);
            return this;
        }

        private static Dictionary<string, Group> buildGroupsDictionary(List<ELine> list2,
    GroupComparator gcom)
        {
            return list2.AsParallel().Select(x =>
            {
                return new { original = x.Original, value = gcom.FindGroup(x.Original) };
            })
                .DistinctBy(x => x.original)
                .ToDictionary(x => x.original, x => x.value);
        }
    }


    public class Group
    {
        public string Prefix { get; }
        public int SortIndex { get; set; }
        public string Display { get; }

        public Group(string prefix, int sortIndex, string display)
        {
            Prefix = prefix;
            SortIndex = sortIndex;
            Display = display;
        }

        public virtual bool IsSomeGroup(string key)
        {
            return key.StartsWith(Prefix);
        }
    }


    public class GroupB : Group
    {
        public GroupB(string prefix, int sortIndex, string display) : base(prefix, sortIndex, display) { }

        public override bool IsSomeGroup(string key)
        {
            return key.Contains(Prefix);
        }
    }


    public class FilesInfo 
    {
        public string schema { get; set; }
        public string placeholder { get; set; } = "%__FILE__%";
        public string[] include_files { get; set; }
        public string[] include_urls { get; set; }
        public Dictionary<string, string> include_lines { get; set; } = new();

        public static FilesInfo? Read(byte[] data)
        {
            return JsonSerializer.Deserialize<FilesInfo>(UTF8Encoding.UTF8.GetString(data));
        }

        public string GetFile(int idx)
        {
            if (idx > include_files.Length)
            {
                return include_urls[idx - include_files.Length];
            }
            return schema.Replace(placeholder, include_files[idx]);
        }


        public int CountFiles()
        {
            int summ = 0;
            if(include_files != null)
            {
                summ = include_files.Length;
            }
            if (include_urls != null)
            {
                summ += include_urls.Length;
            }
            return summ;
        }

        public Tuple<List<ELine>, byte[]> LoadLines()
        {
            try
            {
                List<List<ELine>> lists = new();
                int summ0 = 0;

                using (WebClient webClient = new WebClient())
                {
                    int max = CountFiles();

                    for (int i = 0; i < max; i++)
                    {
                        var lst0 = LangTable.FromJsonBytes(webClient.DownloadData(GetFile(i)));
                    
                        lists.Add(lst0);
                        summ0 += lst0.Count;
                    }


                    //return webClient.DownloadData();
                }

                lists.Add(LangTable.FromMap(include_lines));
                summ0 += lists[lists.Count - 1].Count;

                List<ELine> result = new(summ0);
                int cursor = 0;

                foreach (var lst in lists)
                {
                    foreach (var line in lst)
                    {
                        result[cursor++] = line;
                    }
                }
                return Tuple.Create(result, LangTable.ToJsonBytesFast(result, true));
            }
            catch
            {
                return null;
            }
        }
    }


    //public class TranslationCallback
    //{
    //    private string? _status;
    //    public int maxNum { set; get; }
    //    public int cur { set; get; }
    //    public double prevPercent = 0;
    //    public bool active { set; get; } = false;

    //    public string? status
    //    {
    //        get => _get();
    //        set => _status = value;
    //    }


    //    public void Add(int n)
    //    {
    //        cur += n;
    //    }


    //    public void Reset()
    //    {
    //        cur = 0;
    //        prevPercent = 0;
    //        active = false;
    //    }

    //    private string _get()
    //    {
    //        string prc = null;
    //        if (cur == 0)
    //        {
    //            prc = "0%";
    //        }
    //        if (maxNum <= cur)
    //        {
    //            prevPercent = 100;
    //            prc = "100%";
    //        }
    //        if (prc == null)
    //        {
    //            double a1 = cur / maxNum * 100;
    //            if (a1 > 100)
    //            {
    //                prevPercent = 100;
    //                prc = "100%";
    //            }
    //            else
    //            {
    //                prevPercent = a1;
    //                prc = a1.ToString("P1") + "%";
    //            }
    //        }
    //        if (_status == null)
    //        {
    //            return prc;
    //        }
    //        else
    //        {
    //            if (active && prevPercent > 0)
    //            {
    //                return _status + " " + prc;
    //            }
    //            else
    //            {
    //                return _status;
    //            }
    //        }
    //    }
    //}
}
