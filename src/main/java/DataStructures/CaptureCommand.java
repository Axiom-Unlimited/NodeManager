package DataStructures;

import javolution.io.Struct;

import java.nio.ByteOrder;

public class CaptureCommand extends Struct
{
    public final Signed32       size        = new Signed32();
    public final Signed32       type        = new Signed32();
    public final Signed32       command     = new Signed32();
    public final UTF8String     filename    = new UTF8String(255);

    @Override
    public ByteOrder byteOrder()
    {
        return ByteOrder.BIG_ENDIAN;
    }
}
