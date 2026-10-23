using System.IO.Compression;
using System.IO;
using ArcApp;


namespace GF2_LaunchHX.lib
{
    public class LuModUtils
    {
        private static HashSet<string> RESHADE_IGNORE_DEL = new HashSet<string> {"ReShade.ini", "ReShadePreset.ini" };
        public static void ReshadeInstallTo(string gamePath)
        {
            var fs = File.OpenRead("./reshade.zip");
            try
            {
                var zp = new ZipArchive(fs);

                foreach (var e in zp.Entries)
                {
                    if (e.Name == "") continue;

                    var pth = gamePath + e.FullName;
                    ArcUtils.CreatePathIfNeed(pth);

                    if (RESHADE_IGNORE_DEL.Contains(e.Name))
                    {
                        if (File.Exists(pth)) continue;
                    }

                    if (File.Exists(pth))
                        File.Delete(pth);

                    e.ExtractToFile(pth);
                }
            } finally
            {
                fs.Close(); 
            }
        }


        public static void ReshadeUninstall(string gamePath)
        {
            File.Delete(gamePath + "dxgi.dll");
        }


        public static bool ReshadeIsInstalled(string gamePath)
        {
            return File.Exists(gamePath + "dxgi.dll");
        }
    }
}
