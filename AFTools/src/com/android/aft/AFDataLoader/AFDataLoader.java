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

package com.android.aft.AFDataLoader;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.util.Log;

import com.android.aft.AFApplicationHelper.config.AFConfig;
import com.android.aft.AFCoreTools.AsyncTaskWrapper;

/**
 * NFDataLoader<T> is used to manage easily a loading process and thread, listener management.
 *
 * To use this loader, you have to inherit this class and write the loading code
 * inside the method doLoad().
 *
 * After, there is two ways to launch loading:
 * - load() => return only when the load is finish (successful or with error)
 * - loadAsync() => return directly and call the listener when the load is finish
 *
 * The generic parameter T is the type of class to represent your data after loading.
 * For example:
 * - use a T as String, if you load result data can be set in a String.
 * - use your more complex object like 'NewsList' as result of a webservice to get a list of news.
 *
 * For more complex case, take a look of the enum FinishLoadDetectionMode.
 */
public abstract class AFDataLoader<T> {

    /**
     * Error code (error begins at 10000 to limit conflict with other error code)
     *
     * @param begins
     * @return
     */
    public final static int DM_ERR_CODE_NO_ERROR = 0;
    public final static int DM_ERR_CODE_TIMEOUT = 10001;
    public final static int DM_ERR_CODE_INVALID_URL = 10002;

    /**
     * Listener interface to receive event while Data processing.
     *
     * @author Niji
     * @param <T>
     */
    public interface IDMDataListener<T> {
        // Called when data are finish to load
        public void onDataAvailable(T data);

        public void onError(T data, int errCode, String errMsg);
    }

    /**
     * Method to implement to do load data.
     */
    protected abstract boolean doLoad(Context ctx) throws AFDataException;

    /**
     * Method to implement to reset data if needed when an error occurs.
     * (for example set field with default value, load special persistent data, ...)
     *
     * Use this only to set object state, not as a callback, there is the
     * listener for that.
     */
    protected abstract void setErrorState();

    /**
     * Enum to select how the end of loading is detected.
     * It is used to know when called the callback and release the thread locking.
     *
     * It is really useful when it is not possible to do all loading action in one method.
     * For example, when there is to wait action in another listener, ...)
     */
    public enum FinishLoadDetectionMode {
        /**
         * When the method doLoad return, loading is finish, listener can be called
         */
        FinishLoadAtReturnOfDoLoadMethod,
        /**
         * When the method setLoadAsFinish is called, listener can be called.
         */
        FinishLoadAtSetLoadAsFinishMethodCalled,
    }

    // Debug logger
    private static final String LOG_TAG = AFDataLoader.class.getSimpleName();

    // Data get by the loader
    protected T mData;

    /**
     * Listener
     */
    protected IDMDataListener<T> mListener;

    /**
     * Type of loading
     */
    protected FinishLoadDetectionMode mFinishLoadDetectionMode;

    /**
     * Timeout in case of load type asynchronous (in s.)
     */
    protected int mTimeout = -1;
    protected boolean mHasTimeouted = false;

    /**
     * Save the status to communicate result in load type async with load()
     */
    protected boolean mSavedStatus;

    // Error management
    protected int mErrCode;
    protected String mErrMsg;
    protected AFDataException mException;  //<! Exception throw by doLoad()

    // Synchronization management
    CountDownLatch mBarrier;

    /**
     * Default constructor
     */
    public AFDataLoader() {
        mFinishLoadDetectionMode = FinishLoadDetectionMode.FinishLoadAtReturnOfDoLoadMethod;
    }

    public T getData() {
        return mData;
    }

    /**
     * Async load data action. This method manage Async thread creation then
     * call method load param \ctx can be null if it is not used in doLoad()
     */
    public boolean loadAsync(Context ctx, IDMDataListener<T> listener) {
        setListener(listener);
        return loadAsync(ctx);
    }

    /**
     * param \ctx can be null if it is not used in doLoad()
     *
     * @param ctx
     * @return
     */
    public boolean loadAsync(Context ctx) {
        new DMDataAsyncLoader(ctx).executeParallel();
        return true;
    }

