using DiscordRPC;
using SourceChord.FluentWPF;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Security.Cryptography;
using System.Text;
using System.Windows;
using System.Windows.Input;
using System.Windows.Media.Imaging;

namespace GF2_RU_Launch;


public partial class MainWindow : AcrylicWindow{
    public static string LAUNCH_VERSION_URL = "https://gitlab.com/homaplus/girls-frontline-2-exilium-ru-launcher/-/raw/natives/launcversion.info?ref_type=heads&inline=false";
    public static string LAUNCH_VERSION = "3.3.2";

    public static string GF2_DATA_TABLE    = "GF2_Exilium_Data/LocalCache/Data/Table";
    public static string GF2_LANG_PACK     = "/LangPackageTableEnusData.bytes";
    public static string GF2_BACKUP_PACK   = "/BACKUP_LangPackageTableEnusData.bytes";
    public static string GF2_RU_PACK_JSON  = "./data/translation_ru.json";
    public static string GF2_LOAD_URL      = "https://gitlab.com/homaplus/grils-frontline-team-translate/-/raw/%__BRANCH%__%/translations_release.json?ref_type=heads&inline=false";
    public static string GF2_CHANGELOG_URL = "https://gitlab.com/homaplus/grils-frontline-team-translate/-/raw/%__BRANCH%__%/Changelog.md?ref_type=heads&inline=false";
    public static string GF2_NOTIFICATIONS_URL = "https://gitlab.com/homaplus/grils-frontline-team-translate/-/raw/%__BRANCH%__%/Notifiations.md?ref_type=heads&inline=false";
    public static string STATISTIC_URL     = "https://vk.cc/cOtxNy";
    public static string PACKS_COMPABILITY = "3.1";

    //private static string GF2_MASTER_URL;

    private string? GF2_Path = null;
    private Changelogs subWindow;
    private Changelogs subWindow2;
    private Editor editor;
    private Settings settings;

    private DateTime lastCheckUpdate = DateTime.MinValue;
    private bool locked = false;
    private bool isGameRunning = false;
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
        if (locked || isGameRunning) return;
        if (IsValidGamePath())
        {
            Process proc = Process.Start(GF2_Path + "GF2_Exilium.exe");
            isGameRunning = true;
            this.WindowState = WindowState.Minimized;

            
            if (AppConfig.Get().check("GFT_LaunchSuppression", false))
            {
                CallInDedictedThread(() =>
                {
                    proc.WaitForExit();
                    isGameRunning = false;
                    KillLaunch(3, 0, 75);
                    Dispatcher.Invoke(() =>
                    {
                        editBtnStatus(IsNeedCheckUpdatePack(), true);
                        this.Focus();
                    });
                });
                KillLaunch(2, 5000, 2500);
            }
            else if (IsPackInstalled())
            {
                {
                    CallInDedictedThread(() =>
                    {
                        proc.WaitForExit();
                        isGameRunning = false;

                        Dispatcher.Invoke(() =>
                        {
                            editBtnStatus(IsNeedCheckUpdatePack(), true);
                            this.Focus();
                        });
                    });
                }
                return;
            }
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
        string? val = AppConfig.Get().getStr("GF2_ISHA256");
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
        return GF2_Path + GF2_DATA_TABLE + GF2_BACKUP_PACK;
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
        GF2_Path = AppConfig.Get().getStr("GFT_Path");
    }


    private static void setCfgValue(string key, string value)
    {
        AppConfig.Get().setStr(key, value);
        AppConfig.Get().Save();
    }


    public static string getLoadPackUrl()
    {
        string? loadUrl = AppConfig.Get().getStr("GFT_LoadUrl");
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
        string branch = AppConfig.Get().getStr("GFT_Branch");
        if (branch == null || branch.Length == 0)
        {
            branch = "master";
        }
        return branch;
    }


    public string getLoadChangesUrl()
    {
        string loadUrl = AppConfig.Get().getStr("GFT_ChangesUrl");
        if (loadUrl == null || loadUrl.Length == 0)
        {
            loadUrl = GF2_CHANGELOG_URL;
        }

        return prepareLoadUrl(loadUrl);
    }


