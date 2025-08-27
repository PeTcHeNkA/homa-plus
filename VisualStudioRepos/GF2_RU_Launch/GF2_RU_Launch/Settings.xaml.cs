using System.IO;
using System.Windows;
using System.Windows.Controls;
using SortJsonUtils;
using SourceChord.FluentWPF;


namespace GF2_RU_Launch
{
    public partial class Settings : AcrylicWindow
    {
        public static string UN_BRANCH = "master";
        //private MainWindow parent;
        public Settings(MainWindow main)
        {
            //this.parent = main;
            UN_BRANCH = MainWindow.getBranch();
            InitializeComponent();

            AppConfig.Get().Reinit();
            LoadConfigValues();
        }


        public void LoadConfigValues()
        {
            //AppConfig.Get().Reinit();
            Packs.ItemsSource = getPacks();
            DisplayDS();
            DisplayLSPR();
            DisplayTranslationMode();
        }


        public void SaveConfigValues()
        {
            AppConfig.Get().ConfirmChanges();
            List<PackInf> infs = Packs.ItemsSource as List<PackInf>;
            WritePacks(infs);
        }

        public static List<PackInf> getPacks()
        {
            var cfg = AppConfig.Get();
            int count = cfg.getInt("GFT_Packs_Count");
            List<PackInf> list = new List<PackInf> (count);

            var piv = cfg.getStr("GFT_Compability");
            if(piv == null || piv != MainWindow.PACKS_COMPABILITY)
            {
                count = 0;
                AppConfig.Get().setStr("GFT_Compability", MainWindow.PACKS_COMPABILITY);
                AppConfig.Get().Save();
            }

            if (count > 0) {
                int cursor = 0;
                while (cursor < count) { 
                    PackInf packInf = new PackInf();
                    packInf.Priority = cfg.getInt("GFT_Packs_" + cursor + "_P");
                    packInf.Name     = cfg.getStr("GFT_Packs_" + cursor + "_N");
                    packInf.Source   = cfg.getStr("GFT_Packs_" + cursor + "_S");

                    list.Add(packInf);

                    ++cursor;
                }
            } else
            {
                getDefaultPack(list);
            }
            return list;
        }


        public static void getDefaultPack(List<PackInf> list)
        {
            PackInf inf0 = new PackInf();
            inf0.Priority = 0;
            inf0.Name = "RuTeam";
            inf0.Source = "https://gitlab.com/gf2ruteam/grils-frontline-team-translate/-/raw/master/translations_release.json?ref_type=heads&inline=false";


            PackInf inf = new PackInf();
            inf.Priority = 1;
            inf.Name = "Homa";
            inf.Source = MainWindow.GF2_LOAD_URL;

            list.Add(inf0);
            list.Add(inf);
        }


        public static void WritePacks(List<PackInf> list)
        {
            var cfg = AppConfig.Get();
            int count = cfg.getInt("GFT_Packs_Count");
           
            if (count > 0)
            {
                int cursor = 0;
                while (cursor < count)
                {
                    cfg.del("GFT_Packs_" + cursor + "_P");
                    cfg.del("GFT_Packs_" + cursor + "_N");
                    cfg.del("GFT_Packs_" + cursor + "_S");

                    ++cursor;
                }
            }

            count = list.Count;
            cfg.setInt("GFT_Packs_Count", count);

            if (count > 0)
            {
                int cursor = 0;
                while (cursor < count)
                {
                    PackInf inf = list[cursor];
                    cfg.setInt("GFT_Packs_" + cursor + "_P", inf.Priority);
                    cfg.setStr("GFT_Packs_" + cursor + "_N", inf.Name);
                    cfg.setStr("GFT_Packs_" + cursor + "_S", inf.Source);

                    ++cursor;
                }
            }
            cfg.Save();
        }


        private void ResetSettinsClick(object sender, RoutedEventArgs e)
        {
            AppConfig.Get().rollbackChanges();
            LoadConfigValues();
        }


        private void SaveSettinsClick(object sender, RoutedEventArgs e)
        {
            SaveConfigValues();
        }


        private void DisplayDS()
        {
            if(AppConfig.Get().check("GFT_Discord", false))
            {
                CHSDiscordRPC.SelectedIndex = 0; //0 == true
            }
            else
            {
                CHSDiscordRPC.SelectedIndex = 1;
            }
        }


        private void DisplayLSPR()
        {
            if(AppConfig.Get().check("GFT_LaunchSuppression", false))
            {
                CHSprs.SelectedIndex = 1;
            }
            else
            {
                CHSprs.SelectedIndex = 0;
            }
        }


