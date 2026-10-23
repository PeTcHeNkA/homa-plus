using ArcApp;
using DiscordRPC;
using SourceChord.FluentWPF;
using System.Diagnostics;
using System.IO;
using static GF2_LaunchHX.MainWindow;


namespace GF2_LaunchHX
{
    public partial class MainWindow : AcrylicWindow
    {
        public static GameType? GetGameType(Config cfg)
        {
            foreach (var gt in cfg.game_types)
            {
                if (gt.id == cfg.game_type)
                {
                    return gt;
                }
            }
            return null;
        }


        //
        //Discord RPC start
        //

        public class WRandLine
        {
            public float chance { get; set; } = 0.5f;
            public string? value { get; set; }
        }


        public class WRandBtn : WRandLine
        {
            public string? url { get; set; }
        }


        public class WRandAsset : WRandBtn
        {
            public string small_value { get; set; }
            public string small_url { get; set; }
        }


        private class DsRpcThread
        {
            private DsInfo info;
            private bool active = true;
            private bool beUpdated = false;
            private int timer = -1;
            private DiscordRpcClient client;

            public DsRpcThread(DsInfo info)
            {
                this.info = info;
                client = new DiscordRpcClient(info.app_id);
                client.Initialize();
                client.SetPresence(info.Build());

                RunTImer();
            }


            void RunTImer()
            {
                if (timer != -1)
                {
                    beUpdated = true;
                    Task.Run(() =>
                    {
                        while (active)
                        {
                            beUpdated = true;
                            if (timer != -1)
                            {
                                Thread.Sleep(timer);
                            }
                            client.SetPresence(info.Build());
                            beUpdated = false;
                        }
                    });
                }
                else
                {
                    beUpdated = false;
                }
            }


            public bool TryEdit(DsInfo newInfo)
            {
                if (info.app_id == newInfo.app_id)
                {
                    info.state = newInfo.state;
                    info.details = newInfo.details;
                    info.buttons = newInfo.buttons;
                    info.assets = newInfo.assets;
                    info.updateTime = newInfo.updateTime;

                    if (!beUpdated)
                    {
                        RunTImer();
                    }
                    return true;
                }
                return false;
            }

            public void Stop()
            {
                active = false;
                client.Deinitialize();
                client = null;
            }
        }


        private DsRpcThread? curRpc;


        private void SetDSRPC(DsInfo inf)
        {
            if(inf == null)
            {
                if (curRpc != null)
                {
                    curRpc.Stop();
                }
                curRpc = null;
                return;
            }

            if (curRpc != null)
            {
                if (curRpc.TryEdit(inf))
                {
                    return;
                }
            }
            if(curRpc != null)
            {
                curRpc.Stop();
            }
            curRpc = new DsRpcThread(inf);
        }


        public bool ISDSRPCWork()
        {
            return curRpc != null;
        }
        //
        //Discord RPC end
        //

        //
        //GF2 Launch killer start
        //
        public void KillGF2OrigLauncher(int attempts, int startDelay, int delay)
        {
            if (attempts <= 0) attempts = 1;
            Task.Run(() => {
                if (startDelay > 0)
                {
                    Thread.Sleep(startDelay);
                }
                while (attempts > 0)
                {
                    attempts -= 1;
                    if (KillGF2OrigLauncher0())
                    {
                        return;
                    }
                    Thread.Sleep(delay);
                }
            });
        }


        private bool KillGF2OrigLauncher0()
        {
            Process[] procList = Process.GetProcesses();
            int count = 0;

            foreach (var proc1 in procList)
            {

                if (proc1.ProcessName == "PCLauncher")
                {
                    ProcessStartInfo psi = new ProcessStartInfo();
                    psi.FileName = "CMD.exe";
                    psi.Arguments = "/C taskkill /F /PID " + proc1.Id;
                    psi.UseShellExecute = false;
                    psi.CreateNoWindow = true;
                    psi.WindowStyle = ProcessWindowStyle.Hidden;

                    var proc = new Process();
                    proc.StartInfo = psi;
                    proc.Start();
                    proc.WaitForExit();

                    count += 1;
                }
            }
            return count > 0;
        }
        //
        //GF2 Launch killer end
        //
    
    
        //
        //Auto search game path start
        //
        public static string? TryAutoSearchGamePath()
        {
            DriveInfo[] allDrives = DriveInfo.GetDrives();

            int ops = 0;
            foreach (DriveInfo d in allDrives)
            {
                if(Directory.Exists(d.Name + "Games"))
                {
                    string? res = TryAutoSearchGameRec(d.Name + "Games", new(), 0, ref ops);
                    if (res != null)
                    {
                        return res;
                    }
                }
            }
            return null;
        }

