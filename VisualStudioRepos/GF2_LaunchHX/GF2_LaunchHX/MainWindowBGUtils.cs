using ArcApp;
using SourceChord.FluentWPF;
using System.IO;
using System.Text;
using System.Text.Encodings.Web;
using System.Text.Json;
using System.Text.Json.Serialization;
using System.Windows;
using System.Windows.Media;
using System.Windows.Media.Imaging;


namespace GF2_LaunchHX
{
    public partial class MainWindow : AcrylicWindow
    {
        public string curBGName = null;
        private void SetupBG()
        {
            var res = ChoiceBG();
            if (res.Item1 != null)
            {
                PostChoiceBG(res.Item1, false);
            }

            if (res.Item2 != null)
            {
                Task.Run(() => {
                    var res1 = res.Item2.Invoke();
                    if (res1 != null)
                    {
                        Dispatcher.Invoke(() => {
                            PostChoiceBG(res1, true);
                        });
                    }
                });
            }

            ChangeBgEvery(100);
        }


        void ChangeBgEvery(int secs)
        {
            Task.Run(() => {
                while (true)
                {
                    Thread.Sleep(secs * 1000);
                    var res = ChoiceBG();
                    if (res.Item1 != null)
                    {
                        Dispatcher.Invoke(() =>
                        {
                            PostChoiceBG(res.Item1, true);
                        });
                    }
                }
            });
        }


        class ChangeBgAnimation
        {
            long startTime; //duration 1000ms
            MainWindow? mw;

            public ChangeBgAnimation(MainWindow w, ImageSource bi)
            {
                if(w.BGImgTemp.Visibility != Visibility.Hidden)
                {
                    return;
                }
                mw = w;
                var prev = mw.BGImg.Source;
                mw.BGImg.Source = bi;
                mw.BGImgTemp.Source = prev;
                mw.BGImgTemp.Visibility = Visibility.Visible;
                startTime = ArcUtils.CurTime();
                CompositionTarget.Rendering += this.Handle;
            }


            void Handle(object? obj, EventArgs args)
            {
                double d = (ArcUtils.CurTime() - startTime);
                if (d > 1000)
                {
                    if (mw == null) return;
                    mw.BGImg.Opacity = 1;
                    mw.BGImgTemp.Source.Freeze();
                    mw.BGImgTemp.Visibility = Visibility.Hidden;
                    CompositionTarget.Rendering -= this.Handle;
                    mw = null;
                } 
                else
                {
                    d = d / 1000;
                    mw.BGImg.Opacity = d;
                    mw.BGImgTemp.Opacity = 1 - d;
                }
            }
        }


        private void PostChoiceBG(string img0, bool smooth)
        {

            var bi = new BitmapImage();
            bi.BeginInit();
            bi.UriSource = new Uri(img0);
            bi.EndInit();

            curBGName = img0;

            if (smooth)
            {
                new ChangeBgAnimation(this, Crop0(bi));
            }
            else
            {
                BGImg.Source = Crop0(bi);
            }
        }


        private static BitmapSource Crop0(BitmapSource img)
        {
            var si = 0;
            //var si = img.PixelWidth / 100;
            var hi = img.PixelHeight / 18;

            return new CroppedBitmap(img,
                new Int32Rect(si, 0, img.PixelWidth - si, img.PixelHeight - hi)
            );
        }


        private static Tuple<string?, Func<string?>?> ChoiceBG()
        {
            var bgId = new BgPreloadResponse();

            string? fileName = null;
            Func<string?>? func = null;

            if (bgId.choise != null)
            {
                fileName = BgIdToFIle(bgId.choise);
            }
            bool exists = File.Exists(fileName);

            if (bgId.ShoildInvoke() || !exists)
            {
                func = () => {
                    var item1 = bgId.choise;

                    if(bgId.ShoildInvoke() || item1 == null)
                    {
                        item1 = bgId.Invoke();
                    }
                    fileName = BG_CACHE_PATH + item1 + ".png";

                    ArcUtils.Load(BG_URL_FORMAT.Replace("%BG_NAME%", item1), fileName);
                    return fileName;
                };
            }
            if (!exists && bgId.infoFile.Item1 != null)
            {
                fileName = BgIdToFIle(ChoiceBGID0(bgId.infoFile.Item1, true));
                //try use cache
            }

            return new Tuple<string?, Func<string?>?>(fileName, func);
        }


        static string BgIdToFIle(string id)
        {
            return BG_CACHE_PATH + id + ".png";
        }


        class BgPreloadResponse
        {
            public string? choise;
            public Tuple<BgSInfoFile?, Func<BgSInfoFile?>> infoFile;

            public BgPreloadResponse()
            {
                infoFile = GetInfo0();
                if (infoFile.Item1 != null)
                {
                    choise = ChoiceBGID0(infoFile.Item1, false);
                }
            }

            public bool ShoildInvoke()
            {
                return infoFile.Item2 != null;
            }

            public string? Invoke()
            {
                return ChoiceBGID0(infoFile.Item2.Invoke(), false);
            }

            public Func<string?> ToTask()
            {
                return () => Invoke();
            }
        }


