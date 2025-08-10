using System.Configuration;
using System.Data;
using System.Windows;
using System.Windows.Media;

namespace GF2_RU_Launch;

/// <summary>
/// Interaction logic for App.xaml
/// </summary>
public partial class App : Application{
    private bool enableDark = false;
    public App()
    {
        //AppDomain.CurrentDomain.AppendPrivatePath(@"bin\DLLs");
    }

    protected override void OnStartup(StartupEventArgs e)
    {
        var args = parseArgs(e.Args);

        if (args.ContainsKey("-dark"))
        {
            var tweaks = new string[]{
            "L_Background_Main_Color", "L_Background_Second",
            "Header_Color", "Header_Light", "Header_Text_Color"
        };

            foreach (var tweak in tweaks) tweakColor(tweak);
        }

        base.OnStartup(e);

        //if (args.ContainsKey("-ed"))
        //{
        //    var langPath = args["-src"];
        //    var outPath  = args["-out"];

        //    if (args.ContainsKey("-indir"))
        //    {
        //        var dir = args["-indir"];

        //        string? inPath = null;
        //        if (args.ContainsKey("-inpath"))
        //        {
        //            inPath = args["-inpath"];
        //        }

        //        Editor.BuildPackNogui(null, langPath, dir, inPath, outPath);
        //    }
        //    else
        //    {
        //        var files = new List<string>(args["-in"].Split(";"));

        //        List<string>? inPath = null;
        //        if (args.ContainsKey("-inpath"))
        //        {
        //            inPath = new List<string>(args["-inpath"].Split(";"));
        //        }

        //        Editor.BuildPackNogui(null, langPath, files, inPath, outPath);
        //    }
        //    Console.WriteLine("Success");
        //}
        //else
        //{
            MainWindow main = new MainWindow();
            main.Show();
        //}
    }


    private Dictionary<string, string> parseArgs(string[] args)
    {
        var dict = new Dictionary<string, string>();

        string? last = null;
        foreach (var arg in args) {
            if (arg[0] == '-') {
                if (last == null)
                {
                    last = arg;
                }
                else
                {
                    dict[last] = "";
                    last = null;
                }
            }
            else
            {
                if (last == null) {
                    continue;
                }
                else
                {
                    dict[last] = arg;
                    last = null;
                }
            }
        }
        if (last != null)
        {
            dict[last] = "";
        }

        return dict;
    }


    private void tweakColor(string name)
    {
        var val = Resources[name+"_D"];
        if (val != null) {
            Resources[name] = val;
        }
    }
}


public class AppConfig
{
    private System.Configuration.Configuration cfg;
    private static AppConfig instance;

    public static AppConfig Get()
    {
        if (instance == null)
        {
            instance = new AppConfig();
        }
        return instance;
    }

    public AppConfig()
    {
        cfg = ConfigurationManager.OpenExeConfiguration(ConfigurationUserLevel.None);
    }


    public string? getStr(string key)
    {
        return ConfigurationManager.AppSettings[key];
    }


    public int getInt(string key)
    {
        string? val = getStr(key);
        if (val != null) {
            try
            {
                return int.Parse(val);
            }
            catch (FormatException) {
                return 0;
            }
        }
        return 0;
    }


    public bool check(string key, bool def)
    {
        string? val = getStr(key);
        if (val != null) {
            return val.Equals("true");
        }
        return def;
    }


    public bool? getBool(string key)
    {
        string? val = getStr(key);
        if (val != null)
        {
            return val.Equals("true");
        }
        return null;
    }


    public void del(string key)
    {
        var settings = cfg.AppSettings.Settings;
        settings.Remove(key);
    }


    public void setInt(string key, int val)
    {
        setStr(key, val.ToString());
    }


    public void setBool(string key, bool val)
    {
        if (val)
        {
            setStr(key, "true");
        } else
        {
            setStr(key, "false");
        }
    }


    public void setStr(string key, string value)
    {
        var settings = cfg.AppSettings.Settings;
        if (settings[key] == null)
        {
            settings.Add(key, value);
        }
        else
        {
            settings[key].Value = value;
        }
    }


    public void rollbackChanges()
    {
        var settings = cfg.AppSettings.Settings;

        foreach (string key in settings.AllKeys){
            setStr(key, getStr(key));
        }
    }


    public void confirmChanges()
    {
        ConfigurationManager.RefreshSection(cfg.AppSettings.SectionInformation.Name);
    }


    public void save()
    {
        confirmChanges();
        cfg.Save(ConfigurationSaveMode.Modified);
    }
}