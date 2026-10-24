using CommandLine;

namespace DDSCreator.Entrypoint
{
    public class Options
    {
        [Option("trigger-native-crash", Default = false, HelpText = "Causes a native crash, to test if memory dumping on windows works")]
        public bool TriggerNativeCrash { get; set; }
    }
}
