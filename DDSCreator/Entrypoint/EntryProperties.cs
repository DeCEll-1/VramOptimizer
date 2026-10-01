using DDSCreator.Model;

namespace DDSCreator.Entrypoint
{
    public partial class Program
    {
        public static ulong TotalPixelsProcessed;
        public static ulong PixelsAlreadyCached;
        public static Dictionary<string, FileMetadata> ExistingMetadataCache { get; set; } = new(StringComparer.OrdinalIgnoreCase);

        public static List<ModInfo> FailedToLoadMods = [];
        public static List<ModInfo> ValidMods = []; // this is just the mods that have mod_info.json
        public static List<string> EnabledMods = [];
        public static int ConcurrentFileLimit = Environment.ProcessorCount;
        public static int TextureTaskCount = Environment.ProcessorCount;
        public static CompressionPreset CurrentCompressionPreset = CompressionPreset.Default;
        public static int SmallestMipmapSize = 1;

        public static Options Options;

    }
}
