package is.image;

import is.util.DisjointSet;
import java.util.Queue;
import java.util.LinkedList;

public class BlobRemover
{
    public static void removeBlobs(BilevelImage image, int minBlobSize)
    {
        int rows = image.getRows();
        int cols = image.getCols();
        int[][] assignments = new int[rows][cols];
        int blobNum = 0;

        //assign each pixel to a blob
        System.out.println("Assigning blob numbers...");
        //See "Connected Component Labeling" Wikipedia entry for more
        //information on this algorithm.
        DisjointSet<Integer> ds = new DisjointSet<Integer>();
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                //System.out.println("("+row+", "+col+")");
                byte color = image.get(row, col);
                byte upColor = -1, leftColor = -1;
                if(row > 0)
                {
                    upColor = image.get(row-1, col);
                }
                if(col > 0)
                {
                    leftColor = image.get(row, col-1);
                }

                if(upColor == color && leftColor == color)
                {
                    int upLabel = assignments[row-1][col];
                    int leftLabel = assignments[row][col-1];
                    int label = Math.min(upLabel, leftLabel);
                    if(upLabel != leftLabel)
                    {
                        ds.union(upLabel, leftLabel);
                    }
                    assignments[row][col] = label;
                }
                else if(upColor == color)
                {
                    assignments[row][col] = assignments[row-1][col];
                }
                else if(leftColor == color)
                {
                    assignments[row][col] = assignments[row][col-1];
                }
                else
                {
                    ++blobNum;
                    ds.makeSet(blobNum);
                    assignments[row][col] = blobNum;
                }
            }
        }

        //correct labelings.
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                assignments[row][col] = ds.find(assignments[row][col]);
            }
        }

        //count pixels in each blob
        System.out.println("Calculating blob sizes...");
        int[] blobSizes = new int[blobNum+1];
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                int currBlobNum = assignments[row][col];
                blobSizes[currBlobNum]++;
            }
        }

        //remove small blobs
        System.out.println("Removing small blobs...");
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                int currBlobNum = assignments[row][col];
                if(blobSizes[currBlobNum] < minBlobSize)
                {
                    image.invert(row, col);
                }
            }
        }
    }
}

