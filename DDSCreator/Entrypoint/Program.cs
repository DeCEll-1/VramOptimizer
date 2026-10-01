global using static DDSCreator.Consts;
global using static DDSCreator.Entrypoint.Program;
global using static DDSCreator.Misc;
global using OpenTK.Graphics.OpenGL;
using CommandLine;
using SharpShaders;
using Spectre.Console;
// TODO: https://github.com/copilot/share/822451b6-4200-80d2-b102-140604482053
namespace DDSCreator.Entrypoint
{
    public partial class Program
    {
        static void Main(string[] args)
        {
            Parser.Default.ParseArguments<Options>(args)
                .WithParsed<Options>(o =>
                {
                    Options = o;

                    if (Options.TriggerNativeCrash.ExecuteIf(Misc.TriggerNativeCrash))
                        return;


                    Run();
                });

        }

        static void SaveException(Exception ex)
        {
            string logPath = Path.Combine(AppContext.BaseDirectory, "err.log");
            File.AppendAllText(logPath, ex.ToString());
        }

        static void Run()
        {
            try { Console.Title = Consts.Version; }
            catch (Exception) { Console.Title = "null"; }
            AppDomain.CurrentDomain.UnhandledException += (sender, args) =>
            {
                var ex = (Exception)args.ExceptionObject;
                SaveException(ex);
            };

            UpdateEnabledMods();
            UpdateMetadataCache();
            UpdateValidMods();

            List<MenuChoice> choices = Enum.GetValues<MenuChoice>().ToList();
#if LINUX
            choices.Remove(Program.MenuChoice.EnableLongPaths);
            choices.Remove(Program.MenuChoice.CompactCache);
            choices.Remove(Program.MenuChoice.EnableNativeCrashLogging);
#endif
#if WINDOWS || DEBUG
            // remember to add exception to linux when adding stuff to here
            if (AreLongPathsEnabled())
                choices.Remove(MenuChoice.EnableLongPaths);
            if (IsNativeCrashLoggingEnabled())
                choices.Remove(MenuChoice.EnableNativeCrashLogging);
#endif

            while (true)
            {
                MenuChoice option = AnsiConsole.Prompt(
                        new SelectionPrompt<MenuChoice>()
                            .Title("What would you like to do?")
                            .PageSize(20)
                            .WrapAround()
                            .AddChoices(choices)
                            .UseConverter(MyMenuConverter)
                        );
                if (option == MenuChoice.Quit)
                    goto quit;
                HandleMenuChoice(option);
                Console.Clear();
            }

        quit:;
            SharpS.CloseOGLContext();
        }

    }
}