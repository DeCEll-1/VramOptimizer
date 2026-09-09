using DDSCreator.Model;
using ImageMagick;

namespace DDSCreator
{
    public class Converter
    {
        public static ConversionResult Convert(string srcFilePath, string toDirectory, string? customOutName = null, bool overwrite = false)
        {
            string fileName = Path.GetFileNameWithoutExtension(srcFilePath);
            string outputFileName = string.IsNullOrWhiteSpace(customOutName) ? $"{fileName}.dds" : customOutName;
            string ddsOutputPath = Path.Combine(toDirectory, outputFileName);

            bool isCached = Program.ExistingMetadataCache.TryGetValue(srcFilePath, out FileMetadata? cachedMeta);
            bool fileExists = !overwrite && File.Exists(ddsOutputPath) && new FileInfo(ddsOutputPath).Length != 0;
            bool shouldSkip = fileExists && (!isCached || File.GetLastWriteTimeUtc(srcFilePath) <= cachedMeta!.DDSCreationDate);

            // 1. Handle Cached / Skipped Path
            if (shouldSkip)
            {
                if (isCached && cachedMeta!.ImageHash != "null")
                {
                    Program.PixelsAlreadyCached += (ulong)(cachedMeta.Width * cachedMeta.Height);
                    Program.TotalPixelsProcessed += (ulong)(cachedMeta.Width * cachedMeta.Height);

                    return new ConversionResult
                    {
                        DdsFilePath = ddsOutputPath,
                        Width = cachedMeta.Width,
                        Height = cachedMeta.Height,
                        WasSkipped = true,
                        Colors = [cachedMeta.Mean, cachedMeta.Weighted, cachedMeta.Median],
                        Signature = cachedMeta.ImageHash
                    };
                }

                // since cache doesnt exist read the file to get the required data

                var loaded = LoadAndAnalyzeImage(srcFilePath, processPixelsForEncoding: false);
                if (!loaded.Success) return new ConversionResult { WasSkipped = true };

                Interlocked.Add(ref Program.PixelsAlreadyCached, (ulong)(loaded.Width * loaded.Height));
                Interlocked.Add(ref Program.TotalPixelsProcessed, (ulong)(loaded.Width * loaded.Height));

                return new ConversionResult
                {
                    DdsFilePath = ddsOutputPath,
                    Width = loaded.Width,
                    Height = loaded.Height,
                    WasSkipped = true,
                    Colors = loaded.Colors,
                    Signature = loaded.Signature
                };
            }

            var freshLoad = LoadAndAnalyzeImage(srcFilePath, processPixelsForEncoding: true);
            if (!freshLoad.Success) return new ConversionResult { WasSkipped = true };

            EncodeAndSaveDds(freshLoad.PixelBytes, freshLoad.Width, freshLoad.Height, ddsOutputPath);

            Interlocked.Add(ref Program.TotalPixelsProcessed, (ulong)(freshLoad.Width * freshLoad.Height));

            return new ConversionResult
            {
                DdsFilePath = ddsOutputPath,
                Width = freshLoad.Width,
                Height = freshLoad.Height,
                WasSkipped = false,
                Colors = freshLoad.Colors,
                Signature = freshLoad.Signature
            };
        }

        private static (bool Success, int Width, int Height, byte[] PixelBytes, float[][] Colors, string Signature) LoadAndAnalyzeImage(string srcFilePath, bool processPixelsForEncoding)
        {
            MagickImage magickImage;
            try
            {
                var pingInfo = new MagickImageInfo(srcFilePath);
                if (pingInfo.Format == MagickFormat.Unknown)
                    return (false, -1, -1, [], [], string.Empty);

                magickImage = new MagickImage(srcFilePath);
            }
            catch (Exception)
            {
                // TODO: create an error log
                return (false, -1, -1, [], [], string.Empty);
            }

            string signature = magickImage.Signature;
            GetInfoAboutMagickImage(magickImage, out int width, out int height, out byte[] pixelBytes);

            ImageAnalyzer lyzer = new();

            for (int i = 0; i < pixelBytes.Length; i += 4)
            {
                byte r = pixelBytes[i];
                byte g = pixelBytes[i + 1];
                byte b = pixelBytes[i + 2];
                byte a = pixelBytes[i + 3];

                if (a == 0 && processPixelsForEncoding)
                {
                    // Clean up non-zero color data on transparent pixels
                    pixelBytes[i] = 0;
                    pixelBytes[i + 1] = 0;
                    pixelBytes[i + 2] = 0;
                }
                else if (a != 0)
                {
                    lyzer.AddPixel(r, g, b);
                }
            }

            var colors = MagickColorArrayToFloatArray(lyzer.CalculateAverageColor());
            return (true, width, height, pixelBytes, colors, signature);
        }

