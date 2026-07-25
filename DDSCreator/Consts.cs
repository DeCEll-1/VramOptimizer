namespace DDSCreator
{
    public class Consts
    {
        public readonly static DirectoryInfo AppDir = new(AppContext.BaseDirectory);
        public readonly static DirectoryInfo ModDir = AppDir!.Parent!.Parent!.Parent!;
        public readonly static DirectoryInfo ModsDir = ModDir.Parent!;
        public readonly static DirectoryInfo GameDir = ModsDir.Parent!;
        public readonly static DirectoryInfo StarsectorCodeDir = new(Path.Join(GameDir.FullName, "starsector-core"));
        public readonly static DirectoryInfo CacheDir = new(Path.Join(ModDir.FullName, "DDSCache"));

        public readonly static string DdsMetadataFileName = "dds_metadata.json";
    }
}
