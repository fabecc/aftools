package com.android.aft.AFAppSettings.exception;

@SuppressWarnings("serial")
public class BadFieldTypeAFAppSettingsException extends AFAppSettingsException {

    public BadFieldTypeAFAppSettingsException(String attribut_name) {
        super("Type mismatch between field '" + attribut_name + "' and given default value");
    }

}
