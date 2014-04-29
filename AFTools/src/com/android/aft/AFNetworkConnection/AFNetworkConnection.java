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

package com.android.aft.AFNetworkConnection;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharArrayBuffer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import android.util.Log;

import com.android.aft.AFApplicationHelper.config.AFConfig;
import com.android.aft.AFCoreTools.IoTools;
import com.android.aft.AFNetworkConnection.exception.AFCompulsoryParameterException;
import com.android.aft.AFNetworkConnection.exception.AFRestClientException;

/**
 * This class gives the user methods to easily call a webservice and return the
 * received string
 */
public class AFNetworkConnection {

    public static int CONNECTION_TIMEOUT = 0;
    public static int SOCKET_TIMEOUT = 0;

    /**
     * Http client for connection. If not set, class will use android default
     * http client.
     */
    private HttpClient mHttpClient;

    private HttpContext mHttpContext;

    private static final String LOG_TAG = AFNetworkConnection.class.getSimpleName();

    // Method to use for the request
    public enum HttpMethod {
        Get, Post, Put, Delete
    }

    public AFNetworkConnection(final HttpClient client) {
        mHttpClient = client;
    }

    public AFNetworkConnection() {
        this(null);
    }

    public void setConnectionTimeOut(final int timeout) {
        CONNECTION_TIMEOUT = timeout;
    }

    public void setSocketTimeOut(final int timeout) {
        SOCKET_TIMEOUT = timeout;
    }

    public void setHttpContext(final HttpContext httpContext) {
        mHttpContext = httpContext;
    }

    // public void setHttp

    /**
     * Call a webservice and return the response
     *
     * @param url The url of the webservice
     * @return The {@link String} of the response
     * @throws IllegalStateException
     * @throws IOException
     * @throws URISyntaxException
     * @throws AFRestClientException
     */
    public String retrieveStringFromService(AFNetworkConnectionRequest request) throws IllegalStateException,
            IOException, URISyntaxException, AFRestClientException {
        AFNetworkConnectionResult networkConnectionResult = wget(request);
        return networkConnectionResult == null ? null : networkConnectionResult.mResult;
    }

    /**
     * Call a webservice and return the response
     *
     * @param url The url of the webservice
     * @return The {@link String} of the response
     * @throws IllegalStateException
     * @throws IOException
     * @throws URISyntaxException
     * @throws AFRestClientException
     */
    public String retrieveStringFromService(String url) throws IllegalStateException, IOException, URISyntaxException,
            AFRestClientException {
        AFNetworkConnectionResult networkConnectionResult = wget(new AFNetworkConnectionRequest(url));

        return networkConnectionResult == null ? null : networkConnectionResult.mResult;
    }

    /**
     * Call a webservice and return the response
     *
     * @param url The url of the webservice
     * @param method The method to use (must be one of the following :
     *            {@link #METHOD_GET}, {@link #METHOD_POST}, {@link #METHOD_PUT}
     *            , {@link #METHOD_DELETE}
     * @param ctx context to use for caching SSL sessions (may be null for no
     *            caching)
     * @return The {@link String} of the response
     * @throws IllegalStateException
     * @throws IOException
     * @throws URISyntaxException
     * @throws AFRestClientException
     */
    public String retrieveStringFromService(final String url, final HttpMethod method, final Context ctx)
            throws IllegalStateException, IOException, URISyntaxException, AFRestClientException {
        AFNetworkConnectionResult networkConnectionResult = wget(new AFNetworkConnectionRequest(ctx, url, method));

        return networkConnectionResult == null ? null : networkConnectionResult.mResult;
    }

