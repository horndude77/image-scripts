package is.image;

import is.image.pnm.Pbm;

public class BinaryMorphology
{
    public static BilevelImage box(int rows, int cols)
    {
        BilevelImage output = new BilevelImage(rows, cols);

        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                output.set(row, col, BilevelImage.BLACK);
            }
        }

        return output;
    }

    public static BilevelImage dilation(BilevelImage image, BilevelImage st, int rowOrigin, int colOrigin)
    {
        int rows = image.getRows();
        int cols = image.getCols();
        int stRows = st.getRows();
        int stCols = st.getCols();

        BilevelImage output = new BilevelImage(rows, cols);

        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                if(image.get(row, col) == BilevelImage.BLACK)
                {
                    for(int stRow=0; stRow<stRows; ++stRow)
                    {
                        for(int stCol=0; stCol<stCols; ++stCol)
                        {
                            int outRow = row+(stRow-rowOrigin);
                            int outCol = col+(stCol-colOrigin);
                            if(st.get(stRow, stCol) == BilevelImage.BLACK
                                    && outRow >=0 && outCol >= 0
                                    && outRow < rows && outCol < cols)
                            {
                                output.set(outRow, outCol, BilevelImage.BLACK);
                            }
                        }
                    }
                }
            }
        }

        return output;
    }

    public static BilevelImage erosion(BilevelImage image, BilevelImage st, int rowOrigin, int colOrigin)
    {
        int rows = image.getRows();
        int cols = image.getCols();
        int stRows = st.getRows();
        int stCols = st.getCols();

        BilevelImage output = new BilevelImage(rows, cols);

        boolean match;
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                match = true;
                outer:
                for(int stRow=0; stRow<stRows; ++stRow)
                {
                    for(int stCol=0; stCol<stCols; ++stCol)
                    {
                        int outRow = row+(stRow-rowOrigin);
                        int outCol = col+(stCol-colOrigin);
                        if(st.get(stRow, stCol) == BilevelImage.BLACK
                                && outRow >=0 && outCol >= 0
                                && outRow < rows && outCol < cols
                                && image.get(outRow, outCol) == BilevelImage.WHITE)
                        {
                            match = false;
                            break outer;
                        }
                    }
                }
                if(match)
                {
                    output.set(row, col, BilevelImage.BLACK);
                }
            }
        }

        return output;
    }

    public static BilevelImage closing(BilevelImage image, BilevelImage st, int rowOrigin, int colOrigin)
    {
        BilevelImage output = image;
        output = dilation(output, st, rowOrigin, colOrigin);
        output = erosion(output, st, rowOrigin, colOrigin);
        return output;
    }

    public static BilevelImage opening(BilevelImage image, BilevelImage st, int rowOrigin, int colOrigin)
    {
        BilevelImage output = image;
        output = erosion(output, st, rowOrigin, colOrigin);
        output = dilation(output, st, rowOrigin, colOrigin);
        return output;
    }
}
