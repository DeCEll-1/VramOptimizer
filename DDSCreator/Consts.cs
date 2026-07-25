namespace DDSCreator
{
    public class Consts
    {
        public readonly static DirectoryInfo AppDir = new(AppContext.BaseDirectory);
#if DEBUG
        public readonly static DirectoryInfo ModDir = AppDir!.Parent!.Parent!.Parent!;
#else
public readonly static DirectoryInfo ModDir = AppDir!.Parent!;
#endif
        public readonly static DirectoryInfo ModsDir = ModDir.Parent!;
        public readonly static DirectoryInfo GameDir = ModsDir.Parent!;
        public readonly static DirectoryInfo StarsectorCodeDir = new(Path.Join(GameDir.FullName, "starsector-core"));
        public readonly static DirectoryInfo CacheDir = new(Path.Join(ModDir.FullName, "DDSCache"));

        static Consts()
        {
            if (!CacheDir.Exists)
                CacheDir.Create();
        }

        public readonly static string DdsMetadataFileName = "dds_metadata.json";
    }
}
