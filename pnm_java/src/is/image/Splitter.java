package is.image;

import is.image.pnm.Pbm;
import is.image.pnm.Pgm;

public class Splitter
{
    public static int findMiddle(Pgm input)
    {
        int rows = input.getRows();
        int cols = input.getCols();

        int min = Integer.MAX_VALUE;
        int minCol = 0;

        //Search the middle third part of the page.
        int middleAmount = 5;
        int startCol = (cols*(middleAmount/2))/middleAmount;
        int endCol = (cols*(middleAmount/2+1))/middleAmount;
        for(int col = startCol; col <= endCol; ++col)
        {
            int total = 0;
            for(int row=0; row<rows;++row)
            {
                total += input.get(row, col);
            }
            if(total < min)
            {
                min = total;
                minCol = col;
            }
        }
        return minCol;
    }

    public static Pgm[] split(Pgm input)
    {
        int rows = input.getRows();
        int cols = input.getCols();
        short maxval = input.getMaxval();

        int splitCol = findMiddle(input);

        Pgm out1 = new Pgm(rows, splitCol, maxval);
        for(int row=0; row<rows;++row)
        {
            for(int col=0; col<splitCol; ++col)
            {
                out1.set(row, col, input.get(row, col));
            }
        }

        Pgm out2 = new Pgm(rows, cols-splitCol, maxval);
        for(int row=0; row<rows;++row)
        {
            for(int col=splitCol; col<cols; ++col)
            {
                out2.set(row, col-splitCol, input.get(row, col));
            }
        }

        return new Pgm[]{out1, out2};
    }

    public static void main(String args[])
        throws Exception
    {
        if(args.length < 3)
        {
            System.out.println("Usage: image.Splitter <input filename> <output filename1> <output filename2>");
            System.exit(-1);
        }

        String inputFilename = args[0];
        String outputFilename1 = args[1];
        String outputFilename2 = args[2];

        Pgm input = new Pgm(inputFilename);
        Pgm[] outputs = split(input);
        outputs[0].write(outputFilename1);
        outputs[1].write(outputFilename2);
    }
}

