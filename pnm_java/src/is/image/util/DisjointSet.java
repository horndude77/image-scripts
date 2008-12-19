package is.image.util;

import java.util.Map;
import java.util.HashMap;

public class DisjointSet<A>
{
    private class SetElem<A>
    {
        private A val;
        private int rank;
        private SetElem<A> parent;

        public SetElem(A val)
        {
            this.val = val;
            this.rank = 0;
            this.parent = this;
        }

        public A getVal()
        {
            return val;
        }

        public int getRank()
        {
            return rank;
        }

        public void setRank(int rank)
        {
            this.rank = rank;
        }

        public SetElem<A> getParent()
        {
            return parent;
        }

        public void setParent(SetElem<A> parent)
        {
            this.parent = parent;
        }
    }

    private Map<A, SetElem<A>> sets = new HashMap<A, SetElem<A>>();

    public A find(A x)
    {
        //System.out.println("find("+x+")");
        SetElem<A> xelem = sets.get(x);
        SetElem<A> xroot = find(xelem);
        return xroot.getVal();
    }

    private SetElem<A> find(SetElem<A> x)
    {
        if(x.getParent() == x)
        {
            return x;
        }
        else
        {
            SetElem<A> root = find(x.getParent());
            x.setParent(root);
            return root;
        }
    }

    public void union(A x, A y)
    {
        //System.out.println("union("+x+", "+y+")");
        SetElem<A> xelem = sets.get(x);
        SetElem<A> yelem = sets.get(y);
        SetElem<A> xroot = find(xelem);
        SetElem<A> yroot = find(yelem);
        if(xroot.getRank() > yroot.getRank())
        {
            yroot.setParent(xroot);
        }
        else if(xroot.getRank() < yroot.getRank())
        {
            xroot.setParent(yroot);
        }
        else if(xroot != yroot)
        {
            yroot.setParent(xroot);
            xroot.setRank(xroot.getRank() + 1);
        }
        //else they are already the same set and do nothing
    }

    public void makeSet(A x)
    {
        sets.put(x, new SetElem<A>(x));
    }
}
