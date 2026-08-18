using Microsoft.Win32;
using System.Diagnostics;

namespace DDSCreator
{
    public static class Misc
    {


        private static readonly string[] SizeUnits = { "B", "KB", "MB", "GB", "TB", "PB" };
        // if you have a texture thats pb size ur lowky cooked gng
        public static string FormatBytes(long bytes)
        {
            if (bytes <= 0)
                return "0 B";

            int order = System.Numerics.BitOperations.Log2((ulong)bytes) / 10;

            double len = bytes / Math.Pow(1024, order);

            return $"{len:0.##} {SizeUnits[order]}";
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

#pragma warning disable CA1416 // These are unreachable on non-windows machines
        public static bool AreLongPathsEnabled()
        {
            const string registryKeyPath = @"SYSTEM\CurrentControlSet\Control\FileSystem";
            const string valueName = "LongPathsEnabled";

            try
            {
                using (var key = Registry.CurrentUser.OpenSubKey(registryKeyPath))
                {
                    if (key != null)
                    {
                        object value = key.GetValue(valueName);
                        if (value is int intValue && intValue == 1)
                        {
                            return true;
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Could not read HKCU registry key: {ex.Message}");
            }

            try
            {
                using (var key = Registry.LocalMachine.OpenSubKey(registryKeyPath))
                {
                    if (key != null)
                    {
                        object value = key.GetValue(valueName);
                        if (value is int intValue && intValue == 1)
                        {
                            return true;
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Could not read HKLM registry key: {ex.Message}");
            }

            return false;
        }

        public static bool EnableLongPathsViaPowerShell()
        {
            try
            {
                // Targeting HKLM requires admin rights
                string psCommand =
                    @"Set-ItemProperty -Path 'HKLM:\SYSTEM\CurrentControlSet\Control\FileSystem' -Name 'LongPathsEnabled' -Value 1";

                var startInfo = new ProcessStartInfo
                {
                    FileName = "powershell.exe",
                    // Pass the command safely with encoded text or quotes
                    Arguments = $"-NoProfile -Command \"{psCommand}\"",
                    UseShellExecute = true, // Required to trigger UAC
                    Verb = "runas"          // Triggers the "Run as Administrator" prompt
                };

                using (var process = Process.Start(startInfo))
                {
                    process.WaitForExit();

                    if (process.ExitCode == 0)
                    {
                        Console.WriteLine("Successfully enabled long paths.");
                        return true;
                    }
                    else
                    {
                        Console.WriteLine($"Failed or user cancelled the elevation. Exit code: {process.ExitCode}");
                        return false;
                    }
                }
            }
            catch (System.ComponentModel.Win32Exception)
            {
                // This exception is thrown if the user clicks "No" on the UAC prompt
                Console.WriteLine("The user cancelled the administrator elevation prompt.");
                return false;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"An exception occurred: {ex.Message}");
                return false;
            }
        }
#pragma warning restore CA1416


    }
}
