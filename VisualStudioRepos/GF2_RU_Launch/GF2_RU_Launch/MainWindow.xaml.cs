using System.Configuration;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Security.Cryptography;
using System.Text;
using System.Windows;
using System.Windows.Input;
using System.Windows.Media.Imaging;
using LangEditAPI;
using SourceChord.FluentWPF;

//using Google.Apis.Services;
//using Google.Apis.Analytics.v3;
//using Google.Apis.Auth.OAuth2;

namespace GF2_RU_Launch;


public partial class MainWindow : AcrylicWindow{
    public static string GF2_DATA_TABLE    = "GF2_Exilium_Data/LocalCache/Data/Table";
    public static string GF2_LANG_PACK     = "/LangPackageTableEnusData.bytes";
    public static string GF2_LANG_BACKUP   = "./data/BACKUP_LangPackageTableEnusData.bytes";
    public static string GF2_RU_PACK       = "./data/LangPackageRU.bytes";
    public static string GF2_RU_PACK_JSON  = "./data/translation_ru.json";
    public static string GF2_LOAD_URL      = "https://gitlab.com/homaplus/grils-frontline-team-translate/-/raw/%__BRANCH%__%/translations_release.json?ref_type=heads&inline=false";
    public static string GF2_CHANGELOG_URL = "https://gitlab.com/homaplus/grils-frontline-team-translate/-/raw/%__BRANCH%__%/Changelog.md?ref_type=heads&inline=false";
    public static string NATIVES_LOAD_URL  = "https://gitlab.com/homaplus/girls-frontline-2-exilium-ru-launcher/-/raw/natives/%__FILE__%?ref_type=heads&inline=false";
    
    //private static string GF2_MASTER_URL;

    private string GF2_Path = null;
    private Changelogs subWindow;
    private Editor editor;
    private bool needLoadPack = true;
    private bool locked = false;

    public MainWindow() 
    {
        InitializeComponent();
        SetExtendViewIntoTitleBar(this, true);
        LoadConfig();
        ChooseRandomBg();
        BTnnUninstall.Visibility = Visibility.Hidden;

        UpdatePlayButton();
        editBtnStatus(false, true);

        //OnClickOpenEditor(null, null);

        try
        {
            SetupAnalitics();
        }
        catch { 

        }
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
        if(editor != null)
        {
            editor.Close();
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


    public string getLoadPackUrl()
    {
        string loadUrl = getCfgValue("GFT_LoadUrl");
        if (loadUrl == null || loadUrl.Length == 0)
        {
            loadUrl = GF2_LOAD_URL;
        }

        string branch = getCfgValue("GFT_Branch");
        if(branch == null || branch.Length == 0)
        {
            branch = "master";
        }

        return loadUrl.Replace("%__BRANCH%__%", branch);
    }


    public string getLoadChangesUrl()
    {
        string loadUrl = getCfgValue("GFT_ChangesUrl");
        if (loadUrl == null || loadUrl.Length == 0)
        {
            loadUrl = GF2_CHANGELOG_URL;
        }

        string branch = getCfgValue("GFT_Branch");
        if (branch == null || branch.Length == 0)
        {
            branch = "master";
        }

        //if (branch == "master")
        //{
        //    return GF2_MASTER_URL;
        //}
        return loadUrl.Replace("%__BRANCH%__%", branch);
    }


    public void onBtnLoadClick(object sender, RoutedEventArgs e)
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

        Task.Run(() => {
            var resultT = checkVersion();
            bool result = resultT.Item1;
            bool completed = false;
            bool building  = false;

            Dispatcher.Invoke(() => {
                BTnPlayInstall.Content = "Загрузка обновлений.";
                
                Task.Run(() => {
                    int points = 0;
                    while (!completed) {
                        Thread.Sleep(750);

                        if(points++ >= 3)
                        {
                            points = 0;
                        }

                        Dispatcher.Invoke(() => {
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
                            if (completed)return;
                            if (building)
                            {
                                BTnPlayInstall.Content = "Сборка пакета ." + add;
                            }
                            else
                            {
                                BTnPlayInstall.Content = "Загрузка обновлений ." + add;
                            }
                        });
                    }
                });

                if (resultT.Item2 != null && resultT.Item2.Length > 0)
                {
                    subWindow = new Changelogs(this, resultT.Item2);
                    subWindow.Show();
                }
            });

            var loaded = false;
            if (result)
            {
                try
                {
                    loaded = load(getLoadPackUrl(), GF2_RU_PACK_JSON);
                }
                catch (Exception e)
                {
                    Dispatcher.Invoke(() => {
                        MessageBox.Show("Ошибка загрузки обновления!");
                    });
                }
                if (loaded)
                {
                    if (!Editor.EnsureEditorLoaded())
                    {
                        onPostLoad(false, ref completed);
                        return;
                    }
                    building = true;

                    byte[]? jBytes, pBytes;
                    try
                    {
                        jBytes = File.ReadAllBytes(GF2_RU_PACK_JSON);
                        if (HasBackup())
                        {
                            pBytes = File.ReadAllBytes(GF2_LANG_BACKUP);
                        } else
                        {
                            pBytes = File.ReadAllBytes(getLangPackPath());
                        }
                    } catch
                    {
                        Dispatcher.Invoke(() => {
                            MessageBox.Show("Ошибка загрузки обновления!");
                        });
                        onPostLoad(false, ref completed);
                        return;
                    }

                    Dispatcher.Invoke(() =>
                    {
                        Table table = new Table(101);
                        table.load(pBytes);
                        table.applyMappngs(jBytes);

                        var eData = table.save();

                        Task.Run(() => {
                            try
                            {
                                File.WriteAllBytes(GF2_RU_PACK, eData);
                                onPostLoad(true, ref completed);
                            }
                            catch
                            {
                                onPostLoad(false, ref completed);
                                Dispatcher.Invoke(() => {
                                    MessageBox.Show("Ошибка сборки пакета!");
                                });
                            }
                            finally
                            {
                                table.free();
                            }
                        });
                    });
                }
            } 
            else
            {
                loaded = true;
                onPostLoad(true, ref completed);
            }
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


    private Tuple<bool, string?> checkVersion()
    {
        string logResult = null;
        if (needLoadPack)
        {
            string log = loadStr(getLoadChangesUrl());
            if (log != null)
            {
                string firstLine = log.Split("\n")[0];
                string curVersion = getCfgValue("GFT_Version");

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
        return Tuple.Create(needLoadPack, logResult);
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
                    if(result) RemoveBackup();
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


    private static bool load(string url, string filename)
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


    private static string loadStr(string url)
    {
        using (WebClient webClient = new WebClient())
        {
            byte[] data = webClient.DownloadData(url);
            return Encoding.UTF8.GetString(data);
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

    private void SetupAnalitics()
    {
        //todo
    }
}