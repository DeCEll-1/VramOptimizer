using ImageMagick;

namespace DDSCreator
{
    public class ImageAnalyzer
    {
        private float _totalPixels = 0.0F;
        private float _sumR = 0.0F;
        private float _sumG = 0.0F;
        private float _sumB = 0.0F;

        private readonly float[] _histogramR = new float[256];
        private readonly float[] _histogramG = new float[256];
        private readonly float[] _histogramB = new float[256];

        public void AddPixel(byte r, byte g, byte b)
        {
            int ri = r & 0xFF; // Unsigned int conversion
            int gi = g & 0xFF;
            int bi = b & 0xFF;

            _sumR += ri;
            _sumG += gi;
            _sumB += bi;

            _histogramR[ri]++;
            _histogramG[gi]++;
            _histogramB[bi]++;

            _totalPixels++;
        }

        public MagickColor[] CalculateAverageColor()
        {
            if (_totalPixels <= 0.0F)
            {
                return [MagickColors.White, MagickColors.White, MagickColors.White];
            }

            // Color 0: Arithmetic Mean
            int r0 = ClampCol((int)(_sumR / _totalPixels));
            int g0 = ClampCol((int)(_sumG / _totalPixels));
            int b0 = ClampCol((int)(_sumB / _totalPixels));
            var mean = new MagickColor((byte)r0, (byte)g0, (byte)b0);

            // Color 1: Bright-weighted extraction (method_21184 equivalent)
            float targetWeight = _totalPixels * 0.5F;
            int r1 = ClampCol((int)CalculateWeightedBrightness(_histogramR, targetWeight));
            int g1 = ClampCol((int)CalculateWeightedBrightness(_histogramG, targetWeight));
            int b1 = ClampCol((int)CalculateWeightedBrightness(_histogramB, targetWeight));
            var weighted = new MagickColor((byte)r1, (byte)g1, (byte)b1);

            // Color 2: Median / Percentile extraction
            int r2 = ClampCol((int)CalculateMedian(_histogramR, _totalPixels));
            int g2 = ClampCol((int)CalculateMedian(_histogramG, _totalPixels));

            // in the obfs code the game uses weighted color for blue for some reason
            int b2 = ClampCol((int)CalculateWeightedBrightness(_histogramB, _totalPixels));
            var median = new MagickColor((byte)r2, (byte)g2, (byte)b2);

            return [mean, weighted, median];
        }

        private float CalculateMedian(float[] histogram, float totalWeight)
        {
            float cumulative = 0.0F;
            float halfWeight = totalWeight * 0.5F;

            for (int i = 0; i <= 255; ++i)
            {
                float count = histogram[i];
                cumulative += count;
                if (cumulative >= halfWeight)
                {
                    return i;
                }
            }
            return 0.0F;
        }

        private float CalculateWeightedBrightness(float[] histogram, float targetWeight)
        {
            float cumulative = 0.0F;
            float weightedSum = 0.0F;

            for (int i = 255; i >= 0; --i)
            {
                float count = histogram[i];
                float effectiveCount = count;

                if (cumulative + count >= targetWeight)
                {
                    effectiveCount = targetWeight - cumulative;
                }

                cumulative += effectiveCount;
                weightedSum += i * effectiveCount;

                if (cumulative >= targetWeight)
                {
                    break;
                }
            }
            return cumulative > 0.0F ? weightedSum / cumulative : 0.0F;
        }

        private int ClampCol(int value)
        {
            return Math.Clamp(value, 0, 255);
        }
    }


    /* obfs code from the game:
    
     private static class ImageAnalyzer {
        private float var8 = 0.0F;
        private float var9 = 0.0F;
        private float var10 = 0.0F;
        private float var11 = 0.0F;

        private final float[] var12 = new float[256];
        private final float[] var13 = new float[256];
        private final float[] var14 = new float[256];

        void addPixel(byte r, byte g, byte b) {
            int ri = Byte.toUnsignedInt(r);
            int gi = Byte.toUnsignedInt(g);
            int bi = Byte.toUnsignedInt(b);

            var8 += ri;
            var9 += gi;
            var10 += bi;

            ++var12[ri];
            ++var13[gi];
            ++var14[bi];

            ++var11;
        }

        Color[] calculateAverageColor() {
            if (var11 <= 0.0F) {
                return new Color[]{Color.white, Color.white, Color.white};
            }

            int var22 = (int) (var8 / var11);
            int y = (int) (var9 / var11);
            int x = (int) (var10 / var11);

            y = Math.min(y, 255);
            x = Math.min(x, 255);
            var22 = Math.min(var22, 255);

            y = Math.max(y, 0);
            x = Math.max(x, 0);
            var22 = Math.max(var22, 0);

            Color color0 = new Color(var22, y, x, 255);
            float var23 = 0.5F;
            var22 = (int) method_21184(var12, var11 * var23);
            y = (int) method_21184(var13, var11 * var23);
            x = (int) method_21184(var14, var11 * var23);

            y = Math.min(y, 255);
            x = Math.min(x, 255);
            var22 = Math.min(var22, 255);

            y = Math.max(y, 0);
            x = Math.max(x, 0);
            var22 = Math.max(var22, 0);

            Color color1 = new Color(var22, y, x, 255);
            var22 = (int) method_21183(var12, var11);
            y = (int) method_21183(var13, var11);
            x = (int) method_21184(var14, var11);

            y = Math.min(y, 255);
            x = Math.min(x, 255);
            var22 = Math.min(var22, 255);

            y = Math.max(y, 0);
            x = Math.max(x, 0);
            var22 = Math.max(var22, 0);

            Color color2 = new Color(var22, y, x, 255);

            return new Color[]{color0, color1, color2};
        }

        private float method_21183(float[] var1, float var2) {
            float var3 = 0.0F;
            float var4 = var2 * 0.5F;

            for (int var5 = 0; var5 <= 255; ++var5) {
                float var6 = var1[var5];
                var3 += var6;
                if (var3 >= var4) {
                    return (float) var5;
                }
            }

            return 0.0F;
        }

        private float method_21184(float[] var1, float var2) {
            float var3 = 0.0F;
            float var4 = 0.0F;

            for (int var5 = 255; var5 >= 0; --var5) {
                float var6 = var1[var5];
                float var7 = var6;
                if (var3 + var6 >= var2) {
                    var7 = var2 - var3;
                }

                var3 += var7;
                var4 += (float) var5 * var7;
                if (var3 >= var2) {
                    break;
                }
            }

            return var3 > 0.0F ? var4 / var3 : 0.0F;
        }
    }
     */
}
