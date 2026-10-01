using DDSCreator.Model;
using DDSCreator.OpenGL;
using ImageMagick;
using Spectre.Console;

namespace DDSCreator.Entrypoint
{
    public partial class Program
    {
        #region menu options
        private static string MyMenuConverter(MenuChoice s)
        {
            switch (s)
            {
                case MenuChoice.ProcessMods:
                    return $"Process Mods";
                case MenuChoice.EnableLongPaths: // this should not be a choice in linux
                    if (AreLongPathsEnabled())
                        return $"[grey]Long paths are enabled[/]";
                    else
                        return $"[red]Long paths are not enabled, may cause problems, select this to enable[/]";
                case MenuChoice.EnableNativeCrashLogging: // this should not be a choice in linux
                    if (IsNativeCrashLoggingEnabled())
                        return $"[grey]Native crash logging is enabled[/]";
                    else
                        return $"[red]Native crash logging is not enabled, please enable it incase the application crashes so I can fix the problems[/]";
                case MenuChoice.PrintError:
                    if (FailedToLoadMods.Count > 0)
                        return $"Display Loading Errors ({FailedToLoadMods.Count})";
                    else
                        return $"No Errors Found";
                case MenuChoice.ChangeFileParallelCount:
                    return $"Change max files processed in parallel (currently: {ConcurrentFileLimit})";
                case MenuChoice.ChangeDDSLineParallelCount:
                    return $"Change max threads per texture encoding (currently: {TextureTaskCount})";
                case MenuChoice.ChangeCompressionQuality:
                    return $"Change the compression quality (Current: {CurrentCompressionPreset})";
                case MenuChoice.ClearMetadata:
                    return $"[gray]Purge metadata[/]";
                case MenuChoice.ClearCache:
                    return $"[DarkRed_1]Purge texture cache[/]";
                case MenuChoice.CompactCache:
                    return $"Compact cache folder";

                default:
                    return s.ToString();
            }
        }

        private static void HandleMenuChoice(MenuChoice option)
        {
            switch (option)
            {
                case MenuChoice.EditMods:
                    SelectionHandler.DisplayEnabledModsHandler();
                    break;

                case MenuChoice.ProcessMods:
                    HandleProcessMods();
                    break;

                case MenuChoice.EnableLongPaths: // this should not be in the selection for linux || mac
                    HandleLongPathsChoice();
                    break;

                case MenuChoice.EnableNativeCrashLogging: // this should not be in the selection for linux || mac
                    HandleNativeCrashLoggingChoice();
                    break;

                case MenuChoice.ChangeFileParallelCount:
                    HandleFileParallelCountChoice();
                    break;

                case MenuChoice.ChangeDDSLineParallelCount:
                    HandleDDSLineParallelCountChoice();
                    break;

                case MenuChoice.ChangeCompressionQuality:
                    HandleCompressionQualityChoice();
                    break;

                case MenuChoice.ClearMetadata:
                    HandleClearMetadata();
                    break;

                case MenuChoice.ClearCache:
                    HandleClearCache();
                    break;

                case MenuChoice.CompactCache:
                    HandleCompactCache();
                    break;

                case MenuChoice.PrintDebug:
                    HandlePrintDebug();
                    break;

                case MenuChoice.LogDebug:
                    HandleLogDebug();
                    break;

                case MenuChoice.PrintError:
                    PrintErroredMods();
                    break;
                default:
                    break;
            }
        }

        private enum MenuChoice
        {
            EditMods,
            ProcessMods,
            EnableLongPaths,
            EnableNativeCrashLogging,
            ChangeFileParallelCount,
            ChangeDDSLineParallelCount,
            ChangeCompressionQuality,
            ClearMetadata,
            ClearCache,
            CompactCache,
            PrintDebug,
            LogDebug,
            PrintError,
            Quit,
        }

