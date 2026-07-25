using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace DDSCreator.Model;

public class ModInfo
{
    [JsonProperty("id")]
    public string ID { get; set; } = string.Empty;

    [JsonProperty("name")]
    public string Name { get; set; } = string.Empty;

    [JsonProperty("author")]
    public string Author { get; set; } = string.Empty;

    [JsonProperty("description")]
    public string Description { get; set; } = string.Empty;

    [JsonProperty("gameVersion")]
    public string GameVersion { get; set; } = string.Empty;

    [JsonProperty("jars")]
    public List<string> Jars { get; set; } = [];
    public required DirectoryInfo Dir { get; set; }
    public bool ShouldProcess { get; set; } = false;

    public static ModInfo LoadModInfo(DirectoryInfo modPath)
    {
        string filePath = Path.Combine(modPath.FullName, "mod_info.json");

        string jsonContent = File.ReadAllText(filePath);

        jsonContent = Regex.Replace(jsonContent, "(?:\".*?\"|'.*?')|(#.*)", static s =>
        { // ofcourse lets use #s for comments instead of following any specification
            if (s.Groups[1].Value == string.Empty)
                return s.Value;
            return s.Value.Replace(s.Groups[1].Value, "");
        });

        var zaza = JsonConvert.DeserializeObject<ModInfo>(jsonContent);
        if (zaza != null)
            zaza.Dir = modPath;
        else
            zaza = new() { Dir = modPath };
        return zaza;
    }
}