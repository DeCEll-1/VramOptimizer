global using static DDSCreator.Consts;
global using static DDSCreator.Misc;
global using static DDSCreator.Program;
using DDSCreator.Model;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using ShellProgressBar;
using Spectre.Console;
using System.Linq;

namespace DDSCreator
{
    internal class Program
    {
        public static ulong TotalPixelsProcessed { get; set; }
        public static ulong PixelsAlreadyCached { get; set; }
        public static Dictionary<string, FileMetadata> ExistingMetadataCache { get; set; } = new(StringComparer.OrdinalIgnoreCase);
        public static List<ModInfo> FailedToLoadMods = [];
        public static List<ModInfo> ValidMods = []; // this is just the mods that have mod_info.json
        public static List<string> EnabledMods = [];
        static void Main(string[] args)
        {
            UpdateEnabledMods();
            UpdateMetadataCache();
            UpdateValidMods();
            while (true)
            {
                MenuChoice option = AnsiConsole.Prompt(
                        new SelectionPrompt<MenuChoice>()
                            .Title("What would you like to do?")
                            .PageSize(10)
                            .WrapAround()
                            .AddChoices(Enum.GetValues<MenuChoice>()
                    ));

                switch (option)
                {
                    case MenuChoice.EditMods:
                        SelectionHandler.DisplayEnabledModsHandler();
                        break;
                    case MenuChoice.ProcessMods:
                        if (ModHandler.DisplayConfirmation())
                            ModHandler.HandleMods();
                        break;
                    case MenuChoice.PrintDebug:
                        PrintDirs();
                        break;
                    case MenuChoice.PrintError:
                        PrintErroredMods();
                        break;
                    case MenuChoice.Quit:
                        goto quit;
                    default:
                        break;
                }
                Console.Clear();
            }

        quit:;
        }

        private enum MenuChoice
        { // rename this enum to something that actually makes sense
            EditMods,
            ProcessMods,
            PrintDebug,
            PrintError,
            Quit,
        }


        #region etc
        private static void UpdateEnabledMods()
        {
            var enabledModsLoc = Path.Combine(ModsDir.FullName, "enabled_mods.json");
            if (!File.Exists(enabledModsLoc))
                return;
            var enabledModsText = File.ReadAllText(enabledModsLoc);
            EnabledMods = JObject.Parse(enabledModsText)["enabledMods"]!.ToObject<List<string>>()!;
        }

        private static void UpdateValidMods()
        {
            var starsector = new ModInfo
            {
                ID = "starsector-core",
                Name = "Starsector",
                Author = "Alex",
                Description = "The Game",
                GameVersion = string.Empty,
                Jars = [],
                Dir = StarsectorCodeDir,
                ShouldProcess = true
            };

            ValidMods = [starsector];

            var loadedMods = ModsDir.GetDirectories()
                .Where(mod => File.Exists(Path.Join(mod.FullName, "mod_info.json")))
                .Select(ModInfo.LoadModInfo)
                .ToList();

            FailedToLoadMods = loadedMods.Where(s => string.IsNullOrEmpty(s.ID)).ToList();
            loadedMods.RemoveAll(s => string.IsNullOrEmpty(s.ID));

            foreach (var mod in loadedMods)
            {
                mod.ShouldProcess = EnabledMods.Contains(mod.ID);
            }

            ValidMods.AddRange(loadedMods);
            ValidMods = ValidMods.OrderBy(s => s.Name).ToList();
        }

        private static void UpdateMetadataCache()
        {
            foreach (string? modMetadataPath in CacheDir.GetDirectories().Select(s => Path.Combine(s.FullName, DdsMetadataFileName)))
            {
                if (!File.Exists(modMetadataPath))
                    continue;// metadata does not exist for this specific mod
                string jsonContent = File.ReadAllText(modMetadataPath);
                var existingList = JsonConvert.DeserializeObject<List<FileMetadata>>(jsonContent);
                if (existingList == null)
                    continue;// corrupt

                Func<FileMetadata, string> keySelector;

                if (modMetadataPath.Contains("starsector-core"))
                    keySelector = x => Path.Combine(StarsectorCodeDir.FullName, x.RelativeImagePath);
                else
                    keySelector = x => Path.Combine(ModsDir.FullName, x.ModFolderName, x.RelativeImagePath);

                ExistingMetadataCache = ExistingMetadataCache
                                        .Concat(existingList.ToDictionary(
                                            keySelector,
                                            x => x,
                                            StringComparer.OrdinalIgnoreCase
                                        ))
                                        .ToDictionary(
                                            kvp => kvp.Key,
                                            kvp => kvp.Value,
                                            StringComparer.OrdinalIgnoreCase
                                        );
            }
        }

        private static void PrintDirs()
        {
            const int padding = 17 + 3;

            AnsiConsole.MarkupLine(
                $"[cyan]{nameof(AppDir),-padding}[/] {AppDir}\n" +
                $"[cyan]{nameof(ModDir),-padding}[/] {ModDir}\n" +
                $"[cyan]{nameof(ModsDir),-padding}[/] {ModsDir}\n" +
                $"[cyan]{nameof(GameDir),-padding}[/] {GameDir}\n" +
                $"[cyan]{nameof(StarsectorCodeDir),-padding}[/] {StarsectorCodeDir}\n" +
                $"[cyan]{nameof(CacheDir),-padding}[/] {CacheDir}"
            );
            Console.ReadKey();
        }
        private static void PrintErroredMods()
        {
            if (FailedToLoadMods.Count == 0)
            {
                AnsiConsole.MarkupLine("[blue]No errored mods found.[/]");
                Console.ReadKey();
                return;
            }

            foreach (ModInfo mod in FailedToLoadMods)
            {
                AnsiConsole.WriteLine(mod.Dir.FullName);
            }

            Console.ReadKey();
        }
        #endregion
    }
}