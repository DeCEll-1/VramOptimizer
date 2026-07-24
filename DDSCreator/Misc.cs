using BCnEncoder.Shared;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DDSCreator
{
    public static class Misc
    {

        public static long CalculateBlockCompressedVramSize(int width, int height, CompressionFormat format)
        {
            int blocksX = (width + 3) / 4;
            int blocksY = (height + 3) / 4;
            long totalBlocks = (long)blocksX * blocksY;

            int bytesPerBlock = format switch
            {
                CompressionFormat.Bc1 or CompressionFormat.Bc4 => 8,
                CompressionFormat.Bc2 or CompressionFormat.Bc3 or CompressionFormat.Bc5 or CompressionFormat.Bc6U or CompressionFormat.Bc6S or CompressionFormat.Bc7 => 16,
                _ => 16
            };

            return totalBlocks * bytesPerBlock;
        }


        private static readonly string[] SizeUnits = { "B", "KB", "MB", "GB", "TB", "PB" };
        // if you have a texture thats pb size ur lowky cooked gng
        public static string FormatBytes(long bytes)
        {
            if (bytes <= 0)
                return "0 B";

            int order = System.Numerics.BitOperations.Log2((ulong)bytes) / 10;

            double len = bytes / Math.Pow(1024, order);

            return $"{len:0.##} {SizeUnits[order]}";
        }

        public static string GetFormatName(CompressionFormat format)
        => format switch
        {
            CompressionFormat.Bc1 => "BC1",
            CompressionFormat.Bc2 => "BC2",
            CompressionFormat.Bc3 => "BC3",
            CompressionFormat.Bc4 => "BC4",
            CompressionFormat.Bc5 => "BC5",
            CompressionFormat.Bc6U => "BC6U",
            CompressionFormat.Bc6S => "BC6S",
            CompressionFormat.Bc7 => "BC7",
            _ => "Unknown"
        };

    }
}