    public string getNotifyUrl()
    {
        string loadUrl = AppConfig.Get().getStr("GFT_NotifyUrl");
        if (loadUrl == null || loadUrl.Length == 0)
        {
            loadUrl = GF2_NOTIFICATIONS_URL;
        }

        return prepareLoadUrl(loadUrl);
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
        if (isGameRunning)
        {
            MessageBox.Show("Установка не возможна во время игры!", "Ошибка");
            return;
        }

        if (!IsNeedCheckUpdatePack())
        {
            var isShift = (Keyboard.Modifiers & ModifierKeys.Shift) > 0;
            if (isShift)
            {
                SetNeedCheckUpdatePack(true);
            }
            else
            {
                return;
            }
        }

        locked = true;
        BTnPlayInstall.Content = "Проверка версии...";

        CallInDedictedThread(() =>
        {
            var resultT = CheckVersion();

            CallbackUIEL callbackUIEL = new(this);
            callbackUIEL.resultT = resultT;
            callbackUIEL.runCallbackInf();

            var loaded = false;
            if (resultT.needUpdate)
            {
                try
                {
                    var loaded0 = new PackLoader().loadAndMergeTo(GF2_RU_PACK_JSON);
                    loaded = loaded0.Item1;

                    if(loaded0.Item2.Count != 0)
                    {
                        OnError(null, "Ошибка загрузки обновления: хосты "+string.Join("\n- ",loaded0.Item2.ToArray())+"\nне доступны");
                    }
                }
                catch (Exception e)
                {
                    Dispatcher.Invoke(() =>
                    {
                        OnError(e, "Ошибка загрузки обновления");
                        CallInDedictedThread(() => 
                        {
                            installLoadedJsonPack(callbackUIEL);
                        });
                    });
                    return;
                }
            }
            installLoadedJsonPack(callbackUIEL);
        });
    }

    private class CallbackUIEL
    {
        public bool completed = false;
        public bool building = false;
        public TranslatorionPool.TCallback? cb;
        public VersionResult? resultT = null;
        public MainWindow w;

        public CallbackUIEL(MainWindow w)
        {
            this.w = w;
        }

