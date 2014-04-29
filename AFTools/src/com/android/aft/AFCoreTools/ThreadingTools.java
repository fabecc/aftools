package com.android.aft.AFCoreTools;

import android.os.Handler;
import android.os.Message;


public class ThreadingTools {

    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            Runnable r = (Runnable)message.obj;
            r.run();
        }
    };

    public static void runInBackground(Runnable task) {
        runInBackground(0, task);
    }

    public static void runInBackground(final int delay, final Runnable task) {
        Message m = new Message();
        m.obj = new Runnable() {
            @Override
            public void run() {
              DebugTools.d("Run async");
              new AsyncTaskWrapper<Void, Void, Void>() {
                  @Override
                  protected Void doInBackground(Void... params) {
                      // Nothing to do
                      DebugTools.d("Run async bg");
                      task.run();
                      return null;
                  }

              }.executeParallel();
            }
        };
        mHandler.sendMessageDelayed(m, delay);
    }

    public static void runInMainThread(Runnable task) {
        runInMainThread(0, task);
    }

    public static void runInMainThread(final int delay, final Runnable task) {
        Message m = new Message();
        m.obj = task;
        mHandler.sendMessageDelayed(m, delay);
    }

}
