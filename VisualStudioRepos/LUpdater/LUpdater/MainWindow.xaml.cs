using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using SourceChord.FluentWPF;
namespace LUpdater;


public partial class MainWindow : AcrylicWindow
{
    private static string LAU_URL = "https://gitlab.com/homaplus/girls-frontline-2-exilium-ru-launcher/-/raw/%__BRANCH__%/%__FILE__%?ref_type=heads&inline=false";
    private double progress = 0, prevProgress = 0;
    public MainWindow()
    {
        InitializeComponent();
        SetExtendViewIntoTitleBar(this, true);
        UpdateProgressLoop();

        Task.Run(() => {
            try
            {
                RunLoadTool();
            } catch (Exception e)
            {
                OnError("Неизвестная ошибка! Свяжитесь с разработчиком\n"+e.ToString()+"\n");
            }
        });
    }


    public void WindowsClosing(object sender, System.ComponentModel.CancelEventArgs e)
    {
        e.Cancel = true;
    }


    private void RunLoadTool()
    {
        SetStatus("Загрузка данных обновления ...");
        progress = 1;
        var data = LoadStr(ResUrl("natives", "launcversion.details"));
        var items = data.Split("\n").Select(s => s.Trim());

        var needDel  = items.Where(s => s.StartsWith("-")).Select(s => s.Substring(1)).ToHashSet();
        var needLoad = items.Where(s => s.StartsWith("+")).Select(s => s.Substring(1));
        var needEval = items.Where(s => s.StartsWith("?")).Select(s => s.Substring(1));

        SetStatus("Подготовка к обновлению ...");
        progress = 5;
        KillLauncherIfNeed(needDel);

        DirectoryInfo info = new DirectoryInfo(".");
        foreach (var file in info.EnumerateFiles())
        {
            if (needDel.Contains(file.Name))
            {
                try
                {
                    file.Delete();
                }
                catch
                {
                    OnError("Файл " + file.Name + " не может быть удален");
                    return;
                }
            }
        }
        foreach (var dir in info.EnumerateDirectories())
        {
            if (needDel.Contains(dir.Name))
            {
                try
                {
                    dir.Delete(true);
                }
                catch
                {
                    OnError("Директория " + dir.Name + " не может быть удалена");
                    return;
                }
            }
        }


        progress = 20;

        SetStatus("Загрузка обновлений ...");

        var result = LoadAll(needLoad.Select(s =>
            new Tuple<string, string>(s, ResUrl("main", s))
        ).ToList(), (total, cur) => {
            progress = 20 + ((((double)total) / Math.Min(cur, total)) * 70);

            SetStatus($"Загрузка обновлений {cur}/{total}");
        }
        );

        if (!result)
        {
            OnError("Ошибка загрузки файлов");
            return;
        }

        progress = 90;
        SetStatus("Запуск пост-процессов ...");

        foreach (var cmd in needEval)
        {
            int maxAttempts = 30;
            bool success = false;
            while (maxAttempts > 0)
            {
                maxAttempts--;
                try
                {
                    Process.Start(cmd);
                    success = true;
                    break;
                }
                catch (Win32Exception e)
                { 
                    if (e.ErrorCode == -2147467259)
                    {
                        Thread.Sleep(200);
                    }
                    else
                    {
                        break;
                    }
                }
                
            }
            if (!success)
            {
                OnError("Ошибка выполнения команды \n" + cmd);
            }
        }

        progress = 100;

        SetStatus("Завершено");
        Thread.Sleep(5000);
        Environment.Exit(0);
    }



    private void KillLauncherIfNeed(HashSet<string> needDel)
    {
        Process[] procList = Process.GetProcesses();

        var dirName = Directory.GetCurrentDirectory();
        var needKill = needDel.Select(x => x.Split(".")[0]).ToList();

        foreach (var proc in procList) {
            //if (proc.StartInfo.WorkingDirectory == dirName)
                foreach (var prockName in needKill)
                    {
                        if(proc.ProcessName == prockName)
                        {
                            var prc = Process.Start("CMD.exe", "/C taskkill /F /PID " + proc.Id);
                            prc.WaitForExit();
                        }
                    }
        }
    }



    private void UpdateProgressLoop()
    {
        Task.Run(() => {
            while (true)
            {
                Thread.Sleep(25);
                
                if(prevProgress != progress)
                {
                    if(progress < prevProgress)
                    {
                        continue;
                    }
                    if(progress - prevProgress > 5)
                    {
                        prevProgress = prevProgress + ((progress - prevProgress)/2);
                    } 
                    else
                    {
                        prevProgress = progress;
                    }

                    Dispatcher.Invoke(() =>
                    {
                        TrackProgress(PROGRESSBAR, null);
                    });
                }
            }
        });
    }


    private void OnError(string msg)
    {
        Dispatcher.Invoke(() => {
            TXT_INF.Text = "Ошибка";
            MessageBox.Show(msg+ ", обновление невозможно");
            Environment.Exit(1);
        });
    }


    private void SetStatus(string status)
    {
        Dispatcher.Invoke(() =>
        {
            TXT_INF.Text = status;
        });
    }


    private void On_DragWindow(object sender, RoutedEventArgs e)
    {
        DragMove();
    }


    private void TrackProgress(object sender, SizeChangedEventArgs e)
    {
        Border grd = sender as Border;
        grd.Width = ((grd.Parent as Grid).ActualWidth / 100) * Math.Min(prevProgress, 100);
    }


    private static string ResUrl(string branch, string name)
    {
        return LAU_URL.Replace("%__BRANCH__%", branch).Replace("%__FILE__%", name);
    }


    private static string LoadStr(string url)
    {
        using (WebClient webClient = new WebClient())
        {
            byte[] data = webClient.DownloadData(url);
            return Encoding.UTF8.GetString(data);
        }
        return null;
    }


    private bool LoadAll(List<Tuple<string, string>> loads, OnLoad? cb)
    {
        if (loads.Count == 0) return true;
        var hasErrors = false;
        int count = 0;

        var result = loads.Select(loading =>
            Task.Run(() => {
                using (WebClient webClient = new WebClient())
                {
                    try
                    {
                        webClient.DownloadFile(loading.Item2, loading.Item1);
                        if (cb != null)
                        {
                            ++count;
                            cb.Invoke(loads.Count, count);
                        }
                    }
                    catch (Exception ex)
                    {
                        OnError("Ошибка загрузки " + loading.Item1 + "\n" + ex.Message);
                        hasErrors = true;
                    }
                }
            })
        );
        Task.WaitAll(result.ToArray());

        return !hasErrors;
    }


    public static bool CreatePathIfNeed(string fileName)
    {
        FileInfo file = new FileInfo(fileName);
        DirectoryInfo parent = file.Directory;
        if (parent != null)
        {
            parent.Create();
            return true;
        }
        return false;
    }


    public delegate void OnLoad(int count, int total);
}