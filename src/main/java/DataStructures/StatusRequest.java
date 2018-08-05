package DataStructures;

import javolution.io.Struct;

public class StatusRequest extends Struct
{
    public final Signed32 size = new Signed32();
    public final Signed32 type = new Signed32();
}
