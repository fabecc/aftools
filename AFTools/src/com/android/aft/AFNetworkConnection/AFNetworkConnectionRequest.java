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

import java.util.ArrayList;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHeader;

import android.content.Context;
import android.text.TextUtils;

import com.android.aft.AFNetworkConnection.AFNetworkConnection.HttpMethod;
import com.android.aft.AFNetworkConnection.multipartentity.MultipartEntity;

public class AFNetworkConnectionRequest {

    // Application context used to create http client if not set
    public Context context = null;

    // User Agent used to create a http client if it is not set
    public String userAgent = null;

    // Url of the request
    public String url = null;

    public HttpMethod method = HttpMethod.Get;

    public Map<String, String> parameters = null;

    public String postText = null;

    public ArrayList<Header> headers = null;

    public boolean isGzipEnabled = false;

    // Set the using http client
    public HttpClient client = null;

    public boolean checkResponse = true;

    public boolean mFollowRedirect = true;

    // File to store the result data
    // Use to store result data in file instead of result string
    public String storeResultFile = null;

    // False to not read http response and return object in response without
    // closing http client
    // The response object is set in the NetworkConnectionResponse
    public boolean readHttpResponse = true;

    public MultipartEntity entity;

    //
    // Ctr
    //
    // There is a lot of method to help on the retro compatibility
    //

    public AFNetworkConnectionRequest(String url) {
        this.url = url;
    }

    public AFNetworkConnectionRequest(Context ctx, String url, HttpMethod method) {
        context = ctx;
        this.url = url;
        this.method = method;
    }

    public AFNetworkConnectionRequest(Context ctx, String url, HttpMethod method, Map<String, String> parameters) {
        context = ctx;
        this.url = url;
        this.method = method;
        this.parameters = parameters;
    }

    public AFNetworkConnectionRequest(Context ctx, String url, HttpMethod method, Map<String, String> parameters,
            ArrayList<Header> headers, boolean isGzipEnabled) {
        context = ctx;
        this.url = url;
        this.method = method;
        this.parameters = parameters;
        this.headers = headers;
        this.isGzipEnabled = isGzipEnabled;
    }

    public AFNetworkConnectionRequest(Context ctx, String url, HttpMethod method, Map<String, String> parameters,
            ArrayList<Header> headers, boolean isGzipEnabled, String userAgent) {
        context = ctx;
        this.url = url;
        this.method = method;
        this.parameters = parameters;
        this.headers = headers;
        this.isGzipEnabled = isGzipEnabled;
        this.userAgent = userAgent;
    }

    public AFNetworkConnectionRequest(Context ctx, String url, HttpMethod method, String postText,
            Map<String, String> parameters, ArrayList<Header> headers, boolean isGzipEnabled, String userAgent) {
        context = ctx;
        this.url = url;
        this.method = method;
        this.postText = postText;
        this.parameters = parameters;
        this.headers = headers;
        this.isGzipEnabled = isGzipEnabled;
        this.userAgent = userAgent;
    }

    public AFNetworkConnectionRequest(Context ctx, String url, HttpMethod method, String postText,
            Map<String, String> parameters, ArrayList<Header> headers, boolean isGzipEnabled, String userAgent,
            boolean checkResponse) {
        context = ctx;
        this.url = url;
        this.method = method;
        this.postText = postText;
        this.parameters = parameters;
        this.headers = headers;
        this.isGzipEnabled = isGzipEnabled;
        this.checkResponse = checkResponse;
        this.userAgent = userAgent;
    }

    public AFNetworkConnectionRequest(Context ctx, String url, MultipartEntity entity) {
        context = ctx;
        this.url = url;
        method = HttpMethod.Post;
        this.entity = entity;
    }

    public AFNetworkConnectionRequest(HttpClient client, String url, HttpMethod method, String postText,
            Map<String, String> parameters, ArrayList<Header> headers, boolean isGzipEnabled, boolean checkResponse) {
        this.client = client;
        this.url = url;
        this.method = method;
        this.postText = postText;
        this.parameters = parameters;
        this.headers = headers;
        this.isGzipEnabled = isGzipEnabled;
        this.checkResponse = checkResponse;
    }

    public AFNetworkConnectionRequest(HttpClient client, String url, HttpMethod method, String postText,
            Map<String, String> parameters, ArrayList<Header> headers, boolean isGzipEnabled, boolean checkResponse,
            boolean followRedirect) {
        this.client = client;
        this.url = url;
        this.method = method;
        this.postText = postText;
        this.parameters = parameters;
        this.headers = headers;
        this.isGzipEnabled = isGzipEnabled;
        this.checkResponse = checkResponse;
        mFollowRedirect = followRedirect;
    }

    //
    // Accessor
    //

    public boolean hasToStoreResultInFile() {
        return !TextUtils.isEmpty(storeResultFile);
    }

    public void addHeader(Header header) {
        if (headers == null)
            headers = new ArrayList<Header>();

        headers.add(header);
    }

    public void addHeader(String name, String value) {
        addHeader(new BasicHeader(name, value));
    }

}
