package com.android.aft.AFCoreTools;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class AFNetworkMonitoring {

    public enum NetworkMode {
        NotConnected,
        ConnectedToWifi,
        ConnectedTo3G,
    };

    public interface NetworkMonitoringInterface {
        public void onNetworkUpdate(NetworkMode mode);
    }

    // Network connection
    public NetworkMode mode = NetworkMode.NotConnected;

    // Connection listener
    private BroadcastReceiver mConnReceiver;

    // Listeners
    private ArrayList<NetworkMonitoringInterface> mListeners;

    // Context
    private Context mContext;

    public AFNetworkMonitoring(Context ctx) {
        this(ctx, null);
    }

    public AFNetworkMonitoring(Context ctx, NetworkMonitoringInterface listener) {
        mContext = ctx;
        addListener(listener);

        // Create network state update
        mConnReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateNetworkConnectionStatus();
            }
        };
        mContext.registerReceiver(mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    protected void updateNetworkConnectionStatus() {
        ConnectivityManager conMgr = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conMgr.getActiveNetworkInfo();
        if (info == null || !info.isConnected() || !info.isAvailable()) {
            DebugTools.d("Lost connection detected");
            mode = NetworkMode.NotConnected;
            notifyListener();
            return;
        }

        NetworkInfo infoWifi = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (infoWifi.isConnected() || infoWifi.isAvailable()) {
            DebugTools.d("On connection wifi detected");
            mode = NetworkMode.ConnectedToWifi;
            notifyListener();
            return;
        }

        DebugTools.d("On connection 3G detected");
        mode = NetworkMode.ConnectedTo3G;
        notifyListener();
    }

    protected void notifyListener() {
        if (mListeners == null)
            return ;

        new AsyncTaskWrapper<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // Nothing to do
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                for (NetworkMonitoringInterface l: mListeners)
                    l.onNetworkUpdate(mode);
            };
        }.executeParallel();
    }

    public void addListener(NetworkMonitoringInterface listener) {
        if (listener == null)
            return ;

        if (mListeners == null)
            mListeners = new ArrayList<AFNetworkMonitoring.NetworkMonitoringInterface>();

        mListeners.add(listener);
    }

    public void removeListener(NetworkMonitoringInterface listener) {
        if (mListeners == null)
            return ;
        mListeners.remove(listener);
    }

}
