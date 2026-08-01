using CsvHelper.Configuration.Attributes;

namespace DDSCreator.GLib
{
    public class TextureData
    {
        [Name("id")]
        public string Id { get; set; } = string.Empty;
        [Name("type")]
        public string Type { get; set; } = string.Empty;
        [Name("frame")]
        public int? Frame { get; set; }
        [Name("magnitude")]
        public double? Magnitude { get; set; }
        [Name("map")]
        public string Map { get; set; } = string.Empty;
        [Name("path")]
        public string Path { get; set; } = string.Empty;
    }
}
