package image.util;

public class ArrayUtil
{
    public static double[][] sumNeighborhoods(double[][] arr, int neighborhood)
    {
        int rows = arr.length;
        int cols = arr[0].length;
        double[][] target = new double[rows][cols];
        return sumNeighborhoods(target, arr, neighborhood);
    }

    public static double[][] sumNeighborhoods(double[][] sum, double[][] arr, int neighborhood)
    {
        int n = (neighborhood-1)/2;
        int rows = arr.length;
        int cols = arr[0].length;

        double[] circularBuffer = new double[neighborhood];
        int circularIndex = 0;

        //sums in x direction
        for(int row=0; row<rows; ++row)
        {
            //start sum
            double sumVal = 0.0;
            for(int i=-n; i<=n; ++i)
            {
                double val = 0.0;
                if(i>=0) val = arr[row][i];
                sumVal += val;
                circularBuffer[circularIndex] = val;
                circularIndex = (circularIndex+1)%neighborhood;
            }

            for(int col=0; col<cols; ++col)
            {
                double val = 0.0;
                if(col+n+1 < cols) val = arr[row][col+n+1];
                sum[row][col] = sumVal;
                sumVal -= circularBuffer[circularIndex];
                sumVal += val;
                circularBuffer[circularIndex] = val;
                circularIndex = (circularIndex+1)%neighborhood;
            }
        }

        //sums of sums in y direction
        for(int col=0; col<cols; ++col)
        {
            //start sum
            double sumVal = 0.0;
            for(int i=-n; i<=n; ++i)
            {
                double val = 0.0;
                if(i>=0) val = sum[i][col];
                sumVal += val;
                circularBuffer[circularIndex] = val;
                circularIndex = (circularIndex+1)%neighborhood;
            }

            for(int row=0; row<rows; ++row)
            {
                double val = 0.0;
                if(row+n+1 < rows) val = sum[row+n+1][col];
                sum[row][col] = sumVal;
                sumVal -= circularBuffer[circularIndex];
                sumVal += val;
                circularBuffer[circularIndex] = val;
                circularIndex = (circularIndex+1)%neighborhood;
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
        int index = -1;
        short minVal = 0x7fff;

        //mins in x direction
        for(int row=0; row<rows; ++row)
        {
            minVal = 0x7fff;
            for(int i=0; i<=n; ++i)
            {
                short val = image[row][i];
                if(val < minVal)
                {
                    index = i;
                    minVal = val;
                }
            }

            for(int col=0; col<cols; ++col)
            {
                int low = Math.max(0, col-n);
                int high = Math.min(cols-1, col+n);
                if(image[row][high] < minVal)
                {
                    index = high;
                    minVal = image[row][high];
                }
                else if(index == low-1)
                {
                    minVal = 0x7fff;
                    for(int i=low; i<=high; ++i)
                    {
                        short val = image[row][i];
                        if(val < minVal)
                        {
                            index = i;
                            minVal = val;
                        }
                    }
                }
                minx[row][col] = minVal;
            }
        }

        short[][] min = new short[rows][cols];

        //mins of minx in y direction
        for(int col=0; col<cols; ++col)
        {
            minVal = 0x7fff;
            for(int i=0; i<=n; ++i)
            {
                short val = minx[i][col];
                if(val < minVal)
                {
                    index = i;
                    minVal = val;
                }
            }

            for(int row=0; row<rows; ++row)
            {
                int low = Math.max(0, row-n);
                int high = Math.min(rows-1, row+n);
                if(minx[high][col] < minVal)
                {
                    index = high;
                    minVal = minx[high][col];
                }
                else if(index == low-1)
                {
                    minVal = 0x7fff;
                    for(int i=low; i<=high; ++i)
                    {
                        short val = minx[i][col];
                        if(val < minVal)
                        {
                            index = i;
                            minVal = val;
                        }
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
        int index = -1;
        short maxVal = -1;

        //maxes in x direction
        for(int row=0; row<rows; ++row)
        {
            maxVal = -1;
            for(int i=0; i<=n; ++i)
            {
                short val = image[row][i];
                if(val > maxVal)
                {
                    index = i;
                    maxVal = val;
                }
            }

            for(int col=0; col<cols; ++col)
            {
                int low = Math.max(0, col-n);
                int high = Math.min(cols-1, col+n);
                if(image[row][high] > maxVal)
                {
                    index = high;
                    maxVal = image[row][high];
                }
                else if(index == low-1)
                {
                    maxVal = -1;
                    for(int i=low; i<=high; ++i)
                    {
                        short val = image[row][i];
                        if(val > maxVal)
                        {
                            index = i;
                            maxVal = val;
                        }
                    }
                }
                maxx[row][col] = maxVal;
            }
        }

        short[][] max = new short[rows][cols];

        //maxes of maxx in y direction
        for(int col=0; col<cols; ++col)
        {
            maxVal = -1;
            for(int i=0; i<=n; ++i)
            {
                short val = maxx[i][col];
                if(val > maxVal)
                {
                    index = i;
                    maxVal = val;
                }
            }

            for(int row=0; row<rows; ++row)
            {
                int low = Math.max(0, row-n);
                int high = Math.min(rows-1, row+n);
                if(maxx[high][col] > maxVal)
                {
                    index = high;
                    maxVal = maxx[high][col];
                }
                else if(index == low-1)
                {
                    maxVal = -1;
                    for(int i=low; i<=high; ++i)
                    {
                        short val = maxx[i][col];
                        if(val > maxVal)
                        {
                            index = i;
                            maxVal = val;
                        }
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
        double[][] target = new double[rows][cols];
        return squareEach(target, arr);
    }

    public static double[][] squareEach(double[][] squares, double[][] arr)
    {
        int rows = arr.length;
        int cols = arr[0].length;

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
        double[][] target = new double[rows][cols];
        return squareRootEach(target, arr);
    }

    public static double[][] squareRootEach(double[][] squareRoots, double[][] arr)
    {
        int rows = arr.length;
        int cols = arr[0].length;

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
        double[][] target = new double[rows][cols];
        return multiplyEach(target, arr, val);
    }

    public static double[][] multiplyEach(double[][] products, double[][] arr, double val)
    {
        int rows = arr.length;
        int cols = arr[0].length;

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
        int cols = a[0].length;
        double[][] target = new double[rows][cols];
        return add(target, a, b);
    }

    public static double[][] add(double[][] sum, double[][] a, double[][] b)
    {
        int rows = a.length;
        int cols = b[0].length;

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
        int cols = a[0].length;
        double[][] target = new double[rows][cols];
        return subtract(target, a, b);
    }

    public static double[][] subtract(double[][] difference, double[][] a, double[][] b)
    {
        int rows = a.length;
        int cols = a[0].length;

        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                difference[row][col] = a[row][col] - b[row][col];
            }
        }
        return difference;
    }

    public static double[][] meanNeighborhood(double[][] arr, int neighborhood)
    {
        int rows = arr.length;
        int cols = arr[0].length;
        double[][] target = new double[rows][cols];
        return meanNeighborhood(target, arr, neighborhood);
    }

    public static double[][] meanNeighborhood(double[][] target, double[][] arr, int neighborhood)
    {
        int count = neighborhood*neighborhood;
        double[][] sums = sumNeighborhoods(target, arr, neighborhood);
        return multiplyEach(target, sums, 1.0/count);
    }

    public static double[][] stdevNeighborhood(double[][] arr, int neighborhood)
    {
        int rows = arr.length;
        int cols = arr[0].length;
        double[][] target = new double[rows][cols]; //new array
        return stdevNeighborhood(target, arr, neighborhood);
    }

    public static double[][] stdevNeighborhood(double[][] target, double[][] arr, int neighborhood)
    {
        //See the 'Rapid calculation methods' in the 'Standard Deviation' 
        //Wikipedia entry for this formula.
        int count = neighborhood*neighborhood;

        double[][] meansSquared = meanNeighborhood(arr, neighborhood); //new array
        squareEach(meansSquared, meansSquared);

        double[][] squaresMean = squareEach(target, arr);
        squaresMean = meanNeighborhood(target, squaresMean, neighborhood);

        subtract(target, squaresMean, meansSquared);
        squareRootEach(target, target);
        return target;
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
}
