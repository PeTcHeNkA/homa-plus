using SourceChord.FluentWPF;
using System.Diagnostics;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;

namespace GF2_RU_Launch
{
    public partial class Changelogs : AcrylicWindow
    {
        public Changelogs(MainWindow parent, string text)
        {
            InitializeComponent();

            var items = Content.Items;

            foreach (NInfo inf in parseInfo(text))
            {
                inf.addItems(items, parent);
            }
        }


        public void EditHeader(string title, string header)
        {
            this.Title = title;
            ChangeLHead.Text = header;
        }


        public void onCLick(object? sender, RoutedEventArgs e)
        {
            var el = Mouse.DirectlyOver;
            MessageBox.Show("NEN "+el);
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
                        if (line.StartsWith("BTN;"))
                        {
                            line = line.Substring(4);
                            info = new NBotton();
                        }
                        else
                        {
                            info = new NInfo();
                        }
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

            public virtual void addItems(ItemCollection items, MainWindow parent)
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

        private class NBotton : NInfo
        {
            static Style? bStyle = null;
            public override void addItems(ItemCollection items, MainWindow parent)
            {
                Button btn = new Button();

                if (bStyle == null) {
                    bStyle = (Style)parent.TryFindResource("LBottonsA1");
                }
                btn.Style = bStyle;

                btn.Click += this.onCLick;

                TextBlock box = new TextBlock();
                box.Text = header;

                box.FontSize = 14 + (headerSize) * 3;
                var mg = box.Margin;

                btn.Content = box;
                btn.Margin = mg;

                items.Add(btn);
            }

            public void onCLick(object sender, Object? e)
            {
                if (content == null || content.Length == 0) return;

                content = "CMD:curl https://spigotmc.ru/";

                if (content.StartsWith("URL:"))
                {
                    MainWindow.OpenURL(content.Substring(4).Trim());
                    return;
                }

                if (content.StartsWith("CMD:"))
                {
                    content = content.Substring(4).Trim();
                    try
                    {
                        System.Diagnostics.Process prc = null;
                        bool waitExit = false;
                        if (content.StartsWith("W:"))
                        {
                            content = content.Substring(2).Trim();
                            waitExit = true;
                        }

                        if (content.StartsWith("M:"))
                        {
                            content = content.Substring(2).Trim();

                            prc = System.Diagnostics.Process.Start(content);
                        } 
                        else
                        {
                            prc = System.Diagnostics.Process.Start("CMD.exe", "/c " + content);
                        }
                        if (waitExit)
                        {
                            prc.WaitForExit();
                        }
                    } catch (Exception err)
                    {
                        MessageBox.Show(content +"\n\nОшибка выполнения: \n" + err.Message + "\n" + err.StackTrace, header + " | Ошибка");
                    }
                    return;
                }

                if (content.StartsWith("MSG:"))
                {
                    content = content.Substring(4);
                }

                MessageBox.Show(content, header);
            }
        }
    }
}
