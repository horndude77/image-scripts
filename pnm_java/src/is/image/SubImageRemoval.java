package is.image;

import is.image.pnm.Pbm;
import is.util.ConcurrentUtil;

import java.util.concurrent.ThreadPoolExecutor;

public class SubImageRemoval
{
    public static void blankRectangle(BilevelImage image, int rowStart, int colStart, int rows, int cols)
    {
        System.out.println("Blanking "+rows+"x"+cols+" rectangle at: ("+rowStart+", "+colStart+")");
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                image.set(rowStart+row, colStart+col, BilevelImage.WHITE);
            }
        }
    }

    public static void invertSubImage(BilevelImage image, BilevelImage sub, int rowStart, int colStart)
    {
        System.out.println("Inverting logo at: ("+rowStart+", "+colStart+")");
        int rows = sub.getRows();
        int cols = sub.getCols();

        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                if(sub.get(row, col) == BilevelImage.BLACK)
                {
                    image.set(rowStart+row, colStart+col, BilevelImage.WHITE);
                }
            }
        }
    }

    public static int firstNonBlankRow(BilevelImage image)
    {
        int rows = image.getRows();
        int cols = image.getCols();
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                if(image.get(row, col) == BilevelImage.BLACK)
                {
                    return row;
                }
            }
        }
        return rows;
    }

    public static int scoreSection(BilevelImage main, BilevelImage sub, int rowStart, int colStart, int cutoff)
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
                    //System.out.println("cutting it off score: "+score);
                    return Integer.MAX_VALUE;
                }
                //System.out.println("Add to score: "+(main.get(rowStart+row, colStart+col) ^ sub.get(row, col)));
                score += (main.get(rowStart+row, colStart+col) ^ sub.get(row, col));
            }
        }

        return score;
    }

    public static int[] findImage(final BilevelImage main, final BilevelImage sub, int startRowInput, final int rowCount, final int startCol, final int colCount, final int cutoff)
    {
        //System.out.println("Finding subimage...");
        //skip a bunch of rows.
        final int startRow = Math.max(firstNonBlankRow(main), startRowInput);

        final int rows = main.getRows();
        final int cols = main.getCols();
        final int subRows = sub.getRows();
        final int subCols = sub.getCols();

        int bestRow = -1;
        int bestCol = -1;
        int bestScore = cutoff;
        //System.out.println("Cutoff score: "+bestScore);
        //Lame. Gets around 'final' restriction when using inner classes.
        final int[] best = new int[]{bestRow, bestCol, bestScore};

        final int goodEnough = 100;

        ThreadPoolExecutor pool = ConcurrentUtil.createThreadPool();
        final Object lock = new Object();

        //Lame. See above.
        final boolean[] looping = new boolean[]{true};

        for(int rowc=startRow; rowc<startRow+rowCount && rowc<rows-subRows && looping[0]; ++rowc)
        {
            final int row = rowc;
            pool.execute(new Runnable()
            {
                public void run()
                {
                    for(int col=startCol; col<startCol+colCount && col<cols-subCols && looping[0]; ++col)
                    {
                        int score = scoreSection(main, sub, row, col, best[2]);
                        //System.out.println("curr score: "+score);
                        if(score < best[2])
                        {
                            //System.out.println("New best score: "+score+" - ("+row+", "+col+")");
                            synchronized(lock)
                            {
                                best[0] = row;
                                best[1] = col;
                                best[2] = score;
                            }
                        }
                        if(score < goodEnough)
                        {
                            looping[0] = false;
                        }
                    }
                }
            });
        }
        ConcurrentUtil.shutdownPoolAndAwaitTermination(pool);

        if(best[0] == -1)
        {
            throw new RuntimeException("Unable to find subimage");
        }

        return new int[] {best[0], best[1]};
    }
}
