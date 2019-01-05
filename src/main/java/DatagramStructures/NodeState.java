package DatagramStructures;

import javolution.io.Struct;

import java.nio.ByteOrder;

public class NodeState extends Struct
{
    public final UTF8String captureName     = new UTF8String(256);
    public final Bool       captureState    = new Bool();

    @Override
    public ByteOrder byteOrder()
    {
        return ByteOrder.BIG_ENDIAN;
    }
}
