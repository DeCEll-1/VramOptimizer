using System.Runtime.InteropServices;

namespace DDSCreator
{
    // BC7 encoding through Intel's ispc_texcomp native library. Roughly 50x faster
    // than the managed BCnEncoder path, which stays as the fallback on platforms
    // where we don't ship a native binary yet (see native/README.md).
    public static partial class NativeBc7Encoder
    {
        [StructLayout(LayoutKind.Sequential)]
        private unsafe struct RgbaSurface
        {
            public byte* Pixels;
            public int Width;
            public int Height;
            public int StrideBytes;
        }

        // matches bc7_enc_settings in ispc_texcomp.h; filled in by GetProfile_*,
        // we never touch the fields ourselves
        [StructLayout(LayoutKind.Sequential)]
        private unsafe struct Bc7Settings
        {
            public fixed byte ModeSelection[4];
            public fixed int RefineIterations[8];
            public byte SkipMode2;
            public int FastSkipThresholdMode1;
            public int FastSkipThresholdMode3;
            public int FastSkipThresholdMode7;
            public int Mode45Channel0;
            public int RefineIterationsChannel;
            public int Channels;
        }

        [DllImport("ispc_texcomp", CallingConvention = CallingConvention.Cdecl)]
        private static extern void GetProfile_alpha_slow(out Bc7Settings settings);

        [DllImport("ispc_texcomp", CallingConvention = CallingConvention.Cdecl)]
        private static extern void GetProfile_alpha_basic(out Bc7Settings settings);

        [DllImport("ispc_texcomp", CallingConvention = CallingConvention.Cdecl)]
        private static extern void GetProfile_alpha_fast(out Bc7Settings settings);

        [DllImport("ispc_texcomp", CallingConvention = CallingConvention.Cdecl)]
        private static extern void GetProfile_alpha_veryfast(out Bc7Settings settings);

        [DllImport("ispc_texcomp", CallingConvention = CallingConvention.Cdecl)]
        private static extern void GetProfile_alpha_ultrafast(out Bc7Settings settings);

        // Method to get settings dynamically based on your preset
        private static void GetSettingsForPreset(CompressionPreset preset, out Bc7Settings settings)
        {
            switch (preset)
            {
                case CompressionPreset.Slow:
                    GetProfile_alpha_slow(out settings);
                    break;
                case CompressionPreset.Default:
                    GetProfile_alpha_basic(out settings); // Default maps safely to basic/balanced
                    break;
                case CompressionPreset.Fast:
                    GetProfile_alpha_fast(out settings);
                    break;
                case CompressionPreset.Faster:
                    GetProfile_alpha_veryfast(out settings);
                    break;
                case CompressionPreset.Fastest:
                    GetProfile_alpha_ultrafast(out settings);
                    break;
                default:
                    GetProfile_alpha_basic(out settings);
                    break;
            }
        }

        [DllImport("ispc_texcomp", CallingConvention = CallingConvention.Cdecl)]
        private static extern unsafe void CompressBlocksBC7(ref RgbaSurface src, byte* dst, ref Bc7Settings settings);

        [DllImport("ispc_texcomp", CallingConvention = CallingConvention.Cdecl)]
        private static extern void ReplicateBorders(ref RgbaSurface dst, ref RgbaSurface src, int x, int y, int bpp);

        private const int BlockSize = 4;
        private const int BytesPerBlock = 16;
        private const int BytesPerPixel = 4;

        // don't bother splitting small images across threads
        private const int MinRowsPerBand = 64;

        public static readonly bool IsAvailable = CheckAvailable();

        private static bool CheckAvailable()
        {
            try
            {
                GetProfile_alpha_basic(out _);
                return true;
            }
            catch (DllNotFoundException)
            {
                // no library was built for this platform
                return false;
            }
            catch (BadImageFormatException)
            {
                // wrong word size, e.g. the 64 bit library next to the 32 bit build
                return false;
            }
        }

        // encodes RGBA pixels to BC7 and returns a complete DDS file (148-byte
        // DX10 header + blocks), same layout the BCnEncoder path produces
        public static unsafe byte[] EncodeToDds(List<byte[]> mipPixelBuffers, int baseWidth, int baseHeight, int taskCount)
        {
            GetSettingsForPreset(Program.CurrentCompressionPreset, out Bc7Settings settings);

            int mipCount = mipPixelBuffers.Count;
            int totalPayloadBytes = 0;

            int[] mipWidths = new int[mipCount];
            int[] mipHeights = new int[mipCount];
            int[] levelPayloadBytes = new int[mipCount];

            for (int i = 0; i < mipCount; i++)
            {
                mipWidths[i] =  GetNextMultipleOf4(baseWidth  >> i);
                mipHeights[i] = GetNextMultipleOf4(baseHeight >> i);

                int blockRows = mipWidths[i] / BlockSize;
                int blockColumns = mipHeights[i] / BlockSize;
                levelPayloadBytes[i] = blockRows * blockColumns * BytesPerBlock;
                totalPayloadBytes += levelPayloadBytes[i];
            }

            byte[] dds = new byte[DdsHeader.Length + totalPayloadBytes];
            WriteDdsHeader(dds, baseWidth, baseHeight, totalPayloadBytes, (uint)mipCount);

            fixed (byte* ddsPtr = dds)
            {
                byte* payloadPtr = ddsPtr + DdsHeader.Length;
                long currentPayloadOffset = 0;

                for (int i = 0; i < mipCount; i++)
                {
                    int pWidth = mipWidths[i];
                    int pHeight = mipHeights[i];
                    int stride = pWidth * BytesPerPixel;

                    byte[] pixels = mipPixelBuffers[i];

                    int blockRows = pHeight / BlockSize;
                    int blockColumns = pWidth / BlockSize;
                    byte* levelDestPtr = payloadPtr + currentPayloadOffset;

                    fixed (byte* pixelPtr = pixels)
                    {
                        int bandCount = Math.Min(Math.Max(1, taskCount), Math.Max(1, pHeight / MinRowsPerBand));

                        if (bandCount <= 1)
                        {
                            var surface = new RgbaSurface { Pixels = pixelPtr, Width = pWidth, Height = pHeight, StrideBytes = stride };
                            CompressBlocksBC7(ref surface, levelDestPtr, ref settings);
                        }
                        else
                        {
                            int blockRowsPerBand = (blockRows + bandCount - 1) / bandCount;
                            byte* bandPixelBase = pixelPtr;
                            byte* bandPayloadBase = levelDestPtr;

                            Parallel.For(0, bandCount, band =>
                            {
                                int firstBlockRow = band * blockRowsPerBand;
                                int bandBlockRows = Math.Min(blockRowsPerBand, blockRows - firstBlockRow);
                                if (bandBlockRows <= 0) return;

                                var bandSettings = settings;
                                var surface = new RgbaSurface
                                {
                                    Pixels = bandPixelBase + (long)firstBlockRow * BlockSize * stride,
                                    Width = pWidth,
                                    Height = bandBlockRows * BlockSize,
                                    StrideBytes = stride,
                                };
                                CompressBlocksBC7(ref surface, bandPayloadBase + (long)firstBlockRow * blockColumns * BytesPerBlock, ref bandSettings);
                            });
                        }
                    }

                    currentPayloadOffset += levelPayloadBytes[i];
                }
            }

            return dds;
        }

