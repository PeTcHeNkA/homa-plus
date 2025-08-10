using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;
using GTranslate.Translators;

namespace GF2_RU_Launch
{
    public class TranslatorionPool
    {
        private string from, to;
        private List<TNode> nodes = new List<TNode>();
        private int nextNode = 0;

        public void Setup(string from, string to, string param)
        {
            this.from = from;
            this.to = to;

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


        public bool TranslateAll(List<ELine> lines)
        {
            if (nodes.Count == 0) return false;
            lines.AsParallel().ForAll(async line => {
                line.Value = await TranslateInPool(line.Original);
            });
            return true;
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
        

        private class TNode
        {
            private TranslatorionPool parent;
            public int priority;
            public int maxRate;
            public bool isAlive = true;
            private ITranslator engine;
            private int curRate;
            private long cuTime;
            private long delayTime;
            public bool isFree { get; private set; }

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
                long delta = cur - cuTime;
                long delays = delta / delayTime;

                curRate -= ((int)delays);
                if (curRate < 0)
                {
                    curRate = 0;
                    cuTime = cur;
                }

                if (curRate < maxRate)
                {
                    cuTime += delayTime;
                    curRate += 1;
                    return;
                }

                long toNext = delta % delayTime;
                Thread.Sleep((int)toNext);
                cuTime += toNext;

            }


            public async Task<string> translate(string src)
            {
                lock (this) {
                    isFree = false;
                    awaitToNextRequest();
                }
                var result = await engine.TransliterateAsync(src, parent.to, parent.from);
                lock (this) {
                    isFree = true;
                }
                return result.Transliteration;
            }
        }
    }
}
