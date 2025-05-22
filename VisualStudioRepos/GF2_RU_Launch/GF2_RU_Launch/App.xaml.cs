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
        if (enableDark)
        {
            var tweaks = new string[]{
            "L_Background_Main_Color", "L_Background_Second",
            "Header_Color", "Header_Light", "Header_Text_Color"
        };

            foreach (var tweak in tweaks) tweakColor(tweak);
        }

        base.OnStartup(e);
    }

    private void tweakColor(string name)
    {
        var val = Resources[name+"_D"];
        if (val != null) {
            Resources[name] = val;
        }
    }
}

