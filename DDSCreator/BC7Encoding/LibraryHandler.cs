using System.Reflection;
using System.Runtime.InteropServices;

namespace DDSCreator
{
    public partial class NativeBc7Encoder
    {
        private static IntPtr _libraryHandle = IntPtr.Zero;
        private const string LibraryName = "ispc_texcomp";
        static NativeBc7Encoder()
        {
            NativeLibrary.SetDllImportResolver(typeof(NativeBc7Encoder).Assembly, DllImportResolver);
            LoadLibrary();
        }
        private static IntPtr DllImportResolver(string libraryName, Assembly assembly, DllImportSearchPath? searchPath)
        {
            if (libraryName == LibraryName)
            {
                return _libraryHandle != IntPtr.Zero ? _libraryHandle : IntPtr.Zero;
            }
            return IntPtr.Zero;
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
        subfolder = Path.Combine("ispc_texcomp_linux", "ispc_texcomp.so");
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
