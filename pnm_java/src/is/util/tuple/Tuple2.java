package is.util.tuple;

public class Tuple2<A extends Comparable<A>, B extends Comparable<B>>
    extends Pair<A, Pair<B, Nil>>
{
    public Tuple2(A a, B b)
    {
        super(a, Tuple1.create(b));
    }

    public B getSecond()
    {
        return this.rest.getFirst();
    }

    public static<A extends Comparable<A>, B extends Comparable<B>> Tuple2<A, B> create(A a, B b)
    {
        return new Tuple2<A, B>(a, b);
    }
}
