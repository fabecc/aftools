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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;

/**
 * TTSEngine. This class is a wrapper of the Android TextToSpeech object. It
 * deals with the initialization and the resource releasing.
 */
public class TTSEngine implements TextToSpeech.OnInitListener {

    private final String TAG = "TTSEngine";

    private TextToSpeech mTts;

    private OnUtteranceCompletedListener mOnUtteranceListener;

    /**
     * Constructor used to initialize the TTS Engine. You MUST call this method
     * before using TTS, for example in your Activity.onCreate()
     *
     * @param context Application context
     */
    public TTSEngine(Context context) {
        // TTS Init
        mTts = new TextToSpeech(context, this);
    }

    /**
     *
     * Constructor used to initialize the TTS Engine. You MUST call this method
     * before using TTS, for example in your Activity.onCreate()
     *
     * @param context Application context
     * @param listener Listener that will catch onUtteranceCompleted events
     */
    public TTSEngine(Context context, OnUtteranceCompletedListener listener) {
        // TTS Init
        mTts = new TextToSpeech(context, this);
        mOnUtteranceListener = listener;
    }

    /**
     * Release the TTS engine. You MUST call this method when you are done using
     * TTS. For example, in your Activity.onDestroy()
     */
    public void release() {
        // shutdown TTS
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
    }

    /**
     * Synthesize a given text with the default language
     *
     * @param text Text to synthesize
     */
    public void speak(String text) {
        if (mTts == null)
            return;

        mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    /**
     *
     * Synthesize a given text with the default language
     *
     * @param text Text to synthesize
     * @param hashMap Parameters for the request
     *
     */
    public void speak(String text, HashMap<String, String> hashMap) {
        if (mTts == null)
            return;

        mTts.speak(text, TextToSpeech.QUEUE_FLUSH, hashMap);
    }

    /**
     * Synthesize the given text cut into several parts. The different parts
     * will be spoken in the same order as the given list
     *
     * @param listOfText List of text parts to synthesize
     */
    public void speak(ArrayList<String> listOfText) {
        if (mTts == null)
            return;

        mTts.speak(listOfText.get(0), TextToSpeech.QUEUE_FLUSH, null);
        for (int i = 1; i < listOfText.size(); i++) {
            mTts.speak(listOfText.get(i), TextToSpeech.QUEUE_ADD, null);
        }
    }

    public void speak(ArrayList<String> listOfText, HashMap<String, String> hashMap) {
        if (mTts == null)
            return;

        mTts.speak(listOfText.get(0), TextToSpeech.QUEUE_FLUSH, null);
        for (int i = 1; i < listOfText.size()-1; i++) {
            mTts.speak(listOfText.get(i), TextToSpeech.QUEUE_ADD, null);
        }
        mTts.speak(listOfText.get(listOfText.size()-1), TextToSpeech.QUEUE_ADD, hashMap);
   }

    /**
     * Synthesize a given text with a given language. The given language is used
     * temporary and the previous language is restored after the synthesize
     *
     * @param text Text to synthesize
     * @param locale Language to use with the given text (@see java.util.Locale)
     */
    public void speakToLocale(String text, Locale locale) {
        if (mTts == null)
            return;

        // save previous language
        Locale previousLocale = mTts.getLanguage();
        mTts.setLanguage(locale);

        // speak
        speak(text);

        // restore previous language
        mTts.setLanguage(previousLocale);
    }

    /**
     * Synthesize the given text cut into several parts with a given language.
     * The given language is used temporary and the previous language is
     * restored after the synthesize. The different parts will be spoken in the
     * same order as the given list
     *
     * @param listOfText List of text parts to synthesize
     * @param locale Language to use with the given text (@see java.util.Locale)
     */
    public void speakToLocale(ArrayList<String> listOfText, Locale locale) {
        if (mTts == null)
            return;

        // save previous language
        Locale previousLocale = mTts.getLanguage();
        mTts.setLanguage(locale);

        // speak
        speak(listOfText);

        // restore previous language
        mTts.setLanguage(previousLocale);
    }

    /**
     * Check if the TTS Engine is speaking
     *
     * @return true if TTS is speaking, false otherwise
     */
    public Boolean isSpeaking() {
        return mTts.isSpeaking();
    }

    /**
     * Stop the current synthesize, if any
     */
    public void stop() {
        if (mTts == null)
            return;

        mTts.stop();
    }

    // Implements TextToSpeech.OnInitListener.
    public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            // Set language to default locale
            int result = mTts.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Language data is missing or the language is not supported.
                Log.e(TAG, "Language is not available.");
            } else {
                // The TTS engine has been successfully initialized.
                Log.d(TAG, "TTS engine is initialized");
                // We set the OnUtteranceCompletedListener that was given in the constructor
                if (mOnUtteranceListener!= null) {
                    mTts.setOnUtteranceCompletedListener(mOnUtteranceListener);
                }
            }
        } else {
            // Initialization failed.
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }

}
