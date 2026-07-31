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
        public static (string ddsFilePath, int width, int height, bool wasSkipped, float[][]? colors, string signature) Convert(string srcFilePath, string toDirectory, CompressionFormat format, string? customOutName = null, bool overwrite = false)
        {
            // TODO: organise this function to not be so disorganised
            int width, height;
            byte[] pixelBytes;
            MagickImage magickImage;
            string signature;
            // this is used to get the color values of textures that is necessary for the game
            ImageAnalyzer lyzer = new ImageAnalyzer();

            try
            {
                var pingInfo2 = new MagickImageInfo(srcFilePath);
                if (pingInfo2.Format == MagickFormat.Unknown)
                    return (string.Empty, -1, -1, true, null, string.Empty);


                magickImage = new MagickImage(srcFilePath);
            }
            catch (Exception)
            { // corrupt file // TODO: create a error log or whatever
                return (string.Empty, -1, -1, true, null, string.Empty);
            }

            string fileName = Path.GetFileNameWithoutExtension(srcFilePath);
            string outputFileName = string.IsNullOrWhiteSpace(customOutName) ? $"{fileName}.dds" : customOutName;
            string ddsOutputPath = Path.Combine(toDirectory, outputFileName);

            bool isCached = Program.ExistingMetadataCache.TryGetValue(srcFilePath, out FileMetadata? cachedMeta);
            bool fileExists = !overwrite && File.Exists(ddsOutputPath) && new FileInfo(ddsOutputPath).Length != 0;

            bool shouldSkip = fileExists && (!isCached || File.GetLastWriteTimeUtc(srcFilePath) <= cachedMeta!.DDSCreationDate);

            if (shouldSkip)
            {
                float[][] colors;
                if (isCached)
                {
                    width = cachedMeta!.Width;
                    height = cachedMeta.Height;
                    colors = [cachedMeta.Mean, cachedMeta.Weighted, cachedMeta.Median];
                    signature = cachedMeta.ImageHash;
                }
                else
                {
                    GetInfoAboutMagickImage(magickImage, out width, out height, out pixelBytes, out signature);

                    for (int i = 0; i < pixelBytes.Length; i += 4)
                    { // to get the colors, sadly we will need to iterate over every pixel which is pretty slow but thats what you have to do to get the colors
                        if (pixelBytes[i + 3] != 0) // not alpha
                        {
                            byte r = pixelBytes[i];
                            byte g = pixelBytes[i + 1];
                            byte b = pixelBytes[i + 2];

                            lyzer.AddPixel(r, g, b);
                        }
                    }
                }

                Program.PixelsAlreadyCached += (ulong)(width * height);
                Program.TotalPixelsProcessed += (ulong)(width * height);

                return (ddsOutputPath, width, height, true, MagickColorArrayToFloatArray(lyzer.CalculateAverageColor()), signature);
            }
            GetInfoAboutMagickImage(magickImage, out width, out height, out pixelBytes, out signature);


            for (int i = 0; i < pixelBytes.Length; i += 4)
            {
                if (pixelBytes[i + 3] == 0) // a
                { // for reasons unknown to me, textures have non zero color data even when the pixel is transparent
                    // the confusion is less of "why would it be allowed" and more of "why would they become transparent if they were drawn"

                    pixelBytes[i] = 0;     // r
                    pixelBytes[i + 1] = 0; // g
                    pixelBytes[i + 2] = 0; // b
                }
                else
                { // since we are already iterating over the pixels we can calculate the avarages here
                    byte r = pixelBytes[i];
                    byte g = pixelBytes[i + 1];
                    byte b = pixelBytes[i + 2];

                    lyzer.AddPixel(r, g, b);
                }
            }

            BcEncoder encoder = new();
            encoder.OutputOptions.GenerateMipMaps = false;
            encoder.OutputOptions.Quality = CompressionQuality.Balanced;
            encoder.OutputOptions.FileFormat = OutputFileFormat.Dds;
            encoder.OutputOptions.Format = format;
            encoder.Options.TaskCount = ProcessorCountToUse;

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

            return (ddsOutputPath, width, height, false, MagickColorArrayToFloatArray(lyzer.CalculateAverageColor()), signature);
        }

        private static void GetInfoAboutMagickImage(MagickImage magickImage, out int width, out int height, out byte[] pixelBytes, out string signature)
        {
            magickImage.Flip();
            magickImage.Format = MagickFormat.Rgba;
            magickImage.ColorSpace = ColorSpace.sRGB;
            magickImage.ColorType = ColorType.TrueColorAlpha;
            magickImage.Depth = 8;
            if (magickImage.HasAlpha == false)
                magickImage.Alpha(AlphaOption.On);

            width = (int)magickImage.Width;
            height = (int)magickImage.Height;
            pixelBytes = magickImage.ToByteArray();
            signature = magickImage.Signature;
            magickImage.Dispose();
        }

        private static float[][] MagickColorArrayToFloatArray(MagickColor[] colors)
        {
            float[][] matrix = new float[3][];

            for (int i = 0; i < 3; i++)
            {
                matrix[i] = new float[3];

                matrix[i][0] = (float)colors[i].R / 255f;
                matrix[i][1] = (float)colors[i].G / 255f;
                matrix[i][2] = (float)colors[i].B / 255f;
            }

            return matrix;
        }
    }
}
