package is.util.tuple;

public class Nil
    implements Comparable<Nil>
{
    private static final Nil instance = new Nil();

    public static Nil getInstance()
    {
        return instance;
    }

    private Nil()
    {
    }

    public int compareTo(Nil other)
    {
        return 0;
    }
}
