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

package com.android.aft.AFCuteJsonParser;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONTokener;

import android.content.Context;
import android.text.TextUtils;

import com.android.aft.AFCoreTools.DebugTools;
import com.android.aft.AFCoreTools.StringTools;

/**
 * NFCuteJsonParser
 */
public class AFCuteJsonParser {

    // List of action
    protected Hashtable<String, Vector<AFJsonAction<?>>> mActions;

    // dbg
    public final static DebugTools.Logger dbg = new DebugTools.Logger("NFCuteJsonParser");
    public static boolean hasDebug = true;

    public AFCuteJsonParser() {
        mActions = new Hashtable<String, Vector<AFJsonAction<?>>>();
    }

    /**
     * Parser entry point to launch parsing of xml data
     *
     * @param xml data
     * @return true if parsing success
     */
    public AFCuteJsonParserResult parse(InputStream json) {
        return parse(StringTools.getFullInputStreamData(json));
    }

    public AFCuteJsonParserResult parse(Context ctx, InputStream json) {
        return parse(ctx, StringTools.getFullInputStreamData(json));
    }

    /**
     * Parser entry point to launch parsing of xml data
     *
     * @param xml data
     * @return true if parsing success
     */
    public AFCuteJsonParserResult parse(String json_str) {
        return parse(null, json_str);
    }

    public AFCuteJsonParserResult parse(Context ctx, String json_str) {
        dbg.d("Launch parsing");

        if (json_str == null)
            return new AFCuteJsonParserResult(1, "Json parsing failed: input data is 'null'");

        // Low level parsing of the json string data
        Object json;
        try {
            json = new JSONTokener(json_str).nextValue();
        } catch (JSONException e) {
            dbg.e("Json parsing failed", e);
            return new AFCuteJsonParserResult(1, "Json parsing failed");
        }

        if(hasDebug)
            dbg.d("Json result object: " + json);

        // Create the context
        AFCuteJsonParserContext context = new AFCuteJsonParserContext();
        context.setApplicationContext(ctx);
        context.setCurrent(new AFJsonValue(AFJsonValue.ROOT_VALUE_NAME, json));

        // Read root element (and recursively all values)
        read_root(context);

        // Return parsing result
        return context.getResult();
    }

    /**
     * Read values from root point
     */
    private boolean read_root(AFCuteJsonParserContext ctx) {
        boolean hasReadChildren = tryToCallActionNode(ctx);

        // Read children (to eat token) if was not done by action node
        if (!hasReadChildren)
            return read_values(ctx);

        return true;
    }


    /**
     * Read children node from current position
     *
     * @param ctx Parsing context
     * @return true if parsing success
     */
    public boolean read_values(AFCuteJsonParserContext ctx) {
        ctx.hasReadChildren(true);

        // Direct return if there is no children from here
        if (!ctx.getCurrent().hasChildren())
            return true;

        boolean status = true;

        // Loop on each child
        for (AFJsonValue value: ctx.getCurrent().getChildren()) {

            // Push current tag
            ctx.pushValue(value);

            if (hasDebug) {
                if (!TextUtils.isEmpty(value.getName()))
                    dbg.v(indent(ctx.getValues().size()) + "- Value '" + value.getName() + "': " + value);
                else
                    dbg.v(indent(ctx.getValues().size()) + "- Value: " + value);
            }

            boolean hasReadChildren = tryToCallActionNode(ctx);

            // Read children (to eat token) if was not done by action node
            if (!hasReadChildren) {
                status = read_values(ctx);
                if (!status)
                    break;
            }

            // Reset current tag to parent
            ctx.popValue();
        }

        return status;
    }

    /**
     * Try to call a defined action node on current xml parsing node
     *
     * @param ctx Parsing context
     *
     * @return true if the children has been read
     */
    public boolean tryToCallActionNode(AFCuteJsonParserContext ctx) {
        if (ctx.getCurrent().getType() != AFJsonValue.JsonValueType.JsonArray
            && ctx.getCurrent().getType() != AFJsonValue.JsonValueType.JsonObject)
            return false ;

        for (AFJsonAction<?> action: getActionsPossibleAtValue(ctx, ctx.getCurrent().getName())) {
            if (action.isCorrectNode(ctx)) {
                // Set current context
                action.setContext(ctx);

                // Store old read children status to reset it after
                boolean oldReadChildrenStatus = ctx.hasReadChildren();

                // Tag to know if children are read or not when call action
                ctx.hasReadChildren(false);

                action.doActionBridge(ctx.getCookie());

                // ctx.hasReadChildren() pass to true imply children was read
                // during ActionNode::onNode processing
                boolean hasReadChildren = ctx.hasReadChildren();

                // Restore read children status
                ctx.hasReadChildren(oldReadChildrenStatus);

                return hasReadChildren;
            }
        }

        return false;
    }

    private Vector<AFJsonAction<?>> getActionsPossibleAtValue(AFCuteJsonParserContext ctx, String name) {
        Vector<AFJsonAction<?>> actions = new Vector<AFJsonAction<?>>();

        // Get action for current value name
        Vector<AFJsonAction<?>> v;

        String curName = ctx.getCurrent().getName();
        if (curName == null)
            curName = "";

        v = mActions.get(ctx.getCurrent().getName());
        if (v != null)
            actions.addAll(v);

        // Get anonymous action
        if (!curName.equals("")) {
            v = mActions.get("");
            if (v != null)
                actions.addAll(v);
        }

        return actions;
    }

    /**
     * Add an action node
     *
     * @param action
     * @return true if action node is added
     */
    public boolean addAction(AFJsonAction<?> action) {
        String valueName = action.getValueName();
        if (hasDebug)
            dbg.d("Add action for node name '" + valueName + "'");
        action.setParser(this);

        if (!mActions.containsKey(valueName))
            mActions.put(valueName, new Vector<AFJsonAction<?>>());

        mActions.get(valueName).addElement(action);

        return true;
    }

    private String indent(int depth) {
        StringBuffer str = new StringBuffer();

        for (; depth != 0; --depth)
            str.append("  ");

        return str.toString();
    }

}
