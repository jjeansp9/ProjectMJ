package kr.jeet.edu.manager.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ThreadTask<T1,T2> implements Runnable {
    T1 mArgument[];
    T2 mResult;
    Thread mThread;
    //safe in multithread
    private final AtomicBoolean mCancelled = new AtomicBoolean();
    public final int WORK_DONE = 0;
    public final int UPDATE_PROGRESS = 1;
    public final int WORK_CANCELLED = 2;

    Handler mResultHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WORK_DONE:
                    //onPostExecute
                    onPostExecute(mResult);
                    break;
                case UPDATE_PROGRESS:
                    int progress = msg.arg1;
                    onProgressUpdate(progress);
                    break;
                case WORK_CANCELLED:
                    onCancelled();
                    break;
            }

        }
    };

    //execute
    public final void execute(final T1... arg) {
        //store the argument
        mArgument = arg;
        //call preExecute
        onPreExecute();

        //Begin thread work
        mThread = new Thread(this);
        mThread.start();
    }

    public final void cancel() {
        mCancelled.set(true);
        if (mThread != null) {
            if (mThread.isAlive()) {
                mThread.interrupt();
            }
            mThread = null;
        }
        if (mResultHandler != null) {
            mResultHandler.removeCallbacksAndMessages(null);
            mResultHandler = null;
        }
    }

    public final void publishProgress(int progress) {
        Message msg = mResultHandler.obtainMessage(UPDATE_PROGRESS);
        msg.arg1 = progress;
        mResultHandler.sendMessage(msg);
    }

    public final boolean isCancelled() {
        return mCancelled.get();
    }

    @Override
    public void run() {
        //call doInBackground
        try {
            mResult = doInBackground(mArgument);
            //notify main thread that
            mResultHandler.sendEmptyMessage(WORK_DONE);
        } catch (Throwable tr) {
            mCancelled.set(true);
            mResultHandler.sendEmptyMessage(WORK_CANCELLED);
            throw tr;
        }
    }

    //onPreExecute
    protected void onPreExecute() {
    }

    //onProgressUpdate
    protected void onProgressUpdate(int progress) {
    }

    //doInBackground
    protected abstract T2 doInBackground(T1... arg);

    //onPostExecute
    protected void onPostExecute(T2 result) {
    }

    //onCancelled
    protected void onCancelled() {
    }
}
