using System.Configuration;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Security.Cryptography;
using System.Text;
using System.Windows;
using System.Windows.Input;
using System.Windows.Media.Imaging;
using DiscordRPC;
using SourceChord.FluentWPF;
//using System.Management;

//using Google.Apis.Services;
//using Google.Apis.Analytics.v3;
//using Google.Apis.Auth.OAuth2;

namespace GF2_RU_Launch;


public partial class MainWindow : AcrylicWindow{
    public static string LAUNCH_VERSION_URL = "https://gitlab.com/homaplus/girls-frontline-2-exilium-ru-launcher/-/raw/natives/launcversion.info?ref_type=heads&inline=false";
    public static string LAUNCH_VERSION = "3.1.1";

    public static string GF2_DATA_TABLE    = "GF2_Exilium_Data/LocalCache/Data/Table";
    public static string GF2_LANG_PACK     = "/LangPackageTableEnusData.bytes";
    public static string GF2_LANG_BACKUP   = "./data/BACKUP_LangPackageTableEnusData.bytes";
    public static string GF2_RU_PACK       = "./data/LangPackageRU.bytes";
    public static string GF2_RU_PACK_JSON  = "./data/translation_ru.json";
    public static string GF2_LOAD_URL      = "https://gitlab.com/homaplus/grils-frontline-team-translate/-/raw/%__BRANCH%__%/translations_release.json?ref_type=heads&inline=false";
    public static string GF2_CHANGELOG_URL = "https://gitlab.com/homaplus/grils-frontline-team-translate/-/raw/%__BRANCH%__%/Changelog.md?ref_type=heads&inline=false";
    public static string GF2_NOTIFICATIONS_URL = "https://gitlab.com/homaplus/grils-frontline-team-translate/-/raw/%__BRANCH%__%/Notifiations.md?ref_type=heads&inline=false";
    public static string NATIVES_LOAD_URL  = "https://gitlab.com/homaplus/girls-frontline-2-exilium-ru-launcher/-/raw/natives/%__FILE__%?ref_type=heads&inline=false";
    public static string STATISTIC_URL     = "https://vk.cc/cOtxNy";

    //private static string GF2_MASTER_URL;

    private string GF2_Path = null;
    private Changelogs subWindow;
    private Changelogs subWindow2;
    private Editor editor;
    private Settings settings;

    private bool needLoadPack = true;
    private bool locked = false;
    private bool launchUpdatesChecked = false;


    public MainWindow() 
    {
        InitializeComponent();
        InitUi();
        SetExtendViewIntoTitleBar(this, true);
        LoadConfig();
        ChooseRandomBg();
        BTnnUninstall.Visibility = Visibility.Hidden;

        UpdatePlayButton();
        editBtnStatus(false, true);

        SetupDiscordRPC();

        try
        {
            SetupAnalitics();
        }
        catch { 

        }

        //try
        //{
        //    startLaunchKiller();
        //}
        //catch (Exception ex)
        //{
        //    Console.WriteLine(ex);
        //}
    }


    public void InitUi()
    {
        new ClickProxy(BTNEditor, OnClickOpenEditor);
        new ClickProxy(BTNSettings, OnClickOpenSettings);
    }


    private void OnEnterBtnsBg(object sender, MouseEventArgs e)
    {
        BGBtns.Opacity = 1;
    }

    private void OnLeaveBtnsBg(object sender, MouseEventArgs e)
    {
        BGBtns.Opacity = 0.55;
    }


    private class ClickProxy
    {
        private bool pressed = false;
        private MouseEventHandler handler;

        public ClickProxy(UIElement el, MouseEventHandler handler)
        {
            el.MouseLeftButtonDown += this.OnClick;
            el.MouseLeftButtonUp += this.OnRelease;
            this.handler = handler;
        }

        public void OnClick(object sender, RoutedEventArgs e)
        {
            if (pressed) return;
            pressed = true;
        }
        public void OnRelease(object sender, RoutedEventArgs e)
        {
            if (!pressed) return;
            pressed = false;
            handler.Invoke(sender, null);
        }
    }


    private void OnClickOpenSettings(object sender, RoutedEventArgs e)
    {
        settings = new Settings(this);
        settings.Show();
    }


    public static void CallInDedictedThread(Action action)
    {
        new Thread(new ThreadStart(action)).Start();
    }


