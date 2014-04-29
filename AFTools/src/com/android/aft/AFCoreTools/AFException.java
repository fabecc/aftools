package com.android.aft.AFCoreTools;

@SuppressWarnings("serial")
public class AFException extends RuntimeException {

    public String module;
    public String method;
    public String description;

    public AFException(String module, String method, String description) {
        super(module + "::" + method + " : " + description);
        this.module = module;
        this.method = method;
        this.description = description;
    }

}
