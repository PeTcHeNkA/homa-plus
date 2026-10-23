using MediaDevices;
using System.IO;
using static ArcApp.ArcUtils;

namespace GF2_LaunchHX.lib
{

    public class PhoneGame : IOProxy
    {
        private MediaDevice device;
        private string gameRoot, launcherRoot;


        //com.Sunborn.SnqxExilium.Glo
        public static PhoneGame? ScanPhones(string gameId)
        {

            var devices = MediaDevice.GetDevices();

            foreach (var device in devices)
            {
                device.Connect();
                MediaDirectoryInfo rootDir = device.GetRootDirectory();

                foreach (var dir in rootDir.EnumerateDirectories())
                {
                    var gameRoot = dir.FullName + "\\Android\\data\\"+gameId;
                    if (device.DirectoryExists(gameRoot))
                    {
                        return new PhoneGame()
                        {
                            device = device,
                            gameRoot = gameRoot + "\\",
                            launcherRoot = dir.FullName + "\\GF2Packs\\"
                        };
                    }
                }
            }
            return null;

        }


        public string GetDeviceName()
        {
            return device.Manufacturer+" "+device.Description;
        }


        public string ResolveType(IOType t)
        {
            switch (t)
            {
                case IOType.GAME: return gameRoot;
                case IOType.LAUNCHER: return launcherRoot + "\\launcher\\";
                case IOType.CACHE: return launcherRoot;
            }
            return null;
        }


        public bool Exists(IOType t, string path)
        {
            return device.FileExists(ResolveType(t) + path);
        }


        public byte[]? Read(IOType t, string path)
        {
            MemoryStream stream = new MemoryStream();
            path = ResolveType(t) + path;
            if(!device.FileExists(path)) return null;

            device.DownloadFile(ResolveType(t) + path, stream);
            return stream.ToArray();
        }


        public void Write(IOType t, string path, byte[] bytes)
        {
            path = ResolveType(t) + path;
            if(bytes == null)
            {
                device.DeleteFile(path);
                return;
            }
            MemoryStream stream = new MemoryStream(bytes);
            device.UploadFile(stream, path);
        }


        public FileExistStatus EnsureExistsFor(IOType t, string path)
        {
            path = ResolveType(t) + path;
            bool exists = device.FileExists(path);
            if (exists) return FileExistStatus.ExistFile;


            var i1 = path.LastIndexOf("\\");
            path = path.Substring(0, i1);

            if (device.DirectoryExists(path))
            {
                return FileExistStatus.ExistDir;
            }

            device.CreateDirectory(path);

            return FileExistStatus.CreatedDir;
        }
    }
}