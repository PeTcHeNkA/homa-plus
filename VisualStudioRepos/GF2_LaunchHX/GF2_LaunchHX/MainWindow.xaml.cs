using ArcApp;
using GF2_LaunchHX.lib;
using SourceChord.FluentWPF;
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Text.Encodings.Web;
using System.Text.Json;
using System.Text.Json.Serialization;
using System.Windows;
using System.Windows.Input;
using MediaDevices;

namespace GF2_LaunchHX
{
    //Fluent WPF Acrylic APP template by HomaPlus
    public partial class MainWindow : AcrylicWindow
    {
        public static string APP_VERSION = "0.3.1";
        public static string APP_DATA_PATH = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData).ToString() + "\\gf2rulaunch\\";
        public static string TRANSLATION_CACHE_PATH = APP_DATA_PATH + "packs\\";

        public static string CONFIG_FILE_NAME = "config.json";

        public static string CONFIG_FILE_PATH = APP_DATA_PATH + "config.json";
        public static string LU_URL_FORMAT = "https://gitlab.com/homaplus/girls-frontline-2-exilium-ru-launcher/-/raw/natives/launcher/%FILE_NAME%?ref_type=heads&inline=false";

        private static string BG_URL_FORMAT = "https://gitlab.com/homaplus/girls-frontline-2-exilium-ru-launcher/-/raw/natives/backgrounds/%BG_NAME%.png?ref_type=heads&inline=false";
        private static string BG_CACHE_PATH = APP_DATA_PATH + "bg_cache\\";
        private static string BG_INFS_FILE_NAME = "backgrounds_infs.json";
        public  static string DS_APP_TOKEN = "1378747345121841272";

        private List<Tuple<Task, int>> tasks = new(); //2 - is loading remote config //3 - is installing pack

        public Config config;
        public CF<byte[]> packBytes;
        public CF<GameType> gameType;
        public bool readyInstalled = false;

        public IOProxy io;
        public PhoneGame pg;

        public MainWindow()
        {
            var cb0 = Task.Run(() => LoadCfg());
            SetExtendViewIntoTitleBar(this, true);
            InitializeComponent();
            SetupBG();
            io = new WinStdProxy(this);

            SetupCF();
            ScanAndroidAsync();

            cb0.Wait();
            SetDsRpc(config.enable_discord);
            UpdateBtns01();

            Closing += (o, e) =>
            {
                if (config.shouldSave)
                {
                    SaveCfg();
                }
            };
        }


        public void EnableASD()
        {
            //MediaDevices.
        }


        public void ScanAndroidAsync()
        {
            Task.Run(() => {
                Visibility vb;
                string devName = "";
                if (ScanAndroid())
                {
                    vb = Visibility.Visible;
                    devName = pg.GetDeviceName();
                }
                else
                {
                    vb = Visibility.Hidden;
                }
                if (vb != InstallModeBX.Visibility)
                {
                    Dispatcher.Invoke(() => {
                        InstallModeBX.Visibility = vb;
                        if (devName != null)
                        {
                            AndroidDVName.Text = "Установить на " + devName;
                        }
                    });
                }
            });
        }


        public bool ScanAndroid()
        {
            var gt = gameType.V;
            if (gt.android == null) return false;
            pg = PhoneGame.ScanPhones(gt.android.app_id);
            return pg != null;
        }


        public bool IsAndroid()
        {
            return io.GetType() != typeof(WinStdProxy);
        }


        public void LoadCfg()
        {
            if (File.Exists(CONFIG_FILE_PATH))
            {
                byte[] data = File.ReadAllBytes(CONFIG_FILE_PATH);
                var cfg1 = JsonSerializer.Deserialize<Config>(UTF8Encoding.UTF8.GetString(data));
                if (cfg1 != null) 
                {
                    config = cfg1;
                }
            }
            if(config == null)
            {
                config = new Config();
            }

            if(config.game_types == null)
            {
                RunShoulded(() =>
                {
                    string data20 = ArcUtils.LoadStr(LU_URL_FORMAT.Replace("%FILE_NAME%", "default_packs.json"));
                    var cfg2 = JsonSerializer.Deserialize<Config>(data20);
                    config.game_types = cfg2.game_types;
                    config.shouldSave = true;
                }, "Не удалось получить\nудаленную конфигурацию!", 2);
            }
        }


