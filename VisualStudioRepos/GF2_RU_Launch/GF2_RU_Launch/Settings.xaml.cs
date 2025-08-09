using SourceChord.FluentWPF;


namespace GF2_RU_Launch
{
    public partial class Settings : AcrylicWindow
    {
        public static string UN_BRANCH = "master";
        private MainWindow parent;
        public Settings(MainWindow main)
        {
            this.parent = main;
            UN_BRANCH = main.getBranch();
            InitializeComponent();

            Packs.ItemsSource = new List<PackInf>();
        }
    }


    public class PackInf
    {
        public int Priority { get; set; }
        public string Name { get; set; }
        public string Source { get; set; }
    }
}