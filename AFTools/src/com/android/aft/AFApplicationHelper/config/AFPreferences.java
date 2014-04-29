package com.android.aft.AFApplicationHelper.config;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.aft.AFCoreTools.DebugTools;

public class AFPreferences {

    // Preferences module
    private static SharedPreferences mPreference;
    private static Context context;

    private static boolean encryptionIsEnabled = false;
    private static char[] encryptionKey;
    private static final String ENCRYPTION_ALGORITHM = "PBEWithMD5AndDES";

    private static final String UTF8 = "utf-8";

    //
    // Initialization
    //

    public static void init(Context ctx, String name) {
        context = ctx;
        mPreference = ctx.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static void init(Context ctx, String name, String encryption) {
        init(ctx, name);
        encryptionIsEnabled = true;
        encryptionKey = encryption.toCharArray();
    }

    /**
     * Reset all the preferences
     */
    public static void reset() {
        if (mPreference == null)
            return ;

        Editor editor = mPreference.edit();
        editor.clear();
        editor.commit();
    }

    //
    // Accessor
    //

    protected static void setPreference(String preference, String value) {
        if (value == null) {
            DebugTools.e("Cannot set a (null) value to a String preferences");
            return;
        }
        Editor editor = mPreference.edit();
        editor.putString(encrypt(preference), encrypt(value));
        editor.commit();
    }

    protected static void setPreference(String preference, boolean value) {
        Editor editor = mPreference.edit();
        editor.putString(encrypt(preference), encrypt(Boolean.toString(value)));
        editor.commit();
    }

    protected static void setPreference(String preference, int value) {
        Editor editor = mPreference.edit();
        editor.putString(encrypt(preference), encrypt(Integer.toString(value)));
        editor.commit();
    }

    protected static void setPreference(String preference, long value) {
        Editor editor = mPreference.edit();
        editor.putString(encrypt(preference), encrypt(Long.toString(value)));
        editor.commit();
    }

    protected static void setPreference(String preference, double value) {
        Editor editor = mPreference.edit();
        editor.putString(encrypt(preference), encrypt(Double.toString(value)));
        editor.commit();
    }

    protected static void setPreference(String preference, Date value) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.FRENCH);
        Editor editor = mPreference.edit();
        if(value==null)
            editor.remove(encrypt(preference));
        else
            editor.putString(encrypt(preference), encrypt(sdf.format(value)));
        editor.commit();
    }

    // Getter
    protected static String getSPreference(String preference) {
        return getSPreference(preference, null);
    }

    protected static String getSPreference(String preference, String default_value) {
        String value = mPreference.getString(encrypt(preference), null);
        return value != null ? decrypt(value) : default_value;
    }

    protected static int getIPreference(String preference) {
        return getIPreference(preference, 0);
    }

    protected static int getIPreference(String preference, int default_value) {
        String value = mPreference.getString(encrypt(preference), null);
        return value != null ? Integer.parseInt(decrypt(value)) : default_value;
    }

    protected static boolean getBPreference(String preference) {
        return getBPreference(preference, false);
    }

    protected static boolean getBPreference(String preference, boolean default_value) {
        String value = mPreference.getString(encrypt(preference), null);
        return value != null ? Boolean.parseBoolean(decrypt(value)) : default_value;
    }

    protected static long getLPreference(String preference) {
        return getLPreference(preference, 0);
    }

    protected static long getLPreference(String preference, long default_value) {
        String value = mPreference.getString(encrypt(preference), null);
        return value != null ? Long.parseLong(decrypt(value)) : default_value;
    }

    protected static double getDPreference(String preference) {
        return getDPreference(preference, 0);
    }

    protected static double getDPreference(String preference, double default_value) {
        String value = mPreference.getString(encrypt(preference), null);
        return value != null ? Double.parseDouble(decrypt(value)) : default_value;
    }

    protected static boolean togglePreference(String preference) {
        boolean value = getBPreference(preference);
        value ^= true;
        setPreference(preference, value);

        return value;
    }

    // Return true if preference is set
    protected static boolean hasPreference(String preference) {
        if(mPreference==null)
            return false;

        return mPreference.contains(encrypt(preference));
    }

    protected static Date getDatePreference(String preference) {
        return getDatePreference(preference, null);
    }

    protected static Date getDatePreference(String preference, Date default_value) {
        String valueStrEnc = mPreference.getString(encrypt(preference), null);
        if (!TextUtils.isEmpty(valueStrEnc)) {
            String valueStr = decrypt(valueStrEnc);

            Date d = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.FRENCH);
                d = sdf.parse(valueStr);
            }
            catch (Exception e) {
            }

            if (d != null)
                return d;
        }

        return default_value;
    }

    public static void removePreference(String preference) {
        Editor editor = mPreference.edit();
        editor.remove(encrypt(preference));
        editor.commit();
    }

    //
    // Manage a kind of list of preference.
    // It is just a list of value separate by ':'
    //

    /**
     * @return true if the entry is inside the list
     */
    protected static boolean hasEntryInList(String key, String entry) {
        String entries = getSPreference(key);
        if (TextUtils.isEmpty(entries))
            return false;

        for (String e: entries.split(":"))
            if (e.equalsIgnoreCase(entry))
                return true;

        return false;
    }

    /**
     * Add a new entry in the list
     */
    protected static void addEntryInList(String key, String entry) {
        if (hasEntryInList(key, entry))
            return ;

        String entries = getSPreference(key);
        if (TextUtils.isEmpty(entries))
            entries = entry;
        else
            entries = entries + ":" + entry;

        setPreference(key, entries);
    }

    /**
     * Remove the entry of the list
     */
    protected static void removeEntryInList(String key, String entry) {
        String entries = getSPreference(key);
        if (TextUtils.isEmpty(entries))
            return ;

        StringBuilder sb = new StringBuilder();
        for (String v: entries.split(":"))
            if (!v.equalsIgnoreCase(entry))
                sb.append(v)
                .append(':');

        setPreference(key, sb.toString());
    }

    /**
     * If the entry is in the list => Remove it
     * If the entry is not in the list => Add it
     */
    protected static boolean switchEntryInList(String key, String entry) {
        if (hasEntryInList(key, entry))
            removeEntryInList(key, entry);
        else
            addEntryInList(key, entry);

        return hasEntryInList(key, entry);
    }

    /**
     * @return The number of element in the list
     */
    protected static int getNumberOfEntryInList(String key) {
        String entries = getSPreference(key);
        if (TextUtils.isEmpty(entries))
            return 0;

        int num = 0;
        for (String v: entries.split(":"))
            if (!TextUtils.isEmpty(v))
                ++num;

        return num;
    }

    /**
     * @return All element in a array
     */
    protected static String[] getList(String key) {
        String entries = getSPreference(key);
        if (TextUtils.isEmpty(entries))
            return new String[0];

        return entries.split(":");
    }

    //
    // Encryption
    //

    @SuppressWarnings("deprecation")
    protected static String encrypt(String value) {
        if (encryptionIsEnabled) {
            try {
                final byte[] bytes = value != null ? value.getBytes(UTF8) : new byte[0];
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM);
                SecretKey key = keyFactory.generateSecret(new PBEKeySpec(encryptionKey));
                Cipher pbeCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
                pbeCipher.init(
                        Cipher.ENCRYPT_MODE,
                        key,
                        new PBEParameterSpec(Settings.Secure.getString(context.getContentResolver(),
                                Settings.System.ANDROID_ID).getBytes(UTF8), 20));

                return new String(Base64.encode(pbeCipher.doFinal(bytes), Base64.NO_WRAP), UTF8);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return value;
        }
    }

    @SuppressWarnings("deprecation")
    protected static String decrypt(String value) {
        if (encryptionIsEnabled) {
            try {
                final byte[] bytes = value != null ? Base64.decode(value, Base64.DEFAULT) : new byte[0];
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM);
                SecretKey key = keyFactory.generateSecret(new PBEKeySpec(encryptionKey));
                Cipher pbeCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
                pbeCipher.init(
                        Cipher.DECRYPT_MODE,
                        key,
                        new PBEParameterSpec(Settings.Secure.getString(context.getContentResolver(),
                                Settings.System.ANDROID_ID).getBytes(UTF8), 20));

                return new String(pbeCipher.doFinal(bytes), UTF8);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return value;
        }
    }

}