        private Task RunShoulded(Action a, string? err, int lockNum)
        {
            Task t;
            tasks.Add(Tuple.Create(t = Task.Run(() => {
                try
                {
                    a.Invoke();
                }
                catch (Exception ex)
                {
                    if (err == null)
                    {
                        err = ex.ToString();
                    }
                    OnErrorSafe(err);
                }
                finally
                {
                    RemTask(lockNum);
                }
            }), lockNum));
            return t;
        }


        private Task? RemTask(int lockNum)
        {
            Task? t = null;
            tasks.RemoveAll((task) => {
                if (task.Item2 == lockNum)
                {
                    t = task.Item1;
                    return true;
                }
                return false;
            });
            return t;
        }


        public bool IsTaskRunning(int lockNum)
        {
            foreach (var task in tasks)
            {
                if (task.Item2 == lockNum)
                {
                    return true;
                }
            }
            return false;
        }


        public void Wait(int lockNum)
        {
            tasks.RemoveAll((task) => {
                if (task.Item2 == lockNum)
                {
                    task.Item1.Wait();
                    return true;
                }
                return false;
            });
        }


        public void OnErrorSafe(string text)
        {
            Dispatcher.Invoke(() => OnError(text));
        }


        public void OnError(string text)
        {
            new CDialog(this).Title("Ошибка")
                .Discription(text)
                .With((a) => a.Width += 100).SetActions(CDialog.A_YES).ShowDialog();
        }


        public bool SaveCfg()
        {
            try
            {
                ArcUtils.CreatePathIfNeed(CONFIG_FILE_PATH);
                byte[] data = UTF8Encoding.UTF8.GetBytes(JsonSerializer.Serialize(config, new JsonSerializerOptions
                {
                    Encoder = JavaScriptEncoder.UnsafeRelaxedJsonEscaping,
                    WriteIndented = true,
                    DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull
                }));
                File.WriteAllBytesAsync(CONFIG_FILE_PATH, data);
                config.shouldSave = false;
                return true;
            }
            catch (Exception ex) {
                return false;
            }
        }


        private void UpdateBtns01()
        {
            var vi = IsValidInstallation();
            bool v2 = (vi > 0) ? !IsENMagic() : false;

            SetBtnsMode(vi != 0, v2 ? (readyInstalled ? 1:2) : (vi == -1? -1:0));
        }


        private string GetPackPath()
        {
            var gt = gameType.V;
            string packPath;

            if (IsAndroid())
            {
                packPath = gt.android.target_pack_patch;
            }
            else
            {
                packPath = gt.windows.target_pack_patch;
            }
            return packPath;
        }


        private void SetupCF()
        {
            gameType = new(25, () => GetGameType(config));

            packBytes = new(50, () =>
            {

                return io.Read(IOType.GAME, GetPackPath());
            }, (data) =>
            {
                io.Write(IOType.GAME, GetPackPath(), data);
            });

            OnClosedAction += FreeCF;
        }


        private void FreeCF()
        {
            packBytes.Free();
            gameType.Free();
        }


        public void SetBtnsMode(bool valid, int installed)
        {
            if (valid)
            {
                if (installed > 0)
                {
                    BTNDel.Visibility = Visibility.Visible;
                    if(installed == 2)
                    {
                        //BTNInstallT.Text = "Проверить обновления";
                    }
                    else
                    {
                        //BTNInstallT.Text = "Установлено";
                    }
                }
                else
                {
                    if(installed == -1)
                    {
                        BTNInstall.Visibility = Visibility.Hidden;
                    }
                    else
                    {
                        BTNInstall.Visibility = Visibility.Visible;
                        //BTNInstallT.Text = "Установить";
                    }
                    BTNDel.Visibility = Visibility.Hidden;
                }
                BTNPlayT.Text = "Играть";
            }
            else
            {
                BTNDel.Visibility     = Visibility.Hidden;
                BTNInstall.Visibility = Visibility.Hidden;
                BTNPlayT.Text = "Выбрать игру..";
            }
        }


        private void OnOppenSettings(object sender, RoutedEventArgs e)
        {
            if (!FocusChildIfOpenned(typeof(Settings)))
            {
                OpenChild(new Settings(this));
            }
        }


