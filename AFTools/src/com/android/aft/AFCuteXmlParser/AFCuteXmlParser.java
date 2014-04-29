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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Vector;

import android.text.TextUtils;

import com.android.aft.AFCoreTools.DebugTools;
import com.android.aft.AFCuteXmlParser.LowParser.PullXmlParser.AFPullXmlLowXmlParser;

/**
 * CuteXmlParser
 *
 * CuteXmlParser is a sort of SAX parser but with a simple API to
 * define some action to do at parsing time of wanted node. It is an recursive
 * parser on function read_children whose read all children tag from current
 * point and call read_children on all of its. To do some treatment on parsing,
 * using of this parser need to define and set ActionNode object and implement
 * the method onNode with wanted code to do on the current node. On each find
 * node, test is done to know if an actions is defined for the node, and exists,
 * called is onNode method.
 */
public class AFCuteXmlParser {

    // Low level parser
    public static AFLowXmlParser parser = new AFPullXmlLowXmlParser();

    // Debug
    public final static DebugTools.Logger dbg = new DebugTools.Logger("NFCuteXmlParser");
    public static boolean hasDebug = false;

    // List of action
    protected Hashtable<String, Vector<AFNodeAction<?>>> mActions;

    public AFCuteXmlParser() {
        mActions = new Hashtable<String, Vector<AFNodeAction<?>>>();
    }

    /**
     * Parser entry point to launch parsing of xml data
     *
     * @param xml data
     * @return parsing result object
     */
    public final AFCuteXmlParserResult parse(String xml) {
        return parse(new ByteArrayInputStream(xml.getBytes()));
    }

    /**
     * Parser entry point to launch parsing of xml data
     *
     * @param xml data
     * @return parsing result object
     */
    public final AFCuteXmlParserResult parse(InputStream xml) {
        InputStreamReader xml_reader = null;
        try {
            xml_reader = new InputStreamReader(xml);
        } catch (Exception e) {
            DebugTools.e("Encoding error", e);
            AFCuteXmlParserResult result = new AFCuteXmlParserResult();
            result.setErrorCode(-1);
            result.setErrorMsg("Encoding error: " + e.getMessage());
            return result;
        }

        return parse(xml_reader);
    }

    /**
     * Parser entry point to launch parsing of xml data
     *
     * @param xml data
     * @return parsing result object
     */
    public final AFCuteXmlParserResult parse(InputStreamReader xml) {
        dbg.d("Launch parsing");

        // Initialize context
        AFCuteXmlParserContext context = createContext();

        if (!parser.init(context, xml)) {
            AFCuteXmlParserResult result = context.getResult();
            result.setErrorCode(1);
            return result;
        }

        start(context);

        // Create a marker root node
        context.pushNode(new AFXmlTag("<root>"));

        // Call parsing from root node
        boolean parseStatus = read_children(context);

        finish(context, parseStatus);

        dbg.d("Parsing finish: " + parseStatus);
        AFCuteXmlParserResult result = context.getResult();
        if (!parseStatus && result.getErrorCode() == 0)
            result.setErrorCode(1);

        return result;
    }

    /**
     * Create and return the context object to use for parsing data
     *
     * @return The fresh parsing context
     */
    protected AFCuteXmlParserContext createContext() {
        return new AFCuteXmlParserContext();
    }

    /**
     * This method is called when the parsing start
     * Inherit this method to catch the parsing start
     *
     * @param ctx
     */
    protected void start(AFCuteXmlParserContext ctx) {
        // Nothing to do
    }

    /**
     * This method is called when the parsing is finish
     * Inherit this method to catch the parsing finish
     *
     * @param ctx
     * @param status
     */
    protected void finish(AFCuteXmlParserContext ctx, boolean status) {
        // Nothing to do
    }

    /**
     * Add an action node
     *
     * @param action
     * @return true if action node is added
     */
    public final boolean addNodeAction(AFNodeAction<?> action) {
        String nodeName = action.getNodeName();
        if (hasDebug)
            dbg.d("Add action for node name " + nodeName);

        action.setParser(this);

        if (!mActions.containsKey(nodeName))
            mActions.put(nodeName, new Vector<AFNodeAction<?>>());

        mActions.get(nodeName).addElement(action);

        return true;
    }

