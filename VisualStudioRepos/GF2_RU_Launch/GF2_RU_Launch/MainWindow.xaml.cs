using System.Configuration;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Windows;
using System.Windows.Input;
using System.Windows.Media.Imaging;
using SourceChord.FluentWPF;
namespace GF2_RU_Launch;


public partial class MainWindow : AcrylicWindow{
    private static string GF2_DATA_TABLE    = "GF2_Exilium_Data/LocalCache/Data/Table";
    private static string GF2_LANG_PACK     = "/LangPackageTableEnusData.bytes";
    private static string GF2_LANG_BACKUP   = "/LangPackageTableEnusData_ORIGINAL.bytes";
    private static string GF2_RU_PACK       = "./LangPack.bytes";
    private static string GF2_LOAD_URL      = "https://gitlab.com/homaplus/grils-frontline-team-translate/-/raw/%__BRANCH%__%/LangPackageTableEnusDataRES.bytes?ref_type=heads&inline=false";
    private static string GF2_CHANGELOG_URL = "https://gitlab.com/homaplus/grils-frontline-team-translate/-/raw/%__BRANCH%__%/Changelog.md?ref_type=heads&inline=false";

    private string GF2_Path = null;
    private Changelogs subWindow;
    private bool needLoadPack = true;

    public MainWindow() 
    {
        InitializeComponent();
        loadConfig();
        //BTnnUninstall.Content = "DEL";
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

        updatePlayBtn();
        editBtnStatus(false);
    }


    protected override void OnClosed(EventArgs e)
    {
        base.OnClosed(e);
        if(subWindow != null)
        {
            subWindow.Close();
        }
    }


    private void onClickPlay(object sender, RoutedEventArgs e)
    {

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


    private bool checkValidGamePath()
    {
        if(GF2_Path != null && GF2_Path.Length > 0)
        {
            string tablePath = GF2_Path + GF2_DATA_TABLE;

            string packPath = tablePath + GF2_LANG_PACK;
            string origPath = tablePath + GF2_LANG_BACKUP;

            //MessageBox.Show(packPath);

            var hasBackup = File.Exists(origPath);
            var installed = File.Exists(packPath);

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

        return loadUrl.Replace("%__BRANCH%__%", branch);
    }


    public void onBtnLoadClick(object sender, RoutedEventArgs e)
    {
        bool result = true;
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
                    subWindow = new Changelogs(this, log);
                    subWindow.Show();
                }
            }

            result = load(getLoadPackUrl(), GF2_RU_PACK);
        }
        if (result)
        {
            needLoadPack = false;
        }
        editBtnStatus(result);
    }


    private void onInstallClickProxy(object sender, RoutedEventArgs e)
    {   

        //if ((Keyboard.Modifiers & ModifierKeys.Shift) > 0)
        //{
        //    subWindow = new Changelogs(this, "");
        //    subWindow.Show();

        //    return;
        //}

        if(clickAction != null)
        {
            clickAction.Invoke(sender, e);
        }
    }


    private RoutedEventHandler clickAction;


    private void installLoadedPack(object sender, RoutedEventArgs e)
    {
        //todo install
        string tablePath = GF2_Path + GF2_DATA_TABLE;

        string packPath = tablePath + GF2_LANG_PACK;
        string origPath = tablePath + GF2_LANG_BACKUP;

        if (!File.Exists(origPath))
        {
            File.Move(packPath, origPath);
            File.Copy(GF2_RU_PACK, packPath, true);
        } else
        {
            File.Delete(packPath);
            File.Copy(GF2_RU_PACK, packPath, true);
        }
        BTnnUninstall.Visibility = Visibility.Visible;
    }


    public void onUnInstallClick(object sender, RoutedEventArgs e)
    {
        string tablePath = GF2_Path + GF2_DATA_TABLE;

        string packPath = tablePath + GF2_LANG_PACK;
        string origPath = tablePath + GF2_LANG_BACKUP;

        if (File.Exists(origPath))
        {
            File.Delete(packPath);
            File.Move(origPath, packPath);
            BTnnUninstall.Visibility = Visibility.Hidden;

            MessageBox.Show("Русский пакет локализации удален.");
        } else
        {
            MessageBox.Show("Пакет локализации по умолчанию не найден! Удаление русской локализации было отменено, в связи с тем, что это может сломать игру.");
        }
        editBtnStatus(false);
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
                clickAction = null;
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
        using (WebClient webClient = new WebClient())
        {
            webClient.DownloadFile(url, filename);
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
}