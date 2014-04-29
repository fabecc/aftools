package com.android.aft.AFCuteXmlParser;

import java.io.InputStreamReader;

public abstract class AFLowXmlParser {

    public abstract boolean init(AFCuteXmlParserContext context, InputStreamReader xml);

    public abstract AFXmlTag readTag(AFCuteXmlParserContext ctx);

    public abstract String readContent(AFCuteXmlParserContext ctx);

}
