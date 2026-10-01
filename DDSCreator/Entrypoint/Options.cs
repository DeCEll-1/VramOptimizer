using CommandLine;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DDSCreator.Entrypoint
{
    public class Options
    {
        [Option("trigger-native-crash", Default = false, HelpText = "Causes a native crash, to test if memory dumping on windows works")]
        public bool TriggerNativeCrash { get; set; }
        //
        [Option("create-test-heightmap", Default = false, HelpText = "Tests height map generation by creating a height map of _icon.jpg")]
        public bool TestHeightMapGeneration { get; set; }
        //
        [Option("create-test-normalmap", Default = false, HelpText = "Tests normal map generation by creating a normal map of _icon.jpg")]
        public bool TestNormalMapGeneration { get; set; }
        //
    }
}
