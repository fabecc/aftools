/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package com.android.aft.AFCoreTools;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.params.HttpParams;

import com.android.aft.AFCoreTools.AsyncTaskWrapper.Mode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.http.AndroidHttpClient;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

/**
 * This helper class download images from the Internet and binds those with the
 * provided ImageView.
 * <p>
 * It requires the INTERNET permission, which should be added to your
 * application's manifest file.
 * </p>
 * A local cache of downloaded images is maintained internally to improve
 * performance.
 */
public class ImageDownloader {

    // Singleton instance
    private static ImageDownloader mInstance = null;

    private Context mContext;

    public interface ImageDownloaderListener {
        public void onImageDownloaded(String url, Bitmap bitmap, View view);
    }

    public BitmapFactory.Options createBitmapFactoryOptions() {
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inDither = false; // Disable Dithering mode
        options.inPurgeable = true; // Tell to gc that whether it
        // needs free memory, the Bitmap
        // can be cleared
        options.inInputShareable = true; // Which kind of reference
        // will be used to recover
        // the Bitmap data after
        // being clear, when it will
        // be used in the future
        options.inTempStorage = new byte[32 * 1024];
        options.inSampleSize = 1;

        return options;
    }

    public ImageDownloader(Context context) {
        mContext = context;
    }

    // Prefer to use constructor with Context in parameter
    @Deprecated
    private ImageDownloader() {
        this(null);
    }

    // Prefer to use only one instance in your AppManager
    @Deprecated
    public static synchronized ImageDownloader getInstance() {
        if (mInstance == null)
            mInstance = new ImageDownloader();
        return mInstance;
    }

    // Prefer to use the constructor with Context in parameter
    @Deprecated
    public void setContext(final Context context) {
        mContext = context;
    }

    /**
     * Download the specified image from the Internet and binds it to the
     * provided ImageView. The binding is immediate if the image is found in the
     * cache and will be done asynchronously otherwise. A null bitmap will be
     * associated to the ImageView if an error occurs.
     *
     * @param url The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
    public void download(String url, ImageView imageView, ImageDownloaderListener listener, Animation animation,
            int reqWidth, int reqHeight) {
        if (url != null) {
            resetPurgeTimer();
            Bitmap bitmap = getBitmapFromCache(url, reqWidth, reqHeight);

            if (bitmap == null) {
                forceDownload(url, imageView, reqWidth, reqHeight, listener, animation);
            }
            else {
                cancelPotentialDownload(url, imageView);
                // imageView.setBackgroundDrawable(null);
                imageView.setImageDrawable(null);
                // imageView.setScaleType(ScaleType.FIT_XY);
                imageView.setImageBitmap(bitmap);
                if (animation != null) {
                    imageView.setAnimation(animation);
                }
                if (listener != null) {
                    listener.onImageDownloaded(url, bitmap, imageView);
                }
            }
        }
    }

    public void download(String url, ImageView imageView, ImageDownloaderListener listener, Animation animation) {
        download(url, imageView, listener, animation, 0, 0);
    }

    public void download(String url, ImageView imageView, ImageDownloaderListener listener) {
        download(url, imageView, listener, null);
    }

    public void download(String url, ImageView imageView, Animation animation) {
        download(url, imageView, null, animation);
    }

    public void download(String url, ImageView imageView, int reqWidth, int reqHeight) {
        download(url, imageView, null, null, reqWidth, reqHeight);
    }

    public void download(String url, ImageView imageView) {
        download(url, imageView, 0, 0);
    }

    /*
     * Same as download but the image is always downloaded and the cache is not
     * used. Kept private at the moment as its interest is not clear. private
     * void forceDownload(String url, ImageView view) { forceDownload(url, view,
     * null); }
     */

