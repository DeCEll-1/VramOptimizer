using BCnEncoder.Shared;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DDSCreator.Model
{
    public class FileMetadata
    {
        public required string ModID { get; init; }
        public required string ModFolderName { get; init; }
        public required string RelativeImagePath { get; init; }
        [JsonConverter(typeof(StringEnumConverter))]
        public required ImageFileType ImageType { get; init; }
        public required DateTime ImageCreationDate { get; init; }
        public required DateTime ImageEditDateDate { get; init; }
        public required string ImageHash { get; init; }

        public required string DDSFilePath { get; init; }
        public required DateTime DDSCreationDate { get; init; }
        public required DateTime DDSEditDate { get; init; }
        public required string CompressionFormat { get; init; }
        public required int Width { get; init; }
        public required int Height { get; init; }

        public required float[] Mean { get; init; } // color0
        public required float[] Weighted { get; init; } // color1
        public required float[] Median { get; init; } // color2

        public required string VOptVersion { get; init; }

        public enum ImageFileType
        {
            None,
            Jpg,
            Png,
            Webp,
        }
    }
}
