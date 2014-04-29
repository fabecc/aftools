package com.android.aft.AFCoreTools;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.os.AsyncTask;

public abstract class AsyncTaskWrapper<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

	public enum Mode {
		Serial,
		Parallel,
	};

	public AsyncTask<?, ?, ?> execute(Mode mode, Params... p)
	{
        if (android.os.Build.VERSION.SDK_INT < 11)
            return execute(p);

        try {
            // Find method
            Method getExecuteOnExecutor = null;
            for (Method m: getClass().getMethods())
                if (m.getName().equals("executeOnExecutor")) {
                    getExecuteOnExecutor = m;
                    break ;
                }
            if (getExecuteOnExecutor == null)
                throw new Exception("Cannot find executeOnExecutor() method");

            // Find executor field
            Field fieldExecutor = getClass().getField(mode == Mode.Parallel ?
                                                      "THREAD_POOL_EXECUTOR" :
                                                      "SERIAL_EXECUTOR");

            // Call executeOnExecutor method
            AsyncTask<?, ?, ?> task = (AsyncTask<?, ?, ?>)getExecuteOnExecutor.invoke(this, fieldExecutor.get(this), p);

            DebugTools.d("AsyncTaskWrapper() -> executeOnExecutor(" + fieldExecutor.getName() + ")");

            return task;
        } catch (Exception e) {
            DebugTools.w("Cannot lanch AsyncTaskWrapper with executor", e);
            return execute(p);
        }
    }

    public AsyncTask<?, ?, ?> executeParallel(Params... p)  {
        return execute(Mode.Parallel, p);
    }

    public AsyncTask<?, ?, ?> executeSerial(Params... p)  {
        return execute(Mode.Serial, p);
    }

}
