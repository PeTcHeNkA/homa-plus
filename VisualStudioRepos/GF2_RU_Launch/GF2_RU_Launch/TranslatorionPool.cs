using System.Collections.Concurrent;
using System.IO;
using System.Runtime.CompilerServices;
using System.Text;
using System.Text.Encodings.Web;
using System.Text.Json;
using GTranslate.Results;
using GTranslate.Translators;

namespace GF2_RU_Launch
{
    public class TranslatorionPool
    {
        private string from = "en", to = "ru";
        private List<TNode> nodes = new List<TNode>();
        private int nextNode = 0;
        private bool enableFixes = true;
        private TCache cache = new TCache(null);

        public TranslatorionPool()
        {
            if (!ensureNativesInstalled())
            {
                throw new Exception("Natives dont loaded!");
            }
        }


        public void setCache(string path)
        {
            cache = new TCache(path);
            cache.Read();
        }


        private bool ensureNativesInstalled()
        {
            var b = MainWindow.ensureNativeInstalled("GTranslate.dll");
            b &= MainWindow.ensureNativeInstalled("System.Net.Http.Json.dll");
            b &= MainWindow.ensureNativeInstalled("System.Text.Json.dll");
            b &= MainWindow.ensureNativeInstalled("System.Text.Encodings.Web.dll");
            b &= MainWindow.ensureNativeInstalled("System.IO.Pipelines.dll");
            b &= MainWindow.ensureNativeInstalled("Microsoft.Bcl.AsyncInterfaces.dll");
            return b;
        }


        public bool Setup(string from, string to, string param)
        {
            this.from = from;
            this.to = to;
            nodes.Clear();

            foreach (string pm in param.Split(";")) {
                var inf = pm.Split(':');
                int maxRate = 3;
                if(inf.Length > 1)
                {
                    try
                    {
                        maxRate = int.Parse(inf[1]);
                    }
                    catch (Exception ex) { }
                }
                ITranslator translator = mkFrom(inf[0]);
                if(translator == null)
                {
                    continue;
                }
                nodes.Add(new TNode(this, translator, maxRate));
            }
            return nodes.Count > 0;
        }


        private ITranslator mkFrom(string key)
        {
            switch (key)
            {
                case "google":
                    {
                        return new GoogleTranslator();
                    }
                case "google2":
                    {
                        return new GoogleTranslator2();
                    }
                case "bing":
                    {
                        return new BingTranslator();
                    }
                case "yandex":
                    {
                        return new YandexTranslator();
                    }
                case "aggregate":
                    {
                        return new AggregateTranslator();
                    }
            }
            return null;
        }


        public bool TranslateAll(List<ELine> lines, TCallback cb)
        {
            if (nodes.Count == 0)
            {
                cb.completed = true;
                return false;
            }
            cb.completed = false;
            cb.total = lines.Count;

            if (enableFixes)
            {
                
                TranslateFunc func = (src, future) => {
                    if (!Editor.isTranslatable(src))
                    {
                        future.Invoke(src);
                        return;
                    }
                    if (cache.ContainsKey(src))
                    {
                        future.Invoke(cache.Get(src));
                        return;
                    }
                    string result = Task.Run(() => TranslateInPool(src)).GetAwaiter().GetResult();
                    cache.Set(src, result);
                    future.Invoke(result);
                };
                lines.AsParallel().ForAll(async line => {
                    line.Value = TranslateFixed(line.Original, func);
                    cb.Add();
                });
            }
            else {
                lines.AsParallel().ForAll(async line => {
                    line.Value = await TranslateInPool(line.Original);
                    cb.Add();
                });
            }
            cb.completed = true;
            cache.Write();
            return true;
        }


        private static string TranslateFixed(string src, TranslateFunc func)
        {
            string result = null;
            Action<string> future = (res) => { result = res; };
            TranslateItems(SlitColor(src), 1000, func, future);
            return result;
        }


        private async Task<string> TranslateInPool(string src)
        {
            for (int i = 0; i < nodes.Count; i++)
            {
                var node1 = nodes.ElementAt(i);
                if (node1.isFree)
                {
                    return await node1.translate(src);
                }
            }
            var node = nodes.ElementAt(nextNode % nodes.Count);
            nextNode += 1;
            node.awaitUnlock();
            return await node.translate(src);
        }


        private static long CurTime()
        {
            return DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        }


        public class TCallback
        {
            public int total;
            public int translated;
            public bool completed;
            [MethodImpl(MethodImplOptions.Synchronized)]
            public void Add()
            {
                translated += 1;
            }
        }