    /**
     * Same as download but the image is always downloaded and the cache is not
     * used. Kept private at the moment as its interest is not clear.
     */
    private void forceDownload(String url, ImageView imageView, int reqWidth, int reqHeight,
            ImageDownloaderListener listener, Animation animation) {
        // State sanity: url is guaranteed to never be null in
        // DownloadedDrawable and cache keys.
        if (url == null) {
            imageView.setImageDrawable(null);
            return;
        }

        if (cancelPotentialDownload(url, imageView)) {
            BitmapDownloaderTask task = new BitmapDownloaderTask(imageView, listener, reqWidth, reqHeight);

            Bitmap bg = null;
            if (imageView.getDrawable() instanceof BitmapDrawable)
                bg = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

            DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task, bg);

            imageView.setImageDrawable(downloadedDrawable);
            if (animation != null) {
                imageView.setAnimation(animation);
            }
            imageView.setMinimumHeight(156);

            task.execute(Mode.Parallel, url);
        }
    }

    /**
     * Returns true if the current download has been canceled or if there was no
     * download in progress on this image view. Returns false if the download in
     * progress deals with the same url. The download is not stopped in that
     * case.
     */
    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            }
            else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active download task (if any) associated
     *         with this imageView. null if there is no such task.
     */
    private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    Bitmap downloadBitmap(String url, int reqWidth, int reqHeight) {
        HttpGet getRequest = null;
        try {
            getRequest = new HttpGet(url);
        }
        catch (IllegalArgumentException e) {
            DebugTools.e("ImageDownloader: Image url is invalid: " + url);
            return null;
        }

        // AndroidHttpClient is not allowed to be used from the main thread
        final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        final HttpParams params = client.getParams();
        HttpClientParams.setRedirecting(params, true);
        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                DebugTools.w("Error " + statusCode + " while retrieving bitmap from " + url);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    // return BitmapFactory.decodeStream(inputStream);
                    // Bug on slow connections, fixed in future release.
                    return decodeSampledBitmapFromStream(new FlushedInputStream(inputStream), reqWidth, reqHeight);
                }
                finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        }
        catch (IOException e) {
            getRequest.abort();
            DebugTools.w("I/O error while retrieving bitmap from " + url, e);
        }
        catch (IllegalStateException e) {
            getRequest.abort();
            DebugTools.w("Incorrect URL: " + url);
        }
        catch (Exception e) {
            getRequest.abort();
            DebugTools.w("Error while retrieving bitmap from " + url, e);
        }
        finally {
            client.close();
        }
        return null;
    }

    /*
     * An InputStream that skips the exact number of bytes provided, unless it
     * reaches EOF.
     */
    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break; // we reached EOF
                    }
                    else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }

    /**
     * The actual AsyncTask that will asynchronously download the image.
     */
    class BitmapDownloaderTask extends AsyncTaskWrapper<String, Void, Bitmap> {
        private String url;
        private final WeakReference<ImageView> imageViewReference;
        private final ImageDownloaderListener mListener;
        private int mWidth;
        private int mHeight;

        public BitmapDownloaderTask(ImageView imageView, ImageDownloaderListener listener) {
            imageViewReference = new WeakReference<ImageView>(imageView);
            mListener = listener;
        }

        public BitmapDownloaderTask(ImageView imageView, ImageDownloaderListener listener, int reqWidth, int reqHeight) {
            this(imageView, listener);

            mWidth = reqWidth;
            mHeight = reqHeight;
        }

        /**
         * Actual download method.
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];

            return downloadBitmap(url, mWidth, mHeight);
        }

        /**
         * Once the image is downloaded, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (bitmap == null) {
                if (mListener != null) {
                    mListener.onImageDownloaded(url, bitmap, imageViewReference.get());
                }
                return;
            }

            addBitmapToCache(url, bitmap);

            if (imageViewReference != null) {
                final ImageView imageView = imageViewReference.get();
                BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                // Change bitmap only if this process is still associated with
                // it
                // Or if we don't use any bitmap to task association
                // (NO_DOWNLOADED_DRAWABLE mode)
                if (this == bitmapDownloaderTask) {
                    // imageView.setBackgroundDrawable(null);
                    imageView.setImageDrawable(null);
                    // imageView.setScaleType(ScaleType.FIT_XY);
                    imageView.setImageBitmap(bitmap);
                    if (mListener != null) {
                        mListener.onImageDownloaded(url, bitmap, imageView);
                    }
                }
            }
        }
    }

    /**
     * A fake Drawable that will be attached to the imageView while the download
     * is in progress.
     * <p>
     * Contains a reference to the actual download task, so that a download task
     * can be stopped if a new binding is required, and makes sure that only the
     * last started download process can bind its result, independently of the
     * download finish order.
     * </p>
     */
    static class DownloadedDrawable extends BitmapDrawable {
        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

        public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask, Bitmap bg) {
            super(bg);
            bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }

    /*
     * Cache-related fields and methods. We use a hard and a soft cache. A soft
     * reference cache is too aggressively cleared by the Garbage Collector.
     */

    private static final int HARD_CACHE_CAPACITY = 10;
    private static final int DELAY_BEFORE_PURGE = 10 * 1000; // in milliseconds

    // Hard cache, with a fixed maximum capacity and a life duration
    private final HashMap<String, Bitmap> sHardBitmapCache = new LinkedHashMap<String, Bitmap>(HARD_CACHE_CAPACITY / 2,
            0.75f, true) {
        // fhs: add auto generated serial version UID
        private static final long serialVersionUID = 1152380204626646433L;

        @Override
        protected boolean removeEldestEntry(LinkedHashMap.Entry<String, Bitmap> eldest) {
            if (size() > HARD_CACHE_CAPACITY) {
                // Entries push-out of hard reference cache are transferred to
                // soft reference cache
                sSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
                return true;
            }
            else
                return false;
        }
    };

    // Soft cache for bitmaps kicked out of hard cache
    private final static ConcurrentHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>(
            HARD_CACHE_CAPACITY / 2);

    private final Handler purgeHandler = new Handler();

    private final Runnable purger = new Runnable() {
        public void run() {
            clearCache();
        }
    };

    /**
     * Adds this bitmap to the cache.
     *
     * @param bitmap The newly downloaded bitmap.
     */
    private void addBitmapToCache(String url, Bitmap bitmap) {
        if (bitmap != null) {
            synchronized (sHardBitmapCache) {
                sHardBitmapCache.put(url, bitmap);
            }

            // add it to persistent cache
            if (mContext != null) {
                final String filePath = filePathForUrl(url);
                final String urlUpper = url.toUpperCase();
                try {
                    FileOutputStream fos = new FileOutputStream(filePath);
                    if (urlUpper.contains(".PNG")) {
                        bitmap.compress(CompressFormat.PNG, 100, fos);
                    }
                    else {
                        bitmap.compress(CompressFormat.JPEG, 75, fos);
                    }
                    fos.close();
                }
                catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param url The URL of the image that will be retrieved from the cache.
     * @return The cached bitmap or null if it was not found.
     */
    private Bitmap getBitmapFromCache(String url, int reqWidth, int reqHeight) {
        // First try the hard reference cache
        synchronized (sHardBitmapCache) {
            final Bitmap bitmap = sHardBitmapCache.get(url);
            if (bitmap != null) {
                // Bitmap found in hard cache
                // Move element to first position, so that it is removed last
                sHardBitmapCache.remove(url);
                sHardBitmapCache.put(url, bitmap);
                return bitmap;
            }
        }

        // Then try the soft reference cache
        SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(url);
        if (bitmapReference != null) {
            final Bitmap bitmap = bitmapReference.get();
            if (bitmap != null) {
                // Bitmap found in soft cache
                return bitmap;
            }
            else {
                // Soft reference has been Garbage Collected
                sSoftBitmapCache.remove(url);
            }
        }

        // Then try the persistent cache
        if (mContext != null) {
            try {
                final String filePath = filePathForUrl(url);

                final Bitmap bitmap = decodeSampledBitmapFromFile(filePath, reqWidth, reqHeight);
                if (bitmap != null) {
                    return bitmap;
                }
            }
            catch (Exception ex) {
                return null;
            }
        }

        return null;
    }

    /**
     * Clears the image cache used internally to improve performance. Note that
     * for memory efficiency reasons, the cache will automatically be cleared
     * after a certain inactivity delay.
     */
    public void clearCache() {
        sHardBitmapCache.clear();
        sSoftBitmapCache.clear();
    }

    /**
     * Allow a new delay before the automatic cache clear is done.
     */
    private void resetPurgeTimer() {
        purgeHandler.removeCallbacks(purger);
        purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
    }

    private String filePathForUrl(String url) {
        String filePath = null;
        if (mContext != null) {
            filePath = mContext.getCacheDir().getAbsolutePath() + "/" + MD5Converter.hash(url);
        }
        return filePath;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if ((reqWidth > 0 && reqHeight > 0)) {
            if (height > reqHeight || width > reqWidth) {

                // Calculate ratios of height and width to requested height and
                // width
                final int heightRatio = Math.round((float) height / (float) reqHeight);
                final int widthRatio = Math.round((float) width / (float) reqWidth);

                // Choose the smallest ratio as inSampleSize value, this will
                // guarantee
                // a final image with both dimensions larger than or equal to
                // the
                // requested height and width.
                inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            }
        }

        return inSampleSize;
    }

    public Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = createBitmapFactoryOptions();

        if (reqWidth == 0 && reqHeight == 0) {
            return BitmapFactory.decodeFile(path, options);
        }
        else {
            // First decode with inJustDecodeBounds=true to check dimensions
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            DebugTools.d("Decode file with sample size : " + options.inSampleSize);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(path, options);
        }
    }

    public Bitmap decodeSampledBitmapFromStream(InputStream stream, int reqWidth, int reqHeight) throws IOException {
        if (reqWidth == 0 && reqHeight == 0) {
            return BitmapFactory.decodeStream(stream);
        }
        else {
            final BitmapFactory.Options options = createBitmapFactoryOptions();

            // Copy stream in order to read it 2 times (decode image size then
            // decode image data)
            byte[] byteStream = IoTools.getBytesFromStream(stream);

            InputStream inputStream1 = new ByteArrayInputStream(byteStream);
            InputStream inputStream2 = new ByteArrayInputStream(byteStream);

            // First decode with inJustDecodeBounds=true to check dimensions
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream1, null, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            DebugTools.d("Decode stream with sample size : " + options.inSampleSize);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(inputStream2, null, options);
        }
    }

}
