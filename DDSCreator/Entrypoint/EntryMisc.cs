using DDSCreator.Model;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using SharpShaders;
using Spectre.Console;

namespace DDSCreator.Entrypoint
{
    public partial class Program
    {
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
            ExistingMetadataCache.Clear();
            foreach (string? modMetadataPath in CacheDir.GetDirectories().Select(s => Path.Combine(s.FullName, DdsMetadataFileName)))
            {
                if (!File.Exists(modMetadataPath))
                    continue; // metadata does not exist for this specific mod
                string jsonContent = File.ReadAllText(modMetadataPath);
                var existingList = JsonConvert.DeserializeObject<List<FileMetadata>>(jsonContent);
                if (existingList == null)
                    continue; // corrupt

                Func<FileMetadata, string> keySelector;

                if (modMetadataPath.Contains("starsector-core"))
                    keySelector = x => Path.Combine(StarsectorCoreDir.FullName, x.RelativeImagePath);
                else
                    keySelector = x => Path.Combine(ModsDir.FullName, x.ModFolderName, x.RelativeImagePath);

                // convert existing cache + current list into a dictionary, safely handling duplicate keys by taking the last occurrence
                var merged = ExistingMetadataCache
                    .Concat(existingList.Select(x => new KeyValuePair<string, FileMetadata>(keySelector(x), x)));

                ExistingMetadataCache = merged
                    .GroupBy(kvp => kvp.Key, StringComparer.OrdinalIgnoreCase)
                    .ToDictionary(
                        g => g.Key,
                        g => g.Last().Value,
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
                    break;

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
                    AnsiConsole.MarkupLine(Markup.Escape(selectedMod.JsonContent));
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
