package image;

import image.pnm.Pbm;

public class SubImageRemoval
{
    public static void blankRectangle(Pbm image, int rowStart, int colStart, int rows, int cols)
    {
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                image.set(rowStart+row, colStart+col, Pbm.WHITE);
            }
        }
    }

    public static void invertSubImage(Pbm image, Pbm sub, int rowStart, int colStart)
    {
        int rows = sub.getRows();
        int cols = sub.getCols();

        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                if(sub.get(row, col) == Pbm.BLACK)
                {
                    image.set(rowStart+row, colStart+col, (byte) (0x1 & (~image.get(rowStart+row, colStart+col))));
                }
            }
        }
    }

    public static int scoreSection(Pbm main, Pbm sub, int rowStart, int colStart, int cutoff)
    {
        int rows = sub.getRows();
        int cols = sub.getCols();
        int maxRows = main.getRows();
        int maxCols = main.getCols();

        int score = 0;

        if(rowStart+rows >= maxRows || colStart+cols >= maxCols)
        {
            //System.out.println("Sub image goes off main image at ("+rowStart+", "+colStart+")");
            return Integer.MAX_VALUE;
        }

        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                if(score >= cutoff)
                {
                    return Integer.MAX_VALUE;
                }
                //System.out.println("Add to score: "+(main.get(rowStart+row, colStart+col) ^ sub.get(row, col)));
                score += (main.get(rowStart+row, colStart+col) ^ sub.get(row, col));
            }
        }

        return score;
    }

    public static int[] findImage(Pbm main, Pbm sub)
    {
        int rows = main.getRows();
        int cols = main.getCols();

        int bestRow = -1;
        int bestCol = -1;
        int bestScore = Integer.MAX_VALUE;

        int goodEnough = 100;

        outer:
        for(int row=0; row<rows && row<50; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                int score = scoreSection(main, sub, row, col, bestScore);
                if(score < bestScore)
                {
                    System.out.println("New best score: "+score+" - ("+row+", "+col+")");
                    bestScore = score;
                    bestRow = row;
                    bestCol = col;
                }
                if(score < goodEnough)
                {
                    break outer;
                }
            }
        }
        return new int[] {bestRow, bestCol,};
    }

    public static void main(String args[])
        throws Exception
    {
        if(args.length < 3)
        {
            System.out.println("Usage: image.SubImageRemoval <main image filename> <sub-image filename> <output filename>");
        }

        String mainFilename = args[0];
        String subFilename = args[1];
        String outFilename = args[2];

        Pbm main = new Pbm(mainFilename);
        Pbm sub = new Pbm(subFilename);
        System.out.println(main);
        System.out.println(sub);

        int[] pos = findImage(main, sub);
        System.out.println("Loc: "+pos[0]+", "+pos[1]);
        invertSubImage(main, sub, pos[0], pos[1]);
        //blankRectangle(main, pos[0], pos[1], sub.getRows(), sub.getCols());
        main.write(outFilename);
    }
}

