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

package com.android.aft.AFNetworkConnection.exception;

/**
 * Thrown to indicate that an exception occurred while downloading the
 * webservice data.
 *
 * @author Niji
 */
public class AFRestClientException extends Exception {

    private static final long serialVersionUID = 4658308128254827562L;

    private String mNewUrl;

    private String mResultStr;

    public int mStatusCode = 0;

    /**
     * Constructs a new {@link AFRestClientException} that includes the current
     * stack trace.
     */
    public AFRestClientException() {
        super();
    }

    public AFRestClientException(int statusCode, String detailMessage) {
        super(detailMessage);
        mStatusCode = statusCode;
    }

    /**
     * Constructs a new {@link AFRestClientException} that includes the current
     * stack trace, the specified detail message and the specified cause.
     *
     * @param detailMessage the detail message for this exception.
     * @param throwable the cause of this exception.
     */
    public AFRestClientException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Constructs a new {@link AFRestClientException} that includes the current
     * stack trace and the specified detail message.
     *
     * @param detailMessage the detail message for this exception.
     */
    public AFRestClientException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new {@link AFRestClientException} that includes the current
     * stack trace and the specified detail message.
     *
     * @param detailMessage the detail message for this exception.
     * @param redirection url.
     */
    public AFRestClientException(int statusCode, String detailMessage, String redirectionUrl) {
        super(detailMessage);
        mNewUrl = redirectionUrl;
        mStatusCode = statusCode;
    }

    /**
     * Constructs a new {@link AFRestClientException} that includes the current
     * stack trace and the specified cause.
     *
     * @param throwable the cause of this exception.
     */
    public AFRestClientException(Throwable throwable) {
        super(throwable);
    }

    public String getRedirectionUrl() {
        return mNewUrl;
    }

    /**
     * Constructs a new {@link AFRestClientException} that includes the current
     * stack trace, the specified detail message, redirection url and string of result
     *
     * @param detailMessage the detail message for this exception.
     * @param redirection url
     * @param string of result
     */
    public AFRestClientException(int statusCode, String detailMessage, String redirectionUrl, String resultStr) {
        super(detailMessage);
        mNewUrl = redirectionUrl;
        mStatusCode = statusCode;
        mResultStr = resultStr;
    }

    public String getResultStr() {
        return mResultStr;
    }

}
