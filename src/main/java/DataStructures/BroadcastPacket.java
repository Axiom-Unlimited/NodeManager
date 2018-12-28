package DataStructures;
import javolution.io.Struct;

import java.nio.ByteOrder;

public class BroadcastPacket extends Struct
{
    public final Signed32       port    = new Signed32();

    @Override
    public ByteOrder byteOrder()
    {
        return ByteOrder.BIG_ENDIAN;
    }
}
