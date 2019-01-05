package DatagramStructures;

import javolution.io.Struct;

import java.nio.ByteOrder;

public class BroadcastPackage extends Struct
{
    /**
     * currently only one type of broadcast package 0xf001 which is a capture package
     */
    public final Signed32       type       = new Signed32();
    public final NodeState[]    nodeStates = array(new NodeState[32]);

    @Override
    public ByteOrder byteOrder()
    {
        return ByteOrder.BIG_ENDIAN;
    }
}
