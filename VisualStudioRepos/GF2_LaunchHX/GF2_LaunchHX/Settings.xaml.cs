using ArcApp;
using GF2_LaunchHX.lib;
using Microsoft.Win32;
using SourceChord.FluentWPF;
using System.IO;
using System.Text.Json;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;



namespace GF2_LaunchHX
{
    public partial class Settings : AcrylicWindow
    {
        private MainWindow mw;
        private Config cfg;
        private bool locked = true;




        public Settings(MainWindow w)
        {
            mw = w;
            UseCopyConfig();

            //GameTypes = BuildItems(new List<Tuple<string, string>> { Tuple.Create("a", "1") });


            SetExtendViewIntoTitleBar(this, true);
            InitializeComponent();

            CHSGameType.ItemsSource = mw.GetSupported().Select(x => x.id);
            //CHSGameType.SelectedItem = mw.gameType;


            Loaded += (s, e) => {locked = false;};
            Loaded += (s, e) => {FillValues();};
        }


        public void FillValues()
        {
            //var gt = MainWindow.GetGameType(cfg);

            List<PackInf0> infs = new List<PackInf0>() {
                new PackInf0().OfCfg(mw, new PackInf(){
                    name = "test",
                    version_url = "https://url",
                    branches = new() {"master", "work", "dev"},
                    def_branch = 0,
                    uInfo = new()
                })
            };



            //TT_BRN.ItemsSource = new List<string>() { "master", "work", "dev" };

            Packs.ItemsSource = infs;

        }


        public class PackInf0
        {
            public PackInf inf;
            public UPacksInfo uinf;

            public int? Priority
            {
                get => uinf.priority; set => uinf.priority = value;
            }
            public string Name
            {
                get => inf.name; set => inf.name = uinf.name = value;
            }
            public string Source
            {
                get => inf.version_url; set => inf.version_url = value;
            }
            public ComboBoxItem? Branch
            {
                get ; 
                set;
            }
            public List<ComboBoxItem>? Branches {get; set;}

            private int IndexBranchOf(string name)
            {
                for (int i = 0; i < inf.branches.Count; i++)
                {
                    if (inf.branches[i] == name)return i;
                }
                return 0;
            }

            public PackInf0 OfCfg(MainWindow mw, PackInf inf)
            {
                this.inf = inf;
                this.uinf = inf.uInfo;

                var br = (mw.TryFindResource("ArcSecondText") as Brush);
                uinf.selected_branch = inf.def_branch;

                Branches = (inf.branches.Select((bn) => {
                    var v0 = inf.branches[inf.def_branch] == bn;
                    var itm = BuildItem01(bn, bn, null, -1, -1);
                    if (v0)
                    {
                        Branch = itm;
                        (itm.Content as TextBlock).Foreground = mw.TryFindResource("ArcGreenHover") as Brush;
                    }
                    return itm;
                })).ToList();

                return this;
            }
        }


        private void ComboBox_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (locked) return;
            try
            {
                locked = true;
                var cb = (sender as ComboBox);

                //Future += () => { 
                //    cb.SelectedIndex = 2; 
                //};

                
                //cb.SelectionBoxItem
                //cb.SelectedItem = cb.Items[2];
                //cb.Text = "dev";
            }
            finally
            {
                locked = false;
            }
        }


        private static ComboBoxItem BuildItem01(string? tag, string? display,
    System.Windows.Media.Brush? br, int height, int fontSize
    )
        {
            ComboBoxItem ci = new();
            TextBlock b = new();

            if(br != null) b.Foreground = br;

            b.Background = Brushes.Transparent;

            b.Text = display;
            ci.Tag = tag;
            ci.Content = b;
            return ci;
        }


        public List<ComboBoxItem> BuildItems(List<Tuple<string, string>> content)
        {
            var br = (mw.TryFindResource("ArcSecondText") as System.Windows.Media.Brush);
            return BuildItemsR(content, br, 26, 22);
        }


        private static List<ComboBoxItem> BuildItemsR(List<Tuple<string, string>> content, 
            System.Windows.Media.Brush? br, int height, int fontSize
            )
        {
            
            return content.Select((a) => BuildItem0(a.Item1, a.Item2, br, height, fontSize)).ToList();
        }


