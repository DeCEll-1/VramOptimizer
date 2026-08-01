using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace DDSCreator
{
    public partial class NativeBc7Encoder
    {
        private static IntPtr _libraryHandle = IntPtr.Zero;

        static NativeBc7Encoder()
        {
            LoadLibrary();
        }

        private static void LoadLibrary()
        {

            string dir = Path.Combine(ModDir.FullName, "External", "ispc_texcomp");
            string subfolder = "";
#if DEBUG
            subfolder = Path.Combine("ispc_texcomp_win", "ispc_texcomp.dll");
#elif WINDOWS
        subfolder = Path.Combine("ispc_texcomp_win", "ispc_texcomp.dll");
#elif LINUX
        subfolder = Path.Combine("ispc_texcomp_linux", "ispc_texcomp.dll");
#elif MAC
        subfolder = Path.Combine("ispc_texcomp_macos", "ispc_texcomp.dll");
#else
            throw new PlatformNotSupportedException("Target platform compiler flag (WINDOWS, LINUX, or MAC) is not defined.");
#endif

            string fullPath = Path.Combine(dir, subfolder);

            if (!File.Exists(fullPath))
            {
                throw new FileNotFoundException($"Native library not found at: {fullPath}");
            }

            if (!NativeLibrary.TryLoad(fullPath, out _libraryHandle))
            {
                throw new Exception($"Failed to load native library from: {fullPath}");
            }

        }
    }
}
