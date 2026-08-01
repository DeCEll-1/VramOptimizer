using DDSCreator.Model;
using Spectre.Console;

namespace DDSCreator
{
    public class SelectionHandler
    {
        public static void DisplayEnabledModsHandler()
        {
            while (true)
            {
                Console.Clear();
                Console.SetCursorPosition(0, 9);
                Console.WriteLine("  Enabled mods:");
                foreach (ModInfo? mod in ValidMods.Where(s => s.ShouldProcess))
                {
                    AnsiConsole.MarkupLine($"  [white]{Markup.Escape(mod.Name)}[/]");
                }
                Console.SetCursorPosition(0, 0);


                string choice = AnsiConsole.Prompt(
                    new SelectionPrompt<string>()
                        .Title("Configure which mods to enable:")
                        .WrapAround()
                        .PageSize(5)
                        .AddChoices(["Select all", "Deselect all", "Revert to default", "Edit manually", "Quit"])
                );

                switch (choice)
                {
                    case "Select all":
                        ValidMods.ForEach(m => m.ShouldProcess = true);
                        break;

                    case "Deselect all":
                        ValidMods.ForEach(m => m.ShouldProcess = false);
                        break;

                    case "Revert to default":
                        // Reset to whatever criteria defines your default state (e.g., matching EnabledMods)
                        ValidMods.ForEach(m => m.ShouldProcess = EnabledMods.Contains(m.ID));
                        ValidMods.First(s => s.ID == "starsector-core").ShouldProcess = true;
                        break;

                    case "Edit manually":
                        var prompt = new MultiSelectionPrompt<ModInfo>()
                            .Title("Configure mods to process:")
                            .PageSize(Console.BufferHeight - 4)
                            .WrapAround().Required(false)
                            .MoreChoicesText("[grey](Move up and down to reveal more mods)[/]")
                            .InstructionsText("[grey](Space to toggle, Enter to accept)[/]")
                            .AddChoices(ValidMods)
                            .UseConverter(mod => $"[white]{Markup.Escape(mod.Name)}[/]");

                        // Pre-select based on current state
                        foreach (var mod in ValidMods.Where(m => m.ShouldProcess))
                        {
                            prompt.Select(mod);
                        }

                        var selectedMods = AnsiConsole.Prompt(prompt);

                        // Apply manual choices
                        foreach (var mod in ValidMods)
                        {
                            mod.ShouldProcess = selectedMods.Any(k => k.ID == mod.ID);
                        }
                        break;
                    case "Quit":
                        goto quit;
                    default: break;
                }

            }
        quit:;
        }


    }
}
