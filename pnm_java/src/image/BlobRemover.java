package image;

import image.pnm.Pbm;
import image.util.Pair;
import java.util.Queue;
import java.util.LinkedList;

public class BlobRemover
{
    public static void removeBlobs(Pbm image, int minBlobSize)
    {
        int rows = image.getRows();
        int cols = image.getCols();
        int[][] assignments = new int[rows][cols];
        int blobNum = 0;

        //assign each pixel to a blob
        System.out.println("Assigning blob numbers...");
        for(int row=0; row<rows; ++row)
        {
            for(int col=0; col<cols; ++col)
            {
                if(assignments[row][col] == 0)
                {
                    ++blobNum;
                    byte color = image.get(row, col);
                    assignBlob(image, assignments, row, col, blobNum, color);
                }
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

    private static void assignBlob(Pbm image, int[][] assignments, int row, int col, int blobNum, byte color)
    {
        //Flood fill algorithm
        Queue<Pair<Integer, Integer>> queue = new LinkedList<Pair<Integer, Integer>>();
        queue.offer(new Pair<Integer, Integer>(row, col));
        int rows = image.getRows();
        int cols = image.getCols();

        while(!queue.isEmpty())
        {
            Pair<Integer, Integer> p = queue.poll();
            int currRow = p.getFirst();
            int currCol = p.getSecond();

            if(currRow >= 0 && currRow < rows && currCol >= 0 && currCol < cols && assignments[currRow][currCol] == 0 && image.get(currRow, currCol) == color)
            {
                assignments[currRow][currCol] = blobNum;
                Pair<Integer, Integer> up = new Pair<Integer, Integer>(currRow-1, currCol);
                Pair<Integer, Integer> down = new Pair<Integer, Integer>(currRow+1, currCol);
                Pair<Integer, Integer> left = new Pair<Integer, Integer>(currRow, currCol-1);
                Pair<Integer, Integer> right = new Pair<Integer, Integer>(currRow, currCol+1);
                queue.offer(up);
                queue.offer(down);
                queue.offer(left);
                queue.offer(right);
            }
        }
    }
}