    /**
     * Call a webservice and return the response
     *
     * @param url The url of the webservice
     * @param method The method to use (must be one of the following :
     *            {@link #METHOD_GET}, {@link #METHOD_POST}, {@link #METHOD_PUT}
     *            , {@link #METHOD_DELETE}
     * @param parameters The parameters to add to the request. This is a
     *            "key => value" Map.
     * @param ctx context to use for caching SSL sessions (may be null for no
     *            caching)
     * @return The {@link String} of the response
     * @throws IllegalStateException
     * @throws IOException
     * @throws URISyntaxException
     * @throws AFRestClientException
     */
    public String retrieveStringFromService(String url, HttpMethod method, Map<String, String> parameters,
            final Context ctx) throws IllegalStateException, IOException, URISyntaxException, AFRestClientException {
        AFNetworkConnectionResult networkConnectionResult = wget(new AFNetworkConnectionRequest(ctx, url, method,
                parameters));

        return networkConnectionResult == null ? null : networkConnectionResult.mResult;
    }

    /**
     * Call a webservice and return the response
     *
     * @param url The url of the webservice
     * @param method The method to use (must be one of the following :
     *            {@link #METHOD_GET}, {@link #METHOD_POST}, {@link #METHOD_PUT}
     *            , {@link #METHOD_DELETE}
     * @param parameters The parameters to add to the request. This is a
     *            "key => value" Map.
     * @param headers The headers to add to the request
     * @param isGzipEnabled Whether we should use gzip compression if available
     * @param ctx context to use for caching SSL sessions (may be null for no
     *            caching)
     * @return The {@link String} of the response
     * @throws IllegalStateException
     * @throws IOException
     * @throws URISyntaxException
     * @throws AFRestClientException
     */
    public String retrieveStringFromService(String url, HttpMethod method, Map<String, String> parameters,
            ArrayList<Header> headers, boolean isGzipEnabled, final Context ctx) throws IllegalStateException,
            IOException, URISyntaxException, AFRestClientException {
        AFNetworkConnectionResult networkConnectionResult = wget(new AFNetworkConnectionRequest(ctx, url, method,
                parameters, headers, isGzipEnabled));

        return networkConnectionResult == null ? null : networkConnectionResult.mResult;
    }

    /**
     * Call a webservice and return the response
     *
     * @param url The url of the webservice
     * @param method The method to use (must be one of the following :
     *            {@link #METHOD_GET}, {@link #METHOD_POST}, {@link #METHOD_PUT}
     *            , {@link #METHOD_DELETE}
     * @param parameters The parameters to add to the request. This is a
     *            "key => value" Map.
     * @param headers The headers to add to the request
     * @param isGzipEnabled Whether we should use gzip compression if available
     * @param userAgent The user agent to set in the request. If not given, the
     *            default one will be used
     * @param ctx context to use for caching SSL sessions (may be null for no
     *            caching)
     * @return The {@link String} of the response
     * @throws IllegalStateException
     * @throws IOException
     * @throws URISyntaxException
     * @throws AFRestClientException
     */
    public String retrieveStringFromService(String url, HttpMethod method, Map<String, String> parameters,
            ArrayList<Header> headers, boolean isGzipEnabled, String userAgent, final Context ctx)
            throws IllegalStateException, IOException, URISyntaxException, AFRestClientException {
        AFNetworkConnectionResult networkConnectionResult = wget(new AFNetworkConnectionRequest(ctx, url, method,
                parameters, headers, isGzipEnabled, userAgent));

        return networkConnectionResult == null ? null : networkConnectionResult.mResult;
    }

    /**
     * Call a webservice and return the response
     *
     * @param url The url of the webservice
     * @param method The method to use (must be one of the following :
     *            {@link #METHOD_GET}, {@link #METHOD_POST}, {@link #METHOD_PUT}
     *            , {@link #METHOD_DELETE}
     * @param parameters The parameters to add to the request. This is a
     *            "key => value" Map.
     * @param postText A POSTDATA text that will be added in the request (only
     *            if the method is set to {@link #METHOD_POST})
     * @param headers The headers to add to the request
     * @param isGzipEnabled Whether we should use gzip compression if available
     * @param userAgent The user agent to set in the request. If not given, the
     *            default one will be used
     * @param ctx context to use for caching SSL sessions (may be null for no
     *            caching)
     * @return The {@link String} of the response
     * @throws IllegalStateException
     * @throws IOException
     * @throws URISyntaxException
     * @throws AFRestClientException
     */
    public final String retrieveStringFromService(final String url, final HttpMethod method,
            final Map<String, String> parameters, final String postText, final ArrayList<Header> headers,
            final boolean isGzipEnabled, final String userAgent, final Context ctx) throws IllegalStateException,
            IOException, URISyntaxException, AFRestClientException {
        AFNetworkConnectionResult networkConnectionResult = wget(new AFNetworkConnectionRequest(ctx, url, method, postText,
                parameters, headers, isGzipEnabled, userAgent));

        return networkConnectionResult == null ? null : networkConnectionResult.mResult;
    }

