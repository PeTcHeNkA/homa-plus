using System.Windows;
using ArcApp;

namespace GF2_LaunchHX
{
    //Fluent WPF Acrylic APP template by HomaPlus
    public partial class App : Application
    {
        public App()
        {
            AppDomain.CurrentDomain.AssemblyResolve += ArcUtils.ArcAssemblyResolve;

            //boot code here
        }
    }
}