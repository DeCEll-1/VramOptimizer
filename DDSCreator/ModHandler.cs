using BCnEncoder.Shared;
using BCnEncoder.Shared.ImageFiles;
using ColoredLogger;
using CsvHelper;
using CsvHelper.Configuration;
using DDSCreator.GLib;
using ShellProgressBar;
using SixLabors.ImageSharp;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using static DDSCreator.FileMetadata;

namespace DDSCreator
{
    public class ModHandler
    {
        private static readonly HashSet<string> FileFormats = new(StringComparer.OrdinalIgnoreCase) { ".jpg", ".jpeg", ".webp", ".png" };
        private static readonly HashSet<string> DiscardedFolderNames = new(StringComparer.OrdinalIgnoreCase) { "Promotional", "Memes", "Meme", "cache", "javadoc" };
        // glibs cache is blacklisted as theyre loaded and unloaded mid game, so conversion would be of no use

        public static bool IsDiscarded(string rootModPath, string imagePath)
        {
            string relativeImagePath = Path.GetRelativePath(rootModPath, imagePath);
            return DiscardedFolderNames.Any(folder =>
                relativeImagePath.StartsWith(folder + Path.DirectorySeparatorChar, StringComparison.OrdinalIgnoreCase) ||
                relativeImagePath.Equals(folder, StringComparison.OrdinalIgnoreCase));
        }

        public static List<string> GetValidImageFiles(DirectoryInfo modPath)
        {
            List<string> images = Directory.EnumerateFiles(modPath.FullName, "*.*", SearchOption.AllDirectories)
                    .Where(file => FileFormats.Contains(Path.GetExtension(file)) && !IsDiscarded(modPath.FullName, file))
                    .ToList();

            string csvPath = Path.Combine(modPath.FullName, "data", "lights", "core_texture_data.csv");
            if (File.Exists(csvPath))
            {
                List<TextureData> csv = ReadCsvFile(csvPath);

                var csvPaths = new HashSet<string>(csv.Select(x => x.Path), StringComparer.OrdinalIgnoreCase);

                images = images.Where(s =>
                {
                    string relativePath = Path.GetRelativePath(modPath.FullName, s).Replace('\\', '/');
                    return !csvPaths.Contains(relativePath);
                }).ToList();
            }

            return images;
        }

        public static List<TextureData> ReadCsvFile(string filePath)
        {
            var config = new CsvConfiguration(CultureInfo.InvariantCulture)
            {
                ShouldSkipRecord = args => args.Row.Parser.Record?.All(string.IsNullOrWhiteSpace) ?? true
            };

            using var reader = new StreamReader(filePath);
            using var csv = new CsvReader(reader, config);

            var records = csv.GetRecords<TextureData>().ToList();
            return records;
        }

        public static List<FileMetadata> ConvertMod(DirectoryInfo modPath, List<string> validImageFiles, ChildProgressBar childPbar, Action OnConvert)
        {
            string modID;
            if (modPath.Name == "starsector-core")
                modID = "starsector-core";
            else
                modID = ModInfoJSON.getModID(modPath.FullName);

            List<FileMetadata> processedFiles = [];

            int currentImageIndex = 0;
            foreach (string imagePath in validImageFiles)
            {
                currentImageIndex++;
                string relativeImagePath = Path.GetRelativePath(modPath.FullName, imagePath);

                childPbar?.Tick(currentImageIndex, $"Processing: ({currentImageIndex}/{validImageFiles.Count}) {relativeImagePath}");

                CompressionFormat format = CompressionFormat.Bc7;

                var (ddsFilePath, width, height, wasSkipped) = Converter.Convert(
                    srcFilePath: imagePath,
                    toDirectory: Path.GetDirectoryName(Path.Combine(CacheDir.FullName, modPath.Name, relativeImagePath))!,
                    format: format,
                    overwrite: false
                );

                OnConvert();

                string ext = Path.GetExtension(imagePath).TrimStart('.').ToLowerInvariant();

                ImageFileType imageFileType = ext switch
                {
                    "jpg" or "jpeg" => ImageFileType.Jpg,
                    "png" => ImageFileType.Png,
                    "webp" => ImageFileType.Webp,
                    _ => ImageFileType.None
                };

                processedFiles.Add(new()
                {
                    ModID = modID,
                    ModFolderName = modPath.Name,
                    RelativeImagePath = relativeImagePath,
                    ImageCreationDate = File.GetCreationTimeUtc(imagePath),
                    ImageEditDateDate = File.GetLastWriteTimeUtc(imagePath),
                    DDSCreationDate = File.GetCreationTimeUtc(ddsFilePath),
                    DDSEditDate = File.GetLastWriteTimeUtc(ddsFilePath),
                    DDSFilePath = ddsFilePath.Replace(CacheDir!.Parent!.Parent!.Parent!.FullName, ""),
                    ImageType = imageFileType,
                    CompressionFormat = format.ToString(),
                    Width = width,
                    Height = height,
                });
            }
            childPbar?.Tick(validImageFiles.Count, $"Completed: {modPath.Name} ({currentImageIndex}/{validImageFiles.Count})");
            return processedFiles;
        }
    }
}
