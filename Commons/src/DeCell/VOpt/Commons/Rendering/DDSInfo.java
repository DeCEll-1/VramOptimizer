package DeCell.VOpt.Commons.Rendering;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DDSInfo {
    public int width;
    public int height;
    public int mipCount;
    public int payloadSize;
    public long dxgiFormat;
    public int headerSize;
    public boolean isValid = false;

    public DDSInfo(ByteBuffer buffer) {
        if (buffer == null || buffer.capacity() < 128) {
            resetFields();
            return;
        }

        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // 1. Verify Magic Number ("DDS " = 0x20534444)
        int magic = buffer.getInt(0);
        if (magic != 0x20534444) {
            resetFields();
            return;
        }

        // Determine if DX10 extension header is present (FourCC at offset 84)
        boolean hasDx10 = (buffer.capacity() >= 148 && buffer.getInt(84) == 0x30315844);
        this.headerSize = hasDx10 ? 148 : 128;

        if (buffer.capacity() < this.headerSize) {
            resetFields();
            return;
        }

        // 2. Read standard header metrics
        this.height = buffer.getInt(12);
        this.width = buffer.getInt(16);

        int mips = buffer.getInt(28);
        this.mipCount = Math.max(1, mips);

        // Payload size is everything after the header
        this.payloadSize = buffer.capacity() - this.headerSize;

        // 3. Read DX10 extension format if present
        if (hasDx10) {
            this.dxgiFormat = buffer.getInt(128) & 0xFFFFFFFFL;
        } else {
            this.dxgiFormat = 0;
        }

        this.isValid = true;
    }

    private void resetFields() {
        this.width = 0;
        this.height = 0;
        this.mipCount = 0;
        this.payloadSize = 0;
        this.dxgiFormat = 0;
        this.headerSize = 0;
        this.isValid = false;
    }
}