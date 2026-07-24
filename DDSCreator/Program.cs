global using ColoredLogger;
global using static DDSCreator.Consts;
global using static DDSCreator.Misc;
using Newtonsoft.Json;
using ShellProgressBar;

namespace DDSCreator
{
    internal class Program
    {
        public static ulong TotalPixelsTotal { get; set; }
        public static ulong PixelsAlreadyCached { get; set; }
        public static Dictionary<string, FileMetadata> ExistingMetadataCache { get; set; } = new(StringComparer.OrdinalIgnoreCase);
        static void Main(string[] args)
        {
            int padding = nameof(StarsectorCodeDir).Length + 3; padding *= -1;
            new LogBuilder()
                .C().Write($"{nameof(AppDir)}: ", padding).D().WriteLine(AppDir)
                .C().Write($"{nameof(ModDir)}: ", padding).D().WriteLine(ModDir)
                .C().Write($"{nameof(ModsDir)}: ", padding).D().WriteLine(ModsDir)
                .C().Write($"{nameof(GameDir)}: ", padding).D().WriteLine(GameDir)
                .C().Write($"{nameof(StarsectorCodeDir)}: ", padding).D().WriteLine(StarsectorCodeDir)
                .C().Write($"{nameof(CacheDir)}: ", padding).D().WriteLine(CacheDir)
                .NewLine().Log();

            string outputPath = Path.Combine(ModDir.FullName, "dds_metadata.json");

            if (File.Exists(outputPath))
            {
                try
                {
                    string jsonContent = File.ReadAllText(outputPath);
                    var existingList = JsonConvert.DeserializeObject<List<FileMetadata>>(jsonContent);
                    if (existingList != null)
                    {
                        ExistingMetadataCache = existingList.ToDictionary(
                            x => Path.Combine(ModsDir.FullName, x.ModFolderName, x.RelativeImagePath),
                            x => x,
                            StringComparer.OrdinalIgnoreCase
                        );
                    }
                }
                catch
                {
                    File.WriteAllText(outputPath, string.Empty);
                }
            }


            using StreamWriter sw = new(outputPath);
            using JsonWriter writer = new JsonTextWriter(sw);
            writer.Formatting = Formatting.Indented;
            writer.WriteStartArray();
            JsonSerializer serializer = new JsonSerializer();

            List<DirectoryInfo> validMods = [StarsectorCodeDir];
            validMods.AddRange(ModsDir.GetDirectories()
            .Where(mod => File.Exists(Path.Join(mod.FullName, "mod_info.json")))
            .ToList());


            if (validMods.Count == 0)
            {
                writer.WriteEndArray();
                return;
            }

            var options = new ProgressBarOptions
            {
                ProgressCharacter = '=',
                BackgroundCharacter = '-',
                ProgressBarOnBottom = true,
                ForegroundColor = ConsoleColor.Cyan,
                BackgroundColor = ConsoleColor.DarkGray,
                ForegroundColorDone = ConsoleColor.Green,
            };

            int modIndex = 0;
            using var pbar = new ProgressBar(validMods.Count, "Overall Progress", options);

            foreach (DirectoryInfo mod in validMods)
            {
                modIndex++;

                void updateMessage()
                {
                    string cachedFormatted = FormatCompactNumber(PixelsAlreadyCached);
                    string totalFormatted = FormatCompactNumber(TotalPixelsTotal);

                    pbar.Message = $"[{modIndex}/{validMods.Count}] Current Mod: {mod.Name,-30} Total Pixels Processed (already processed/total): ({cachedFormatted}/{totalFormatted})";
                }
                updateMessage();

                var validImageFiles = ModHandler.GetValidImageFiles(mod);
                int childTicks = Math.Max(validImageFiles.Count, 1);

                using (var childPbar = pbar.Spawn(childTicks, $"Scanning images for {mod.Name}", options))
                {
                    List<FileMetadata> newItems = ModHandler.ConvertMod(mod, validImageFiles, childPbar, updateMessage);

                    foreach (var item in newItems)
                    {
                        serializer.Serialize(writer, item);
                    }
                    if (validImageFiles.Count == 0)
                        childPbar.Tick();
                }

                pbar.Tick($"Completed mod: {mod.Name}");
            }

            writer.WriteEndArray();
            pbar.Dispose();
            Console.SetCursorPosition(0, Console.WindowHeight - 2);

            string cachedFormatted = FormatCompactNumber(PixelsAlreadyCached);
            string totalFormatted = FormatCompactNumber(TotalPixelsTotal);

            Logger.Log($"All modifications processed successfully!\nTotal Pixels Processed (already processed/total): ({cachedFormatted}/{totalFormatted})", LogLevel.Info);
        }
        public static string FormatCompactNumber(ulong value)
        {
            if (value >= 1_000_000_000)
                return $"{value / 1_000_000_000D:0.#}B";
            if (value >= 1_000_000)
                return $"{value / 1_000_000D:0.#}M";
            if (value >= 1_000)
                return $"{value / 1_000D:0.#}K";

            return value.ToString();
        }
    }
}