        private void CHSDiscordRPC_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            string uid = ((sender as ComboBox).SelectedItem as ComboBoxItem).Uid;
            AppConfig.Get().setBool("GFT_Discord", uid == "dsc1en");
        }


        private void ResetPacks_Click(object sender, RoutedEventArgs e)
        {
            List<PackInf> packs = new List<PackInf>();
            getDefaultPack(packs);
            Packs.ItemsSource = packs;
        }


        private void DisplayTranslationMode()
        {
            var tag0 = AppConfig.Get().getStr("GFT_DTranlation");
            if (tag0 == null || tag0 == "disabled") return;
            foreach (var item in CHSTranslateMode.Items)
            {
                ComboBoxItem ci = item as ComboBoxItem;
                if(ci.Tag != null)
                {
                    var tag = ci.Tag.ToString();
                    if(tag == tag0)
                    {
                        ci.IsSelected = true;
                        return;
                    }
                }
            }
        }


        private void CHSTranslateMode_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            var tag = ((sender as ComboBox).SelectedItem as ComboBoxItem).Tag;
            string param;
            if(tag == null)
            {
                param = "disabled";
            }
            else
            {
                param = tag.ToString();
            }
            AppConfig.Get().setStr("GFT_DTranlation", param);
        }


        private void CHSDiscordRPC1_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            bool enable = ((sender as ComboBox).SelectedItem as ComboBoxItem).Tag.ToString() == "1";
            AppConfig.Get().setBool("GFT_LaunchSuppression", enable);
        }
    }


    public class PackInf
    {
        public int Priority { get; set; }
        public string Name { get; set; }
        public string Source { get; set; }
    }


    public class PackLoader
    {
        private List<PackInf> packs;

        public PackLoader()
        {
            packs = Settings.getPacks();
        }


        public bool loadAndMergeTo(string path)
        {
            MainWindow.CreatePathIfNeed(path);

            var lines = loadAndMerge();
            if(lines == null || lines.Count == 0)
            {
                return false;
            }
            byte[] data = Editor.toJsonBytes(lines, true, new GroupComparator());
            if(data != null)
            {
                File.WriteAllBytes(path, data);
                return true;
            }
            return false;
        }


        public List<ELine> loadAndMerge()
        {
            List<PackResult> results = packs.AsParallel().Select(inf =>
            {
                PackResult res = new PackResult();
                res.inf = inf;
                if (!string.IsNullOrEmpty(inf.Source))
                {
                    try
                    {
                        if (inf.Source.StartsWith("http"))
                        {
                            res.data = MainWindow.loadBytes(MainWindow.prepareLoadUrl(inf.Source));

                        }
                        else if (inf.Source.StartsWith("file:"))
                        {
                            var path = inf.Source.Substring(5);
                            if (File.Exists(path))
                            {
                                res.data = File.ReadAllBytes(path);
                            }
                        }

                        if (res.data != null)
                        {
                            res.lines = Editor.fromJsonBytes(res.data);
                        }
                    }
                    catch (Exception ex)
                    {
                        //
                    }
                }
                if (res.lines == null)
                {
                    res.lines = new List<ELine>();
                }
                return res;
            }).ToList().FindAll(inf => inf.lines != null)
                .OrderBy(inf => inf.inf.Priority).ToList();
            return new PackMerger().merge(results);
        }


        private class PackResult
        {
            public PackInf inf;
            public byte[]? data;
            public List<ELine> lines;
        }


        private class PackMerger
        {
            private Dictionary<int, ELine> idIndex = new Dictionary<int, ELine>();
            private Dictionary<string, List<ELine>> keyIndex = new Dictionary<string, List<ELine>>();

            public List<ELine> merge(List<PackResult> packs)
            {
                packs.Sort((r, r2) => {
                    if (r.inf.Priority > r2.inf.Priority) return 1;
                    return -1;
                });
                List<ELine> result = new List<ELine>();
                if (packs.Count == 0) return result;
                result.AddRange(packs[0].lines.Select(line => line.copy()));

                foreach (ELine line in result)
                {
                    addToIndex(line);
                }

                int count = packs.Count;
                for (int i = 1; i < count; i++)
                {
                    foreach (ELine line in packs[i].lines)
                    {
                        addToIndex(line.copy());
                    }
                }
                return result;
            }


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
                        if(line.Value != null)
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
                        if(kv.Id == line.Id)
                        {
                            needAdd = false;
                            if(line.Value != null) {
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
                    keyIndex[line.Original] = new List<ELine>{line};
                }
            }
        }
    }
}