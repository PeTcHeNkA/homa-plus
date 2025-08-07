using SourceChord.FluentWPF;

namespace GF2_RU_Launch
{
    /// <summary>
    /// Логика взаимодействия для Window1.xaml
    /// </summary>
    public partial class Changelogs : AcrylicWindow
    {
        public Changelogs(MainWindow parent, string text)
        {
            InitializeComponent();

            ChangeLText.Text = text;
        }

        public void EditHeader(string title, string header)
        {
            this.Title = title;
            ChangeLHead.Text = header;
        }
    }
}
