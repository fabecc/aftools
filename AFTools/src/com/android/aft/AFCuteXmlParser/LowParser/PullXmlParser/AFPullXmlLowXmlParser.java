package com.android.aft.AFCuteXmlParser.LowParser.PullXmlParser;

import java.io.InputStreamReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.text.TextUtils;

import com.android.aft.AFCuteXmlParser.AFCuteXmlParser;
import com.android.aft.AFCuteXmlParser.AFCuteXmlParserContext;
import com.android.aft.AFCuteXmlParser.AFLowXmlParser;
import com.android.aft.AFCuteXmlParser.AFXmlTag;

public class AFPullXmlLowXmlParser extends AFLowXmlParser {

    @Override
    public boolean init(AFCuteXmlParserContext context, InputStreamReader xml) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(xml);
            context.setXmlPullParser(xpp);
            context.setHasReadCurrentXmlPullParserToken(false);
        } catch (XmlPullParserException e) {
            AFCuteXmlParser.dbg.e("Cannot initialize XmlPullParser", e);
            return false;
        }

        return true;
    }

    @Override
    public AFXmlTag readTag(AFCuteXmlParserContext ctx) {
        XmlPullParser xpp = ctx.getXmlPullParser();

        // Read element until find a tag
        int eventType;
        try {
            if (ctx.hasReadCurrentXmlPullParserToken())
                xpp.next();

            eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG
                    || eventType == XmlPullParser.END_TAG)
                    break ;
                eventType = xpp.next();
            };
        } catch (Exception e) {
            AFCuteXmlParser.dbg.e("Cannot read next token", e);
            return null;
        }
        ctx.setHasReadCurrentXmlPullParserToken(true);

        String name = xpp.getName();
        if (!TextUtils.isEmpty(xpp.getPrefix()))
            name = xpp.getPrefix() + ":" + name;

        // Create tag
        AFXmlTag tag = new AFXmlTag(name);
        if (eventType == XmlPullParser.END_TAG)
            tag.setType(AFXmlTag.TAG_TYPE_END);

        // Read attributes
        for (int i = 0; i < xpp.getAttributeCount(); ++i)
            tag.setAttribut(xpp.getAttributeName(i), xpp.getAttributeValue(i));

        return tag;
    }

    @Override
    public String readContent(AFCuteXmlParserContext ctx) {
        String content = "";

        try {
            XmlPullParser xpp = ctx.getXmlPullParser();
            content = ctx.getXmlPullParser().nextText();
            // Workaround for bug with API 14
            if (xpp.getEventType() != XmlPullParser.END_TAG)
                xpp.next();
            ctx.setHasReadCurrentXmlPullParserToken(false);
        } catch (Exception e) {
            AFCuteXmlParser.dbg.e("Cannot read text", e);
        }

        return content;
    }

}
