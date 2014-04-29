package com.android.aft.AFCuteXmlParser.LowParser.HomeMade;

import java.io.InputStreamReader;

import com.android.aft.AFCuteXmlParser.AFCuteXmlParser;
import com.android.aft.AFCuteXmlParser.AFCuteXmlParserContext;
import com.android.aft.AFCuteXmlParser.AFLowXmlParser;
import com.android.aft.AFCuteXmlParser.AFXmlTag;

public class AFHomeMadeLowXmlParser extends AFLowXmlParser {

    @Override
    public boolean init(AFCuteXmlParserContext context, InputStreamReader xml) {
        context.setStream(xml);
        return true;
    }

    @Override
    public AFXmlTag readTag(AFCuteXmlParserContext ctx) {
        // Go to xml open tag token
        AFXmlToken tag_begin_token;
        do {
            tag_begin_token = AFXmlTokenizer.getNextToken(ctx);
            if (tag_begin_token == null || tag_begin_token.getType() == AFXmlToken.Type.EOF)
                return null;
        } while (tag_begin_token.getType() != AFXmlToken.Type.TAG_BEGIN
                && tag_begin_token.getType() != AFXmlToken.Type.TAG_BEGIN_TERMINAL);

        // Read id
        AFXmlToken tag_id = AFXmlTokenizer.getNextToken(ctx);
        if (tag_id.getType() != AFXmlToken.Type.STRING) {
            AFCuteXmlParser.dbg.e("Unexpected token " + tag_id.getType() + " expected tag id");
            return null;
        }

        // Create tag
        AFXmlTag tag = new AFXmlTag(tag_id.getValue());
        if (tag_begin_token.getType() == AFXmlToken.Type.TAG_BEGIN_TERMINAL)
            tag.setType(AFXmlTag.TAG_TYPE_END);

        // Read attributes until end tag token
        AFXmlToken tmp = AFXmlTokenizer.getNextToken(ctx);
        while (tmp.getType() != AFXmlToken.Type.TAG_END && tmp.getType() != AFXmlToken.Type.TAG_END_TERMINAL) {
            // Attribut name
            if (tmp.getType() != AFXmlToken.Type.STRING) {
                AFCuteXmlParser.dbg.e("Unexpected token " + tag_id.getType() + " expected tag '>' or '/>' or id");
                return null;
            }
            AFXmlToken attr_name = tmp;

            // Read equal token
            tmp = AFXmlTokenizer.getNextToken(ctx);
            if (tmp.getType() != AFXmlToken.Type.EQUAL && tmp.getType() != AFXmlToken.Type.STRING
                    && tmp.getType() != AFXmlToken.Type.TAG_END && tmp.getType() != AFXmlToken.Type.TAG_END_TERMINAL) {
                AFCuteXmlParser.dbg.e("Unexpected token " + tag_id.getType() + " expected tag '>' or '/>' or '=' or id");
                return null;
            }

            if (tmp.getType() != AFXmlToken.Type.EQUAL) {
                tag.setAttribut(attr_name.getValue(), "");
                continue;
            }

            // Read value
            AFXmlToken attr_value = AFXmlTokenizer.getNextToken(ctx);
            if (attr_value.getType() != AFXmlToken.Type.STRING) {
                AFCuteXmlParser.dbg.e("Unexpected token " + tag_id.getType() + " expected tag text");
                return null;
            }

            // Set attribut
            tag.setAttribut(attr_name.getValue(), attr_value.getValue());

            tmp = AFXmlTokenizer.getNextToken(ctx);
        }

        if (tmp.getType() == AFXmlToken.Type.TAG_END_TERMINAL)
            tag.setType(AFXmlTag.TAG_TYPE_START_END);

        return tag;
    }

    @Override
    public String readContent(AFCuteXmlParserContext context) {
        AFXmlToken content = AFXmlTokenizer.getNextToken(context);
        if (content.getType() == AFXmlToken.Type.STRING)
            return content.getValue();

        context.setCurrentToken(content);

        return "";
    }

}
