using BCnEncoder.Encoder;
using BCnEncoder.Shared;
using DDSCreator.Model;
using ImageMagick;
using Spectre.Console;
using System.Diagnostics;

namespace DDSCreator
{
    public class Converter
    {
        public static (string ddsFilePath, int width, int height, bool wasSkipped) Convert(string srcFilePath, string toDirectory, CompressionFormat format, string? customOutName = null, bool overwrite = false)
        {
            string fileName = Path.GetFileNameWithoutExtension(srcFilePath);
            string outputFileName = string.IsNullOrWhiteSpace(customOutName) ? $"{fileName}.dds" : customOutName;
            string ddsOutputPath = Path.Combine(toDirectory, outputFileName);

            bool isCached = Program.ExistingMetadataCache.TryGetValue(srcFilePath, out FileMetadata? cachedMeta);
            bool fileExists = !overwrite && File.Exists(ddsOutputPath) && new FileInfo(ddsOutputPath).Length != 0;

            bool shouldSkip = fileExists && (!isCached || File.GetLastWriteTimeUtc(srcFilePath) <= cachedMeta!.DDSCreationDate);

            if (shouldSkip)
            {
                int pWidth;
                int pHeight;

                if (isCached)
                {
                    pWidth = cachedMeta!.Width;
                    pHeight = cachedMeta.Height;
                }
                else
                {
                    var pingInfo = new MagickImageInfo(srcFilePath);
                    pWidth = (int)pingInfo.Width;
                    pHeight = (int)pingInfo.Height;
                }

                Program.PixelsAlreadyCached += (ulong)(pWidth * pHeight);
                Program.TotalPixelsProcessed += (ulong)(pWidth * pHeight);

                return (ddsOutputPath, pWidth, pHeight, true);
            }

            try
            {
                var pingInfo2 = new MagickImageInfo(srcFilePath);
                if (pingInfo2.Format == MagickFormat.Unknown)
                    return (string.Empty, -1, -1, true);
            }
            catch (Exception)
            { // corrupt file
                return (string.Empty, -1, -1, true);
            }

            using var magickImage = new MagickImage(srcFilePath);
            magickImage.Flip();
            magickImage.Format = MagickFormat.Rgba;
            magickImage.ColorSpace = ColorSpace.sRGB;
            magickImage.ColorType = ColorType.TrueColorAlpha;
            magickImage.Depth = 8;
            if (magickImage.HasAlpha == false)
                magickImage.Alpha(AlphaOption.On);

            int width = (int)magickImage.Width;
            int height = (int)magickImage.Height;
            byte[] pixelBytes = magickImage.ToByteArray();

            BcEncoder encoder = new();
            encoder.OutputOptions.GenerateMipMaps = false;
            encoder.OutputOptions.Quality = CompressionQuality.Balanced;
            encoder.OutputOptions.FileFormat = OutputFileFormat.Dds;
            encoder.OutputOptions.Format = format;

            try
            {
                if (!Directory.Exists(toDirectory))
                    Directory.CreateDirectory(toDirectory);

                using FileStream fs = File.OpenWrite(ddsOutputPath);
                encoder.EncodeToStream(pixelBytes, width, height, PixelFormat.Rgba32, fs);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                throw;
            }

            Program.TotalPixelsProcessed += (ulong)(width * height);

            return (ddsOutputPath, width, height, false);
        }

    }
}
