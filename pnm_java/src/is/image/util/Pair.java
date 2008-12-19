package is.image.util;

public class Pair<A,B>
{
    private A first;
    private B second;

    public Pair(A first, B second)
    {
        this.first = first;
        this.second = second;
    }

    public A getFirst()
    {
        return first;
    }

    public B getSecond()
    {
        return second;
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
            && ((second == other.second) || ((second != null) && second.equals(other.second)));
    }

    public int hashCode()
    {
        int firstPrime = 23;
        int secondPrime = 31;
        int hash = 7;
        if(first != null) hash += firstPrime*hash + first.hashCode();
        if(second != null) hash += secondPrime*hash + second.hashCode();
        return hash;
    }
}