        private static string ChoiceBGID0(BgSInfoFile? inf, bool exists)
        {
            if (inf != null && inf.backgrounds.Count > 0)
            {
                var time = DateTime.Now;
                DayTime dayTime = GetDayTime(time);
                bool isSummer = time.Month >= 4 && time.Month <= 9;
                float summW = 0;

                var infs01 = inf.backgrounds.Select((k, i) =>
                {
                    var w = k.Value.CalcWeight(dayTime, isSummer);
                    summW += w;
                    return new BgInfoExtended
                    {
                        background_id = k.Key,
                        info = k.Value,
                    };
                });

                BgInfoExtended[] infs;
                if (exists)
                {
                    var v = Directory.EnumerateFiles(BG_CACHE_PATH, "*.png").Select((n)=>n.Replace(BG_CACHE_PATH,"").Replace(".png","")).ToHashSet();

                    infs = infs01.Where((a) => v.Contains(a.background_id)).ToArray();

                } else
                {
                    infs = infs01.ToArray();
                }

                var choice = (float)new Random().NextDouble() * summW;

                float curW = 0;

                for (int i = 0; i < infs.Length; i++)
                {
                    curW += infs[i].info.CalcWeight(dayTime, isSummer);

                    if (curW > choice)
                    {
                        return infs[i].background_id;
                    }
                }
                return infs[infs.Length - 1].background_id;

            }
            return "assets/default_bg.jpg";
        }


        private static DayTime GetDayTime(DateTime time)
        {
            DayTime dayTime;
            if (time.Hour >= 6 && time.Hour < 12)
            {
                dayTime = DayTime.Morning;
            }
            else if (time.Hour >= 12 && time.Hour < 18)
            {
                dayTime = DayTime.Day;
            }
            else if (time.Hour >= 18 && time.Hour < 24)
            {
                dayTime = DayTime.Evening;
            }
            else
            {
                dayTime = DayTime.Night;
            }
            return dayTime;
        }


        private static Tuple<BgSInfoFile?, Func<BgSInfoFile?>> GetInfo0()
        {
            var status = ArcUtils.CreatePathIfNeed(BG_CACHE_PATH + BG_INFS_FILE_NAME);
            var update = false;

            BgSInfoFile? inf = null;

            if (status == ArcUtils.FileExistStatus.ExistFile)
            {
                inf = Read0();
            }

            if (inf != null)
            {
                if (inf.version != APP_VERSION || DateTimeOffset.FromUnixTimeSeconds(long.Parse(inf.check_time)).AddHours(12) < DateTimeOffset.Now)
                {
                    update = true;
                }
                if (inf.version != APP_VERSION)
                {
                    update = true;
                }
            }
            if (inf == null || update)
            {
                return new Tuple<BgSInfoFile?, Func<BgSInfoFile?>?>(inf, () => LoadRemote());
            }
            return new Tuple<BgSInfoFile?, Func<BgSInfoFile?>?>(inf, null);
        }


        private static BgSInfoFile? LoadRemote()
        {
            BgSInfoFile? inf = null;
            string str = ArcUtils.LoadStr(BG_URL_FORMAT.Replace("%BG_NAME%.png", BG_INFS_FILE_NAME));
            if (str != null)
            {
                inf = JsonSerializer.Deserialize<BgSInfoFile>(str);
                inf.check_time = DateTimeOffset.Now.ToUnixTimeSeconds().ToString();
                inf.version = APP_VERSION;

                str = JsonSerializer.Serialize(inf, new JsonSerializerOptions
                {
                    Encoder = JavaScriptEncoder.UnsafeRelaxedJsonEscaping,
                    WriteIndented = true,
                });
                File.WriteAllBytes(BG_CACHE_PATH + BG_INFS_FILE_NAME, UTF8Encoding.UTF8.GetBytes(str));
            }
            return inf;
        }


        private static BgSInfoFile Read0()
        {
            byte[] data = File.ReadAllBytes(BG_CACHE_PATH + BG_INFS_FILE_NAME);
            return JsonSerializer.Deserialize<BgSInfoFile>(UTF8Encoding.UTF8.GetString(data));
        }
    }


    public enum DayTime
    {
        Morning,
        Day,
        Evening,
        Night
    }


    class BgSInfoFile
    {
        public string check_time { get; set; } = "-1";
        public string version { get; set; } = MainWindow.APP_VERSION;
        public Dictionary<string, BgInfo> backgrounds { get; set; } = new Dictionary<string, BgInfo>();
        [JsonIgnore]
        public HashSet<string>? existFiles;
    }


    class BgInfoExtended
    {
        public string background_id;
        public BgInfo info;
    }


    class BgInfo
    {
        public float morning { get; set; } = 0.5f;
        public float day { get; set; } = 0.5f;
        public float evening { get; set; } = 0.5f;
        public float night { get; set; } = 0.5f;
        public float summer { get; set; } = 0.5f;
        public float winter { get; set; } = 0.5f;

        public float CalcWeight(DayTime dayTime, bool isSummer)
        {
            float weight = 0;
            switch (dayTime)
            {
                case DayTime.Morning:
                    weight += morning;
                    break;
                case DayTime.Day:
                    weight += day;
                    break;
                case DayTime.Evening:
                    weight += evening;
                    break;
                case DayTime.Night:
                    weight += night;
                    break;
            }
            weight *= isSummer ? summer : winter;
            return weight;
        }
    }
}