    private void ChooseRandomBg()
    {
        var rand = new Random(DateTime.Now.Second);
        string[] characters = { "colphne", "groza" };

        string name = characters[rand.Next(characters.Length)];

        int hr = DateTime.Now.Hour;

        if (hr > 7 && hr < 23)
        {
            name += "1";
        }
        else
        {
            name += "0";
        }
        BGImg.Source = ImgByName(name);
    }


    public void OnEditorClosed()
    {
        if (editor != null)
        {
            editor = null;
        }
    }


    protected override void OnClosed(EventArgs e)
    {
        base.OnClosed(e);
        if(subWindow != null)
        {
            subWindow.Close();
        }
        if(subWindow2 != null)
        {
            subWindow2.Close();
        }
        if(editor != null)
        {
            editor.Close();
        }
        if(settings != null)
        {
            settings.Close();
        }
    }


    private void OnClickOpenEditor(object sender, RoutedEventArgs e)
    {
        if (editor != null)
        {
            editor.Focus();
            return;
        }
        editor = new Editor(this);
        editor.Show();
        editor.Focus();
    }


    private void OnClickPlay(object sender, RoutedEventArgs e)
    {
        if (locked) return;
        if (IsValidGamePath())
        {
            Process.Start(GF2_Path + "GF2_Exilium.exe");
            this.WindowState = WindowState.Minimized;
            return;
        }



        Microsoft.Win32.OpenFileDialog dlg = new Microsoft.Win32.OpenFileDialog();

        dlg.Title = "Укажите путь к игре Girls Frontline 2: Exilium";
        dlg.DefaultExt = ".exe";
        dlg.Filter = "GF2_Exilium|*.exe";

        Nullable<bool> result = dlg.ShowDialog();

        if (result == true)
        {
            string filename = dlg.FileName;
            string gameName = "GF2_Exilium.exe";

            if (filename != null) {
                if (filename.EndsWith(gameName))
                {
                    GF2_Path = filename.Substring(0, filename.Length - gameName.Length);
                    GF2_Path = GF2_Path.Replace("\\", "/");

                    setCfgValue("GFT_Path", GF2_Path);
                }
            }
        }
        UpdatePlayButton();
    }


    private void UpdatePlayButton()
    {

        if (IsValidGamePath()) 
        {
            BTnPlay.Content = "Играть";
            BTnPlayInstall.Visibility = Visibility.Visible;
        } 
        else
        {
            BTnPlay.Content = "Выбрать игру";
            BTnPlayInstall.Visibility = Visibility.Hidden;
            BTnnUninstall.Visibility = Visibility.Hidden;
        }
    }


    public bool IsValidGamePath()
    {
        return GF2_Path != null && GF2_Path.Length > 0;
    }


    public bool IsPackInstalled()
    {
        string val = getCfgValue("GF2_ISHA256");
        if (val == null || val.Length == 0) return false;

        var curSha = getSHA256(getLangPackPath());

        return curSha == val;
    }

    public string getLangPackPath()
    {
        return GF2_Path + GF2_DATA_TABLE + GF2_LANG_PACK;
    }


    public string getBackupPackPath()
    {
        return GF2_LANG_BACKUP;
    }


    //private bool checkValidGamePath()
    //{
    //    if(IsValidGamePath())
    //    {
    //        var hasBackup = File.Exists(getBackupPackPath());
    //        var installed = File.Exists(getLangPackPath());

    //        if (this.hasBackup()) 
    //        {
    //            BTnnUninstall.Visibility = Visibility.Visible;
    //        } 
    //        else
    //        {
    //            BTnnUninstall.Visibility = Visibility.Hidden;
    //        }
    //        return installed;
    //    }
    //    return false;
    //}


    private void LoadConfig()  {
        GF2_Path = getCfgValue("GFT_Path");
    }


    public static string getCfgValue(string key)
    {
        return ConfigurationManager.AppSettings[key];
    }


    public static void setCfgValue(string key, string value)
    {
        var configFile = ConfigurationManager.OpenExeConfiguration(ConfigurationUserLevel.None);
        var settings = configFile.AppSettings.Settings;
        if (settings[key] == null)
        {
            settings.Add(key, value);
        }
        else
        {
            settings[key].Value = value;
        }
        configFile.Save(ConfigurationSaveMode.Modified);
    }


    public static string getLoadPackUrl()
    {
        string loadUrl = getCfgValue("GFT_LoadUrl");
        if (loadUrl == null || loadUrl.Length == 0)
        {
            loadUrl = GF2_LOAD_URL;
        }

        return prepareLoadUrl(loadUrl);
    }


