package is.image;

public class Border
{
    public static void blankBorder(BilevelImage image, int topBorder, int bottomBorder, int leftBorder, int rightBorder)
    {
        System.out.println("Removing border pixels...");
        int rows = image.getRows();
        int cols = image.getCols();

        for(int row=0; row<=topBorder; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                image.set(row, col, BilevelImage.WHITE);
            }
        }
        for(int row=rows-bottomBorder; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                image.set(row, col, BilevelImage.WHITE);
            }
        }
        for(int col=0; col<=leftBorder; ++col)
        {
            for(int row=0; row<rows; ++row)
            {
                image.set(row, col, BilevelImage.WHITE);
            }
        }
        for(int col=cols-rightBorder; col<cols; ++col)
        {
            for(int row=0; row<rows; ++row)
            {
                image.set(row, col, BilevelImage.WHITE);
            }
        }
    }

    public static BilevelImage centerImage(BilevelImage image)
    {
        System.out.println("Centering image on page...");

        int rows = image.getRows();
        int cols = image.getCols();

        //current margins
        int top=0, bottom=0, left=0, right=0;
        outer:
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                if(image.get(row, col) == BilevelImage.BLACK)
                {
                    top = row;
                    break outer;
                }
            }
        }

        outer:
        for(int row=rows-1; row>=0; --row)
        {
            for(int col=0; col<cols; ++col)
            {
                if(image.get(row, col) == BilevelImage.BLACK)
                {
                    bottom = rows - row;
                    break outer;
                }
            }
        }

        outer:
        for(int col=0; col<cols; ++col)
        {
            for(int row=0; row<rows; ++row)
            {
                if(image.get(row, col) == BilevelImage.BLACK)
                {
                    left = col;
                    break outer;
                }
            }
        }

        outer:
        for(int col=cols-1; col>=0; --col)
        {
            for(int row=0; row<rows; ++row)
            {
                if(image.get(row, col) == BilevelImage.BLACK)
                {
                    right = cols - col;
                    break outer;
                }
            }
        }

        System.out.println("top: "+top+", "+"bottom: "+bottom+", "+"left: "+left+", "+"right: "+right);

        //determing how far to shift image
        int rowShift = (top + bottom)/2 - top;
        int colShift = (left + right)/2 - left;

        return shift(image, rowShift, colShift);
    }

    public static BilevelImage shift(BilevelImage image, int rowShift, int colShift)
    {
        int rows = image.getRows();
        int cols = image.getCols();

        //determing how far to shift image
        System.out.println("rowShift: "+rowShift+", "+"colShift: "+colShift);

        BilevelImage output = new BilevelImage(rows, cols);
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                output.set(row, col, image.getWhiteWhenOutOfRange(row-rowShift, col-colShift));
            }
        }
        return output;
    }

    public static BilevelImage centerImageMass(BilevelImage image)
    {
        System.out.println("Centering image on page...");
        int rows = image.getRows();
        int cols = image.getCols();
        double cx=0, cy=0, mass=0;

        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                if(image.get(row, col) == BilevelImage.BLACK)
                {
                    cx += col;
                    cy += row;
                    mass += 1;
                }
            }
        }

        cx = cx/mass;
        cy = cy/mass;

        int rowShift = (int) (cy - rows/2);
        int colShift = (int) (cx - cols/2);

        return shift(image, rowShift, colShift);
    }
}
