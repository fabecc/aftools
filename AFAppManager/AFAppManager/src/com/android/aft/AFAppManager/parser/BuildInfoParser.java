package com.android.aft.AFAppManager.parser;

import com.android.aft.AFAppManager.model.ApplicationBuildInfo;
import com.android.aft.AFAppManager.model.DeliveryInfo;
import com.android.aft.AFCuteXmlParser.AFCuteXmlParser;
import com.android.aft.AFCuteXmlParser.AFNodeAction;

public class BuildInfoParser extends AFCuteXmlParser {

    public BuildInfoParser() {

        addNodeAction(new AFNodeAction<Object>("project") {
            @Override
            public void onNode(Object cookie) {
                ApplicationBuildInfo info = new ApplicationBuildInfo();

                info.name = getStringAttribute("name");
                info.revision = getStringAttribute("revision");

                read_children(info);

                getContext().getResult().setData(info);
            }
        });

        addNodeAction(new AFNodeAction<ApplicationBuildInfo>("delivery") {
            @Override
            public void onNode(ApplicationBuildInfo info) {
                DeliveryInfo d = new DeliveryInfo(info.directory);

                d.name = getStringAttribute("name");
                d.mode = getStringAttribute("mode");
                d.useReleaseKey = getBooleanAttribute("release_key");
                d.isSigned = getBooleanAttribute("signed");

                info.addDelevery(d);
            }
        });

    }

}
