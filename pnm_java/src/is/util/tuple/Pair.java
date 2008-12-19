package is.util.tuple;

public class Pair<A extends Comparable<A>, B extends Comparable<B>>
    implements Comparable<Pair<A,B>>
{
    protected A first;
    protected B rest;

    public Pair(A first, B rest)
    {
        this.first = first;
        this.rest = rest;
    }

    public A getFirst()
    {
        return first;
    }

    public B getRest()
    {
        return rest;
    }

    public int compareTo(Pair<A,B> other)
    {
        int compareFirst = this.first.compareTo(other.first);
        return (compareFirst == 0) ? this.rest.compareTo(other.rest) : compareFirst;
    }

    public boolean equals(Object otherObj)
    {
        if(otherObj == null || !(otherObj instanceof Pair))
        {
            return false;
        }

        @SuppressWarnings("unchecked")
        Pair<A,B> other = (Pair<A,B>) otherObj;

        return ((first == other.first) || ((first != null) && first.equals(other.first)))
            && ((rest == other.rest) || ((rest != null) && rest.equals(other.rest)));
    }

    public int hashCode()
    {
        int firstPrime = 23;
        int restPrime = 31;
        int hash = 7;
        if(first != null) hash += firstPrime*hash + first.hashCode();
        if(rest != null) hash += restPrime*hash + rest.hashCode();
        return hash;
    }
}