        public void runCallbackInf()
        {
            w.Dispatcher.Invoke(() =>
            {
                w.BTnPlayInstall.Content = "Загрузка обновлений.";

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

                        w.Dispatcher.Invoke(() =>
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
                                    w.BTnPlayInstall.Content = "Перевод " + cb.translated + "/" + cb.total;
                                }
                                else
                                {
                                    w.BTnPlayInstall.Content = "Сборка пакета ." + add;
                                }
                            }
                            else
                            {
                                if (resultT != null && resultT.needUpdate)
                                {
                                    w.BTnPlayInstall.Content = "Загрузка обновлений ." + add;
                                }
                                else
                                {
                                    w.BTnPlayInstall.Content = "Сборка пакета ." + add;
                                }
                                
                            }
                        });
                    }
                });

                if(resultT == null)return;

                if (resultT.log != null && resultT.log.Length > 0)
                {
                    w.subWindow = new Changelogs(this.w, resultT.log);
                    w.subWindow.Show();
                }

                if (resultT.hasNityfy)
                {
                    var notify = loadStr(w.getNotifyUrl());
                    if (notify != null)
                    {
                        var seen = AppConfig.Get().getStr("GFT_SeenNotify");
                        var cur = AppConfig.Get().getStr("GFT_Version");

                        if (seen == null || seen != cur)
                        {
                            setCfgValue("GFT_SeenNotify", cur);
                            w.subWindow2 = new Changelogs(this.w, notify);
                            w.subWindow2.EditHeader(
                                "Girls Frontline 2: Exilium RU Launcher | Notify",
                                "Объявления"
                                );
                            w.subWindow2.Show();
                        }
                    }
                }
            });
        }
    }


    private void installLoadedJsonPack(CallbackUIEL callbackUIEL)
    {
        var param = AppConfig.Get().getStr("GFT_DTranlation");

        byte[]? jBytes = null, pBytes;

        try
        {
            if (HasBackup())
            {
                pBytes = File.ReadAllBytes(getBackupPackPath());
            }
            else
            {
                pBytes = File.ReadAllBytes(getLangPackPath());
            }
        }
        catch (Exception e)
        {
            Dispatcher.Invoke(() =>
            {
                MessageBox.Show("Что-то пошло не так ... \nИгра установлена некорректно, либо какой-то процесс блокирует доступ к файлам переводов");
            });
            OnPostLoad(false, callbackUIEL, null);
            return;
        }
        try
        {
            jBytes = File.ReadAllBytes(GF2_RU_PACK_JSON);
        }
        catch (Exception e)
        {
            if (param != null && param != "disabled")
            {
                Dispatcher.Invoke(() =>
                {
                    OnError(e, "Ошибка загрузки обновления!\n Будет использован до-перевод");
                });
            }
            else
            {
                Dispatcher.Invoke(() =>
                {
                    OnError(e, "Ошибка загрузки обновления");
                });
                OnPostLoad(false, callbackUIEL, null);
                return;
            }
        }

        LangEditCS table = new LangEditCS();

        table.Read(pBytes);
        if (jBytes != null)
        {
            table.mergeAll(Editor.fromJsonBytes(jBytes));
        }

        if (param != null && param != "disabled")
        {
            List<ELine> notTranslated = table.getNotTranslated(true);
            if (notTranslated.Count > 0)
            {
                TranslatorionPool pool = new TranslatorionPool();

                if (pool.Setup("en", "ru", param))
                {
                    callbackUIEL.building = true;
                    pool.setCache("data/translation_cache.json");
                    callbackUIEL.cb = new();
                    pool.TranslateAll(notTranslated, callbackUIEL.cb);
                    callbackUIEL.cb = null;
                }
            }
        }

        var eData = table.Write();

        try
        {
            //dont save to cache?
            //File.WriteAllBytes(GF2_RU_PACK, eData);
            OnPostLoad(true, callbackUIEL, eData);
        }
        catch (Exception err)
        {
            OnPostLoad(false, callbackUIEL, eData);
            Dispatcher.Invoke(() =>
            {
                OnError(err, "Ошибка сборки пакета");
            });
        }
    }


    public void OnError(Exception err, string? msg1)
    {
        if (msg1 == null)
        {
            msg1 = "Произошла непредвиденная ошибка\nПопробуйте запустить лаунчер от имени Администратора";
        }
        var all = "Error: " + msg1 + "\n";
        all += "Cause: " + err.Message;
        if (err.StackTrace != null)
        {
            all += "\n\nStack trace:\n";
            all += err.StackTrace;
        }
        try
        {
            File.AppendAllTextAsync("error.log", DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss") + "\n\n"+all+"\n\n\n");
            MessageBox.Show(msg1+"!\nЛог ошибки сохранен в error.log");
        }
        catch
        {
            MessageBox.Show(all, msg1+": " + err.Message);
        }
    }


    private void OnPostLoad(bool loaded, CallbackUIEL callbackUIEL, byte[]? packData)
    {
        if (loaded)
        {
            SetNeedCheckUpdatePack(false);
        }
        callbackUIEL.completed = true;
        if (loaded)
        {
            installLoadedPack(packData);
        }
        Dispatcher.Invoke(() => {
            locked = false;
            editBtnStatus(loaded, false);
        });
    }


    public bool IsNeedCheckUpdatePack()
    {
        return lastCheckUpdate.CompareTo(DateTime.Now.AddMinutes(10)) < 0;
    }


    public void SetNeedCheckUpdatePack(bool b)
    {
        if (b)
        {
            lastCheckUpdate = DateTime.MinValue;
        }
        else
        {
            lastCheckUpdate = DateTime.Now; 
        }
    }


    private class VersionResult
    {
        public bool needUpdate = true;
        public bool hasNityfy = false;
        public string? log = null;
    }


    private VersionResult CheckVersion()
    {
        VersionResult result = new VersionResult();
        string logResult = null;

        var needLoadPack = IsNeedCheckUpdatePack();

        if (IsNeedCheckUpdatePack())
        {
            string log = loadStr(getLoadChangesUrl());
            if (log != null)
            {
                string firstLine = log.Split("\n")[0];
                string curVersion = AppConfig.Get().getStr("GFT_Version");

                firstLine = firstLine.Trim();
                if (firstLine.StartsWith("!"))
                {
                    result.hasNityfy = true;
                    firstLine = firstLine.Substring(1);
                    log = log.Substring(1);
                }

                if(firstLine != null && curVersion != null && curVersion.Length != 0)
                {
                    if (firstLine.Equals(curVersion) && File.Exists(GF2_RU_PACK_JSON))
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

    }


    private void onInstallClickProxy(object sender, RoutedEventArgs e)
    {
        if (clickAction != null)
        {
            clickAction.Invoke(sender, e);
        }
    }


    private RoutedEventHandler clickAction;


    private void installLoadedPack(byte[]? packData)
    {
        try
        {

            string packPath = getLangPackPath();
            string backupPath = getBackupPackPath();

            App.CreatePathIfNeed(backupPath);

            if (packData != null) {

                if (File.Exists(backupPath))
                {
                    File.Delete(backupPath);
                }

                File.Move(packPath, backupPath);
                File.WriteAllBytes(packPath, packData);

                setCfgValue("GF2_ISHA256", getSHA256(packPath));

                Dispatcher.Invoke(() => {
                    BTnnUninstall.Visibility = Visibility.Visible;
                });
            }
        } catch (Exception err)
        {
            OnError(err, null);
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
        } catch (Exception err)
        {
            OnError(err, null);
        }
    }


    private bool HasBackup()
    {
        return File.Exists(getBackupPackPath());
    }


    private bool RemoveBackup()
    {
        if (HasBackup())
        {
            try
            {
                File.Delete(getBackupPackPath());
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
        var loaded = File.Exists(GF2_RU_PACK_JSON);

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

        if (AppConfig.Get().check("GFT_DisableUpdated", false))
        {
            return true;
        }
        var remoteVersion = loadStr(LAUNCH_VERSION_URL);
        if (remoteVersion == null || remoteVersion.Length == 0) return true;
        remoteVersion = remoteVersion.Trim();

        try
        {
            var lv = Double.Parse(LAUNCH_VERSION.Replace(".",""));
            var rv = Double.Parse(remoteVersion.Replace(".", ""));
            if (lv > rv) return true;
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
        try
        {
            using (WebClient webClient = new WebClient())
            {
                return webClient.DownloadData(url);
            }
        } catch
        {
            return null;
        }
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
        var status = AppConfig.Get().getStr("GFT_Discord");
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

        EnableDs0();
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
        if (!AppConfig.Get().check("GF2_HasFirstLaunch", false))
        {
            AppConfig.Get().setBool("GF2_HasFirstLaunch", true);
            AppConfig.Get().Save();
            OpenURL(STATISTIC_URL);
        }
    }


    public static void OpenURL(string url)
    {
        var sInfo = new System.Diagnostics.ProcessStartInfo(url)
        {
            UseShellExecute = true,
        };
        System.Diagnostics.Process.Start(sInfo);
    }


    public void KillLaunch(int attempts, int startDelay, int delay)
    {
        if (attempts <= 0) attempts = 1;
        CallInDedictedThread(() => {
            if(startDelay > 0)
            {
                Thread.Sleep(startDelay);
            }
            while (attempts > 0)
            {
                attempts -= 1;
                if (KillLaunch0())
                {
                    return;
                }
                Thread.Sleep(delay);
            }
        });
    }


    private bool KillLaunch0()
    {
        Process[] procList = Process.GetProcesses();

        var dirName = Directory.GetCurrentDirectory();
        int count = 0;

        foreach (var proc1 in procList)
        {

            if (proc1.ProcessName == "PCLauncher")
            {

                ProcessStartInfo psi = new ProcessStartInfo();
                psi.FileName = "CMD.exe";
                psi.Arguments = "/C taskkill /F /PID " + proc1.Id;
                psi.UseShellExecute = false;
                psi.CreateNoWindow = true;
                psi.WindowStyle = ProcessWindowStyle.Hidden;

                var proc = new Process();
                proc.StartInfo = psi;
                proc.Start();
                proc.WaitForExit();

                count += 1;
            }
        }
        return count > 0;
    }
}