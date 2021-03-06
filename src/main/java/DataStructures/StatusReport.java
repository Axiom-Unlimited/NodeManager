package DataStructures;

import javolution.io.Struct;

import java.nio.ByteOrder;

public class StatusReport extends Struct
{
    public final Signed32 size      = new Signed32();
    public final Signed32 type      = new Signed32();
    public final Signed32 status    = new Signed32();

    @Override
    public ByteOrder byteOrder()
    {
        return ByteOrder.BIG_ENDIAN;
    }
}