    /**
     * Read children node from current position
     *
     * @param ctx Parsing context
     * @return true if parsing success
     */
    public final boolean read_children(AFCuteXmlParserContext ctx) {
        return read_children(ctx, true);
    }

    /**
     * Read children node from current position
     *
     * @param ctx Parsing context
     * @param check_action Test action
     * @return true if parsing success
     */
    public final boolean read_children(AFCuteXmlParserContext ctx, boolean check_action) {
        ctx.hasReadChildren(true);

        // Direct return if current is not a start tag
        if (ctx.getCurrent().getType() != AFXmlTag.TAG_TYPE_START)
            return true;

        boolean status = true;

        // Loop on each child
        ctx.increase_depth();
        while (true) {
            // Read next tag
            AFXmlTag tag = read_tag(ctx);
            if (tag == null) {
                status = false;
                break;
            }
            else if (TextUtils.isEmpty(tag.getName())) {
                break;
            }

            // If tag is closing of current => finish to read children
            if (tag.isFinalOf(ctx.getCurrent()))
                break;

            if (hasDebug) {
                dbg.v(indent(ctx.getDepth()) + "- Node[" + tag.getType() + "]: " + tag.getName());
                Hashtable<String, String> attributs = tag.getAttributs();
                if (attributs != null)
                    for (String key : attributs.keySet())
                        dbg.v(indent(ctx.getDepth()) + "  _ attribut: " + key + ": " + attributs.get(key));
            }

            // Push current tag
            ctx.pushNode(tag);

            boolean hasReadChildren = false;
            if (check_action) {
                hasReadChildren = tryToCallActionNode(ctx);
                if (ctx.requestStopParser()) {
                    status = false;
                    break ;
                }
            }

            // Read children (to eat token) if was not done by action node
            if (!hasReadChildren)
                if (tag.getType() == AFXmlTag.TAG_TYPE_START) {
                    status = read_children(ctx, check_action);
                    if (!status)
                        break;
                }

            // Reset current tag to parent
            ctx.popNode();

            // End of parsing if return on <root>
            if (!ctx.acceptSeveralNodeOnRoot()) {
                if (ctx.getCurrent().getName().equals("<root>"))
                    break;
            }
        }
        ctx.decrease_depth();

        return status;
    }

    /**
     * Try to call a defined action node on current xml parsing node
     *
     * @param ctx Parsing context
     *
     * @return true if the children has been read
     */
    private final boolean tryToCallActionNode(AFCuteXmlParserContext ctx) {
        if (!mActions.containsKey(ctx.getCurrent().getName()))
            return false;

        for (AFNodeAction<?> action : mActions.get(ctx.getCurrent().getName())) {
            if (action.isCorrectNode(ctx)) {
                // Set current context
                action.setContext(ctx);

                // Store old read children status to reset it after
                boolean oldReadChildrenStatus = ctx.hasReadChildren();

                // Tag to know if children are read or not when call action
                ctx.hasReadChildren(false);

                action.onNodeBridge(ctx.getCookie());

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

    /**
     * Read the next xml tag
     *
     * @param ctx
     * @return xml tag
     */
    private final AFXmlTag read_tag(AFCuteXmlParserContext ctx) {
        return parser.readTag(ctx);
    }

    /**
     * Read content into STag and ETag Parsing is considered at the end of an
     * STag, so return text until beginning of an ETag Example: <a>CONTENT</a>
     * => "CONTENT"
     *
     * @param ctx Parsing context
     * @return content
     */
    public final String read_content(AFCuteXmlParserContext ctx) {
        return parser.readContent(ctx);
    }

    private final String indent(int depth) {
        StringBuffer str = new StringBuffer();

        for (; depth != 0; --depth)
            str.append("  ");

        return str.toString();
    }

}