        private static string? TryAutoSearchGameRec(string dir, Queue<Tuple<DirectoryInfo,int>> queue, int deepLvl, ref int ioOps)
        {
            DirectoryInfo dr = new DirectoryInfo(dir);
            
            foreach(FileInfo inf in dr.GetFiles())
            {
                ++ioOps;
                if(inf.Name == "GF2_Exilium.exe")
                {
                    return inf.FullName;
                }
            }
            if(deepLvl >= 5 && ioOps > 1000)
            {
                return null;
            }

            foreach (DirectoryInfo sub in dr.GetDirectories())
            {
                ++ioOps;
                queue.Enqueue(Tuple.Create(sub, deepLvl+1));
            }

            if(deepLvl == 0)
            {
                while (queue.Count != 0)
                {
                    var r = queue.Dequeue();
                    string? res = TryAutoSearchGameRec(r.Item1.FullName, queue, r.Item2, ref ioOps);
                    if (res != null)
                    {
                        return res;
                    }
                }
            }
            return null;
        }
        //
        //Auto search game path end
        //
    }


    public class DsInfo
    {
        public string app_id { get; set; } = MainWindow.DS_APP_TOKEN;
        public WRandLine[] state { get; set; } = { };
        public WRandLine[] details { get; set; } = { };
        public WRandBtn[] buttons { get; set; } = {
                new WRandBtn() {
                    value = "Скачать",
                    url = "https://gitlab.com/homaplus/girls-frontline-2-exilium-ru-launcher"
                }
            };
        public WRandAsset[] assets { get; set; } = { };
        public int updateTime { get; set; }

        public RichPresence Build()
        {
            var rp = new RichPresence();

            Random r = new Random();

            var rState = Rand(r, state);
            var rDetails = Rand(r, details);
            var rBtn = Rand(r, buttons) as WRandBtn;
            var rAss = Rand(r, assets) as WRandAsset;

            if (rState != null)
                rp.State = rState.value;
            if (rDetails != null)
                rp.Details = rDetails.value;
            if (rBtn != null)
            {
                rp.Buttons = new Button[]{new Button()
                    {
                        Label = rBtn.value,
                        Url = rBtn.url,
                    }};
            }
            if (rAss != null)
            {
                rp.Assets = new Assets()
                {
                    LargeImageKey = rAss.url,
                    LargeImageText = rAss.value,
                    SmallImageKey = rAss.small_url,
                    SmallImageText = rAss.small_value,
                };
            }
            return rp;
        }


        private static WRandLine? Rand(Random r, WRandLine[] lines)
        {
            int ln = lines.Length;
            if (ln == 0) return null;
            if (ln == 1) return lines[0];

            float summ = 0;
            float rNum = ((float)r.NextDouble());

            foreach (var line in lines)
            {
                summ += line.chance;
            }
            rNum *= summ;
            summ = 0;

            foreach (var line in lines)
            {
                summ += line.chance;
                if (summ > rNum)
                {
                    return line;
                }
            }
            return lines[lines.Length - 1];
        }
    }


    public class CF<T> : IDisposable
    {
        private static List<CF<T>> all = new List<CF<T>>();
        private static bool cleanRunned = false;

        private T? _value;
        private Func<T?> _func;
        private Action<T?>? _funcSet;
        private int maxtime;
        private long saveTime;

        private static void CleanAll()
        {
            long time = ArcUtils.CurTime();
            try
            {
                foreach (var item in all)
                {
                    item._clean(time);
                }
            }
            catch {}
        }


        private void _clean(long curTime)
        {
            if (saveTime > curTime)
            {
                _value = default(T);
                saveTime = 0;
            }
        }


        private static void OnInit(CF<T> cf)
        {
            all.Add(cf);

            if (!cleanRunned)
            {
                cleanRunned = true;
                Task.Run(() => {
                    while (all.Count != 0)
                    {
                        Thread.Sleep(Math.Clamp(500, all.Count / 2, 5000));
                        CleanAll();
                    }
                    cleanRunned = false;
                });
            }
        }


        private static void OnFree(CF<T> cf)
        {
            all.Remove(cf);
        }



        public void Dispose()
        {
            _clean(long.MaxValue);
        }


        public CF(int time, Func<T?> func)
        {
            this.maxtime = time;
            this._func = func;
            OnInit(this);
        }


        public CF(int time, Func<T?> funcGet, Action<T?> funcSet)
        {
            this.maxtime = time;
            this._func = funcGet;
            this._funcSet = funcSet;
            OnInit(this);
        }


        public void Free()
        {
            OnFree(this);
            this._value = default(T);
            this._func = null;
        }


        public T? V
        {
            get => _get(); set => _set(value);
        }


        private void _set(T? v)
        {
            this._value = v;
            saveTime = ArcUtils.CurTime() + maxtime;
            if (_funcSet != null) _funcSet.Invoke(v);
        }


        private T? _get()
        {
            var ct = ArcUtils.CurTime();
            if (ct > saveTime) return _value = _func.Invoke();
            return _value;
        }
    }
}