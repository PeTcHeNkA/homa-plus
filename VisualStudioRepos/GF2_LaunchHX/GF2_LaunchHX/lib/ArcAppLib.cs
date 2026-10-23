using ArcApp;
using GF2_LaunchHX.lib;
using SourceChord.FluentWPF;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Net.Http;
using System.Reflection;
using System.Security.Cryptography;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media.Imaging;


namespace GF2_LaunchHX
{
    //Fluent WPF Acrylic APP generated part by HomaPlus
    public partial class MainWindow : AcrylicWindow
    {
        private List<Window> arcSubWindows = new List<Window>();
        public Action OnClosedAction = delegate{};

        public virtual BitmapImage GetImgByName(string name)
        {
            return ArcUtils.GetImgByName("GF2_LaunchHX", name);
        }


        protected override void OnClosed(EventArgs e)
        {
            foreach (Window window in arcSubWindows)
                window.Close();
            try
            {
                OnClosedAction.Invoke();
            } catch {}
            base.OnClosed(e);
        }


        //Needs to be called when the child window is closed
        public virtual void OnChildClosed(Window child)
        {
            arcSubWindows.Remove(child);
        }


        //Needs to be called when the child window is opened
        public virtual void OnChildOpenned(Window child)
        {
            arcSubWindows.Add(child);
        }


        //Open and remember child window
        public virtual void OpenChild(Window child)
        {
            OnChildOpenned(child);
            child.Show();
            child.Focus();
        }


        public bool IsChildOpenned(Type type)
        {
            foreach(Window window in arcSubWindows)
            {
                if(window.GetType() == type)
                    return true;
            }
            return false;
        }


        public bool FocusChildIfOpenned(Type type)
        {
            foreach (Window window in arcSubWindows)
            {
                if (window.GetType() == type)
                {
                    window.Focus();
                    return true;
                }
            }
            return false;
        }


        public bool CloseIfOpenned(Type type)
        {
            foreach (Window window in arcSubWindows)
            {
                if (window.GetType() == type)
                {
                    arcSubWindows.Remove(window);
                    window.Close();
                    return true;
                }
            }
            return false;
        }
    }
}

namespace ArcApp
{
    //Fluent WPF Acrylic APP standart lib by HomaPlus
    public class ArcUtils
    {
        public static string NATIVES_LOAD_URL = null;     //remote natives load url, example: https://cdn.example.com/dlls/%__FILE__%
        public static string NATIVES_PATH = "./lib/"; //additional directory to load DLLs


        private static HashSet<string> BLACKLIST = new HashSet<string>
        {
            //Blacklist of DLLs that don't allow custom loading
        };


        public static Assembly? ArcAssemblyResolve(object? sender, ResolveEventArgs args)
        {
            string assemblyName = new AssemblyName(args.Name).Name + ".dll";

            if (BLACKLIST.Contains(assemblyName) || assemblyName.StartsWith("Presentation")
                || assemblyName.StartsWith("System.Private") || assemblyName.StartsWith("FluentWPF"))
                return null;

            if (assemblyName.Contains(".resources")) return null;

            Assembly? loadedAssembly = null;

            var path = NATIVES_PATH + assemblyName;

            if (File.Exists(path))
            {
                loadedAssembly = Assembly.LoadFrom(path);
            }
            else
            {
                if (NATIVES_LOAD_URL != null)
                {
                    if (Load(NATIVES_LOAD_URL.Replace("%__FILE__%", assemblyName), path))
                        loadedAssembly = Assembly.Load(path);
                }
            }
            return loadedAssembly;
        }


        public static BitmapImage GetImgByName(string appPkg, string name)
        {
            var bi = new BitmapImage();
            bi.BeginInit();
            bi.UriSource = GetInnerURI(appPkg, name);
            bi.EndInit();
            return bi;
        }


        public static Uri GetInnerURI(string appPkg, string name)
        {
            return new Uri("pack://application:,,,/" + appPkg + ";component/" + name);
        }


        // Download file and save to path
        public static bool Load(string url, string filename)
        {
            CreatePathIfNeed(filename);
            using (WebClient webClient = new())
            {
                try
                {
                    webClient.DownloadFile(url, filename);
                }
                catch (Exception e)
                {
                    return false;
                }
                return true;
            }
            return false;
        }


        public static string? LoadStr(string url)
        {
            byte[]? data = LoadBytes(url);
            if (data == null)
            {
                return null;
            }
            return Encoding.UTF8.GetString(data);
        }


        public static byte[]? LoadBytes(string url)
        {
            try
            {
                using (var webClient = new WebClient())
                {
                    return webClient.DownloadData(url);
                }
            }
            catch
            {
                return null;
            }
        }


        public static async Task<byte[]?> LoadBytes(string url, int bufSize, Action<float> callback)
        {
            try
            {
                using (var client = new HttpClient())
                {
                    var resp = await client.GetAsync(url);

                    resp.EnsureSuccessStatusCode();

                    resp.Content.Headers.TryGetValues("Content-Length", out var values);

                    int totalBytes = values != null && values.Any() ? int.Parse(values.First()) : -1;


                    var stream = resp.Content.ReadAsStream();
                    var result = new MemoryStream(totalBytes);

                    byte[] buffer = new byte[bufSize];

                    callback(0f);

                    while (true)
                    {
                        int bytesRead = await stream.ReadAsync(buffer, 0, buffer.Length);
                        if (bytesRead == 0)
                            break;
                        result.Write(buffer, 0, bytesRead);
                        if (totalBytes > 0)
                        {
                            float progress = (float)result.Length / totalBytes;
                            callback(progress);
                        }
                    }
                    return result.ToArray();
                }
            }
            catch
            {
                return null;
            }
        }


