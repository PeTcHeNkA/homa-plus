using ArcApp;
using System.Windows;

namespace $safeprojectname$;


//Fluent WPF Acrylic APP template by HomaPlus
public partial class App : Application
{
    public App()
    {
        AppDomain.CurrentDomain.AssemblyResolve += ArcUtils.ArcAssemblyResolve;

        //boot code here
    }
}