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

        if (args.ContainsKey("-ed"))
        {
            var langPath = args["-src"];
            var outPath  = args["-out"];

            if (args.ContainsKey("-indir"))
            {
                var dir = args["-indir"];

                string? inPath = null;
                if (args.ContainsKey("-inpath"))
                {
                    inPath = args["-inpath"];
                }

                Editor.BuildPackNogui(null, langPath, dir, inPath, outPath);
            }
            else
            {
                var files = new List<string>(args["-in"].Split(";"));

                List<string>? inPath = null;
                if (args.ContainsKey("-inpath"))
                {
                    inPath = new List<string>(args["-inpath"].Split(";"));
                }

                Editor.BuildPackNogui(null, langPath, files, inPath, outPath);
            }
            Console.WriteLine("Success");
        }
        else
        {
            MainWindow main = new MainWindow();
            main.Show();
        }
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