        private void BtnEditor_Click(object sender, RoutedEventArgs e)
        {
            if (!FocusChildIfOpenned(typeof(Editor)))
            {
                OpenChild(new Editor(this));
            }
        }


        public string GetMagic()
        {
            LangTable tb = new();
            tb.Read(packBytes.V);

            return tb.GetMagic(gameType.V.magic_version.id);
        }


        public bool IsENMagic()
        {
            return GetMagic() == gameType.V.magic_version.key;
        }


        //public int MagicType()

        //{
        //    string s = GetMagic();
        //    var ver = gameType.V.magic_version;
        //    if (s == ver.key) return 1;
        //    if(s == ver.)
        //}


        public void LoadPack(TranslationCallback cb)
        {
            LangTable tb = new();
            RepoLoader rp = new(config);
            ITranslateEngine tr1 = new NeiroTranslate();

            cb.status = "Чтение пакета локализации";
            tb.Read(packBytes.V);
            cb.Reset();

            rp.Translate(tb, cb, null);
            cb.Reset();
            cb.status = "Запись";
            packBytes.V = tb.Write();
            cb.Reset();
        }


        //public void ShowChangelog(object sender, RoutedEventArgs e)
        //{

        //    tr1.LMStudioHostURL = "http://127.0.0.1:1234";
        //    tr1.LMStudioModelDownloadURL = "https://huggingface.co/Jackrong/Negentropy-claude-opus-4.7-9B-GGUF";
        //    tr1.LMStudioModelID = "negentropy-claude-opus-4.7-9b";

        //    tr1.SetupModel();

        //    tr1.systemPrompt = "Переведи данный фрагмент таблицы локализации на русский язык. В ответе не должно быть ничего лишнего.";

        //    List<ELine> lines = new();
        //    lines.Add(new ELine(0, "Complete Story Mode Stage QA-1-9"));
        //    lines.Add(new ELine(1, "Route Survey: Challenge Mode"));
        //    lines.Add(new ELine(2, "Complete event Challenge Mode Stage QT-1-5"));
        //    lines.Add(new ELine(3, "Expend 25,000 Sardis Gold"));
        //    //MessageBox.Show(tr1.SingleTranslate("Complete event Challenge Mode Stage QT-1-5"));

        //    TranslationCallback cb = new TranslationCallback();

        //    Task.Run(() => {
        //        tr1.TranslateLines(lines, cb);
        //    });

        //    Task.Run(() => {
        //        while (!cb.active)
        //        {
        //            Dispatcher.Invoke(() => {
        //                BTNPlay.Content = lines[new Random().Next() % 4].Value;
        //            });
        //            Thread.Sleep(500);
        //        }

        //        using (var client = new HttpClient())
        //        {
        //            tr1.UnloadModel(client);
        //        }

        //        while (true)
        //        {
        //            Dispatcher.Invoke(() => {
        //                BTNPlay.Content = lines[new Random().Next() % 4].Value;
        //            });
        //            Thread.Sleep(500);
        //        }
        //    });






        //    //OpenChild(new ArcChangelogs(this, "### Example header\n Example text 1\nExample text 2").EditHeader("Title", "Header"));

        //    //MessageBox.Show(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData).ToString());

        //    //new LuModUtils().Install();
        //}


        private void CommandBinding_Execute(object sender, ExecutedRoutedEventArgs e)
        {
            e.Handled = true;

            var src = e.Source as FrameworkElement;
            if (src == null) return;

            if (src.Name == "BtnSettings")
            {
                OnOppenSettings(src, e);
            }
            if (src.Name == "BTNInstall")
            {
                BTNInstall_Click(src, e);
            }
            if (src.Name == "BTNPlay")
            {
                BTNInstall_Click(src, e);
            }
            if (src.Name == "BTNDiscord")
            {
                ToggleDiscord();
            }
        }


        private void ToggleDiscord()
        {
            config.enable_discord = !config.enable_discord;
            SetDsRpc(config.enable_discord);
        }


        private void SetDsRpc(bool enable)
        {
            if (ISDSRPCWork()) {
                if (enable) return;
                var inf = config.ds_info;
                if (inf == null)
                {
                    inf = new DsInfo();
                }
                SetDSRPC(inf);
            }
            else
            {
                if (!enable) return;
                SetDSRPC(null);
            }
        }