    /**
     * Call a webservice and return the response
     *
     * @param url The url of the webservice
     * @param method The method to use (must be one of the following :
     *            {@link #METHOD_GET}, {@link #METHOD_POST}, {@link #METHOD_PUT}
     *            , {@link #METHOD_DELETE}
     * @param parameters The parameters to add to the request. This is a
     *            "key => value" Map.
     * @param postText A POSTDATA text that will be added in the request (only
     *            if the method is set to {@link #METHOD_POST})
     * @param headers The headers to add to the request
     * @param isGzipEnabled Whether we should use gzip compression if available
     * @param userAgent The user agent to set in the request. If not given, the
     *            default one will be used
     * @param ctx context to use for caching SSL sessions (may be null for no
     *            caching)
     * @return A NetworkConnectionResult containing the response
     * @throws IllegalStateException
     * @throws IOException
     * @throws URISyntaxException
     * @throws AFRestClientException
     */
    public final AFNetworkConnectionResult retrieveStringAndHeadersFromService(String url, HttpMethod method,
            Map<String, String> parameters, String postText, ArrayList<Header> headers, boolean isGzipEnabled,
            String userAgent, final Context ctx) throws IllegalStateException, IOException, URISyntaxException,
            AFRestClientException {
        return wget(new AFNetworkConnectionRequest(ctx, url, method, postText, parameters, headers, isGzipEnabled,
                userAgent));
    }

    public final AFNetworkConnectionResult retrieveStringAndHeadersFromService(final String url, final HttpMethod method,
            final Map<String, String> parameters, final String postText, final ArrayList<Header> headers,
            final boolean isGzipEnabled, final String userAgent, final boolean checkResponse, final Context ctx)
            throws IllegalStateException, IOException, URISyntaxException, AFRestClientException {
        return wget(new AFNetworkConnectionRequest(ctx, url, method, postText, parameters, headers, isGzipEnabled,
                userAgent, checkResponse));
    }

    /**
     * Call a webservice and return the response
     *
     * @param url The url of the webservice
     * @param method The method to use (must be one of the following :
     *            {@link #METHOD_GET}, {@link #METHOD_POST}, {@link #METHOD_PUT}
     *            , {@link #METHOD_DELETE}
     * @param parameters The parameters to add to the request. This is a
     *            "key => value" Map.
     * @param postText A POSTDATA text that will be added in the request (only
     *            if the method is set to {@link #METHOD_POST})
     * @param headers The headers to add to the request
     * @param isGzipEnabled Whether we should use gzip compression if available
     * @param client HTTP client for the request. Mandatory
     * @param checkResponse Whether a {@link AFRestClientException} must be
     *            launched if we get a status code different from
     *            {@link HttpStatus#SC_OK}
     * @return A NetworkConnectionResult containing the response
     * @throws IllegalStateException
     * @throws IOException
     * @throws URISyntaxException
     * @throws AFRestClientException
     */
    public final AFNetworkConnectionResult retrieveStringAndHeadersFromService(final String url, final HttpMethod method,
            final Map<String, String> parameters, final String postText, final ArrayList<Header> headers,
            final boolean isGzipEnabled, final HttpClient client, final boolean checkResponse)
            throws IllegalStateException, IOException, URISyntaxException, AFRestClientException {
        return wget(new AFNetworkConnectionRequest(client, url, method, postText, parameters, headers, isGzipEnabled,
                checkResponse));
    }

