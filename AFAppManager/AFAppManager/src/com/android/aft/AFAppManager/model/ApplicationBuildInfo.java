package com.android.aft.AFAppManager.model;

import java.util.ArrayList;

public class ApplicationBuildInfo {

    public String name;
    public String revision;
    public String directory;

    public ArrayList<DeliveryInfo> mDeliveries = new ArrayList<DeliveryInfo>();

    public void addDelevery(DeliveryInfo d) {
        mDeliveries.add(d);
    }

}