    public static string prepareLoadUrl(string loadUrl)
    {
        return loadUrl.Replace("%__BRANCH%__%", getBranch());
    }


    public static string getBranch()
    {
        string branch = getCfgValue("GFT_Branch");
        if (branch == null || branch.Length == 0)
        {
            branch = "master";
        }
        return branch;
    }


    public string getLoadChangesUrl()
    {
        string loadUrl = getCfgValue("GFT_ChangesUrl");
        if (loadUrl == null || loadUrl.Length == 0)
        {
            loadUrl = GF2_CHANGELOG_URL;
        }

        return loadUrl.Replace("%__BRANCH%__%", getBranch());
    }


    public string getNotifyUrl()
    {
        string loadUrl = getCfgValue("GFT_NotifyUrl");
        if (loadUrl == null || loadUrl.Length == 0)
        {
            loadUrl = GF2_NOTIFICATIONS_URL;
        }

        string branch = getCfgValue("GFT_Branch");
        if (branch == null || branch.Length == 0)
        {
            branch = "master";
        }

        return loadUrl.Replace("%__BRANCH%__%", branch);
    }


    public void onBtnLoadClick(object sender, RoutedEventArgs e)
    {
        if (!launchUpdatesChecked)
        {
            launchUpdatesChecked = true;
            Task.Run(() => {
                var flag = IsActualLaunchVersion();

                if (flag)
                {
                    Dispatcher.Invoke(() => {
                        onBtnLoadClick2();
                    });
                } 
                else
                {
                    Dispatcher.Invoke(() => {
                        MessageBox.Show("Вышла новая версия лаунчера!");
                        UpdateLauncher();
                    });
                    return;
                }
            });
            return;
        }
        onBtnLoadClick2();
    }


    public void onBtnLoadClick2()
    {
        if (locked) return;

        if (!needLoadPack)
        {
            var isShift = (Keyboard.Modifiers & ModifierKeys.Shift) > 0;
            if (isShift)
            {
                needLoadPack = true;
            }
            else
            {
                locked = true;
                Task.Run(() =>
                {
                    installLoadedPack();
                    Dispatcher.Invoke(() =>
                    {
                        locked = false;
                        editBtnStatus(true, false);
                    });
                });
                return;
            }
        }
        locked = true;
        BTnPlayInstall.Content = "Проверка версии...";

        CallInDedictedThread(() =>
        {
            var resultT = checkVersion();

            bool completed = false;
            bool building = false;
            TranslatorionPool.TCallback? cb = null;
            var param = getCfgValue("GFT_DTranlation");

            Dispatcher.Invoke(() =>
            {
                BTnPlayInstall.Content = "Загрузка обновлений.";

                Task.Run(() =>
                {
                    int points = 0;
                    while (!completed)
                    {
                        Thread.Sleep(750);

                        if (points++ >= 3)
                        {
                            points = 0;
                        }

                        Dispatcher.Invoke(() =>
                        {
                            var add = "";
                            int i = 0;
                            for (i = 0; i < points; i++)
                            {
                                add += ".";
                            }
                            for (; i < 3; ++i)
                            {
                                add += " ";
                            }
                            if (completed) return;
                            if (building)
                            {
                                if (cb != null)
                                {
                                    BTnPlayInstall.Content = "Перевод " + cb.translated + "/" + cb.total;
                                }
                                else
                                {
                                    BTnPlayInstall.Content = "Сборка пакета ." + add;
                                }               
                            }
                            else
                            {
                                 BTnPlayInstall.Content = "Загрузка обновлений ." + add;
                            }
                        });
                    }
                });

                if (resultT.log != null && resultT.log.Length > 0)
                {
                    subWindow = new Changelogs(this, resultT.log);
                    subWindow.Show();
                }

                if (resultT.hasNityfy)
                {
                    var notify = loadStr(getNotifyUrl());
                    if (notify != null) {
                        var seen = getCfgValue("GFT_SeenNotify");
                        var cur  = getCfgValue("GFT_Version");

                        if(seen == null || seen != cur)
                        {
                            setCfgValue("GFT_SeenNotify", cur);
                            subWindow2 = new Changelogs(this, notify);
                            subWindow2.EditHeader(
                                "Girls Frontline 2: Exilium RU Launcher | Notify",
                                "Объявления"
                                );
                            subWindow2.Show();
                        }
                    }
                }
            });

            var loaded = false;
            if (resultT.needUpdate)
            {
                try
                {
                    loaded = new PackLoader().loadAndMergeTo(GF2_RU_PACK_JSON);
                    //loaded = load(getLoadPackUrl(), GF2_RU_PACK_JSON);
                }
                catch (Exception e)
                {
                    Dispatcher.Invoke(() =>
                    {
                        MessageBox.Show("Ошибка загрузки обновления!");
                    });
                }
            }
            else
            {
                onPostLoad(true, ref completed);
                return;
            }

            if (!Editor.EnsureEditorLoaded())
            {
                onPostLoad(false, ref completed);
                return;
            }
            building = true;

            byte[]? jBytes = null, pBytes;

            try
            {
                if (HasBackup())
                {
                    pBytes = File.ReadAllBytes(GF2_LANG_BACKUP);
                }
                else
                {
                    pBytes = File.ReadAllBytes(getLangPackPath());
                }
            }
            catch (Exception e) {
                Dispatcher.Invoke(() =>
                {
                    MessageBox.Show("Что-то пошло не так ... \nИгра установлена некорректно, либо какой-то процесс блокирует доступ к файлам переводов");
                });
                onPostLoad(false, ref completed);
                return;
            }
            try
            {
                jBytes = File.ReadAllBytes(GF2_RU_PACK_JSON);
            }
            catch
            {
                if (param != null && param != "disabled")
                {
                    Dispatcher.Invoke(() =>
                    {
                        MessageBox.Show("Ошибка загрузки обновления!\n Будет использован до-перевод");
                    });
                } else
                {
                    Dispatcher.Invoke(() =>
                    {
                        MessageBox.Show("Ошибка загрузки обновления!");
                    });
                    onPostLoad(false, ref completed);
                    return;
                }
            }

            LangEditCS table = new LangEditCS();
            
            table.Read(pBytes);
            if(jBytes != null)
            {
                table.mergeAll(Editor.fromJsonBytes(jBytes));
            }
            
            if(param != null && param != "disabled")
            {
                List<ELine> notTranslated = table.getNotTranslated(true);
                if (notTranslated.Count > 0) {
                    TranslatorionPool pool = new TranslatorionPool();

                    if (pool.Setup("en", "ru", param))
                    {
                        pool.setCache("data/translation_cache.json");
                        cb = new();
                        pool.TranslateAll(notTranslated, cb);
                        cb = null;
                    }
                }
            }

            var eData = table.Write();

            try
            {
                File.WriteAllBytes(GF2_RU_PACK, eData);
                onPostLoad(true, ref completed);
            }
            catch
            {
                onPostLoad(false, ref completed);
                Dispatcher.Invoke(() =>
                {
                    MessageBox.Show("Ошибка сборки пакета!");
                });
            }
            finally
            {
                //table.free();
            }

            //onPostLoad(true, ref completed);
        });
    }


