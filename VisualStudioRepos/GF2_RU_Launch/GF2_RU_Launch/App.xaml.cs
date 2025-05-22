using System.Configuration;
using System.Data;
using System.Windows;
using System.Windows.Media;

namespace GF2_RU_Launch;

/// <summary>
/// Interaction logic for App.xaml
/// </summary>
public partial class App : Application{

    public App()
    {
    }

    protected override void OnStartup(StartupEventArgs e)
    {
        //tweakColor("L_Background_Main_Color");
        //tweakColor("L_Background_Second");
        //tweakColor("Header_Color");
        //tweakColor("Header_Light");
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

