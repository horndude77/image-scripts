package image.util;

public class ArrayUtil
{
    public static double[][] sumNeighborhoods(double[][] image, int neighborhood)
    {
        int n = (neighborhood-1)/2;
        int rows = image.length;
        int cols = image[0].length;
        double[][] sumx = new double[rows][cols];

        //sums in x direction
        for(int row=0; row<rows; ++row)
        {
            //start sum
            double sumVal = 0.0;
            for(int i=0; i<=n; ++i)
            {
                sumVal += image[row][i];
            }

            for(int col=0; col<cols; ++col)
            {
                sumx[row][col] = sumVal;
                if(col-n >= 0)
                {
                    sumVal -= image[row][col-n];
                }
                if(col+n+1 < cols)
                {
                    sumVal += image[row][col+n+1];
                }
            }
        }

        double[][] sum = new double[rows][cols];

        //sums of sumx in y direction
        for(int col=0; col<cols; ++col)
        {
            //start sum
            double sumVal = 0.0;
            for(int i=0; i<=n; ++i)
            {
                sumVal += sumx[i][col];
            }

            for(int row=0; row<rows; ++row)
            {
                sum[row][col] = sumVal;
                if(row-n >= 0)
                {
                    sumVal -= sumx[row-n][col];
                }
                if(row+n+1 < rows)
                {
                    sumVal += sumx[row+n+1][col];
                }
            }
        }
        return sum;
    }

    public static short[][] minNeighborhoods(short[][] image, int neighborhood)
    {
        int n = (neighborhood-1)/2;
        int rows = image.length;
        int cols = image[0].length;
        short[][] minx = new short[rows][cols];

        //mins in x direction
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                int low = Math.max(0, col-n);
                int high = Math.min(cols-1, col+n);
                short minVal = 0x7fff;
                for(int i=low; i<=high; ++i)
                {
                    short val = image[row][i];
                    if(val < minVal)
                    {
                        minVal = val;
                    }
                }
                minx[row][col] = minVal;
            }
        }

        short[][] min = new short[rows][cols];

        //mins of minx in y direction
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                int low = Math.max(0, row-n);
                int high = Math.min(rows-1, row+n);
                short minVal = 0x7fff;
                for(int i=low; i<=high; ++i)
                {
                    short val = minx[i][col];
                    if(val < minVal)
                    {
                        minVal = val;
                    }
                }
                min[row][col] = minVal;
            }
        }
        return min;
    }

    public static short[][] maxNeighborhoods(short[][] image, int neighborhood)
    {
        int n = (neighborhood-1)/2;
        int rows = image.length;
        int cols = image[0].length;
        short[][] maxx = new short[rows][cols];

        //maxes in x direction
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                int low = Math.max(0, col-n);
                int high = Math.min(cols-1, col+n);
                short maxVal = -1;
                for(int i=low; i<=high; ++i)
                {
                    short val = image[row][i];
                    if(val > maxVal)
                    {
                        maxVal = val;
                    }
                }
                maxx[row][col] = maxVal;
            }
        }

        short[][] max = new short[rows][cols];

        //maxes of maxx in y direction
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                int low = Math.max(0, row-n);
                int high = Math.min(rows-1, row+n);
                short maxVal = -1;
                for(int i=low; i<=high; ++i)
                {
                    short val = maxx[i][col];
                    if(val > maxVal)
                    {
                        maxVal = val;
                    }
                }
                max[row][col] = maxVal;
            }
        }
        return max;
    }

    public static double[][] squareEach(double[][] arr)
    {
        int rows = arr.length;
        int cols = arr[0].length;
        double[][] squares = new double[rows][cols];

        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                double val = arr[row][col];
                squares[row][col] = val*val;
            }
        }
        return squares;
    }

    public static double[][] squareRootEach(double[][] arr)
    {
        int rows = arr.length;
        int cols = arr[0].length;
        double[][] squareRoots = new double[rows][cols];

        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                squareRoots[row][col] = Math.sqrt(arr[row][col]);
            }
        }
        return squareRoots;
    }

    public static double[][] multiplyEach(double[][] arr, double val)
    {
        int rows = arr.length;
        int cols = arr[0].length;
        double[][] products = new double[rows][cols];

        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                products[row][col] = arr[row][col]*val;
            }
        }
        return products;
    }

    public static double[][] add(double[][] a, double[][] b)
    {
        int rows = a.length;
        int cols = b[0].length;
        double[][] sum = new double[rows][cols];

        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                sum[row][col] = a[row][col] + b[row][col];
            }
        }
        return sum;
    }

    public static double[][] subtract(double[][] a, double[][] b)
    {
        int rows = a.length;
        int cols = b[0].length;
        double[][] difference = new double[rows][cols];

        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                difference[row][col] = a[row][col] - b[row][col];
            }
        }
        return difference;
    }

    public static double[][] meanNeighborhood(double[][] image, int neighborhood)
    {
        int count = neighborhood*neighborhood;
        double[][] sums = sumNeighborhoods(image, neighborhood);
        return multiplyEach(sums, 1.0/count);
    }

    public static double[][] stdevNeighborhood(double[][] image, int neighborhood)
    {
        //See the 'Rapid calculation methods' in the 'Standard Deviation' 
        //Wikipedia entry for this formula.
        int count = neighborhood*neighborhood;
        return multiplyEach(
                squareRootEach(
                    subtract(
                        multiplyEach(sumNeighborhoods(squareEach(image), neighborhood), count),
                        squareEach(sumNeighborhoods(image, neighborhood)))),
                1.0/count);
    }

    public static double mean(short[] arr)
    {
        int len = arr.length;

        double total = 0.0;
        for(int i=0; i<len; ++i)
        {
            total += arr[i];
        }
        return total/len;
    }

    public static double mean(short[][] arr)
    {
        int len = arr.length;

        double total = 0.0;
        for(int i=0; i<len; ++i)
        {
            total += mean(arr[i]);
        }
        return total/len;
    }

    public static void printArr(short[][] arr)
    {
        int rows = arr.length;
        int cols = arr[0].length;
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                System.out.print(arr[row][col] + " ");
            }
            System.out.println();
        }
    }

    public static double[][] toDoubleArray(short[][] arr)
    {
        int rows = arr.length;
        int cols = arr[0].length;
        double[][] outArr = new double[rows][cols];
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                outArr[row][col] = (double) arr[row][col];
            }
        }
        return outArr;
    }

    public static void main(String[] args)
    {
        int rows = 10;
        int cols = 10;
        short[][] arr = new short[rows][cols];
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                arr[row][col] = 7;
            }
        }
        arr[0][0] = 1;
        arr[5][5] = 3;
        arr[7][7] = 2;
        short[][] max = minNeighborhoods(arr, 3);
        printArr(max);
    }
}
