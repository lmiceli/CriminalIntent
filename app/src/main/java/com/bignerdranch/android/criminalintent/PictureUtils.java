package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

/**
 * Created by lmiceli on 26/05/2016.
 */
public class PictureUtils {

    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {

        // Read dimmensions of image on disk
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float scrWidth = options.outWidth;
        float scrHeight = options.outHeight;

        // figure how much to scale down by
        int inSampleSize = 1;

        if (scrHeight > destHeight || scrWidth > destWidth) {

            int widthSampleSize = Math.round(scrWidth / destWidth);
            int heightSampleSize = Math.round(scrHeight / destHeight);

            inSampleSize = Math.max(widthSampleSize, heightSampleSize);
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        // Read in and create final bitmap
        return BitmapFactory.decodeFile(path, options);

    }

    /**
     * less exact approach. avoids waiting for layout pass to know exact size of image view by getting a bigger image
     * (screen size instead of ImageView size)
     *
     * "This method checks to see how big the screen is, and then scales the image down to that size. The
        ImageView you load into will always be smaller than this size, so this is a very conservative estimate."

     * @param path
     * @param activity
     * @return
     */
    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay()
                .getSize(size);
        return getScaledBitmap(path, size.x, size.y);
    }

}
