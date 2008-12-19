package is.image.threshold;

import is.image.BilevelImage;
import is.image.GrayscaleImage;

public interface Thresholder
{
    public BilevelImage threshold(GrayscaleImage input);
}
