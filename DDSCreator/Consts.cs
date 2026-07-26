namespace DDSCreator
{
    public class Consts
    {

#if DEBUG
        public readonly static DirectoryInfo AppDir = new(AppContext.BaseDirectory);
        public readonly static DirectoryInfo ModDir = AppDir!.Parent!.Parent!.Parent!;
        public readonly static DirectoryInfo ModsDir = ModDir.Parent!;
        public readonly static DirectoryInfo GameDir = ModsDir.Parent!;
        public readonly static DirectoryInfo StarsectorCodeDir = new(Path.Join(GameDir.FullName, "starsector-core"));
        public readonly static DirectoryInfo CacheDir = new(Path.Join(ModDir.FullName, "DDSCache"));
#elif WINDOWS
        public readonly static DirectoryInfo AppDir = new(AppContext.BaseDirectory);
        public readonly static DirectoryInfo ModDir = AppDir!.Parent!;
        public readonly static DirectoryInfo ModsDir = ModDir.Parent!;
        public readonly static DirectoryInfo GameDir = ModsDir.Parent!;
        public readonly static DirectoryInfo StarsectorCodeDir = new(Path.Join(GameDir.FullName, "starsector-core"));
        public readonly static DirectoryInfo CacheDir = new(Path.Join(ModDir.FullName, "DDSCache"));
#elif MAC
        public readonly static DirectoryInfo AppDir = new(AppContext.BaseDirectory);
        public readonly static DirectoryInfo ModDir = AppDir!.Parent!;
        public readonly static DirectoryInfo ModsDir = ModDir.Parent!;
        public readonly static DirectoryInfo GameDir = ModsDir.Parent!;
        public readonly static DirectoryInfo StarsectorCodeDir = new(Path.Join(GameDir.FullName, "Contents", "Resources", "Java"));
        public readonly static DirectoryInfo CacheDir = new(Path.Join(ModDir.FullName, "DDSCache"));
#elif LINUX
        public readonly static DirectoryInfo AppDir = new(AppContext.BaseDirectory);
        public readonly static DirectoryInfo ModDir = AppDir!.Parent!;
        public readonly static DirectoryInfo ModsDir = ModDir.Parent!;
        public readonly static DirectoryInfo GameDir = ModsDir.Parent!;
        public readonly static DirectoryInfo StarsectorCodeDir = GameDir;
        public readonly static DirectoryInfo CacheDir = new(Path.Join(ModDir.FullName, "DDSCache"));
#endif




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