    private void onPostLoad(bool loaded, ref bool completed)
    {
        if (loaded)
        {
            needLoadPack = false;
        }
        completed = true;
        if (loaded)
        {
            installLoadedPack();
        }
        Dispatcher.Invoke(() => {
            locked = false;
            editBtnStatus(loaded, false);
        });
    }


    private class VersionResult
    {
        public bool needUpdate = true;
        public bool hasNityfy = false;
        public string? log = null;

        //private VersionResult(bool update, bool notyfy, string vers)
        //{
        //    needUpdate = update;
        //    hasNityfy = notyfy;
        //    version = vers;
        //}
    }


    private VersionResult checkVersion()
    {
        VersionResult result = new VersionResult();
        string logResult = null;
        if (needLoadPack)
        {
            string log = loadStr(getLoadChangesUrl());
            if (log != null)
            {
                string firstLine = log.Split("\n")[0];
                string curVersion = getCfgValue("GFT_Version");

                firstLine = firstLine.Trim();
                if (firstLine.StartsWith("!"))
                {
                    result.hasNityfy = true;
                    firstLine = firstLine.Substring(1);
                    log = log.Substring(1);
                }

                if(firstLine != null && curVersion != null && curVersion.Length != 0)
                {
                    if (firstLine.Equals(curVersion) && File.Exists(GF2_RU_PACK))
                    {
                        needLoadPack = false;
                    }
                }
                if (needLoadPack)
                {
                    if(firstLine != null && firstLine.Length != 0)
                    {
                        setCfgValue("GFT_Version", firstLine);
                    }
                    logResult = log;
                }
            }
        }

        result.needUpdate = needLoadPack;
        result.log = logResult;

        return result;
        //return Tuple.Create(needLoadPack, logResult);
    }


