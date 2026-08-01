namespace DDSCreator
{
    public class Consts
    {

#if DEBUG
        public readonly static DirectoryInfo AppDir = new(AppContext.BaseDirectory);
        public readonly static DirectoryInfo ModDir = AppDir!.Parent!.Parent!.Parent!;
        public readonly static DirectoryInfo ModsDir = ModDir.Parent!;
        public readonly static DirectoryInfo GameDir = ModsDir.Parent!;
        public readonly static DirectoryInfo StarsectorCoreDir = new(Path.Join(GameDir.FullName, "starsector-core"));
#elif WINDOWS
        public readonly static DirectoryInfo AppDir = new(AppContext.BaseDirectory);
        public readonly static DirectoryInfo ModDir = AppDir!.Parent!;
        public readonly static DirectoryInfo ModsDir = ModDir.Parent!;
        public readonly static DirectoryInfo GameDir = ModsDir.Parent!;
        public readonly static DirectoryInfo StarsectorCoreDir = new(Path.Join(GameDir.FullName, "starsector-core"));
#elif MAC
        public readonly static DirectoryInfo AppDir = new(AppContext.BaseDirectory);
        public readonly static DirectoryInfo ModDir = AppDir!.Parent!;
        public readonly static DirectoryInfo ModsDir = ModDir.Parent!;
        public readonly static DirectoryInfo GameDir = ModsDir.Parent!;
        public readonly static DirectoryInfo StarsectorCoreDir = new(Path.Join(GameDir.FullName, "Contents", "Resources", "Java"));
#elif LINUX
        public readonly static DirectoryInfo AppDir = new(AppContext.BaseDirectory);
        public readonly static DirectoryInfo ModDir = AppDir!.Parent!;
        public readonly static DirectoryInfo ModsDir = ModDir.Parent!;
        public readonly static DirectoryInfo GameDir = ModsDir.Parent!;
        public readonly static DirectoryInfo StarsectorCoreDir = GameDir;
#endif

        // i want the cache to be in the mods folder as to not cause problems with updates
        public readonly static DirectoryInfo CacheDir = new(Path.Join(ModsDir.FullName, "DDSCache"));

        public readonly static string Version = File.ReadAllText(Path.Combine(ModDir.FullName, "VOpt_VERSION.txt")).Trim();

        //public readonly static DirectoryInfo ModsDir = ModDir.Parent!;
        //public readonly static DirectoryInfo GameDir = ModsDir.Parent!;
        //public readonly static DirectoryInfo StarsectorCodeDir = new(Path.Join(GameDir.FullName, "starsector-core"));
        //public readonly static DirectoryInfo CacheDir = new(Path.Join(ModDir.FullName, "DDSCache"));

        static Consts()
        {
            if (!CacheDir.Exists)
                CacheDir.Create();
        }

        public readonly static string DdsMetadataFileName = "dds_metadata.json";
    }
}
