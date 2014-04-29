package com.android.aft.AFAppManager.model;

import com.android.aft.AFAppManager.AMConfig;

public class DeliveryInfo {

    public String name;
    public String mode;
    public boolean isSigned;
    public boolean useReleaseKey;

    public String appDirectory;

    public DeliveryInfo(String appDirectory) {
        this.appDirectory = appDirectory;
    }

    public String getApkUrl() {
        StringBuilder sb = new StringBuilder();

        sb.append(AMConfig.NIJI_JENKINS_URL)
          .append("/job/")
          .append(appDirectory)
          .append("/ws/")
          .append(name);

        return sb.toString();
    }

}
