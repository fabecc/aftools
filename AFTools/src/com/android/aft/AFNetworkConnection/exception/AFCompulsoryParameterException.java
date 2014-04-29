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
 * Thrown to indicate that a compulsory parameter is missing.
 *
 * @author Niji
 */
public class AFCompulsoryParameterException extends RuntimeException {

    private static final long serialVersionUID = -6031863210486494461L;

    /**
     * Constructs a new {@link AFCompulsoryParameterException} that includes the
     * current stack trace.
     */
    public AFCompulsoryParameterException() {
        super();
    }

    /**
     * Constructs a new {@link AFCompulsoryParameterException} that includes the
     * current stack trace, the specified detail message and the specified
     * cause.
     *
     * @param detailMessage the detail message for this exception.
     * @param throwable the cause of this exception.
     */
    public AFCompulsoryParameterException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Constructs a new {@link AFCompulsoryParameterException} that includes the
     * current stack trace and the specified detail message.
     *
     * @param detailMessage the detail message for this exception.
     */
    public AFCompulsoryParameterException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new {@link AFCompulsoryParameterException} that includes the
     * current stack trace and the specified cause.
     *
     * @param throwable the cause of this exception.
     */
    public AFCompulsoryParameterException(Throwable throwable) {
        super(throwable);
    }

}