        private static class DdsHeader
        {
            // 128-byte standard header + 20-byte DX10 extension; the mod's Java
            // loader skips exactly this many bytes before the block data
            public const int Length = 148;
        }

        private static void WriteDdsHeader(byte[] dds, int width, int height, int payloadBytes, uint mipCount)
        {
            using var stream = new MemoryStream(dds, 0, DdsHeader.Length);
            using var w = new BinaryWriter(stream);

            // DDS Flag definitions indicating which fields in the header are valid
            const uint DDSD_CAPS = 0x1, DDSD_HEIGHT = 0x2, DDSD_WIDTH = 0x4, DDSD_PIXELFORMAT = 0x1000, DDSD_LINEARSIZE = 0x80000, DDSD_MIPMAPCOUNT = 0x20000;
            const uint DDPF_FOURCC = 0x4; // Indicates compressed data format using a FourCC code
            const uint DDSCAPS_TEXTURE = 0x1000; // Required for all textures
            const uint DDSCAPS_COMPLEX = 0x8; // Required for textures with multiple surfaces (like mipmaps)
            const uint DDSCAPS_MIPMAP = 0x400000; // Marks this texture as having mipmaps
            const uint DXGI_FORMAT_BC7_UNORM = 98; // DirectX format identifier for BC7 compression
            const uint D3D10_RESOURCE_DIMENSION_TEXTURE2D = 3; // Specifies a 2D texture resource for the DX10 extension

            // Base capabilities and flags required for a standard texture
            uint caps = DDSCAPS_TEXTURE;
            uint flags = DDSD_CAPS | DDSD_HEIGHT | DDSD_WIDTH | DDSD_PIXELFORMAT | DDSD_LINEARSIZE;

            // Enable complex/mipmap flags and mipmap count header flag if multiple mip levels exist
            if (mipCount > 1)
            {
                caps |= DDSCAPS_COMPLEX | DDSCAPS_MIPMAP;
                flags |= DDSD_MIPMAPCOUNT;
            }

            // --- Standard DDS Header (128 bytes total) ---
            w.Write(0x20534444u); // Magic value "DDS " marking the start of the file
            w.Write(124u); // Size of the DDS header structure itself (always 124)
            w.Write(flags); // Bitwise flags indicating active header fields
            w.Write((uint)height); // Base image height in pixels
            w.Write((uint)width); // Base image width in pixels
            w.Write((uint)payloadBytes); // Total size in bytes of all compressed texture blocks combined
            w.Write(0u); // Depth (unused for 2D textures)
            w.Write(mipCount); // Total number of mipmap levels included
            for (int i = 0; i < 11; i++) w.Write(0u); // Reserved/unused header slots

            // --- Pixel Format Sub-structure (32 bytes) ---
            w.Write(32u); // Size of the pixel format structure (always 32)
            w.Write(DDPF_FOURCC); // Specifies that data uses a FourCC format instead of raw RGBA masks
            w.Write(0x30315844u); // FourCC signature "DX10", pointing parsers to look at the DX10 extension
            for (int i = 0; i < 5; i++) w.Write(0u); // Unused RGB bit counts and masks when using FourCC/DX10

            // --- Surface Capabilities ---
            w.Write(caps); // Capabilities flags (includes texture, complex, and mipmaps if applicable)
            for (int i = 0; i < 4; i++) w.Write(0u); // Reserved/unused caps2, caps3, caps4 fields

            // --- DX10 Extension Header (20 bytes) ---
            w.Write(DXGI_FORMAT_BC7_UNORM); // The precise DXGI pixel format (BC7 block compression)
            w.Write(D3D10_RESOURCE_DIMENSION_TEXTURE2D); // Resource type: standard 2D texture
            w.Write(0u); // Misc flags (e.g., cube maps)
            w.Write(1u); // Texture array size (1 for a single texture map)
            w.Write(0u); // Alpha mode (0 = unknown/not specified)
        }
    }
}