        private class TCache
        {
            public ConcurrentDictionary<string, string> cache { get; private set; } = new ConcurrentDictionary<string, string>();
            private string path;
            private int modifications = 0;
            private long lastSaveTime = CurTime();

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
                        MainWindow.CreatePathIfNeed(path);
                        File.WriteAllBytes(path, UTF8Encoding.UTF8.GetBytes(result));
                        lastSaveTime = CurTime();
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
                        if (lastSaveTime + 15_000 < CurTime()) {
                            Write();
                        }
                    }
                }
            }

            public string Get(string key)
            {
                return cache[key];
            }

            public bool ContainsKey(string key)
            {
                return cache.ContainsKey(key);
            }
        }


        private class TNode
        {
            private TranslatorionPool parent;
            public int priority;
            public int maxRate;
            public bool isAlive = true;
            private ITranslator engine;
            private long cuTime = CurTime()-1000;
            private long delayTime;
            public bool isFree { get; private set; }
            private int countErrors = 0;

            public TNode(TranslatorionPool parent, ITranslator engine, int rate)
            {
                this.parent = parent;
                this.engine = engine;
                this.maxRate = rate;
                this.delayTime = 1000 / rate;
            }


            public void awaitUnlock()
            {
                if (isFree) return;
                lock (this) {
                    isFree = true;
                }
            }


            private void awaitToNextRequest()
            {

                long cur = CurTime();
                long delta = cuTime - cur; ;

                if(cuTime < cur)
                {
                    cuTime = cur;
                }
                cuTime += delayTime;

                long delays = delta / delayTime;
                if(delays >= maxRate)
                {
                    long toNext = (delta % delayTime) + 1;
                    if(delays > maxRate)
                    {
                        toNext += delayTime;
                    }
                    Thread.Sleep((int)toNext);
                }
            }


            public async Task<string> translate(string src)
            {
                lock (this) {
                    isFree = false;
                    awaitToNextRequest();
                }
                ITranslationResult result;
                try
                {
                    result = await engine.TranslateAsync(src, parent.to, parent.from);
                    countErrors = 0;
                }
                catch (Exception e) {
                    countErrors += 1;
                    cuTime += (delayTime * maxRate);

                    var result2 = await parent.TranslateInPool(src);

                    lock (this)
                    {
                        isFree = true;
                    }
                    return result2;
                }
                lock (this) {
                    isFree = true;
                }
                return result.Translation;
            }
        }


        private class Item
        {
            public int mode;
            public string text;
            public Item(int mode, string text)
            {
                this.mode = mode;
                this.text = text;
            }
        }


        private delegate void TranslateFunc(string src, Action<string> future);


        private static void SplitedTranslate(string text, int lim, TranslateFunc func, Action<string> future)
        {
            if (text.Length > lim)
            {
                LinkedList<string> chunks = new LinkedList<string>();

                int pos = 0;
                while (pos != -1)
                {
                    int index;
                    if (pos + lim > text.Length)
                    {
                        index = -1;
                    }
                    else
                    {
                        index = text.LastIndexOf(" ", pos + lim);
                    }
                    string tTextR;
                    if (index == -1)
                    {
                        tTextR = text.Substring(pos);
                    }
                    else
                    {
                        tTextR = text.Substring(pos, index - pos);
                    }

                    chunks.AddLast(tTextR);

                    if (index == -1)
                    {
                        break;
                    }
                    pos = index + 1;
                }
                Action<string> future1 = null;
                Action<string> temp = s =>
                {
                    StringBuilder builder = new StringBuilder();
                    builder.Append(s);
                    if (chunks.Count == 0)
                    {
                        future(builder.ToString());
                    }
                    else
                    {
                        builder.Append(" ");
                        var first = chunks.First.Value;
                        chunks.RemoveFirst();
                        func.Invoke(first, future1);
                    }
                };
                future1 = temp;

                var first = chunks.First.Value;
                chunks.RemoveFirst();
                func.Invoke(first, future1);
            }
            else
            {
                func.Invoke(text, future);
            }
        }


        private static void TranslateItems(List<Item> data, int lim, TranslateFunc func, Action<string> future)
        {
            LinkedList<Item> chunks = new LinkedList<Item>(data);
            Action<string> future1 = null;
            future1 = s =>
            {
                StringBuilder builder = new StringBuilder();
                string curText = null;

                if (curText != null)
                {
                    if (curText.StartsWith(" ") && !s.StartsWith(" "))
                    {
                        builder.Append(" ");
                    }
                    builder.Append(s);
                    if (curText.EndsWith(" ") && !s.EndsWith(" "))
                    {
                        builder.Append(" ");
                    }
                }
                else
                {
                    builder.Append(s);
                }

                if (chunks.Count == 0)
                {
                    future(builder.ToString());
                }
                else
                {
                    Item next = chunks.First.Value;
                    chunks.RemoveFirst();

                    if (next.mode == 0)
                    {
                        SplitedTranslate(curText = next.text, lim, func, future1);
                    }
                    else
                    {
                        future1(next.text);
                    }
                }
            };
            future1("");
        }


        private static List<Item> SlitColor(string text)
        {
            List<Item> items = new List<Item>();
            int len1 = text.Length;
            int prev = 0;
            int i = 0;

            while (i < len1)
            {
                char chr = text[i];
                i++;

                if (chr == '<')
                {
                    items.Add(new Item(0, text.Substring(prev, i - 1 - prev)));
                    for (int j = i; j < len1; j++)
                    {
                        char chr2 = text[j];

                        if (chr2 == '>')
                        {
                            items.Add(new Item(1, text.Substring(i - 1, j + 2 - i)));
                            prev = j + 1;
                            i = j + 1;
                            break;
                        }
                    }
                }
            }
            if (prev != len1)
                items.Add(new Item(0, text.Substring(prev)));
            return items;
        }
    }
}