    /**
     * Sync load param \ctx can be null if it is not used in doLoad()
     */
    public boolean load(Context ctx) {
        // Initialize the barrier to wait end of loading in async mode
        if (mFinishLoadDetectionMode == FinishLoadDetectionMode.FinishLoadAtSetLoadAsFinishMethodCalled)
            mBarrier = new CountDownLatch(1);

        boolean status = processDataLoading(ctx);

        // Return status in synchronous mode
        if (mFinishLoadDetectionMode == FinishLoadDetectionMode.FinishLoadAtReturnOfDoLoadMethod)
            return status;

        if (AFConfig.INFO_LOGS_ENABLED) {
            Log.i(LOG_TAG, "Using DM in sync load for an Async load type can cause a lock ...");
        }
        // Wait end of loading
        while (true) {
            try {
                if (AFConfig.INFO_LOGS_ENABLED) {
                    Log.i(LOG_TAG, "Waiting ...");
                }
                if (mTimeout <= 0)
                    mBarrier.await();
                else {
                    status = mBarrier.await(mTimeout, TimeUnit.SECONDS);
                    if (!status) {
                        if (AFConfig.INFO_LOGS_ENABLED) {
                            Log.e(LOG_TAG, "DM load task timeout");
                        }
                        mErrCode = DM_ERR_CODE_TIMEOUT;
                        mErrMsg = "DM load task timeout";

                        mHasTimeouted = true;
                    }
                }
                if (AFConfig.INFO_LOGS_ENABLED) {
                    Log.i(LOG_TAG, "Waiting done.");
                }

                // Set status with saved value from setLaodAsFinish()
                status = mSavedStatus;

                break;
            } catch (InterruptedException e) {
                if (AFConfig.INFO_LOGS_ENABLED) {
                    Log.e(LOG_TAG, "An interruption arrived while waiting for end of DMData loading ...");
                }
            }
        }

        mBarrier = null;

        return status;
    }

    /**
     * Process the data loading, manage error
     *
     * @param ctx
     * @return
     */
    public boolean processDataLoading(Context ctx) {
        boolean res;
        try {
            res = doLoad(ctx);
        } catch (AFDataException e) {
            mException = e;
            mErrCode = e.getErrCode();
            mErrMsg = e.getMessage();
            setErrorState();
            if (AFConfig.INFO_LOGS_ENABLED) {
                Log.e(LOG_TAG, "An occurs during DMData loading [" + mErrCode + "]: " + mErrMsg);
            }
            return false;
        }
        return res;
    }

    /**
     * Set the data loading as finish to call listener
     *
     * @param status
     */
    protected void setLoadAsFinish(boolean status) {
        if (mFinishLoadDetectionMode != FinishLoadDetectionMode.FinishLoadAtSetLoadAsFinishMethodCalled)
            return;

        mSavedStatus = status;

        if (mHasTimeouted)
            return;

        // if mBarrier is set, load was called by load() => countDown
        if (mBarrier != null)
            mBarrier.countDown();
        else
            // Else, call the listener
            callListener(mSavedStatus);
    }

    /**
     * Call listener
     *
     * @param status
     */
    public void callListener(boolean status) {
        if (mListener == null)
            return;

        if (status)
            mListener.onDataAvailable(mData);
        else
            mListener.onError(mData, mErrCode, mErrMsg);
    }

    /**
     * Accessor
     *
     * @param listener
     */
    public void setListener(IDMDataListener<T> listener) {
        mListener = listener;
    }

    public void setTimeout(int timeout) {
        mTimeout = timeout;
    }

    public void setErr(int errCode, String errMsg) {
        mErrCode = errCode;
        mErrMsg = errMsg;
    }

    public void setErrCode(int errCode) {
        mErrCode = errCode;
    }

    public int getErrCode() {
        return mErrCode;
    }

    public void setErrMsg(String errMsg) {
        mErrMsg = errMsg;
    }

    public String getErrMsg() {
        return mErrMsg;
    }

    public AFDataException getErrException() {
        return mException;
    }

    /**
     * Async class to call async call method of inherited class
     */
    class DMDataAsyncLoader extends AsyncTaskWrapper<Void, Void, Boolean> {

        protected Context mCtx;

        public DMDataAsyncLoader(Context ctx) {
            mCtx = ctx;
        }

        /**
         * Launch load request
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            boolean res = processDataLoading(mCtx);
            return Boolean.valueOf(res);
        }

        /**
         * Send result to listener
         */
        @Override
        protected void onPostExecute(Boolean status) {
            if (mListener == null)
                return;

            // Do not call listener in Asynchronous load if status is true
            if (mFinishLoadDetectionMode == FinishLoadDetectionMode.FinishLoadAtSetLoadAsFinishMethodCalled)
                return;

            // Call listener
            callListener(status);
        }
    }

}