    public final AFNetworkConnectionResult wget(String url) throws IllegalStateException, IOException,
            URISyntaxException, AFRestClientException {
        return wget(new AFNetworkConnectionRequest(url));
    }

    public final AFNetworkConnectionResult wget(AFNetworkConnectionRequest request) throws IllegalStateException,
            IOException, URISyntaxException, AFRestClientException {
        // Get the request URL
        if (request.url == null) {
            if (AFConfig.ERROR_LOGS_ENABLED) {
                Log.e(LOG_TAG, "retrieveStringFromService - Compulsory Parameter : request URL has not been set");
            }
            throw new AFCompulsoryParameterException("Request URL has not been set");
        }
        if (AFConfig.DEBUG_LOGS_ENABLED) {
            Log.d(LOG_TAG, "retrieveStringFromService - Request url : " + request.url);
        }

        // Get the request method
        if (request.method != HttpMethod.Get && request.method != HttpMethod.Post && request.method != HttpMethod.Put
                && request.method != HttpMethod.Delete) {
            if (AFConfig.ERROR_LOGS_ENABLED) {
                Log.e(LOG_TAG,
                        "retrieveStringFromService - Request method other than METHOD_GET, METHOD_POST are not implemented");
            }
            throw new IllegalArgumentException(
                    "retrieveStringFromService - Request method other than METHOD_GET, METHOD_POST are not implemented");
        }
        if (AFConfig.DEBUG_LOGS_ENABLED) {
            Log.d(LOG_TAG, "retrieveStringFromService - Request method : " + request.method);
        }

        // Get the request parameters
        if (AFConfig.DEBUG_LOGS_ENABLED) {
            Log.d(LOG_TAG, "retrieveStringFromService - Request parameters (number) : "
                    + ((request.parameters != null) ? request.parameters.size() : ""));
        }

        // Get the request headers
        if (AFConfig.DEBUG_LOGS_ENABLED) {
            Log.d(LOG_TAG, "retrieveStringFromService - Request headers (number) : "
                    + ((request.headers != null) ? request.headers.size() : ""));
        }

        // Set http client
        HttpClient client = null;
        if (mHttpClient != null) {
            client = mHttpClient;
            configureHttpClient(client);
        } else if (request.client != null)
            client = request.client;
        else {
            if (AFConfig.DEBUG_LOGS_ENABLED) {
                Log.d(LOG_TAG, "retrieveStringFromService - Request user agent : " + request.userAgent);
            }

            client = AndroidHttpClient.newInstance(request.userAgent, request.context);
        }

        // Result object
        AFNetworkConnectionResult result = null;
        HttpUriRequest uri_request = buildUriRequest(request);

        try {
            HttpResponse response = makeRequest(client, uri_request, request);
            StatusLine status = response.getStatusLine();
            if (AFConfig.DEBUG_LOGS_ENABLED) {
                Log.d(LOG_TAG, "retrieveStringFromService - Response status : " + status.getStatusCode());
            }

            while (status.getStatusCode() < HttpStatus.SC_OK
                    || status.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
                if (AFConfig.ERROR_LOGS_ENABLED) {
                    Log.e(LOG_TAG, "retrieveStringFromService - Invalid response from server : " + status.toString());
                }
                if (status.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY
                        || status.getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY) {
                    final Header newLocation = response.getFirstHeader("Location");
                    if (AFConfig.INFO_LOGS_ENABLED) {
                        Log.i(LOG_TAG, "New location : " + newLocation.getValue());
                    }
                    if (request.checkResponse) {
                        final String newLocationValue = newLocation.getValue();
                        if (request.mFollowRedirect) {
                            request.url = newLocationValue;
                            uri_request = buildUriRequest(request);
                            response = makeRequest(client, uri_request, request);
                            status = response.getStatusLine();
                        } else {
                            throw new AFRestClientException(status.getStatusCode(), "New location : " + newLocation,
                                    newLocationValue);
                        }
                    }
                }
                if (request.checkResponse && status.getStatusCode() != HttpStatus.SC_OK) {
                    String result_str = convertStream(response, request);
                    throw new AFRestClientException(status.getStatusCode(), "Invalid response from server : "
                            + status.toString(), null, result_str);
                }
            }

            // Read the response data
            if (request.readHttpResponse) {
                final String result_str = convertStream(response, request);
                result = new AFNetworkConnectionResult(request, response);
                result.mResult = result_str;
            } else {
                result = new AFNetworkConnectionResult(request, response);
                result.mHttpClient = client;
                result.mNeedToCloseClient = mHttpClient == null && request.client == null;
            }
            result.mHttpRequest = uri_request;

            if (AFConfig.INFO_LOGS_ENABLED) {
                if (!request.readHttpResponse)
                    Log.i(LOG_TAG, "retrieveStringFromService - Result need to be read with result object data");
                else if (!request.hasToStoreResultInFile())
                    Log.i(LOG_TAG, "retrieveStringFromService - Result from webservice : " + result.mResult);
                else
                    Log.i(LOG_TAG, "retrieveStringFromService - Result store in file : " + request.storeResultFile);
            }
        } finally {
            if (uri_request != null && request.readHttpResponse) {
                uri_request.abort();
            }

            if (mHttpClient == null && request.client == null && request.readHttpResponse) {
                if (AFConfig.DEBUG_LOGS_ENABLED) {
                    Log.d(LOG_TAG, "Close android http client");
                }
                ((AndroidHttpClient) client).close();
            }
        }
        return result;
    }