        private void CommandBinding_CanExecute(object sender, CanExecuteRoutedEventArgs e)
        {
            e.CanExecute = true;
            e.Handled = true;
        }


        private void BTNInstall_Click(object sender, RoutedEventArgs e)
        {
            if (IsTaskRunning(3))
            {
                new CDialog(this)
                    .Title("Установка пакета локализации")
                    .Discription("Установка пакета локализации уже запущена.\nПодождите, пока она завершится.")
                    .With((a) => a.Width += 230).SetActions(CDialog.A_YES).ShowDialog();
                return;
            }


            //todo
            int t = IsValidInstallation();

            string magic = GetMagic();
            var infs0 = RepoLoader.GetSelectedPacksInfs(config);

            bool install = true;
            bool useOriginal = true;
            bool showChangelog = true;


            if(magic == infs0.Item3 && readyInstalled) 
            {
                //ready installed
                install = new CDialog(this)
                    .Title("Проверка обновлений")
                    .Discription("Пакет локализации уже установлен и\nактуален. Хотите переустановить его?")
                    .With((a) => a.Width += 150).AskB();

                useOriginal = false;
                showChangelog = false;
            }
            else
            {
                if (magic == infs0.Item1.magic_version.key)
                {

                    //not installed
                    install = true;
                    useOriginal = true;
                    
                }
                else
                {
                    //installed, unknown
                    useOriginal = false;
                }
            }

            if (install)
            {
                RunShoulded(() =>
                {
                    InstallPack(useOriginal, showChangelog, magic);
                }, "Не удалось установить пакет локализации!", 3);
            }
        }


        public void InstallPack(bool useOriginal, bool showChangelog, string? magic)
        {
            byte[] packBytes;

            if (useOriginal || !io.Exists(IOType.LAUNCHER, "backup.bytes")) {
                packBytes = this.packBytes.V;
            } 
            else
            {
                packBytes = io.Read(IOType.LAUNCHER, "backup.bytes");
            }

            var pl = new RepoLoader(config);

            ITranslateEngine eng = ChoiseTranslateEngine();
            TranslationCallback cb = new TranslationCallback();
            cb.Reset();

            LangTable tb = new();

            Task.Run(() => {
                string prevStatus = "";
                while (cb.active)
                {
                    Thread.Sleep(500);
                    var status = cb.status;
                    if(status != prevStatus && !string.IsNullOrEmpty(status))
                    {
                        Dispatcher.Invoke(() =>
                        {
                            if (cb.active)
                            {
                                //BTNInstallT.Text = status;
                            }
                        });
                        prevStatus = status;
                    }
                }
            });

            cb.status = "Чтение пакета локализации";
            tb.Read(packBytes);
            cb.Reset();

            eng.Setup(this, tb, cb);
            pl.Translate(tb, cb, (cb, lines) => eng.TranslateLines(lines, cb));
            eng.Finish();

            packBytes = tb.Write();

            this.packBytes.V = packBytes;
            readyInstalled = true;

            cb.Stop();
            Dispatcher.Invoke(() => SetBtnsMode(true, 1));
        }


        public ITranslateEngine ChoiseTranslateEngine()
        {
            if (config.translation_method == 2)
            {
                return new NeiroTranslate();
            }
            else if (config.translation_method == 1)
            {
                return new TranslatorionPool();
            }
            return new TryCopyTranslate();
        }


        private void BTNPlay_Click(object sender, RoutedEventArgs e)
        {
            if (IsValidInstallation() != 0)
            {
                Process proc = Process.Start(config.game_path + "\\GF2_Exilium.exe");

                if(config.suppress_gf2_launcher_method != 0)
                {
                    if(config.suppress_gf2_launcher_method == 1)
                    {
                        Task.Run(() => {
                            proc.WaitForExit();
                            KillGF2OrigLauncher(20, 1, 10);
                        });
                    }
                }
            }
            else
            {
                SelectGame();
            }
        }


