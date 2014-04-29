package com.android.aft.AFAppSettings.exception;

@SuppressWarnings("serial")
public class BadFieldNameAFAppSettingsException extends AFAppSettingsException {

    public BadFieldNameAFAppSettingsException(String class_name, String attribut_name) {
        super("Cannot find atribut " + attribut_name + " in class " + class_name);
    }

}