    private HttpUriRequest buildUriRequest(AFNetworkConnectionRequest request) throws UnsupportedEncodingException,
            URISyntaxException {
        // Create uri
        HttpUriRequest uri_request = null;

        switch (request.method) {

            case Post:
                final URI uriPost = new URI(request.url);
                uri_request = new HttpPost(uriPost);

                // Add the parameters to the POST request if any
                if (request.parameters != null && !request.parameters.isEmpty()) {

                    final List<NameValuePair> postRequestParameters = new ArrayList<NameValuePair>();
                    final ArrayList<String> keyList = new ArrayList<String>(request.parameters.keySet());
                    final int keyListLength = keyList.size();

                    for (int i = 0; i < keyListLength; i++) {
                        final String key = keyList.get(i);
                        postRequestParameters.add(new BasicNameValuePair(key, request.parameters.get(key)));
                    }

                    if (AFConfig.INFO_LOGS_ENABLED) {
                        Log.i(LOG_TAG, "retrieveStringFromService - POST Request - parameters list (key => value) : ");

                        final int postRequestParametersLength = postRequestParameters.size();
                        for (int i = 0; i < postRequestParametersLength; i++) {
                            final NameValuePair nameValuePair = postRequestParameters.get(i);
                            Log.i(LOG_TAG, "- " + nameValuePair.getName() + " => " + nameValuePair.getValue());
                        }
                    }

                    // uri_request.setHeader(HTTP.CONTENT_TYPE,
                    // "application/x-www-form-urlencoded");
                    ((HttpPost) uri_request).setEntity(new UrlEncodedFormEntity(postRequestParameters, "UTF-8"));
                }
                if (null != request.postText) {
                    ((HttpPost) uri_request).setEntity(new StringEntity(request.postText));
                }
                if (null != request.entity) {
                    ((HttpPost) uri_request).setEntity(request.entity);
                }
                break;
            case Delete:
                final URI uriDelete = new URI(request.url);
                uri_request = new HttpDelete(uriDelete);

                // Add the parameters to the DELETE request if any
                if (request.parameters != null && !request.parameters.isEmpty()) {

                    final List<NameValuePair> deleteRequestParameters = new ArrayList<NameValuePair>();
                    final ArrayList<String> keyList = new ArrayList<String>(request.parameters.keySet());
                    final int keyListLength = keyList.size();

                    for (int i = 0; i < keyListLength; i++) {
                        final String key = keyList.get(i);
                        deleteRequestParameters.add(new BasicNameValuePair(key, request.parameters.get(key)));
                    }

                    if (AFConfig.INFO_LOGS_ENABLED) {
                        Log.i(LOG_TAG, "retrieveStringFromService - DELETE Request - parameters list (key => value) : ");

                        final int deleteRequestParametersLength = deleteRequestParameters.size();
                        for (int i = 0; i < deleteRequestParametersLength; i++) {
                            final NameValuePair nameValuePair = deleteRequestParameters.get(i);
                            Log.i(LOG_TAG, "- " + nameValuePair.getName() + " => " + nameValuePair.getValue());
                        }
                    }

                }
                break;
            case Put:
                final URI uriPut = new URI(request.url);
                uri_request = new HttpPut(uriPut);

                // Add the parameters to the PUT request if any
                if (request.parameters != null && !request.parameters.isEmpty()) {

                    final List<NameValuePair> putRequestParameters = new ArrayList<NameValuePair>();
                    final ArrayList<String> keyList = new ArrayList<String>(request.parameters.keySet());
                    final int keyListLength = keyList.size();

                    for (int i = 0; i < keyListLength; i++) {
                        final String key = keyList.get(i);
                        putRequestParameters.add(new BasicNameValuePair(key, request.parameters.get(key)));
                    }

                    if (AFConfig.INFO_LOGS_ENABLED) {
                        Log.i(LOG_TAG, "retrieveStringFromService - PUT Request - parameters list (key => value) : ");

                        final int putRequestParametersLength = putRequestParameters.size();
                        for (int i = 0; i < putRequestParametersLength; i++) {
                            final NameValuePair nameValuePair = putRequestParameters.get(i);
                            Log.i(LOG_TAG, "- " + nameValuePair.getName() + " => " + nameValuePair.getValue());
                        }
                    }
                    if (null != request.postText) {
                        ((HttpPut) uri_request).setEntity(new StringEntity(request.postText));
                    }

                }
                break;
            case Get:
            default:
                final StringBuffer sb = new StringBuffer();
                sb.append(request.url);
                // Add the parameters to the GET url if any
                if (request.parameters != null && !request.parameters.isEmpty()) {
                    sb.append("?");

                    final ArrayList<String> keyList = new ArrayList<String>(request.parameters.keySet());
                    final int keyListLength = keyList.size();

                    for (int i = 0; i < keyListLength; i++) {
                        final String key = keyList.get(i);
                        sb.append(URLEncoder.encode(key, "UTF-8"));
                        sb.append("=");
                        sb.append(request.parameters.get(key));
                        if (i < keyListLength - 1) {
                            sb.append("&");
                        }
                    }
                }

                if (AFConfig.INFO_LOGS_ENABLED) {
                    Log.i(LOG_TAG, "retrieveStringFromService - GET Request - complete URL with parameters if any : "
                            + sb.toString());
                }

                final URI uri = new URI(sb.toString());
                uri_request = new HttpGet(uri);
        }
        // Add post text (send xml)

        // Add the request headers if any
        if (request.headers != null && !request.headers.isEmpty()) {

            final int headersLength = request.headers.size();

            for (int i = 0; i < headersLength; i++) {
                uri_request.addHeader(request.headers.get(i));
            }
        }
        return uri_request;
    }

