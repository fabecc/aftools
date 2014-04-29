package com.android.aft.test.TestAFAppManager;

import com.android.aft.AFAppSettings.AFAppSettingsContentProvider;
import com.android.aft.AFCoreTools.DebugTools;

public class Settings extends AFAppSettingsContentProvider {

    public enum HttpMode {
        Get,
        Post
    };

	public static String host = "http://www.ma_value.fr";
	public static int port = 42;
    public static String request = "get_auth";
    public static int user_id = 42;
    public static HttpMode mode = HttpMode.Get;
    public static boolean connect_at_start = true;

    private static int ACTION_FLUSH = 421;
    private static int ACTION_DUMP_TRACE = 4578;

	public Settings() {
	    try {
            addEntry("host", "http://www.ma_value.fr");
            addEntry("port", 42);
            addEntry("request", "get_auth", "list_player", "read_matches");
            addEntry("user_id", 42, 51, 69);
            addEntry("mode", HttpMode.values());
            addEntry("connect_at_start", true);
            addAction(ACTION_FLUSH, "Flush data");
            addAction(ACTION_DUMP_TRACE, "Dump communication trace");
        } catch (Exception e) {
        	DebugTools.e("Failed to configure SettingsProvider", e);
        }
	}

	@Override
    protected void onAttributeUpdated(String name) {
         super.onAttributeUpdated(name);

         if (name.equals("mode")) {
        	 DebugTools.d("Settings of 'mode' us updated with: " + mode);
         }
    }

	@Override
	protected void onAction(int id) {
		if (id == ACTION_FLUSH)
			DebugTools.d("Action: Flush");
		else if (id == ACTION_DUMP_TRACE)
			DebugTools.d("Action: Dump trace");
	}


}
