using DDSCreator.Model;
using DDSCreator.OpenGL;
using DDSCreator.Resources;
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
                    return Strings.MenuChoice_ProcessMods;
                case MenuChoice.EnableLongPaths: // this should not be a choice in linux
                    if (AreLongPathsEnabled())
                        return Strings.MenuChoice_EnableLongPaths_Enabled;
                    else
                        return Strings.MenuChoice_EnableLongPaths_Disabled;
                case MenuChoice.EnableNativeCrashLogging: // this should not be a choice in linux
                    if (IsNativeCrashLoggingEnabled())
                        return Strings.MenuChoice_EnableNativeCrashLogging_Enabled;
                    else
                        return Strings.MenuChoice_EnableNativeCrashLogging_Disabled;
                case MenuChoice.PrintError:
                    if (FailedToLoadMods.Count > 0)
                        return string.Format(Strings.MenuChoice_PrintError_WithErrors, FailedToLoadMods.Count);
                    else
                        return Strings.MenuChoice_PrintError_NoErrors;
                case MenuChoice.ChangeFileParallelCount:
                    return string.Format(Strings.MenuChoice_ChangeFileParallelCount, ConcurrentFileLimit);
                case MenuChoice.ChangeDDSLineParallelCount:
                    return string.Format(Strings.MenuChoice_ChangeDDSLineParallelCount, TextureTaskCount);
                case MenuChoice.ChangeCompressionQuality:
                    return string.Format(Strings.MenuChoice_ChangeCompressionQuality, CurrentCompressionPreset);
                case MenuChoice.ClearMetadata:
                    return Strings.MenuChoice_ClearMetadata;
                case MenuChoice.ClearCache:
                    return Strings.MenuChoice_ClearCache;
                case MenuChoice.CompactCache:
                    return Strings.MenuChoice_CompactCache;

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
                Console.WriteLine(Strings.MenuChoice_Handle_LongPaths_Enabled);
            else
                EnableLongPathsViaPowerShell();

            Console.WriteLine(Strings.General_PressAnyKeyToGoBackToMenu);
            Console.ReadKey();
        }

        private static void HandleNativeCrashLoggingChoice()
        {
            if (IsNativeCrashLoggingEnabled())
                Console.WriteLine(Strings.MenuChoice_Handle_NativeCrashLogging_Enabled);
            else
                EnableNativeCrashLogging();

            Console.WriteLine(Strings.General_PressAnyKeyToGoBackToMenu);

            Console.ReadKey();
        }

        private static void HandleFileParallelCountChoice()
        {
            int[] threadChoices = Enumerable.Range(1, Environment.ProcessorCount).ToArray();

            int selected = AnsiConsole.Prompt(
                new SelectionPrompt<int>()
                    .Title(Strings.MenuChoice_Handle_ChangeFileParallelCount_Title)
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
                    .Title(Strings.MenuChoice_Handle_ChangeDDSLineParallelCount)
                    .PageSize(10).WrapAround()
                    .AddChoices(threadChoices));

            TextureTaskCount = selected;
        }

        private static void HandleCompressionQualityChoice()
        {
            CompressionPreset compQualityRes = AnsiConsole.Prompt(
                new SelectionPrompt<CompressionPreset>()
                    .Title(Strings.MenuChoice_Handle_ChangeCompressionQuality_Title)
                    .PageSize(10)
                    .WrapAround()
                    .AddChoices(Enum.GetValues<CompressionPreset>())
                    .UseConverter(s =>
                    {
                        switch (s)
                        {
                            case CompressionPreset.Slow:
                                return Strings.MenuChoice_Handle_ChangeCompressionQuality_Slow;
                            case CompressionPreset.Default:
                                return Strings.MenuChoice_Handle_ChangeCompressionQuality_Default;
                            case CompressionPreset.Fast:
                                return Strings.MenuChoice_Handle_ChangeCompressionQuality_Fast;
                            case CompressionPreset.Faster:
                                return Strings.MenuChoice_Handle_ChangeCompressionQuality_Faster;
                            case CompressionPreset.Fastest:
                                return Strings.MenuChoice_Handle_ChangeCompressionQuality_Fastest;
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
            AnsiConsole.MarkupLine(Strings.MenuChoice_Handle_ClearMetadata_Warning);

            if (AnsiConsole.Confirm(Strings.MenuChoice_Handle_ClearMetadata_Confirmation, false))
            {
                AnsiConsole.Status()
                .Start(Strings.MenuChoice_Handle_ClearMetadata_Status_Title, ctx =>
                {
                    foreach (ModInfo mod in ValidMods)
                    {
                        string cachePath = Path.Combine(CacheDir.FullName, mod.Dir.Name, DdsMetadataFileName);

                        if (!File.Exists(cachePath))
                            continue;

                        ctx.Status(string.Format(Strings.MenuChoice_Handle_ClearMetadata_Status_Deleting, Markup.Escape(cachePath)));

                        File.Delete(cachePath);
                    }
                    UpdateMetadataCache();

                });
                AnsiConsole.MarkupLine(Strings.MenuChoice_Handle_ClearMetadata_Success);
            }
            Console.ReadKey();
        }

        private static void HandleClearCache()
        {
            AnsiConsole.MarkupLine(Strings.MenuChoice_Handle_ClearCache_Warning);

            string confirmationText = Strings.MenuChoice_Handle_ClearCache_ConfirmationText;
            string res = AnsiConsole.Ask<string>(string.Format(Strings.MenuChoice_Handle_ClearCache_Confirmation, confirmationText));

            if (confirmationText == res)
            {
                AnsiConsole.MarkupLine(string.Format(Strings.MenuChoice_Handle_ClearCache_Deleting, CacheDir.FullName));
                CacheDir.Delete(true);
                CacheDir.Create();
                AnsiConsole.MarkupLine(Strings.MenuChoice_Handle_ClearCache_Success);
            }
            else
            {
                AnsiConsole.MarkupLine(Strings.General_PressAnyKeyToGoBackToMenu);
            }

            Console.ReadKey();
        }

        private static void HandleCompactCache()
        {
            AnsiConsole.MarkupLine(Strings.MenuChoice_Handle_CompactCache_Title);

            var choice = AnsiConsole.Prompt(
                new SelectionPrompt<string>()
                    .Title("")
                    .PageSize(5)
                    .AddChoices(
                    [
                        Strings.MenuChoice_Handle_CompactCache_CompressCache,
                        Strings.MenuChoice_Handle_CompactCache_DecompressCache,
                        Strings.MenuChoice_Handle_CompactCache_CacheCancel
                    ]));

            if (choice.Contains(Strings.MenuChoice_Handle_CompactCache_CacheCancel_Contains, StringComparison.OrdinalIgnoreCase))
                return;

            bool isCompressing = choice.Contains(Strings.MenuChoice_Handle_CompactCache_CompressCache_Contains, StringComparison.OrdinalIgnoreCase) && !choice.Contains(Strings.MenuChoice_Handle_CompactCache_DecompressCache_Contains, StringComparison.OrdinalIgnoreCase);
            string actionVerb = isCompressing ? Strings.MenuChoice_Handle_CompactCache_Verb_Compressing : Strings.MenuChoice_Handle_CompactCache_Verb_Decompressing;
            string flag = isCompressing ? "/c" : "/u";

            RunCompactProcess(flag, actionVerb);

            Console.ReadKey();
        }

        private static void RunCompactProcess(string flag, string actionVerb)
        {
            var allOutputLines = new List<string>();

            AnsiConsole.Status()
                .Start(string.Format(Strings.MenuChoice_Handle_CompactCache_Run_Status_Title, actionVerb), ctx =>
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
                        AnsiConsole.MarkupLine(string.Format(Strings.MenuChoice_Handle_CompactCache_Run_Status_Fail, Markup.Escape(ex.Message)));
                    }
                });

            AnsiConsole.MarkupLine(Strings.MenuChoice_Handle_CompactCache_Run_Status_Success);

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

#if WINDOWS
            Console.WriteLine();
            AnsiConsole.MarkupLine($"[cyan]{nameof(AreLongPathsEnabled),-20}[/] {AreLongPathsEnabled()}");
#endif


            Console.ReadKey();
        }

        private static void HandleLogDebug()
        {
            OpenOGLContextIfClosed();
            DumpOpenGL.SaveDebugLog();
            Console.WriteLine(string.Format(Strings.Log_DebugValuesSaved, DebugLogPath.FullName));
            Console.ReadKey();
        }

        #endregion
    }
}
