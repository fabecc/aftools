package com.android.aft.AFNetworkConnection;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;

import android.net.http.AndroidHttpClient;

/**
 * The result of a webservice call. Contain the Header of the response and the
 * body of the response as an unparsed String
 */
public class AFNetworkConnectionResult {

    // Http client
    public HttpClient mHttpClient;
    public boolean mNeedToCloseClient = false;

    // Request
    public AFNetworkConnectionRequest mRequest;

    // Http request
    public HttpUriRequest mHttpRequest;

    // Http response
    public HttpResponse mResponse;

    // List of result header
    public Header[] mResultHeader;

    // Result string
    public String mResult;

    /**
     * Http request result container.
     *
     * @param resultHeader
     * @param result
     */
    public AFNetworkConnectionResult(AFNetworkConnectionRequest request, HttpResponse response) {
        mRequest = request;
        mResponse = response;
        mResultHeader = response.getAllHeaders();
        mNeedToCloseClient = false;
    }

    /**
     * Close the http client if needed Call this method the request parameter
     * readHttpResponse was set to false
     */
    public void close() {
        if (mHttpClient == null)
            return;

        if (!(mHttpClient instanceof AndroidHttpClient))
            return;

        ((AndroidHttpClient) mHttpClient).close();
    }

    /**
     * Read true if there is result data to read
     *
     * @return
     */
    public boolean hasResult() {
        if (mResponse == null)
            return false;

        HttpEntity entity = mResponse.getEntity();
        if (entity == null)
            return false;

        return true;
    }

}
