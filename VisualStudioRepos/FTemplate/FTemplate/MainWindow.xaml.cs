using SourceChord.FluentWPF;
using System.Windows;

namespace $safeprojectname$;


//Fluent WPF Acrylic APP template by HomaPlus
public partial class MainWindow : AcrylicWindow
{
    public MainWindow()
    {
        SetExtendViewIntoTitleBar(this, true);
        InitializeComponent();

        //code here
    }


    public void ShowChangelog(object sender, RoutedEventArgs e)
    {
        OpenChild(new ArcChangelogs(this, "### Example header\n Example text 1\nExample text 2").EditHeader("Title", "Header"));
    }
}