global using static DDSCreator.Consts;
global using static DDSCreator.Misc;
global using static DDSCreator.Program;
using DDSCreator.Model;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using ShellProgressBar;
using Spectre.Console;
using System.Linq;
using System.Runtime.CompilerServices;

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
        public static int ProcessorCountToUse = Environment.ProcessorCount;
        static void Main(string[] args)
        {
            // TODO: add a uhhh gpu check if the gpu allows dds textures
            // i doubt theres any gpus left that cant support dds but eh
            UpdateEnabledMods();
            UpdateMetadataCache();
            UpdateValidMods();

            List<MenuChoice> choices = Enum.GetValues<MenuChoice>().ToList();
#if LINUX || MAC
            choices.Remove(Program.MenuChoice.EnableLongPaths);
#endif
#if WINDOWS || DEBUG
            if (AreLongPathsEnabled())
                choices.Remove(MenuChoice.EnableLongPaths);
#endif

            while (true)
            {
                MenuChoice option = AnsiConsole.Prompt(
                        new SelectionPrompt<MenuChoice>()
                            .Title("What would you like to do?")
                            .PageSize(10)
                            .WrapAround()
                            .AddChoices(choices)
                            .UseConverter(s =>
                            {
                                switch (s)
                                {
                                    case MenuChoice.ProcessMods:
                                        return $"Process Mods";
                                    case MenuChoice.EnableLongPaths: // this should not be a choice in linux || mac
                                        if (AreLongPathsEnabled())
                                            return $"[grey]Long paths are enabled[/]";
                                        else
                                            return $"[red]Long paths are not enabled, may cause problems, select this to enable[/]";
                                    case MenuChoice.PrintError:
                                        if (FailedToLoadMods.Count > 0)
                                            return $"Display Loading Errors ({FailedToLoadMods.Count})";
                                        else
                                            return $"No Errors Found";
                                    case MenuChoice.ChangeProcessorCount:
                                        return $"Change amount of processors to use (currently using {ProcessorCountToUse} cores)";
                                    case MenuChoice.ClearMetadata:
                                        return $"[gray]Purge metadata[/]";
                                    case MenuChoice.ClearCache:
                                        return $"[DarkRed_1]Clear texture cache[/]";
                                    default:
                                        return s.ToString();
                                }
                            })
                        );

                switch (option)
                {
                    case MenuChoice.EditMods:
                        SelectionHandler.DisplayEnabledModsHandler();
                        break;
                    case MenuChoice.ProcessMods:
                        if (ModHandler.DisplayConfirmation())
                            ModHandler.HandleMods();
                        break;
                    case MenuChoice.EnableLongPaths: // this should not be in the selection for linux || mac
                        if (AreLongPathsEnabled())
                            Console.WriteLine("Long paths are already enabled");
                        else
                            EnableLongPathsViaPowerShell();
                        Console.ReadKey();
                        break;
                    case MenuChoice.ChangeProcessorCount:
                        int[] threadChoices = Enumerable.Range(1, Environment.ProcessorCount).ToArray();

                        int selectedThreads = AnsiConsole.Prompt(
                            new SelectionPrompt<int>()
                                .Title("Select task count for [green]BC7 encoding[/]:")
                                .PageSize(10).WrapAround()
                                .AddChoices(threadChoices));

                        ProcessorCountToUse = selectedThreads;
                        break;
                    case MenuChoice.ClearMetadata:
                        AnsiConsole.MarkupLine("This will [red]delete[/] your metadata files, you will need to re generate them using [blue]Process Mods[/].\n\nThis is generally needed when you change mod folder names or if the cache location changes.\n");

                        if (AnsiConsole.Confirm("Are you sure you want to [red]purge[/] the metadata?", false))
                        {
                            AnsiConsole.Status()
                            .Start("Deleting cache files...", ctx =>
                            {
                                foreach (ModInfo mod in ValidMods)
                                {
                                    string cachePath = Path.Combine(CacheDir.FullName, mod.Dir.Name, DdsMetadataFileName);

                                    if (!File.Exists(cachePath))
                                        continue;

                                    ctx.Status($"Deleting: {Markup.Escape(cachePath)}");

                                    File.Delete(cachePath);
                                }
                            });
                            AnsiConsole.MarkupLine("[green]Cache cleared successfully![/]\nBe sure to run Process Mods again for cache to be regenerated.");
                        }
                        Console.ReadKey();
                        break;
                    case MenuChoice.ClearCache:
                        AnsiConsole.MarkupLine("This will [red]delete[/] your cached texture files, you will need to re generate them using [blue]Process Mods[/].\n\nThis should only be necessary if every texture thats generated needs to be regenerated.\n");

                        string confirmationText = "DELETE";

                        string res = AnsiConsole.Ask<string>($"To purge cached textures, please type [red]'{confirmationText}'[/] to confirm (anything else to quit):");

                        if (confirmationText == res)
                        {
                            AnsiConsole.MarkupLine($"Deleting: {CacheDir.FullName}");
                            CacheDir.Delete(true);
                            CacheDir.Create();
                            AnsiConsole.MarkupLine("[green]Cache cleared successfully![/]\nBe sure to run Process Mods again to  regenerate textures.");
                        }
                        else
                            AnsiConsole.MarkupLine("Press any key to return back to menu.");
                        Console.ReadKey();
                        break;
                    case MenuChoice.PrintDebug:
                        PrintDirs();
                        Console.WriteLine();
#if WINDOWS || DEBUG
                        AnsiConsole.MarkupLine($"[cyan]{nameof(AreLongPathsEnabled),-20}[/] {AreLongPathsEnabled()}");
#endif
                        Console.ReadKey();
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
        {
            EditMods,
            ProcessMods,
            EnableLongPaths,
            ChangeProcessorCount,
            ClearMetadata,
            ClearCache,
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
                //Author = "Alex",
                //Description = "The Game",
                //GameVersion = string.Empty,
                //Jars = [],
                Dir = StarsectorCoreDir,
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
                    keySelector = x => Path.Combine(StarsectorCoreDir.FullName, x.RelativeImagePath);
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
                $"[cyan]{nameof(StarsectorCoreDir),-padding}[/] {StarsectorCoreDir}\n" +
                $"[cyan]{nameof(CacheDir),-padding}[/] {CacheDir}"
            );
        }
        private static void PrintErroredMods()
        {
            if (FailedToLoadMods.Count == 0)
            {
                AnsiConsole.MarkupLine("[blue]No errored mods found.[/]");
                Console.ReadKey();
                return;
            }

            var table = new Table();
            table.Border(TableBorder.Rounded);
            table.AddColumn("[yellow]Mod Directory[/]");
            table.AddColumn("[red]Error Type[/]");

            foreach (var mod in FailedToLoadMods)
            {
                string errorTypeName = mod.LoadErrorException?.GetType().Name ?? "Unknown Error";
                table.AddRow(Markup.Escape(mod.Dir.FullName), $"[red]{Markup.Escape(errorTypeName)}[/]");
            }

            AnsiConsole.Write(table);
            AnsiConsole.WriteLine();

            while (true)
            {
                var choices = FailedToLoadMods.Select(m => m.Dir.Name).ToList();
                choices.Add("[green]Exit Inspector[/]");

                var selection = AnsiConsole.Prompt(
                    new SelectionPrompt<string>()
                        .Title("[bold cyan]Select a failed mod to inspect details (or exit):[/]")
                        .PageSize(10)
                        .AddChoices(choices));

                if (selection == "[green]Exit Inspector[/]")
                {
                    break;
                }

                // Find the selected mod
                var selectedMod = FailedToLoadMods.First(m => m.Dir.Name == selection);

                // Display details inside a styled panel
                AnsiConsole.Clear();

                AnsiConsole.MarkupLine($"[bold red]Exception: {Markup.Escape(selectedMod.Dir.Name)}[/]");

                string errorMessage = selectedMod.LoadErrorException?.ToString() ?? "No exception details available.";
                AnsiConsole.MarkupLine(Markup.Escape(errorMessage));

                AnsiConsole.WriteLine();

                // Display JSON content as plain text
                AnsiConsole.MarkupLine("[bold yellow]JsonContent:[/]");

                if (!string.IsNullOrWhiteSpace(selectedMod.JsonContent))
                {
                    AnsiConsole.MarkupLine(Markup.Escape(selectedMod.JsonContent));
                }
                else
                {
                    AnsiConsole.MarkupLine("[grey]No JsonContent available for this mod.[/]");
                }

                AnsiConsole.WriteLine();
                AnsiConsole.MarkupLine("[dim]Press any key to return to the list...[/]");
                Console.ReadKey(true);
                AnsiConsole.Clear();

                // Re-display the summary table for context
                AnsiConsole.Write(table);
                AnsiConsole.WriteLine();
            }
        }
        #endregion
    }
}