using System.Configuration;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Text;
using System.Windows;
using System.Windows.Input;
using System.Windows.Media.Imaging;
using SourceChord.FluentWPF;

//using Google.Apis.Services;
//using Google.Apis.Analytics.v3;
//using Google.Apis.Auth.OAuth2;

namespace GF2_RU_Launch;


public partial class MainWindow : AcrylicWindow{
    public static string GF2_DATA_TABLE    = "GF2_Exilium_Data/LocalCache/Data/Table";
    public static string GF2_LANG_PACK     = "/LangPackageTableEnusData.bytes";
    public static string GF2_LANG_BACKUP   = "./backups/LangPackageTableEnusData.bytes";
    public static string GF2_RU_PACK       = "./loadings/LangPack.bytes";
    public static string GF2_LOAD_URL      = "https://gitlab.com/homaplus/grils-frontline-team-translate/-/raw/%__BRANCH%__%/LangPackageTableEnusDataRES.bytes?ref_type=heads&inline=false";
    public static string GF2_CHANGELOG_URL = "https://gitlab.com/homaplus/grils-frontline-team-translate/-/raw/%__BRANCH%__%/Changelog.md?ref_type=heads&inline=false";
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
        loadConfig();

        BTnnUninstall.Visibility = Visibility.Hidden;

        var rand = new Random(DateTime.Now.Second);
        string[] characters = {"colphne", "groza"};

        string name = characters[rand.Next(characters.Length)];

        int hr = DateTime.Now.Hour;

        if (hr > 7 && hr < 23)
        {
            name += "1";
        } else
        {
            name += "0";
        }

        BGImg.Source = ImgByName(name);

        BTnPlay.Click += onClickPlay;
        BTnPlayInstall.Click += onInstallClickProxy;
        BTnnUninstall.Click += onUnInstallClick;

        BTNEditor.Click += onClickOpenEditor;

        updatePlayBtn();
        editBtnStatus(false);

        onClickOpenEditor(null, null);
        

        try
        {
            setupAnalitics();
        }
        catch { 

        }
    }


    public void onEditorClosed()
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


    private void onClickOpenEditor(object sender, RoutedEventArgs e)
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


    private void onClickPlay(object sender, RoutedEventArgs e)
    {
        if (locked) return;
        if (checkValidGamePath())
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
        updatePlayBtn();
    }


    private void updatePlayBtn()
    {

        if (checkValidGamePath()) 
        {
            BTnPlay.Content = "Играть";
            BTnPlayInstall.Visibility = Visibility.Visible;
        } 
        else
        {
            BTnPlay.Content = "Выбрать игру";
            BTnPlayInstall.Visibility = Visibility.Hidden;
        }
    }


    public bool isValidGamePath()
    {
        return GF2_Path != null && GF2_Path.Length > 0;
    }


    public string getLangPackPath()
    {
        return GF2_Path + GF2_DATA_TABLE + GF2_LANG_PACK;
    }


    public string getBackupPackPath()
    {
        return GF2_Path + GF2_DATA_TABLE + GF2_LANG_BACKUP;
    }


    private bool checkValidGamePath()
    {
        if(isValidGamePath())
        {
            var hasBackup = File.Exists(getBackupPackPath());
            var installed = File.Exists(getLangPackPath());

            if (this.hasBackup()) 
            {
                BTnnUninstall.Visibility = Visibility.Visible;
            } 
            else
            {
                BTnnUninstall.Visibility = Visibility.Hidden;
            }
            return installed;
        }
        return false;
    }


    private void loadConfig()  {
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
                return;
            }
        }
        locked = true;
        BTnPlayInstall.Content = "Проверка версии...";

        Task.Run(() => {
            var resultT = checkVersion();
            bool result = resultT.Item1;
            bool completed = false;

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
                            BTnPlayInstall.Content = "Загрузка обновлений ."+add;
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
                    loaded = load(getLoadPackUrl(), GF2_RU_PACK);
                }
                catch
                {
                    Dispatcher.Invoke(() => {
                        MessageBox.Show("Ошибка загрузки обновления!");
                    });
                }
            } 
            else
            {
                loaded = true;
            }

            if (loaded)
            {
                needLoadPack = false;
            }
            completed = true;

            Dispatcher.Invoke(() => {
                locked = false;
                editBtnStatus(loaded);
            });
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


    private void installLoadedPack(object sender, RoutedEventArgs e)
    {
        try
        {
            string packPath = getLangPackPath();
            string origPath = getBackupPackPath();

            CreatePathIfNeed(origPath);

            if (!File.Exists(origPath))
            {
                File.Move(packPath, origPath);
                File.Copy(GF2_RU_PACK, packPath, true);
            }
            else
            {
                File.Delete(packPath);
                File.Copy(GF2_RU_PACK, packPath, true);
            }
            BTnnUninstall.Visibility = Visibility.Visible;
        } catch
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

                MessageBox.Show("Русский пакет локализации удален.");
            }
            else
            {
                MessageBox.Show("Пакет локализации по умолчанию не найден! Удаление русской локализации было отменено, в связи с тем, что это может сломать игру.");
            }
            editBtnStatus(false);
        } catch
        {
            onError();
        }
    }

    public void onError()
    {
        MessageBox.Show("Произошла непредвиденная ошибка\nПопробуйте запустить лаунчер от имени Администратора", "Ошибка");
    }


    private bool hasBackup()
    {
        return File.Exists(GF2_Path + GF2_DATA_TABLE + GF2_LANG_BACKUP);
    }


    private void editBtnStatus(bool updated)
    {
        var loaded = File.Exists(GF2_RU_PACK);

        string msg = null;
        if (loaded)
        {
            if (updated)
            {
                msg = "Установлено";
                installLoadedPack(null, null);
            } 
            else
            {
                if (hasBackup())
                {
                    msg = "Проверить обновления";
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
                if (hasBackup())
                {
                    msg = "Проверить обновления";
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


    private static bool load(string url, string filename)
    {
        CreatePathIfNeed(filename);
        using (WebClient webClient = new WebClient())
        {
            webClient.DownloadFile(url, filename);
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


    private static string getSHA256(byte[] src)
    {
        //SHA256 mySHA256 = SHA256.Create();
        //todo
        return "";
    }


    private void setupAnalitics()
    {
        //todo
    }
}