    private HttpResponse makeRequest(HttpClient client, HttpUriRequest uri_request, AFNetworkConnectionRequest request)
            throws ClientProtocolException, IOException {

        HttpResponse response;
        // try {

        // Activate the gzip compression if asked
        if (request.isGzipEnabled) {
            AndroidHttpClient.modifyRequestToAcceptGzipResponse(uri_request);
        }

        if (AFConfig.INFO_LOGS_ENABLED) {
            Log.i(LOG_TAG, "retrieveStringFromService - Request - headers list (name => value) : ");

            final HeaderIterator iterator = uri_request.headerIterator();
            while (iterator.hasNext()) {
                final Header header = iterator.nextHeader();
                Log.i(LOG_TAG, "- " + header.getName() + " => " + header.getValue());
            }
        }

        if (AFConfig.DEBUG_LOGS_ENABLED) {
            Log.d(LOG_TAG, "retrieveStringFromService - Executing the request");
        }
        if (mHttpContext != null)
            response = client.execute(uri_request, mHttpContext);
        else
            response = client.execute(uri_request);
        if (AFConfig.INFO_LOGS_ENABLED) {
            Log.i(LOG_TAG, "retrieveStringFromService - Response - headers list (name => value) : ");
            for (Header header : response.getAllHeaders()) {
                Log.i(LOG_TAG, String.format("- %s => %s", header.getName(), header.getValue()));
            }
        }

        return response;
    }

