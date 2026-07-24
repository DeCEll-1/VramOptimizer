using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace DDSCreator;

public class ModInfoJSON
{
    public static string getModID(string modFileName)
    => Regex.Match(File.ReadAllText(Path.Combine(modFileName, "mod_info.json")), @"""id""\s*:\s*""([^""]+)").Groups[1].Value.ToString();
}