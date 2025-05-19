using System.Runtime.InteropServices;

namespace LangEditAPI
{
    public class Table
    {
        [DllImport("langedit.dll", CallingConvention = CallingConvention.Cdecl)]
        protected static extern int LoadPack(int id, GoByteArr data);
        [DllImport("langedit.dll", CallingConvention = CallingConvention.Cdecl)]
        protected static extern int FreePack(int id);
        [DllImport("langedit.dll", CallingConvention = CallingConvention.Cdecl)]
        protected static extern int WritePack(int id);
        [DllImport("langedit.dll", CallingConvention = CallingConvention.Cdecl)]
        protected static extern int ApplyMappings(int id, GoByteArr data);
        [DllImport("langedit.dll", CallingConvention = CallingConvention.Cdecl)]
        protected static extern int ExportMappings(int keyId, int valId, int flags);
        [DllImport("langedit.dll", CallingConvention = CallingConvention.Cdecl)]
        protected static extern int FlushTo(int id, GoByteArr data);


        public static int LoadPackC(int id, byte[] data)
        {
            var arr = new GoByteArr(data);
            int res = LoadPack(id, arr);
            arr.free();
            return res;
        }

        public static int ApplyMappingsC(int id, byte[] data)
        {
            var arr = new GoByteArr(data);
            int res = ApplyMappings(id, arr);
            arr.free();
            return res;
        }

        public static byte[] WritePackC(int id)
        {
            var len = WritePack(id);
            var arr = new GoByteArr(len);
            FlushTo(id, arr);
            byte[] data = arr.GetBytes();
            arr.free();
            return data;
        }

        public static byte[] ExportMappingsC(int keyId, int valId, int flags)
        {
            var len = ExportMappings(keyId, valId, flags);
            var arr = new GoByteArr(len);
            FlushTo(keyId, arr);
            byte[] data = arr.GetBytes();
            arr.free();
            return data;
        }


        public static void FreePackC(int id)
        {
            FreePack(id);
        }


        private int Id;

        public Table(int id)
        {
            Id = id;
        }

        public int getId()
        {
            return Id;
        }

        public void load(byte[] data)
        {
            LoadPackC(Id, data);
        }

        public void applyMappngs(byte[] data)
        {
            ApplyMappingsC(Id, data);
        }

        public byte[] save()
        {
            return WritePackC(Id);
        }

        public byte[] exportMappings(Table? valSrc, int flags)
        {
            var valId = Id;
            if (valSrc != null)
            {
                valId = valSrc.Id;
            }
            return ExportMappingsC(Id, valId, flags);
        }

        public void free()
        {
            FreePackC(Id);
        }
    }


    public struct GoByteArr
    {
        public IntPtr data;
        public long len, cap;

        public GoByteArr(int len0)
        {
            data = Marshal.AllocHGlobal(len0);
            len = len0;
            cap = len0;
        }

        public GoByteArr(byte[] data0)
        {
            data = Marshal.AllocHGlobal(data0.Length);
            Marshal.Copy(data0, 0, data, data0.Length);
            len = data0.Length;
            cap = data0.Length;
        }

        public void free()
        {
            Marshal.FreeHGlobal(data);
        }

        public byte[] GetBytes()
        {
            byte[] data0 = new byte[len];
            Marshal.Copy(data, data0, 0, data0.Length);
            return data0;
        }
    }
}
