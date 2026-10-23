using ArcApp;
using System.Text;
using System.Collections.Concurrent;
using System.IO;
using System.Runtime.CompilerServices;
using System.Text.Encodings.Web;
using System.Text.Json;


namespace GF2_LaunchHX.lib
{
    public interface ITranslateEngine
    {
        public bool Setup(MainWindow window, LangTable tb, TranslationCallback cb);
        public bool TranslateLines(List<ELine> lines, TranslationCallback cb);
        public bool Finish();
    }


    public class TranslationCallback
    {
        public int maxNum;
        public int cur;
        public bool active = true;
        public string _status;
        public double prevPercent = 0;

        [MethodImpl(MethodImplOptions.Synchronized)]
        public void Add(int n)
        {
            cur += n;
        }

        public string? status
        {
            get => _get();
            set => _status = value;
        }


        public void Reset()
        {
            cur = 0;
            prevPercent = 0;
            active = true;
        }


        public void Stop()
        {
            cur = 0;
            prevPercent = 0;
            active = false;
        }


        private string _get()
        {
            string prc = null;
            if (cur == 0)
            {
                prc = "0%";
            }
            if (maxNum <= cur)
            {
                prevPercent = 100;
                prc = "100%";
            }
            if (prc == null)
            {
                double a1 = ((double)cur / (double)maxNum) * 100d;
                if (a1 > 100)
                {
                    prevPercent = 100;
                    prc = "100%";
                }
                else
                {
                    prevPercent = a1;

                    double d2 = a1 % 1;
                    if (d2 < 0.099 || d2 > 0.099)
                    {
                        prc = a1.ToString("F0") + "%";
                    } 
                    else
                    {
                        prc = a1.ToString("F1").Replace(',', '.') + "%";
                    }

                    
                }
            }
            if (_status == null)
            {
                return prc;
            }
            else
            {
                if (active && prevPercent > 0)
                {
                    return _status + " " + prc;
                }
                else
                {
                    return _status;
                }
            }
        }
    }


    public class TCache
    {
        public ConcurrentDictionary<string, string> cache { get; private set; } = new ConcurrentDictionary<string, string>();
        private string path;
        private int modifications = 0;
        private long lastSaveTime = ArcUtils.CurTime();

        public TCache(string path)
        {
            this.path = path;
        }

        public void Read()
        {
            if (path == null) return;
            if (File.Exists(path))
            {
                byte[] data = File.ReadAllBytes(path);
                cache = JsonSerializer.Deserialize<ConcurrentDictionary<string, string>>(UTF8Encoding.UTF8.GetString(data));
            }
        }

        public void Write()
        {
            if (path == null) return;
            Task.Run(() =>
            {
                lock (path)
                {
                    var options = new JsonSerializerOptions
                    {
                        Encoder = JavaScriptEncoder.UnsafeRelaxedJsonEscaping,
                        WriteIndented = true,
                    };

                    var result = JsonSerializer.Serialize(cache, options);
                    ArcUtils.CreatePathIfNeed(path);
                    File.WriteAllBytes(path, UTF8Encoding.UTF8.GetBytes(result));
                    lastSaveTime = ArcUtils.CurTime();
                }
            });
        }


        public void Set(string key, string value)
        {
            cache[key] = value;
            lock (this)
            {
                ++modifications;
                if (modifications >= 100)
                {
                    modifications = 0;
                    if (lastSaveTime + 15_000 < ArcUtils.CurTime())
                    {
                        Write();
                    }
                }
            }
        }

        public string? Get(string key)
        {
            return cache.ContainsKey(key) ? cache[key] : null;
        }


        public bool ContainsKey(string key)
        {
            return cache.ContainsKey(key);
        }
    }
}