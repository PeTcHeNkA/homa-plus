using SourceChord.FluentWPF;


namespace GF2_RU_Launch
{
    public partial class Settings : AcrylicWindow
    {
        private MainWindow parent;
        public Settings(MainWindow main)
        {
            this.parent = main;
            InitializeComponent();

            Packs.ItemsSource = new List<PackInf>();

        }


        public class PackInf
        {
            public int Priority { get; set; }
            public string Name{ get; set; }
            public string Source{ get; set; }
        }
    }
}