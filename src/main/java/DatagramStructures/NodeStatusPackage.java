package DatagramStructures;

import javolution.io.Struct;

import java.nio.ByteOrder;

public class NodeStatusPackage extends Struct
{
    public final Signed32   nodeId          = new Signed32();
    public final UTF8String bitStatus       = new UTF8String(1000);
    public final Bool       captureState    = new Bool();

    @Override
    public ByteOrder byteOrder()
    {
        return ByteOrder.BIG_ENDIAN;
    }
}
