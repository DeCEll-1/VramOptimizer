using OpenTK.Graphics.OpenGL;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DDSCreator.OpenGL
{
    public partial class DumpOpenGL
    {
        public class CapabilityResult
        {
            public string Name { get; set; }
            public string Type { get; set; }
            public string Value { get; set; }
            public bool Success { get; set; }
        }
        public static List<CapabilityResult> DumpAllGPUCapabilities()
        {
            var names = Enum.GetValues(typeof(GetPName)).Cast<GetPName>().Select(s => s.ToString()).ToArray();
            var ids = Enum.GetValues(typeof(GetPName)).Cast<GetPName>().Select(s => (int)s).ToArray();

            var results = new List<CapabilityResult>();

            for (int i = 0; i < ids.Length; i++)
            {
                int id;
                try
                { id = ids[i]; }
                catch (Exception)
                { continue; }
                string name = names[i];

                if (name == null) continue;
                string type = "Unknown";
                string valStr = "N/A";
                bool success = true;

                try
                {
                    //////////////////////////////////////////////////
                    while (GL.GetError() != ErrorCode.NoError) ;
                    float[] floatBuffer = new float[4];
                    GL.GetFloat((GetPName)id, floatBuffer);
                    if (GL.GetError() == ErrorCode.NoError)
                    {
                        results.Add(new CapabilityResult
                        {
                            Name = name,
                            Type = "float",
                            Value = floatBuffer[1] == 0 && floatBuffer[2] == 0 && floatBuffer[3] == 0
                                    ? floatBuffer[0].ToString()
                                    : string.Join(", ", floatBuffer),
                            Success = true
                        });
                        continue;
                    }

                    //////////////////////////////////////////////////
                    while (GL.GetError() != ErrorCode.NoError) ;
                    GL.GetBoolean((GetPName)id, out bool boolVal);
                    if (GL.GetError() == ErrorCode.NoError)
                    {
                        results.Add(new CapabilityResult
                        {
                            Name = name,
                            Type = "bool",
                            Value = boolVal.ToString(),
                            Success = true
                        });
                        continue;
                    }

                    //////////////////////////////////////////////////
                    while (GL.GetError() != ErrorCode.NoError) ;
                    int[] buffer = new int[4];
                    GL.GetInteger((GetPName)id, buffer);
                    if (GL.GetError() == ErrorCode.NoError)
                    {
                        results.Add(new CapabilityResult
                        {
                            Name = name,
                            Type = "int",
                            Value = string.Join(", ", buffer),
                            Success = true
                        });
                        continue;
                    }


                    success = false;
                }
                catch
                {
                    success = false;
                }

                if (!success)
                {
                    results.Add(new CapabilityResult { Name = name, Type = type, Value = "Unsupported/Error", Success = false });
                }
            }

            return results;
        }
        public static List<string> GetGPUExtensions()
        {
            List<string> extensions = new List<string>();

            int numExtensions = GL.GetInteger(GetPName.NumExtensions);

            for (uint i = 0; i < numExtensions; i++)
            {
                string extension = GL.GetString(StringNameIndexed.Extensions, i);
                if (!string.IsNullOrEmpty(extension))
                {
                    extensions.Add(extension);
                }
            }

            return extensions;
        }
        private static Dictionary<string, string> GetGPUStrings()
        {
            var results = new Dictionary<string, string>();

            foreach (StringName nameEnum in Enum.GetValues(typeof(StringName)))
            {
                if (nameEnum == StringName.Extensions)
                    continue;

                while (GL.GetError() != ErrorCode.NoError) ;

                try
                {
                    string value = GL.GetString(nameEnum);

                    if (GL.GetError() == ErrorCode.NoError && !string.IsNullOrEmpty(value))
                    {
                        results.Add(nameEnum.ToString(), value);
                    }
                    else
                    {
                        results.Add(nameEnum.ToString(), "Unsupported/Error");
                    }
                }
                catch
                {
                    results.Add(nameEnum.ToString(), "Unsupported/Error");
                }
            }

            return results;
        }
        public static List<string> DumpAllFormats()
        {
            var resultList = new List<string>();

            // 1. Define the list of major internal formats you want to probe
            SizedInternalFormat[] formatsToTest =
            [
                // Standard RGBA / RGB
                SizedInternalFormat.Rgba8, SizedInternalFormat.Rgba16f, SizedInternalFormat.Rgba32f, SizedInternalFormat.Rgb8,  SizedInternalFormat.Srgb8Alpha8,
                // BC / S3TC / DXT (BC1 - BC3)
                SizedInternalFormat.CompressedRgbS3tcDxt1Ext, SizedInternalFormat.CompressedRgbaS3tcDxt1Ext,
                SizedInternalFormat.CompressedRgbaS3tcDxt3Ext, SizedInternalFormat.CompressedRgbaS3tcDxt5Ext,
                // RGTC (BC4 - BC5)
                SizedInternalFormat.CompressedRedRgtc1, SizedInternalFormat.CompressedSignedRedRgtc1,
                SizedInternalFormat.CompressedRgRgtc2, SizedInternalFormat.CompressedSignedRgRgtc2,
                // BPTC (BC6H - BC7)
                SizedInternalFormat.CompressedRgbBptcUnsignedFloat, SizedInternalFormat.CompressedRgbBptcSignedFloat,
                SizedInternalFormat.CompressedRgbaBptcUnorm, SizedInternalFormat.CompressedSrgbAlphaBptcUnorm
            ];

            InternalFormatParameter[] allParameters = ((InternalFormatParameter[])Enum.GetValues(typeof(InternalFormatParameter)));

            // Define column padding width (e.g., 22 characters per column)
            int colWidth = allParameters.Aggregate(0, (acc, next) => Math.Max(acc, next.ToString().Length));

            // Build the header row with padding
            var headerCols = new List<string> { Pad("Format", colWidth) };
            foreach (var param in allParameters)
            {
                headerCols.Add(Pad(param.ToString(), colWidth));
            }
            resultList.Add(string.Join(" | ", headerCols));
            resultList.Add(new string('-', headerCols.Count * (colWidth + 3)));

            // Outer loop: Formats as rows
            foreach (var format in formatsToTest)
            {
                string formatName = GetPrimaryEnumName((All)format);
                var rowValues = new List<string> { Pad(formatName, colWidth) };

                // Inner loop: Parameters as columns
                foreach (var param in allParameters)
                {
                    while (GL.GetError() != ErrorCode.NoError) ;

                    if (param == InternalFormatParameter.Samples)
                    {
                        int[] countBuffer = [-1];
                        GL.GetInternalformat(ImageTarget.Texture2D, format, (InternalFormatParameter)All.NumSampleCounts, 1, countBuffer);

                        if (GL.GetError() == ErrorCode.NoError && countBuffer[0] > 0)
                        {
                            int[] sampleBuffer = new int[countBuffer[0]];
                            GL.GetInternalformat(ImageTarget.Texture2D, format, param, sampleBuffer.Length, sampleBuffer);

                            if (GL.GetError() == ErrorCode.NoError)
                            {
                                rowValues.Add(Pad(string.Join(", ", sampleBuffer), colWidth));
                                continue;
                            }
                        }

                        rowValues.Add(Pad("N/A", colWidth));
                        continue;
                    }

                    int[] buffer = [-1];
                    GL.GetInternalformat(ImageTarget.Texture2D, format, param, 1, buffer);

                    if (GL.GetError() == ErrorCode.NoError)
                    {
                        int formatValue = buffer[0];
                        rowValues.Add(Pad(FormatParameterValue(param, formatValue), colWidth));
                    }
                    else
                    {
                        rowValues.Add(Pad("N/A", colWidth));
                    }
                }

                resultList.Add(string.Join(" | ", rowValues));
            }

            return resultList;
        }
        private static string Pad(string text, int width)
        {
            if (string.IsNullOrEmpty(text))
                return new string(' ', width);

            if (text.Length > width)
                return text.Substring(0, width - 2) + ".."; // Truncate cleanly if too long

            return text.PadRight(width);
        }

        private static string GetPrimaryEnumName(All formatValue)
        {
            int intValue = (int)formatValue;
            var matchingField = typeof(All)
                .GetFields(System.Reflection.BindingFlags.Public | System.Reflection.BindingFlags.Static)
                .FirstOrDefault(f => (int)f.GetValue(null) == intValue);

            return matchingField?.Name ?? formatValue.ToString();
        }
        private static string FormatParameterValue(InternalFormatParameter param, int rawValue)
        {
            if (rawValue == -999) return "N/A";

            if (!formatParamaterTypePairings.TryGetValue(param, out Type targetType))
            {
                return rawValue.ToString();
            }

            try
            {
                if (targetType == typeof(bool))
                {
                    return rawValue == 1 ? "Yes" : (rawValue == 0 ? "No" : rawValue.ToString());
                }
                else if (targetType == typeof(FramebufferStatus))
                {
                    if (rawValue == 0x82B7) return "Full Support";
                    if (rawValue == 0x82B8) return "Caveat Support";
                    if (rawValue == 0) return "No";
                    if (rawValue == 1) return "Yes";
                    return Enum.IsDefined(targetType, rawValue) ? Enum.GetName(targetType, rawValue)! : rawValue.ToString();
                }
                else if (targetType == typeof(SizedInternalFormat))
                {
                    return GetPrimaryEnumName((All)rawValue);
                }
                else if (targetType.IsEnum)
                {
                    return Enum.IsDefined(targetType, rawValue) ? GetPrimaryEnumName((All)rawValue) : rawValue.ToString();
                }
                else if (targetType == typeof(Int32))
                {
                    if (rawValue <= 0 && (param == InternalFormatParameter.MaxWidth ||
                                          param == InternalFormatParameter.MaxHeight ||
                                          param == InternalFormatParameter.MaxDepth ||
                                          param == InternalFormatParameter.MaxLayers))
                    {
                        return "None";
                    }
                    return rawValue.ToString();
                }
            }
            catch
            {
                // Fallback if formatting fails
            }

            return rawValue.ToString();
        }

        public static void SaveDebugLog()
        {
            List<CapabilityResult> capabilities = DumpAllGPUCapabilities().OrderBy(s => s.Name).ToList();
            List<string> extensions = GetGPUExtensions();
            Dictionary<string, string> strings = GetGPUStrings();
            List<string> formats = DumpAllFormats();

            //capabilities = capabilities.FindAll(s => s.Success);
            int capabilitiesMaxName = capabilities.Aggregate((longest, next) => next.Name.Length > longest.Name.Length ? next : longest).Name.Length;
            int extensionsMaxName = extensions.Aggregate((longest, next) => next.Length > longest.Length ? next : longest).Length;
            int stringsMaxName = strings.Aggregate((longest, next) => next.Key.Length > longest.Key.Length ? next : longest).Key.Length;

            int maxNameLenth = Math.Max(Math.Max(capabilitiesMaxName, extensionsMaxName), stringsMaxName);


            List<string> capabilitiesText = capabilities.Select(s => $"{s.Name.PadRight(maxNameLenth+ 2)}:  {s.Value}").ToList();

            List<string> stringsText = strings.Select(s => $"{s.Key.PadRight(maxNameLenth+ 2)}:  {s.Value}").ToList();

            const int pathPadding = 17 + 3;

            List<string> paths = [
                $"{nameof(AppDir),-pathPadding}:  {AppDir}",
                $"{nameof(ModDir),-pathPadding}:  {ModDir}",
                $"{nameof(ModsDir),-pathPadding}:  {ModsDir}",
                $"{nameof(GameDir),-pathPadding}:  {GameDir}",
                $"{nameof(StarsectorCoreDir),-pathPadding}:  {StarsectorCoreDir}",
                $"{nameof(CacheDir),-pathPadding}:  {CacheDir}"
                ];


            List<string> text = [
                "PATHS".PadRight(pathPadding, '/'),
                ..paths,
                "GPU CAPABILITIES".PadRight(maxNameLenth, '/'),
                .. capabilitiesText,
                "EXTENSIONS".PadRight(maxNameLenth, '/'),
                .. extensions,
                "FORMATS".PadRight(maxNameLenth, '/'),
                .. formats,
                "STRINGS".PadRight(maxNameLenth, '/'),
                ..stringsText
                ];

            File.WriteAllText(DebugLogPath.FullName, string.Join(Environment.NewLine, text));
        }



    }
}
