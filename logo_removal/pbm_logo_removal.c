/*
 * Searches through an image (pbm only for now) for another image then makes
 * that section white.
 *
 * Author: Jay Anderson
 *
 * This program comes with ABSOLUTELY NO WARRANTY. Use at your own risk.
 */

#include <pbm.h>

#define INFINITY 0x7fffffff

int score_rect(bit** logo, int logo_rows, int logo_cols, bit** image, int image_rows, int image_cols, int row_zero, int col_zero, int cutoff)
{
    if(row_zero+logo_rows >= image_rows || col_zero+logo_cols >= image_cols)
    {
        return INFINITY;
    }

    int score = 0;
    int row;
    int col;
    for(row=0; row<logo_rows; ++row)
    {
        for(col=0; col<logo_cols; ++col)
        {
            score += (logo[row][col] ^ image[row+row_zero][col+col_zero]);

            if(score > cutoff)
            {
                return INFINITY;
            }
        }
    }
    return score;
}

void blank_rectangle(bit** image, int image_rows, int image_cols, int row_zero, int col_zero, int rows, int cols)
{
    int row, col;
    for(row=0; row<rows && row+row_zero<image_rows; ++row)
    {
        for(col=0; col<cols && col+col_zero<image_cols; ++col)
        {
            image[row+row_zero][col+col_zero] = PBM_WHITE;
        }
    }
}

void invert_logo(bit** image, int image_rows, int image_cols, int row_zero, int col_zero, bit** logo, int logo_rows, int logo_cols)
{
    int row, col;
    for(row=0; row<logo_rows && row+row_zero<image_rows; ++row)
    {
        for(col=0; col<logo_cols && col+col_zero<image_cols; ++col)
        {
            if(logo[row][col] == PBM_BLACK)
            {
                bit image_val = image[row+row_zero][col+col_zero];
                image[row+row_zero][col+col_zero] = 0x1 & (~image_val);
            }
        }
    }
}

int main(int argc, char** argv)
{
    if(argc < 3)
    {
        printf("Usage: pbm_logo_removal <image> <logo image> <output filename> <start row> <number of rows to search> <start col> <number of columns to search>\n");
        exit(-1);
    }

    /* Ugh! Too many arguments. */
    char* target_filename = argv[1];
    char* logo_filename = argv[2];
    char* output_filename = argv[3];
    double percent_close = atof(argv[4]);
    int start_row = atoi(argv[5]);
    int rows_to_search = atoi(argv[6]);
    int start_col = atoi(argv[7]);
    int cols_to_search = atoi(argv[8]);
    char* removal_method = argv[9];
    FILE* file;

    /* load image */
    file = fopen(target_filename, "r");
    int image_rows;
    int image_cols;
    bit** image = pbm_readpbm(file, &image_cols, &image_rows);
    fclose(file);
    /*
    printf("image: %dx%d\n", image_rows, image_cols);
    */

    /* load logo */
    file = fopen(logo_filename, "r");
    int logo_rows;
    int logo_cols;
    bit** logo = pbm_readpbm(file, &logo_cols, &logo_rows);
    fclose(file);
    /*
    printf("logo: %dx%d\n", logo_rows, logo_cols);
    */

    /* search for logo */
    /* This is stupid and slow, but it might just be good enough. */
    printf("Beginning search...\n");
    int cutoff = (logo_rows*logo_cols) * (percent_close/100.0);
    int best = cutoff;
    int best_row = -1;
    int best_col = -1;
    int good_enough = 150;
    if(cols_to_search == -1)
    {
        cols_to_search = image_cols;
    }
    if(rows_to_search == -1)
    {
        rows_to_search = image_rows;
    }

    /*
    printf("Cutoff: %d\n", cutoff);
    printf("start_row: %d\n", start_row);
    printf("rows_to_search: %d\n", rows_to_search);
    printf("start_col: %d\n", start_col);
    printf("cols_to_search: %d\n", cols_to_search);
    */

    int row, col, score;
    for(row=start_row; row<start_row+rows_to_search && best > good_enough; ++row)
    {
        for(col=start_col; col<start_col+cols_to_search && best > good_enough; ++col)
        {
            score = score_rect(logo, logo_rows, logo_cols, image, image_rows, image_cols, row, col, best);
            if(score < best)
            {
                printf("New best score %d (%d, %d)\n", score, row, col);
                best = score;
                best_row = row;
                best_col = col;
            }
        }
    }
    printf("Search done!\n");
    if(best_row < 0 || best_col < 0)
    {
        printf("Unable to find logo.\n");
        exit(-1);
    }

    /* blank logo */
    printf("Blanking logo...\n");
    if(strcmp(removal_method, "blank_rectangle") == 0)
    {
        blank_rectangle(image, image_rows, image_cols, best_row, best_col, logo_rows, logo_cols);
    }
    else /* if(strcmp(removal_method, "invert_logo") == 0) */
    {
        invert_logo(image, image_rows, image_cols, best_row, best_col, logo, logo_rows, logo_cols);
    }

    /* write new image */
    printf("Writing output image...\n");
    file = fopen(output_filename, "w");
    pbm_writepbm(file, image, image_cols, image_rows, 0);
    fclose(file);

    printf("Done!\n");

    pbm_freearray(image, image_rows);
    pbm_freearray(logo, logo_rows);
}

