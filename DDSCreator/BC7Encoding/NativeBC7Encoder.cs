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
        public static unsafe byte[] EncodeToDds(byte[] rgbaPixels, int width, int height, int taskCount)
        {
            GetSettingsForPreset(Program.CurrentCompressionPreset, out Bc7Settings settings);

            // the encoder needs dimensions in whole 4x4 blocks, so images with odd
            // sizes get their edge pixels repeated out to the block boundary
            int paddedWidth = (width + BlockSize - 1) / BlockSize * BlockSize;
            int paddedHeight = (height + BlockSize - 1) / BlockSize * BlockSize;

            byte[] pixels = rgbaPixels;
            if (paddedWidth != width || paddedHeight != height)
                pixels = PadWithEdgePixels(rgbaPixels, width, height, paddedWidth, paddedHeight);

            int blockRows = paddedHeight / BlockSize;
            int blockColumns = paddedWidth / BlockSize;
            int payloadBytes = blockRows * blockColumns * BytesPerBlock;

            byte[] dds = new byte[DdsHeader.Length + payloadBytes];
            WriteDdsHeader(dds, width, height, payloadBytes);

            fixed (byte* pixelPtr = pixels)
            fixed (byte* ddsPtr = dds)
            {
                byte* payloadPtr = ddsPtr + DdsHeader.Length;
                int stride = paddedWidth * BytesPerPixel;

                // one native call encodes on one thread, so big images are split
                // into bands of rows and encoded on several cores at once. taskCount
                // is the core count the user picked in the menu.
                int bandCount = Math.Min(Math.Max(1, taskCount), Math.Max(1, paddedHeight / MinRowsPerBand));

                if (bandCount <= 1)
                {
                    var surface = new RgbaSurface { Pixels = pixelPtr, Width = paddedWidth, Height = paddedHeight, StrideBytes = stride };
                    CompressBlocksBC7(ref surface, payloadPtr, ref settings);
                }
                else
                {
                    int blockRowsPerBand = (blockRows + bandCount - 1) / bandCount;

                    // locals so the parallel lambda doesn't capture fixed pointers directly
                    byte* bandPixelBase = pixelPtr;
                    byte* bandPayloadBase = payloadPtr;

                    Parallel.For(0, bandCount, band =>
                    {
                        int firstBlockRow = band * blockRowsPerBand;
                        int bandBlockRows = Math.Min(blockRowsPerBand, blockRows - firstBlockRow);
                        if (bandBlockRows <= 0)
                            return;

                        var bandSettings = settings;
                        var surface = new RgbaSurface
                        {
                            Pixels = bandPixelBase + (long)firstBlockRow * BlockSize * stride,
                            Width = paddedWidth,
                            Height = bandBlockRows * BlockSize,
                            StrideBytes = stride,
                        };
                        CompressBlocksBC7(ref surface, bandPayloadBase + (long)firstBlockRow * blockColumns * BytesPerBlock, ref bandSettings);
                    });
                }
            }

            return dds;
        }

        private static unsafe byte[] PadWithEdgePixels(byte[] rgbaPixels, int width, int height, int paddedWidth, int paddedHeight)
        {
            byte[] padded = new byte[paddedWidth * paddedHeight * BytesPerPixel];

            fixed (byte* srcPtr = rgbaPixels)
            fixed (byte* dstPtr = padded)
            {
                var src = new RgbaSurface { Pixels = srcPtr, Width = width, Height = height, StrideBytes = width * BytesPerPixel };
                var dst = new RgbaSurface { Pixels = dstPtr, Width = paddedWidth, Height = paddedHeight, StrideBytes = paddedWidth * BytesPerPixel };
                ReplicateBorders(ref dst, ref src, 0, 0, 32);
            }

            return padded;
        }

        private static class DdsHeader
        {
            // 128-byte standard header + 20-byte DX10 extension; the mod's Java
            // loader skips exactly this many bytes before the block data
            public const int Length = 148;
        }

        private static void WriteDdsHeader(byte[] dds, int width, int height, int payloadBytes)
        {
            using var stream = new MemoryStream(dds, 0, DdsHeader.Length);
            using var w = new BinaryWriter(stream);

            const uint DDSD_CAPS = 0x1, DDSD_HEIGHT = 0x2, DDSD_WIDTH = 0x4, DDSD_PIXELFORMAT = 0x1000, DDSD_MIPMAPCOUNT = 0x20000, DDSD_LINEARSIZE = 0x80000;
            const uint DDPF_FOURCC = 0x4;
            const uint DDSCAPS_TEXTURE = 0x1000;
            const uint DXGI_FORMAT_BC7_UNORM = 98;
            const uint D3D10_RESOURCE_DIMENSION_TEXTURE2D = 3;

            w.Write(0x20534444u); // "DDS "
            w.Write(124u); // header struct size
            w.Write(DDSD_CAPS | DDSD_HEIGHT | DDSD_WIDTH | DDSD_PIXELFORMAT | DDSD_MIPMAPCOUNT | DDSD_LINEARSIZE);
            w.Write((uint)height);
            w.Write((uint)width);
            w.Write((uint)payloadBytes);
            w.Write(0u); // depth
            w.Write(1u); // mip count
            for (int i = 0; i < 11; i++)
                w.Write(0u); // reserved

            w.Write(32u); // pixel format struct size
            w.Write(DDPF_FOURCC);
            w.Write(0x30315844u); // "DX10"
            for (int i = 0; i < 5; i++)
                w.Write(0u); // rgb bit counts and masks, unused with a fourCC

            w.Write(DDSCAPS_TEXTURE);
            for (int i = 0; i < 4; i++)
                w.Write(0u); // caps2-4 and reserved

            // DX10 extension
            w.Write(DXGI_FORMAT_BC7_UNORM);
            w.Write(D3D10_RESOURCE_DIMENSION_TEXTURE2D);
            w.Write(0u); // misc flags
            w.Write(1u); // array size
            w.Write(0u); // alpha mode: unknown
        }
    }
}

