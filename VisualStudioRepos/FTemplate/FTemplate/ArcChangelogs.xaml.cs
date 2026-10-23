using SourceChord.FluentWPF;
using ArcApp;

namespace $safeprojectname$;


//Fluent WPF Acrylic APP template by HomaPlus
public partial class ArcChangelogs : AcrylicWindow
{
    private MainWindow parent;

    public ArcChangelogs(MainWindow parent, string text)
    {
        this.parent = parent;
        SetExtendViewIntoTitleBar(this, true);
        InitializeComponent();

        new ArcSmartTextCollection(parent, ChangelogContent.Items).AddText(text);
    }


    public ArcChangelogs EditHeader(string title, string header)
    {
        this.Title = title;
        ChangelogHeader.Text = header;
        return this;
    }


    protected override void OnClosed(EventArgs e)
    {
        parent.OnChildClosed(this);
        base.OnClosed(e);
    }
}