        //Create all necessary subdirectories so that a file with the specified name can be created
        public static FileExistStatus CreatePathIfNeed(string fileName)
        {
            FileInfo file = new FileInfo(fileName);
            bool exists = file.Exists;
            if(exists) return FileExistStatus.ExistFile;

            DirectoryInfo? parent = file.Directory;
            if (parent != null)
            {
                if (!parent.Exists)
                {
                    parent.Create();
                    return FileExistStatus.CreatedDir;
                }
            }
            return FileExistStatus.ExistDir;
        }


        public enum FileExistStatus
        {
            CreatedDir = 0,
            ExistDir = 1,
            ExistFile = 2
        }


        // Open URL in default browser
        public static void OpenURL(string url)
        {
            Process.Start(new ProcessStartInfo(url) { UseShellExecute = true });
        }


        // Calc SHA256 from file and return it as Base64 string
        public static string GetSHA256(string file)
        {
            if (File.Exists(file))
            {
                return GetSHA256(File.ReadAllBytes(file));
            }
            return "";
        }


        // Calc SHA256 and return it as Base64 string
        public static string GetSHA256(byte[] src)
        {
            return Convert.ToBase64String(SHA256.Create().ComputeHash(src));
        }


        // Return current time in miliseconds
        public static long CurTime()
        {
            return DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        }
    }


    public class ArcSmartTextCollection
    {
        protected ItemCollection items;
        protected GF2_LaunchHX.MainWindow parent;
        protected Style? textStyle, buttonStyle;

        public ArcSmartTextCollection(GF2_LaunchHX.MainWindow parent, ItemCollection items)
        {
            this.parent = parent;
            this.items = items;

            UseStyle(null, "ArcBotton");
        }


        public ArcSmartTextCollection UseStyle(string? text, string? button)
        {
            if (text != null)
            {
                textStyle = (Style)this.parent.TryFindResource(text);
            }

            if (button != null)
            {
                buttonStyle = (Style)this.parent.TryFindResource(button);
            }
            return this;
        }


        public ArcSmartTextCollection Clear()
        {
            items.Clear();
            return this;
        }


        public ArcSmartTextCollection AddText(string text)
        {
            foreach (ArcSInfo inf in ParseInfo(text))
            {
                inf.AddItems(this);
            }
            return this;
        }


        protected List<ArcSInfo> ParseInfo(string text)
        {
            List<ArcSInfo> list = new List<ArcSInfo>();
            ArcSInfo info = new ArcSInfo();
            list.Add(info);
            bool shouldNext = false;

            foreach (string line1 in text.Split("\n"))
            {
                string line = line1;
                int size = 5;
                while (line.StartsWith("#"))
                {
                    --size;
                    line = line.Substring(1);
                }
                if (size <= 0)
                {
                    size = 1;
                }
                if (size == 5)
                {
                    info.content += ("\n" + line);
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
                            info = new ArcSBotton();
                        }
                        else
                        {
                            info = new ArcSBotton();
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


        protected class ArcSInfo
        {
            public string header;
            public int headerSize;
            public string content = "";

            public virtual void AddItems(ArcSmartTextCollection txt)
            {
                if (!string.IsNullOrEmpty(header))
                {
                    TextBlock box = new TextBlock();
                    box.Text = header;

                    box.FontSize = 14 + (headerSize) * 3;
                    var mg = box.Margin;
                    mg.Left = 15;
                    box.Margin = mg;
                    txt.items.Add(box);
                }
                if (!string.IsNullOrEmpty(content))
                {
                    TextBlock box2 = new TextBlock();
                    box2.Text = content.Trim();
                    box2.FontSize = 14;
                    var pd = box2.Padding;
                    pd.Bottom = 8;
                    box2.Padding = pd;
                    txt.items.Add(box2);
                }
            }
        }


        protected class ArcSBotton : ArcSInfo
        {
            public override void AddItems(ArcSmartTextCollection txt)
            {
                Button btn = new Button();

                btn.Style = txt.buttonStyle;

                btn.Click += this.OnCLick;

                TextBlock box = new TextBlock();
                box.Text = header;

                box.FontSize = 14 + (headerSize) * 3;
                var mg = box.Margin;

                btn.Content = box;
                btn.Margin = mg;

                txt.items.Add(btn);
            }


            protected virtual void OnCLick(object sender, Object? e)
            {
                if (content == null || content.Length == 0) return;

                if (content.StartsWith("URL:"))
                {
                    ArcUtils.OpenURL(content.Substring(4).Trim());
                    return;
                }

                if (content.StartsWith("CMD:"))
                {
                    content = content.Substring(4).Trim();
                    try
                    {
                        Process prc = null;
                        bool waitExit = false;
                        if (content.StartsWith("W:"))
                        {
                            content = content.Substring(2).Trim();
                            waitExit = true;
                        }

                        if (content.StartsWith("M:"))
                        {
                            content = content.Substring(2).Trim();

                            prc = Process.Start(content);
                        }
                        else
                        {
                            prc = Process.Start("CMD.exe", "/c " + content);
                        }
                        if (waitExit)
                        {
                            prc.WaitForExit();
                        }
                    }
                    catch (Exception err)
                    {
                        MessageBox.Show(content + "\n\nОшибка выполнения: \n" + err.Message + "\n" + err.StackTrace, header + " | Ошибка");
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