    private void onInstallClickProxy(object sender, RoutedEventArgs e)
    {
        if (clickAction != null)
        {
            clickAction.Invoke(sender, e);
        }
    }


    private RoutedEventHandler clickAction;


    private void installLoadedPack()
    {
        try
        {
            string packPath = getLangPackPath();
            string backupPath = getBackupPackPath();

            CreatePathIfNeed(backupPath);

            if (File.Exists(backupPath))
            {
                File.Delete(backupPath);
            }

            File.Move(packPath, backupPath);
            File.Copy(GF2_RU_PACK, packPath, true);

            //if (!File.Exists(backupPath))
            //{
            //    CreatePathIfNeed(backupPath);
            //    File.Move(packPath, backupPath);
            //    File.Copy(GF2_RU_PACK, packPath, true);
            //}
            //else
            //{
            //    File.Delete(packPath);
            //    File.Copy(GF2_RU_PACK, packPath, true);
            //}

            setCfgValue("GF2_ISHA256", getSHA256(packPath));

            Dispatcher.Invoke(() => {
                BTnnUninstall.Visibility = Visibility.Visible;
            });
        } catch (Exception err)
        {
            onError();
        }
    }


    public void onUnInstallClick(object sender, RoutedEventArgs e)
    {
        if (locked) return;
        try
        {
            string packPath = getLangPackPath();
            string origPath = getBackupPackPath();

            if (File.Exists(origPath))
            {
                File.Delete(packPath);
                File.Move(origPath, packPath);
                BTnnUninstall.Visibility = Visibility.Hidden;

                setCfgValue("GF2_ISHA256", "");
                MessageBox.Show("Русский пакет локализации удален.");
            }
            else
            {
                MessageBox.Show("Пакет локализации по умолчанию не найден! Удаление русской локализации было отменено, в связи с тем, что это может сломать игру.");
            }
            editBtnStatus(false, false);
        } catch
        {
            onError();
        }
    }


    public void onError()
    {
        MessageBox.Show("Произошла непредвиденная ошибка\nПопробуйте запустить лаунчер от имени Администратора", "Ошибка");
    }


    private bool HasBackup()
    {
        return File.Exists(GF2_LANG_BACKUP);
    }


    private bool RemoveBackup()
    {
        if (HasBackup())
        {
            try
            {
                File.Delete(GF2_LANG_BACKUP);
            }
            catch
            {
                return false;
            }
        }
        return true;
    }


    private void editBtnStatus(bool updated, bool checkSha)
    {
        var loaded = File.Exists(GF2_RU_PACK);

        string msg = null;
        if (loaded)
        {
            if (updated)
            {
                msg = "Установлено";
                BTnnUninstall.Visibility = Visibility.Visible;
            } 
            else
            {
                var result = false;
                if (checkSha)
                {
                    result = IsPackInstalled();
                    if (!result)
                    {
                        RemoveBackup();
                    }
                }
                else
                {
                    result = HasBackup();
                }
                if (result)
                {
                    msg = "Проверить обновления";
                    BTnnUninstall.Visibility = Visibility.Visible;
                } 
                else
                {
                    msg = "Установить";
                }
                clickAction = onBtnLoadClick;

            }
        }
        else
        {
            if (updated) 
            { 
                msg = "Ошибка";
            } 
            else
            {
                var result = false;
                if (checkSha)
                {
                    result = IsPackInstalled();
                    if (result) RemoveBackup();
                }
                else
                {
                    result = HasBackup();
                }
                if (result)
                {
                    msg = "Проверить обновления";
                    BTnnUninstall.Visibility = Visibility.Visible;
                }
                else
                {
                    msg = "Установить";
                }
                clickAction = onBtnLoadClick;
            }
        }
        
        BTnPlayInstall.Content = msg;
    }


    public static bool IsActualLaunchVersion()
    {
        if (LAUNCH_VERSION.Contains("dev"))
        {
            return true;
        }
        if(getCfgValue("GFT_DisableUpdated") == "true")
        {
            return true;
        }
        var remoteVersion = loadStr(LAUNCH_VERSION_URL);
        if (remoteVersion == null || remoteVersion.Length == 0) return true;
        remoteVersion = remoteVersion.Trim();

        try
        {
            var lv = Double.Parse(LAUNCH_VERSION);
            var rv = Double.Parse(remoteVersion);
            if (lv > rv) return false;
        }
        catch {}

        return remoteVersion == LAUNCH_VERSION;
    }


