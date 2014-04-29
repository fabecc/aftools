package com.android.aft.AFAppManager.loader;

import android.content.Context;

import com.android.aft.AFAppManager.AMConfig;
import com.android.aft.AFAppManager.model.ApplicationBuildInfo;
import com.android.aft.AFAppManager.model.DeliveryInfo;
import com.android.aft.AFAppManager.parser.BuildInfoParser;
import com.android.aft.AFCoreTools.DebugTools;
import com.android.aft.AFCuteXmlParser.AFCuteXmlParser;
import com.android.aft.AFCuteXmlParser.AFCuteXmlParserResult;
import com.android.aft.AFDataLoader.AFDataException;
import com.android.aft.AFDataLoader.AFDataLoader;
import com.android.aft.AFNetworkConnection.AFNetworkConnection;
import com.android.aft.AFNetworkConnection.AFNetworkConnectionResult;

public class GetApplicationBuildInfoLoader extends AFDataLoader<ApplicationBuildInfo> {

    private String mProjectDir;

    public GetApplicationBuildInfoLoader(String projectDir) {
        mProjectDir = projectDir;
    }

    @Override
    protected boolean doLoad(Context ctx) throws AFDataException {
        DebugTools.TimeLogger tl = new DebugTools.TimeLogger(mProjectDir + " - build-info.xml");

        // Dl data
        tl.step("Download file");
        AFNetworkConnectionResult result_dl;
        try {
            result_dl = new AFNetworkConnection().wget(getBuildInfoUrl());
        } catch (Exception e) {
            DebugTools.e("Cannot download build-info file", e);
            tl.finish();
            return false;
        }

        // Dump data
        DebugTools.stringdump("build-info.xml", result_dl.mResult);

        // Parse data
        tl.step("Parse data");
        AFCuteXmlParser parser = new BuildInfoParser();
        AFCuteXmlParserResult result_parser = parser.parse(result_dl.mResult);

        if (!result_parser.status()) {
            DebugTools.d("Parsing failed");
            tl.finish();
            return false;
        }

        mData = (ApplicationBuildInfo)result_parser.getData();
        mData.directory = mProjectDir;
        for (DeliveryInfo i: mData.mDeliveries)
            i.appDirectory = mProjectDir;

        tl.finish();

        return true;
    }

    private String getBuildInfoUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append(AMConfig.NIJI_JENKINS_URL)
          .append("/job/")
          .append(mProjectDir)
          .append("/ws/build-info.xml");

        return sb.toString();
    }

    @Override
    protected void setErrorState() {
        // TODO Auto-generated method stub
    }

}
