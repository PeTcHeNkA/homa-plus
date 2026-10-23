using ArcApp;
using SourceChord.FluentWPF;
using System.Security.Policy;
using System.Windows;
using System.Windows.Media.Imaging;

namespace GF2_LaunchHX
{
    public partial class CDialog : AcrylicWindow
    {
        public static Action<CDialog> A_YES = (w) => {
            w.Callback = true; w.Close();
        };
        public static Action<CDialog> A_NO = (w) => {
            w.Callback = false; w.Close();
        };

        private MainWindow mw;
        private Action<CDialog>? yes = A_YES, no = A_NO;
        public Object? Callback { get; set; }
        public CDialog(MainWindow mw)
        {
            this.mw = mw;
            SetExtendViewIntoTitleBar(this, true);
            InitializeComponent();
            Loaded += Pre0;
        }



        public CDialog With(Action<CDialog> a)
        {
            a.Invoke(this);
            return this;
        }


        public CDialog Title(string title)
        {
            base.Title = title;
            return this;
        }


        public Object Ask()
        {
            ShowDialog();
            return Callback;
        }



        public bool AskB()
        {
            ShowDialog();
            return Callback != null && (bool)Callback;
        }


        private void Pre0(object sender, RoutedEventArgs e)
        {
            if(BgImg.Visibility == Visibility.Collapsed)
            {
                SetBG(mw.curBGName == null ? GetDefBg() : mw.curBGName, true);
                BgImg.Visibility = Visibility.Visible;
            }
        }


        private string GetDefBg()
        {
            return ArcUtils.GetInnerURI("GF2_LaunchHX", "assets/default_bg.jpg").ToString();
        }


        public CDialog SetBG(string? url, bool crop)
        {
            if(url == null)
            {
                BgImg.Visibility = Visibility.Hidden;
                return this;
            }

            var bi = new BitmapImage();
            bi.BeginInit();
            bi.UriSource = new Uri(url);
            bi.EndInit();

            if (crop)
            {
                BgImg.Source = Crop0(bi);
            } else
            {
                BgImg.Source = bi;
            }
            return this;
        }


        private double GetCoef()
        {
            var w = mw.Width / Width;
            var h = mw.Height / Height;
            return Math.Min(w, h) * 1.3;
        }


        private BitmapSource Crop0(BitmapSource img)
        {
            var c = GetCoef();
            //var si = 0;
            var si = ((int)((double)(img.PixelWidth) / c));
            var hi = ((int)((double)(img.PixelHeight) / c));

            return new CroppedBitmap(img,
                new Int32Rect(si/2, hi/14, img.PixelWidth - si, img.PixelHeight - hi)
            );
        }


        public CDialog SetBtnText(string no, string yes)
        {
            BtnYes.Content = yes;
            BtnNo.Content = no;
            ModeMerge(false);
            return this;
        }


        public CDialog SetBtnText(string str)
        {
            BtnOk.Content = str;
            ModeMerge(true);
            return this;
        }

        private void ModeMerge(bool b)
        {
            if (b)
            {
                BtnOk.Visibility = Visibility.Visible;
                BtnYes.Visibility = Visibility.Hidden;
                BtnNo.Visibility = Visibility.Hidden;
            } else
            {

                BtnOk.Visibility = Visibility.Hidden;
                BtnYes.Visibility = Visibility.Visible;
                BtnNo.Visibility = Visibility.Visible;
            }
        }


        public CDialog SetActions(Action<CDialog> ok)
        {
            yes = ok;
            ModeMerge(true);
            return this;
        }


        public CDialog SetActions(Action<CDialog> no, Action<CDialog> yes)
        {
            this.yes = yes;
            this.no = no;
            ModeMerge(false);
            return this;
        }


        public CDialog Discription(string text)
        {
            DiscriptionCTN.Visibility = Visibility.Visible;
            DiscriptionVB.Text = text;

            int lines = text.Split('\n').Length;

            if (lines > 2)
            {
                int add = (lines-2)* ((int)(DiscriptionVB.FontSize * 1.2));

                this.Height += (add);
            }
            return this;
        }


        private void BtnYes_Click(object sender, RoutedEventArgs e)
        {
            if (yes != null)
            yes.Invoke(this);
        }


        private void BtnNo_Click(object sender, RoutedEventArgs e)
        {
            if(no != null)
            no.Invoke(this);
        }
    }
}