    public String convertStream(HttpResponse response, AFNetworkConnectionRequest request) throws IOException {
        if (request.method != HttpMethod.Get && request.method != HttpMethod.Post)
            return null;

        HttpEntity entity = response.getEntity();
        if (entity == null)
            return null;

        Header contentEncoding = response.getFirstHeader("Content-Encoding");
        boolean isGzip = contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip");

        String result = null;
        if (request.hasToStoreResultInFile()) {
            convertHttpStreamToFile(entity.getContent(), isGzip, request.storeResultFile);
        } else {
            result = convertStreamToString(entity.getContent(), isGzip, request.method, (int) entity.getContentLength());
        }

        // Consume all content
        entity.consumeContent();

        return result;
    }

    /**
     * Transform an InputStream into a String
     *
     * @param is InputStream
     * @return String from the InputStream
     * @throws IOException If a problem occurs while reading the InputStream
     */
    public static String convertStreamToString(InputStream is, boolean isGzipEnabled, HttpMethod method,
            int contentLength) throws IOException {
        InputStream cleanedIs = is;
        if (isGzipEnabled) {
            cleanedIs = new GZIPInputStream(is);
        }

        try {
            switch (method) {
                case Get:
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(cleanedIs));
                    final StringBuilder sb = new StringBuilder();

                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }

                    return sb.toString();

                case Post:
                    int i = contentLength;
                    if (i < 0) {
                        i = 4096;
                    }

                    final Reader readerPost = new InputStreamReader(cleanedIs);
                    final CharArrayBuffer buffer = new CharArrayBuffer(i);
                    final char[] tmp = new char[1024];
                    int l;
                    while ((l = readerPost.read(tmp)) != -1) {
                        buffer.append(tmp, 0, l);
                    }
                    return buffer.toString();

                default:
                    return null;
            }
        } finally {
            cleanedIs.close();

            if (isGzipEnabled) {
                is.close();
            }
        }
    }

    /**
     * Set Http result InputStream in a file
     *
     * @param is InputStream
     * @return String from the InputStream
     * @throws IOException If a problem occurs while reading the InputStream
     */
    public static void convertHttpStreamToFile(InputStream is, boolean isGzipEnabled, String filepath)
            throws IOException {
        InputStream cleanedIs = is;
        if (isGzipEnabled) {
            cleanedIs = new GZIPInputStream(is);
        }

        OutputStream out = new FileOutputStream(filepath);

        IoTools.copy(cleanedIs, out);

        cleanedIs.close();

        if (isGzipEnabled) {
            is.close();
        }
    }

    /**
     * Configure the using http client
     *
     * @param client
     */
    private void configureHttpClient(HttpClient client) {
        final HttpParams httpParameters = client.getParams();

        if (CONNECTION_TIMEOUT > 0)
            HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);

        if (SOCKET_TIMEOUT > 0)
            HttpConnectionParams.setSoTimeout(httpParameters, SOCKET_TIMEOUT);
    }

    public static boolean isNetworkAvailable(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

}