        protected void SelectGame()
        {
            string gameName = "GF2_Exilium.exe";

            var filename = TryAutoSearchGamePath();
            bool update = false;
            bool select = true;

            if (filename != null)
            {
                var d = new CDialog(this)
                    .Title("Игра обнаружена!")
                    .Discription(filename.Replace("\\", "/"))
                    .SetBtnText("Выбрать другую", "Подтвердить");

                d.Width += 150;
                d.BtnYes.Width += 75;
                d.BtnNo.Width += 75;
                d.ShowDialog();
                if (d.Callback != null && d.Callback.Equals(true))
                {
                    config.game_path = filename.Substring(0, filename.Length - gameName.Length);
                    config.shouldSave = true;
                    update = true;
                    select = false;
                }
            }

            if (select)
            {
                Microsoft.Win32.OpenFileDialog dlg = new Microsoft.Win32.OpenFileDialog();

                dlg.Title = "Укажите путь к игре Girls Frontline 2: Exilium";
                dlg.DefaultExt = ".exe";
                dlg.Filter = "GF2_Exilium|*.exe";

                Nullable<bool> result = dlg.ShowDialog();

                if (result == true)
                {
                    filename = dlg.FileName;

                    if (filename != null)
                    {
                        if (filename.EndsWith(gameName))
                        {
                            config.game_path = filename.Substring(0, filename.Length - gameName.Length);
                            config.shouldSave = true;
                            update = true;

                        }
                    }
                }
            }

            //todo choice game type
            var gt = GetSupported();
            if(gt.Count == 1)
            {
                config.game_type = gt[0].id;
                config.shouldSave = true;
            }

            if (update)
            {
                //todo select mode
                UpdateBtns01();
            }

        }


        public int IsValidInstallation()
        {
            if (!string.IsNullOrEmpty(config.game_path))
            {
                var sp = GetSupported();

                if (sp.Count == 0) return 0;
                if (string.IsNullOrEmpty(config.game_type)) return 1;

                foreach (var item in sp) {
                    if (item.id == config.game_type) return 2;
                }
            }

            return (config.game_types != null && config.game_types.Count > 0) ? 0 : -1;
        }


        public List<GameType> GetSupported()
        {
            if (config.game_types == null)return new List<GameType>();
            return config.game_types.Where((t) => t.IsSupported(config.game_path)).ToList();
        }
    }


    public class WinSpecGT
    {
        public string? app_info_path { get; set; }
        public string? app_info_content { get; set; }
        public string? target_pack_patch { get; set; }
        public string? skins_pack_patch { get; set; }
    }


    public class AndroidSpecGT
    {
        public string? app_id { get; set; }
        public string? target_pack_patch { get; set; }
        public string? skins_pack_patch { get; set; }
    }


    public class GameType
    {
        public string id { get; set; }
        public string lang_name { get; set; }
        public string prefix { get; set; }

        
        public WinSpecGT? windows { get; set; }
        public AndroidSpecGT android { get; set; }

        public MagicVersion magic_version { get; set; }
        public Dictionary<string, PackInf> packs { get; set; }
        public List<UPacksInfo> default_info { get; set; }
        public NeirTranslateOptions? neir_translation { get; set; }

        public bool IsSupported(string gamePath)
        {
            if(!string.IsNullOrEmpty(windows.app_info_path))
            {
                var fl = gamePath + "\\" + windows.app_info_path;
                if (File.Exists(fl))
                {
                    if (!File.ReadAllText(fl).Contains(windows.app_info_content))
                    {
                        return false;
                    }
                }
                else
                {
                    return false;
                }
            }
            if (!string.IsNullOrEmpty(windows.target_pack_patch))
            {
                var fl = gamePath + "\\" + windows.target_pack_patch;
                if (!File.Exists(fl))
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
            return true;
            //var cleanPath = gamePath.Replace("GF2_Exilium.exe", "");
        }
    }


    public class Config {
        //IgnoreNullValues = true
        public string? license_key { get; set; }
        public bool enable_discord { get; set; }
        public int? translation_method { get; set; }
        public string? lm_studio_path { get; set; }
        public int? suppress_gf2_launcher_method { get; set; }
        public List<GameType>? game_types { get; set; }
        public List<UPacksInfo>? packs { get; set; }
        public string? game_path { get; set; }
        public string? game_type { get; set; }
        [JsonIgnore]
        public bool shouldSave { get; set; }

        public DsInfo? ds_info { get; set; }

        //public Dictionary<string, NeirTranslateOptions>? ai_translate { get; set; }
        //public string ORIG_SHA256;
        //public string RU_SHA256;
    }
}