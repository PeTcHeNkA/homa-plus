using ArcApp;
using System.IO;
using static ArcApp.ArcUtils;

namespace GF2_LaunchHX.lib
{
    public interface IOProxy
    {
        public abstract void Write(IOType t, string path, byte[]? bytes);
        public abstract byte[]? Read(IOType t, string path);
        public abstract bool Exists(IOType t, string path);
        public abstract FileExistStatus EnsureExistsFor(IOType t, string path);
        protected string ResolveType(IOType t);
    }

    public enum IOType
    {
        CACHE, GAME, LAUNCHER
    }


    public class WinStdProxy : IOProxy
    {
        private MainWindow window;

        public WinStdProxy(MainWindow window)
        {
            this.window = window;
        }


        public string ResolveType(IOType t)
        {
            switch (t)
            {
                case IOType.CACHE: return MainWindow.TRANSLATION_CACHE_PATH;
                case IOType.LAUNCHER: return MainWindow.APP_DATA_PATH;
                case IOType.GAME: return window.config.game_path;
            }
            return null;
        }


        public bool Exists(IOType t, string path)
        {
            return File.Exists(ResolveType(t)+path);
        }


        public byte[]? Read(IOType t, string path)
        {

            return File.ReadAllBytes(ResolveType(t) + path);
        }


        public void Write(IOType t, string path, byte[]? bytes)
        {
            path = ResolveType(t) + path;
            if (bytes == null)
            {
                File.Delete(path);
                return;
            }
            File.WriteAllBytes(path, bytes);
        }


        public FileExistStatus EnsureExistsFor(IOType t, string path)
        {
            return ArcUtils.CreatePathIfNeed(ResolveType(t) + path);
        }
    }
}