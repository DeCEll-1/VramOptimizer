using CsvHelper;
using CsvHelper.Configuration;
using DDSCreator.GLib;
using DDSCreator.Model;
using Newtonsoft.Json;
using ShellProgressBar;
using Spectre.Console;
using System.Diagnostics;
using System.Globalization;
using static DDSCreator.Model.FileMetadata;

namespace DDSCreator
{
    public class ModHandler
    {
        #region per mod handling
        #region misc
        private static readonly HashSet<string> FileFormats = new(StringComparer.OrdinalIgnoreCase) { ".jpg", ".jpeg", ".webp", ".png" };

        private static readonly HashSet<string> DiscardedFolderPaths = new(StringComparer.OrdinalIgnoreCase)
            {
                "Promotional",
                "Memes",
                "Meme",
                "cache",
                "javadoc",
                // backgrounds are discarded as theyre already loaded on demand
                "/backgrounds/",

            };


        public static bool IsDiscarded(string rootModPath, string imagePath)
        {
            string relativeImagePath = Path.GetRelativePath(rootModPath, imagePath).Replace(Path.DirectorySeparatorChar, '/');

            return DiscardedFolderPaths.Any(discarded =>
                relativeImagePath.Contains(discarded, StringComparison.OrdinalIgnoreCase)
                || relativeImagePath.StartsWith("mods/")
            );
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
        #endregion
        public static List<FileMetadata> ConvertMod(ModInfo mod, List<string> validImageFiles, ChildProgressBar childPbar, Action OnConvert)
        {
            string metadataPath = Path.Combine(CacheDir.FullName, mod.Dir.Name, DdsMetadataFileName);

            FileMetadata[] processed = new FileMetadata[validImageFiles.Count];

            int currentImageIndex = 0;

            ParallelOptions parallelOptions = new() { MaxDegreeOfParallelism = Program.ProcessorCountToUse };
            Parallel.ForEach(Enumerable.Range(0, validImageFiles.Count), parallelOptions, i =>
            {
                string imagePath = validImageFiles[i];

                #region Metadata creation
                int currentIndex = Interlocked.Increment(ref currentImageIndex);
                string relativeImagePath = Path.GetRelativePath(mod.Dir.FullName, imagePath);

                childPbar?.Tick(currentImageIndex, $"Processing: ({currentIndex}/{validImageFiles.Count}) {relativeImagePath}");

                var result = Converter.Convert(
                    srcFilePath: imagePath,
                    toDirectory: Path.GetDirectoryName(Path.Combine(CacheDir.FullName, mod.Dir.Name, relativeImagePath))!,
                    overwrite: false
                );

                if (!result.IsSuccess)
                    return;

                OnConvert();

                string ext = Path.GetExtension(imagePath).TrimStart('.').ToLowerInvariant();

                ImageFileType imageFileType = ext switch
                {
                    "jpg" or "jpeg" => ImageFileType.Jpg,
                    "png" => ImageFileType.Png,
                    "webp" => ImageFileType.Webp,
                    _ => ImageFileType.None
                };

                FileMetadata metadata = new()
                {
                    ModID = mod.ID,
                    ModFolderName = mod.Dir.Name,
                    RelativeImagePath = relativeImagePath,
                    ImageCreationDate = File.GetCreationTimeUtc(imagePath),
                    ImageEditDateDate = File.GetLastWriteTimeUtc(imagePath),
                    DDSCreationDate = File.GetCreationTimeUtc(result.DdsFilePath),
                    DDSEditDate = File.GetLastWriteTimeUtc(result.DdsFilePath),
                    DDSFilePath = result.DdsFilePath.Replace(GameDir.FullName, ""),
                    ImageType = imageFileType,
                    CompressionFormat = "BC7",
                    Width = result.Width,
                    Height = result.Height,
                    Mean = result.Colors![0],
                    Weighted = result.Colors[1],
                    Median = result.Colors[0],
                    ImageHash = result.Signature,
                    VOptVersion = Consts.Version
                };

                processed[i] = metadata;
                #endregion
            });

            List<FileMetadata> processedFiles = processed.Where(file => file is not null).Select(file => file!).ToList();

            childPbar?.Tick(validImageFiles.Count, $"Completed: {mod.Name} ({currentImageIndex}/{validImageFiles.Count})");

            if (File.Exists(metadataPath))
                File.Delete(metadataPath);
            if (validImageFiles.Count != 0) // need this as otherwise it would try to put the file into a non existing folder
                                            // and theres no reason to create meaningless folders
                File.WriteAllText(metadataPath, JsonConvert.SerializeObject(processedFiles, Formatting.Indented));

            return processedFiles;
        }
        #endregion
        #region mod list handling
        #region misc
        #endregion
        public static void HandleMods()
        {
            Stopwatch timer = new();
            timer.Start();

            List<ModInfo> ModsToProcess = ValidMods.Where(s => s.ShouldProcess).ToList();

            var options = new ProgressBarOptions
            {
                ProgressCharacter = '=',
                BackgroundCharacter = '-',
                ProgressBarOnBottom = true,
                CollapseWhenFinished = true,
                ForegroundColor = ConsoleColor.Cyan,
                BackgroundColor = ConsoleColor.DarkGray,
                ForegroundColorDone = ConsoleColor.Green,
            };

            int modIndex = 0;
            var pbar = new ProgressBar(ModsToProcess.Count, "Overall Progress", options);

            foreach (ModInfo mod in ModsToProcess)
            {
                modIndex++;
                #region flavor message
                void updateMessage()
                {
                    string cachedFormatted = FormatCompactNumber(PixelsAlreadyCached);
                    string totalFormatted = FormatCompactNumber(TotalPixelsProcessed);

                    pbar.Message = $"[{modIndex}/{ModsToProcess.Count}] Current Mod: {mod.Name,-30} Total Pixels Processed: {totalFormatted}";
                }
                updateMessage();
                #endregion

                List<string> validImageFiles = GetValidImageFiles(mod.Dir);
                int childTicks = Math.Max(validImageFiles.Count, 1);

                using (ChildProgressBar childPbar = pbar.Spawn(childTicks, $"Scanning images for {mod.Name}", options))
                {
                    List<FileMetadata> newItems = ConvertMod(mod, validImageFiles, childPbar, updateMessage);

                    if (validImageFiles.Count == 0)
                        childPbar.Tick(); // so that it doesnt stuck on non complete for 0 imaged mods
                }

                pbar.Tick($"Completed mod: {mod.Name}");
            }
            Thread.Sleep(5); // wait for progress bars to be done with their job

            timer.Stop();

            Console.Clear();
            string totalFormatted = FormatCompactNumber(TotalPixelsProcessed);

            AnsiConsole.MarkupLine($"[blue]All modifications processed successfully!\nTotal Pixels Processed {totalFormatted}\nDuration {timer.Elapsed:c}ms[/]");
            Console.WriteLine("Press any key to go back to the main menu.");
            Console.ReadKey();

            PixelsAlreadyCached = 0;
            TotalPixelsProcessed = 0;

        }

        internal static bool DisplayConfirmation()
        {
            AnsiConsole.MarkupLine("[white]Mods to be processed[/]");

            foreach (ModInfo mod in ValidMods.Where(s => s.ShouldProcess))
                AnsiConsole.MarkupLine($"[Grey70]{Markup.Escape(mod.Name)}[/]");

            AnsiConsole.MarkupLine($"[white]Compression quality [/][Chartreuse1]{Program.CurrentCompressionPreset}[/]");


            string choice = AnsiConsole.Prompt(
                new SelectionPrompt<string>()
                    .Title("").WrapAround()
                    .PageSize(3) // it has a title for padding
                    .AddChoices(["Continue", "Cancel"]
                )
            );

            return choice == "Continue";
        }


        #endregion
    }
}
