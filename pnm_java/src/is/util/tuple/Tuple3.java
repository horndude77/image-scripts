package is.util.tuple;

public class Tuple3<A extends Comparable<A>, B extends Comparable<B>, C extends Comparable<C>>
    extends Pair<A, Pair<B, Pair<C, Nil>>>
{
    public Tuple3(A a, B b, C c)
    {
        super(a, Tuple2.create(b, c));
    }

    public B getSecond()
    {
        return this.rest.getFirst();
    }

    public C getThird()
    {
        return this.rest.getRest().getFirst();
    }

    public static<A extends Comparable<A>, B extends Comparable<B>, C extends Comparable<C>> Tuple3<A, B, C> create(A a, B b, C c)
    {
        return new Tuple3<A, B, C>(a, b, c);
    }
}