        private static void EncodeAndSaveDds(byte[] pixelBytes, int width, int height, string ddsOutputPath)
        {
            if (!Directory.Exists(Path.GetDirectoryName(ddsOutputPath)))
                Directory.CreateDirectory(Path.GetDirectoryName(ddsOutputPath)!);

            List<byte[]> mipBuffers = new List<byte[]>();
            int targetMips = CalculateOptimalMipLevels(width,height);
            int maxPossibleMips = (int)(Math.Floor(Math.Log(Math.Max(width, height), 2))) + 1;
            targetMips = Math.Min(targetMips, maxPossibleMips);

            using (var magickImage = new MagickImage())
            {
                var readSettings = new PixelReadSettings((uint)width, (uint)height, StorageType.Char, "RGBA");
                magickImage.ReadPixels(pixelBytes, readSettings);

                for (int i = 0; i < targetMips; i++)
                {
                    using var clone = new MagickImage(magickImage);
                    int currentW = Math.Max(1, width >> i);
                    int currentH = Math.Max(1, height >> i);

                    if (i > 0)
                    {
                        clone.Resize((uint)currentW, (uint)currentH);
                    }

                    clone.Format = MagickFormat.Rgba;
                    byte[] levelBytes = clone.ToByteArray();

                    // Clean up non-zero color data on transparent pixels for each mip level
                    for (int p = 0; p < levelBytes.Length; p += 4)
                    {
                        if (levelBytes[p + 3] == 0)
                        {
                            levelBytes[p] = 0;
                            levelBytes[p + 1] = 0;
                            levelBytes[p + 2] = 0;
                        }
                    }

                    mipBuffers.Add(levelBytes);
                }
            }

            byte[] ddsData = NativeBc7Encoder.EncodeToDds(mipBuffers, width, height, Program.TextureTaskCount);
            File.WriteAllBytes(ddsOutputPath, ddsData);
        }

        private static void GetInfoAboutMagickImage(MagickImage magickImage, out int width, out int height, out byte[] pixelBytes)
        {
            magickImage.Flip();
            magickImage.Format = MagickFormat.Rgba;
            magickImage.ColorSpace = ColorSpace.sRGB;
            magickImage.ColorType = ColorType.TrueColorAlpha;
            magickImage.Depth = 8;
            if (!magickImage.HasAlpha)
                magickImage.Alpha(AlphaOption.On);

            width = (int)magickImage.Width;
            height = (int)magickImage.Height;
            pixelBytes = magickImage.ToByteArray();
            magickImage.Dispose();
        }

        private static float[][] MagickColorArrayToFloatArray(MagickColor[] colors)
        {
            float[][] matrix = new float[3][];

            for (int i = 0; i < 3; i++)
            {
                matrix[i] = new float[3];
                matrix[i][0] = colors[i].R / 255f;
                matrix[i][1] = colors[i].G / 255f;
                matrix[i][2] = colors[i].B / 255f;
            }

            return matrix;
        }

        private static int CalculateOptimalMipLevels(int width, int height)
        {
            int minSize = Program.SmallestMipmapSize;
            int targetMips = 0;
            int currentW = width;
            int currentH = height;

            while (currentW >= minSize && currentH >= minSize)
            {
                targetMips++;
                currentW >>= 1;
                currentH >>= 1;
            }

            return Math.Max(1, targetMips);
        }

        public class ConversionResult
        {
            public string DdsFilePath { get; set; } = string.Empty;
            public int Width { get; set; } = -1;
            public int Height { get; set; } = -1;
            public bool WasSkipped { get; set; }
            public float[][]? Colors { get; set; }
            public string Signature { get; set; } = string.Empty;

            public bool IsSuccess => Width != -1 && Height != -1 && Colors != null && !string.IsNullOrEmpty(Signature);
        }
    }
}
