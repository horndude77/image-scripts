package is.util.tuple;

public class Tuple1<A extends Comparable<A>>
    extends Pair<A, Nil>
{
    public Tuple1(A a)
    {
        super(a, Nil.getInstance());
    }

    public static<A extends Comparable<A>> Tuple1<A> create(A a)
    {
        return new Tuple1<A>(a);
    }
}