        private static ComboBoxItem BuildItem0(string tag, string display, 
            System.Windows.Media.Brush? br, int height, int fontSize
            )
        {
            ComboBoxItem ci = new();
            TextBox b = new();
            b.Foreground = br;
            b.Background = Brushes.Transparent;
            if(height != -1)   b.Height = 26;
            if(fontSize != -1) b.FontSize = 22;

            b.Text = display;
            ci.Tag = tag;
            ci.Content = b;
            return ci;
        }


        private bool CheckLMStudioInstalled()
        {

            if (cfg.lm_studio_path != null && cfg.lm_studio_path != "") 
            {
                return File.Exists(cfg.lm_studio_path);
            }

            string userProfilePath = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);

            return File.Exists(userProfilePath + "\\.lmstudio\\settings.json");
        }


        private void UseCopyConfig()
        {
            cfg = JsonSerializer.Deserialize<Config>(JsonSerializer.Serialize(mw.config));
            cfg.shouldSave = mw.config.shouldSave;
        }


        protected override void OnClosed(EventArgs e)
        {
            mw.OnChildClosed(this);
            base.OnClosed(e);
        }


        protected void ResetPacks_Click(object sender, RoutedEventArgs e)
        {
            var gt = MainWindow.GetGameType(cfg);
            if (gt != null) 
            {
                cfg.packs = gt.default_info;
            }
            else
            {
                cfg.packs = null;
            }
            //todo update visual
            cfg.shouldSave = true;
        }


        private void SaveSettinsClick(object sender, RoutedEventArgs e)
        {
            mw.config = cfg;
            UseCopyConfig();

            if (mw.SaveCfg())
            {
                new CDialog(mw).Title("Инфо")
                    .Discription("Сохранено!").SetActions(CDialog.A_YES).ShowDialog();
            }
            else
            {
                mw.OnError("Файл заблокирован!\nИзменения не были сохранены\nна диск!");
            }
        }


        private void CHSDiscordRPC1_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (locked) return;
            string uid = ((sender as ComboBox).SelectedItem as ComboBoxItem).Uid;
            cfg.enable_discord = (uid == "dsc1en");
            cfg.shouldSave = true;
        }


        private string GetSelected(object sender)
        {
            return ((sender as ComboBox).SelectedItem as ComboBoxItem).Uid;
        }


        private void RollbackSelected(object sender, SelectionChangedEventArgs e)
        {
            try
            {
                locked = true;
                (sender as ComboBox).SelectedItem = e.RemovedItems[0];
            }
            finally
            {
                locked = false;
            }
        }


        protected void CHSTranslateMode_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if(locked) return;
            string uid = GetSelected(sender);

            bool back = false;

            if (uid == "tr0neir")
            {
                if (CheckLMStudioInstalled())
                {
                    bool c1 = false;
                    l1: if (string.IsNullOrEmpty(cfg.lm_studio_path) || !File.Exists(cfg.lm_studio_path))
                    {
                        if (c1)
                        {
                            back = true;
                            goto l2;
                        }

                        c1 = true;

                        Nullable<bool> result = null;

                        OpenFileDialog dlg = new OpenFileDialog();

                        dlg.Title = "Укажите путь к LM Studio";
                        dlg.DefaultExt = ".exe";
                        dlg.Filter = "LM Studio|*.exe";

                        result = dlg.ShowDialog();

                        if (result != null && result.Value) 
                        {
                            cfg.lm_studio_path = dlg.FileName;
                            cfg.shouldSave = true;
                            goto l1;
                        }
                    }

                    cfg.translation_method = 2;
                    cfg.shouldSave = true;
                } else
                {
                    back = true;
                    var cd = new CDialog(mw)
                        .Title("LM Studio не установлена!")
                        .Discription("LM Studio не установлена!\nНеобхобимо скачать и установить\nдля работы доперевода через ИИ!")
                        .SetActions(CDialog.A_NO, (w) => {
                            ArcUtils.OpenURL("https://lmstudio.ai/");
                            w.Close();
                        });
                    cd.Width += 100;
                    cd.ShowDialog();  
                }
            }

            cfg.translation_method = uid == "tr0onl"?1:0;
            cfg.shouldSave = true;

            l2: {
                if (back)
                {
                    RollbackSelected(sender, e);
                }
            }
            //cfg.translation_method = 1;
        }


        private void CHSprsLaunch_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (locked) return;
            string uid = GetSelected(sender);
            cfg.suppress_gf2_launcher_method = uid == "sps0en" ? 1 : 0;
            cfg.shouldSave = true;
        }

        private void CHSGameType_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {

        }
    }
}