        private static void HandleProcessMods()
        {
            if (ModHandler.DisplayConfirmation())
            {
                ModHandler.HandleMods();
                UpdateMetadataCache();
            }
        }

        private static void HandleLongPathsChoice()
        {
            if (AreLongPathsEnabled())
                Console.WriteLine("Long paths are already enabled");
            else
                EnableLongPathsViaPowerShell();

            Console.ReadKey();
        }

        private static void HandleNativeCrashLoggingChoice()
        {
            if (IsNativeCrashLoggingEnabled())
                Console.WriteLine("Native crash logging is already enabled");
            else
                EnableNativeCrashLogging();

            Console.WriteLine("Press any key to continue");

            Console.ReadKey();
        }

        private static void HandleFileParallelCountChoice()
        {
            int[] threadChoices = Enumerable.Range(1, Environment.ProcessorCount).ToArray();

            int selected = AnsiConsole.Prompt(
                new SelectionPrompt<int>()
                    .Title("Select max concurrent files to process for [green]Batch Concurrency[/]:")
                    .PageSize(10).WrapAround()
                    .AddChoices(threadChoices));

            ConcurrentFileLimit = selected;

            ResourceLimits.Thread = (ulong)TextureTaskCount;
        }

        private static void HandleDDSLineParallelCountChoice()
        {
            int[] threadChoices = Enumerable.Range(1, Environment.ProcessorCount).ToArray();

            int selected = AnsiConsole.Prompt(
                new SelectionPrompt<int>()
                    .Title("Select max thread count per texture for [green]BC7 Encoder Threads[/]:")
                    .PageSize(10).WrapAround()
                    .AddChoices(threadChoices));

            TextureTaskCount = selected;

        }

        private static void HandleCompressionQualityChoice()
        {
            CompressionPreset compQualityRes = AnsiConsole.Prompt(
                new SelectionPrompt<CompressionPreset>()
                    .Title("Select compression speed.\nFaster compression may cause more graphical artifacts.")
                    .PageSize(10)
                    .WrapAround()
                    .AddChoices(Enum.GetValues<CompressionPreset>())
                    .UseConverter(s =>
                    {
                        switch (s)
                        {
                            case CompressionPreset.Slow:
                                return "Slow";
                            case CompressionPreset.Default:
                                return "Default";
                            case CompressionPreset.Fast:
                                return "Fast";
                            case CompressionPreset.Faster:
                                return "Faster";
                            case CompressionPreset.Fastest:
                                return "Fastest";
                            default:
                                return "";
                        }
                    }));

            CurrentCompressionPreset = compQualityRes;
        }

        public enum CompressionPreset
        {
            Slow,
            Default,
            Fast,
            Faster,
            Fastest,
        }

