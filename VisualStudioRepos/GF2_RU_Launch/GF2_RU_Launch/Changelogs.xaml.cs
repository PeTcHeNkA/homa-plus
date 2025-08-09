using System.Windows;
using System.Windows.Controls;
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

            var items = Content.Items;

            foreach(NInfo inf in parseInfo(text))
            {
                inf.addItems(items);
            }
        }


        public void EditHeader(string title, string header)
        {
            this.Title = title;
            ChangeLHead.Text = header;
        }


        private List<NInfo> parseInfo(string text)
        {
            List<NInfo> list = new List<NInfo> ();
            NInfo info = new NInfo();
            list.Add(info);
            bool shouldNext = false;

            foreach (string line1 in text.Split("\n")) {
                string line = line1;
                int size = 5;
                while (line.StartsWith("#")) {
                    --size;
                    line = line.Substring(1);
                }
                if(size <= 0)
                {
                    size = 1;
                }
                if(size == 5)
                {
                    info.content += ("\n"+line);
                    shouldNext = true;
                    continue;
                }
                else
                {
                    if (shouldNext)
                    {
                        info = new NInfo();
                        list.Add(info);
                        shouldNext = false;
                    }
                }
                info.header = line;
                info.headerSize = size;
            }
            return list;
        }


        private class NInfo
        {
            public string header;
            public int headerSize;
            public string content = "";

            public void addItems(ItemCollection items)
            {
                if(!string.IsNullOrEmpty(header))
                {
                    TextBlock box = new TextBlock();
                    box.Text = header;

                    box.FontSize = 14 + (headerSize) * 3;
                    var mg = box.Margin;
                    mg.Left = 15;
                    box.Margin = mg;
                    items.Add(box);
                }
                if(!string.IsNullOrEmpty(content))
                {
                    TextBlock box2 = new TextBlock();
                    box2.Text = content.Trim();
                    box2.FontSize = 14;
                    var pd = box2.Padding;
                    pd.Bottom = 8;
                    box2.Padding = pd;
                    items.Add(box2);
                }
            }
        }
    }
}
