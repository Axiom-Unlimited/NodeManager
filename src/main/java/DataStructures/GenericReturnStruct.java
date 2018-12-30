package DataStructures;

public class GenericReturnStruct <T> extends ReturnStruct
{
    private T var;

    public T getVar()
    {
        return this.var;
    }

    public void setVar(T var)
    {
        this.var = var;
    }
}