        private static void HandleClearMetadata()
        {
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
                    UpdateMetadataCache();

                });
                AnsiConsole.MarkupLine("[green]Cache cleared successfully![/]\nBe sure to run Process Mods again for cache to be regenerated.");
            }
            Console.ReadKey();
        }

        private static void HandleClearCache()
        {
            AnsiConsole.MarkupLine("This will [red]delete[/] your cached texture files, you will need to re generate them using [blue]Process Mods[/].\n\nThis should only be necessary if every texture thats generated needs to be regenerated.\n");

            string confirmationText = "DELETE";
            string res = AnsiConsole.Ask<string>($"To purge cached textures, please type [red]'{confirmationText}'[/] to confirm (anything else to quit):");

            if (confirmationText == res)
            {
                AnsiConsole.MarkupLine($"Deleting: {CacheDir.FullName}");
                CacheDir.Delete(true);
                CacheDir.Create();
                AnsiConsole.MarkupLine("[green]Cache cleared successfully![/]\nBe sure to run Process Mods again to regenerate textures.");
            }
            else
            {
                AnsiConsole.MarkupLine("Press any key to return back to menu.");
            }

            Console.ReadKey();
        }

        private static void HandleCompactCache()
        {
            AnsiConsole.MarkupLine($"Compacting the cache folder will reduce storage usage using Windows Overlay Filter LZX compression.\nIt has no in-game performance change, it [b]only[/] changes disk space usage.\nYou will need to recompress after updating your cache.");

            var choice = AnsiConsole.Prompt(
                new SelectionPrompt<string>()
                    .Title("")
                    .PageSize(5)
                    .AddChoices(new[]
                    {
                        "[green]Compress cache[/]",
                        "[yellow]Decompress cache[/]",
                        "[grey]Cancel[/]"
                    }));

            if (choice.Contains("Cancel", StringComparison.OrdinalIgnoreCase))
                return;

            bool isCompressing = choice.Contains("Compress", StringComparison.OrdinalIgnoreCase) && !choice.Contains("Decompress", StringComparison.OrdinalIgnoreCase);
            string actionVerb = isCompressing ? "Compressing" : "Decompressing";
            string flag = isCompressing ? "/c" : "/u";

            RunCompactProcess(flag, actionVerb);

            Console.ReadKey();
        }

        private static void RunCompactProcess(string flag, string actionVerb)
        {
            var allOutputLines = new List<string>();

            AnsiConsole.Status()
                .Start($"{actionVerb} cache folder (this may take a while)...", ctx =>
                {
                    try
                    {
                        var startInfo = new System.Diagnostics.ProcessStartInfo
                        {
                            FileName = "compact.exe",
                            Arguments = $"{flag} /q /s /i /a /exe:lzx \"{CacheDir.FullName}\\*\"",
                            UseShellExecute = false,
                            CreateNoWindow = true,
                            RedirectStandardOutput = true,
                            RedirectStandardError = true
                        };

                        using var process = new System.Diagnostics.Process { StartInfo = startInfo };

                        process.OutputDataReceived += (sender, e) =>
                        {
                            if (!string.IsNullOrWhiteSpace(e.Data))
                            {
                                string line = e.Data.Trim();
                                lock (allOutputLines)
                                {
                                    allOutputLines.Add(line);
                                }

                                ctx.Status($"[grey]{Markup.Escape(line)}[/]");
                            }
                        };

                        process.Start();
                        process.BeginOutputReadLine();
                        process.WaitForExit();
                    }
                    catch (Exception ex)
                    {
                        AnsiConsole.MarkupLine($"[red]Failed to modify cache compression: {Markup.Escape(ex.Message)}[/]");
                    }
                });

            AnsiConsole.MarkupLine("[green]Cache compression operation finished![/]\n");

            lock (allOutputLines)
            {
                if (allOutputLines.Count >= 5)
                {
                    IEnumerable<string> lastFiveLines = allOutputLines.TakeLast(3);
                    string panelContent = string.Join("\n", lastFiveLines.Select(l => Markup.Escape(l)));

                    AnsiConsole.Write(panelContent);
                }
            }
        }

        private static void HandlePrintDebug()
        {
            PrintDirs();

            Console.WriteLine();
            AnsiConsole.MarkupLine($"[cyan]{nameof(Consts.Version),-20}[/] {Consts.Version}");

            Console.WriteLine();
            AnsiConsole.MarkupLine($"[cyan]{nameof(CurrentCompressionPreset),-20}[/] {CurrentCompressionPreset}");

#if WINDOWS || DEBUG
            Console.WriteLine();
            AnsiConsole.MarkupLine($"[cyan]{nameof(AreLongPathsEnabled),-20}[/] {AreLongPathsEnabled()}");
#endif


            Console.ReadKey();
        }

        private static void HandleLogDebug()
        {
            OpenOGLContextIfClosed();
            DumpOpenGL.SaveDebugLog();
            Console.WriteLine("Debug values has been saved to:\n" + DebugLogPath.FullName);
            Console.ReadKey();
        }

        #endregion
    }
}
