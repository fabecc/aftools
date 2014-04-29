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

package com.android.aft.AFCuteXmlParser;

public class AFCuteXmlParserResult {

    private int mErrCode;
    private String mErrMsg;
    private Object mData;

    public AFCuteXmlParserResult() {
        mErrCode = 0;
    }

    public AFCuteXmlParserResult(int errCode, String errMsg) {
        mErrCode = errCode;
        mErrMsg = errMsg;
    }

    public boolean status() {
        return mErrCode == 0;
    }

    public void setErrorCode(int errCode) {
        mErrCode = errCode;
    }

    public int getErrorCode() {
        return mErrCode;
    }

    public void setErrorMsg(String errMsg) {
        mErrMsg = errMsg;
    }

    public String getErrorMsg() {
        return mErrMsg;
    }

    public void setData(Object data) {
        mData = data;
    }

    public Object getData() {
        return mData;
    }

}
