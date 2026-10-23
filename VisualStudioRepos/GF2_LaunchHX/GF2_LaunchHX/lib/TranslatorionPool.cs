using System.Text;
using ArcApp;
using GTranslate.Results;
using GTranslate.Translators;


namespace GF2_LaunchHX.lib
{
    public class TranslatorionPool : ITranslateEngine
    {
        private string from = "en", to = "ru";
        private List<TNode> nodes = new List<TNode>();
        private int nextNode = 0;
        private bool enableFixes = true;
        private TCache cache = new TCache(null);

        public TranslatorionPool()
        {

        }


        public void setCache(string path)
        {
            cache = new TCache(path);
            cache.Read();
        }


        public bool Setup(MainWindow window, LangTable tb, TranslationCallback cb)
        {
            //todo 

            return false;
        }


        public bool Finish()
        {
            cache.Write();
            return true;
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


        public bool TranslateLines(List<ELine> lines, TranslationCallback cb)
        {
            if (nodes.Count == 0)
            {
                cb.active = true;
                return false;
            }
            cb.active = false;
            cb.maxNum = lines.Count;

            if (enableFixes)
            {
                
                TranslateFunc func = (src, future) => {
                    if (!ELine.IsTranslatable(src))
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
                    cb.Add(1);
                });
            }
            else {
                lines.AsParallel().ForAll(async line => {
                    line.Value = await TranslateInPool(line.Original);
                    cb.Add(1);
                });
            }
            cb.active = true;
            cache.Write();
            return true;
        }


        private static string TranslateFixed(string src, TranslateFunc func)
        {
            string result = null;
            Action<string> future = (res) => { result = res; };
            TranslateItems(SlitColor(src), 1000, func, future);
            if (result != null && result.Length > 1)
            { 
                if(src.StartsWith(" ") && !result.StartsWith(" "))
                {
                    result = " "+result;
                }
            }
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



        private class TNode
        {
            private TranslatorionPool parent;
            public int priority;
            public int maxRate;
            private ITranslator engine;
            private long cuTime = ArcUtils.CurTime()-1000;
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

                long cur = ArcUtils.CurTime();
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