    public void UpdateLauncher()
    {
        try
        {
            //File.Delete("LUpdater.exe");
            //File.Delete("LUpdater.dll");

            //ensureNativeInstalled("LUpdater.exe");
            //ensureNativeInstalled("LUpdater.dll");
            //ensureNativeInstalled("LUpdater.runtimeconfig.json");

            Process.Start("LUpdater.exe");
            Environment.Exit(0);
        }
        catch
        {
            Dispatcher.Invoke(() => {
                MessageBox.Show("Инструмент автоматического обновления не установлен. Автоматическая переустановка невозможна.");
            });
        }
    }


    private static BitmapImage ImgByName(string name)
    {
        var bi = new BitmapImage();
        bi.BeginInit();
        bi.UriSource = new Uri("pack://application:,,,/GF2_RU_Launch;component/Images/"+name+".png");
        bi.EndInit();
        return bi;
    }


    public static bool ensureNativeInstalled(string name)
    {
        if (File.Exists(name))
        {
            return true;
        }
        var url = NATIVES_LOAD_URL.Replace("%__FILE__%", name);

        return load(url, name);
    }


    public static bool load(string url, string filename)
    {
        CreatePathIfNeed(filename);
        using (WebClient webClient = new WebClient())
        {
            try
            {
                webClient.DownloadFile(url, filename);
            }
            catch (Exception ex) {
                return false;
            }
            return true;
        }
        return false;
    }


    public static bool CreatePathIfNeed(string fileName)
    {
        FileInfo file = new FileInfo(fileName);
        DirectoryInfo parent = file.Directory;
        if (parent != null)
        {
            parent.Create();
            return true;
        }
        return false;
    }


    private static string? loadStr(string url)
    {
        byte[]? data = loadBytes(url);
        if(data == null)
        {
            return null;
        }
        return Encoding.UTF8.GetString(data);
    }


    public static byte[]? loadBytes(string url)
    {
        using (WebClient webClient = new WebClient())
        {
            return webClient.DownloadData(url);
        }
        return null;
    }


    private static string getSHA256(string file)
    {
        if (File.Exists(file))
        {
            return getSHA256(File.ReadAllBytes(file));
        }
        return "";
    }

    private static string getSHA256(byte[] src)
    {
        SHA256 sha = SHA256.Create();
        byte[] hash = sha.ComputeHash(src);
        return Convert.ToBase64String(hash);
    }


    private void SetupDiscordRPC()
    {
        var status = getCfgValue("GFT_Discord");
        bool enableDs = false;

        if (status == null || status.Length == 0)
        {
            var res = MessageBox.Show("Включить ли отображение игры в Discord?", "Girls Frontline 2: Exilium RU Launcher | Discord RPC", MessageBoxButton.YesNo);

            if (res == MessageBoxResult.Yes)
            {
                setCfgValue("GFT_Discord", "true");
                enableDs = true;
            }
            else
            {
                setCfgValue("GFT_Discord", "false");
            }
        }
        else
        {
            enableDs = status == "true";
        }

        if (!enableDs) return;

        var i1 = ensureNativeInstalled("DiscordRPC.dll");
        var i2 = ensureNativeInstalled("Newtonsoft.Json.dll");

        if(i1 && i2)
        {
            EnableDs0();
        }
    }


    private void EnableDs0()
    {
        try
        {
            var rpcClient = new DiscordRpcClient("1378747345121841272");

            rpcClient.Initialize();

            rpcClient.SetPresence(new RichPresence()
            {
                Buttons = new Button[]
                {
                    new Button() { Label = "Скачать", Url = "https://gitlab.com/homaplus/girls-frontline-2-exilium-ru-launcher" }
                }
            });

        }
        catch
        {

        }
    }

    private void SetupAnalitics()
    {
        var res = getCfgValue("GF2_HasFirstLaunch");
        if (res != null) return;
        setCfgValue("GF2_HasFirstLaunch", "true");
        var sInfo = new System.Diagnostics.ProcessStartInfo(STATISTIC_URL)
        {
            UseShellExecute = true,
        };
        System.Diagnostics.Process.Start(sInfo);
    }


    //public void startLaunchKiller()
    //{
    //    ManagementEventWatcher startWatch = new ManagementEventWatcher(
    //            new WqlEventQuery("SELECT * FROM Win32_ProcessStartTrace")); //WHERE ProcessName = \"PCLauncher.exe\"

    //    startWatch.EventArrived += (e, e2) => {
    //        MessageBox.Show("NEN");
    //    };

    //    startWatch.Start();
    //}
}