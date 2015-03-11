package com.door43.util.threads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class manages multiple threads from a static singleton so you can easily keep track of
 * threads accross activities.
 */
public class ThreadManager {

    private static Map<Integer, ManagedTask> mTaskMap = new HashMap<>();
    private static int mCurrentTaskIndex = 0;
    private static final ThreadManager sInstance;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static final BlockingQueue<Runnable> mWorkQueue = new LinkedBlockingQueue<>();
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private ThreadPoolExecutor mThreadPool = new ThreadPoolExecutor(
            NUMBER_OF_CORES,       // Initial pool size
            NUMBER_OF_CORES,       // Max pool size
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT,
            mWorkQueue);

    static {
        sInstance = new ThreadManager();
    }

    private ThreadManager() {

    }

    /**
     * Adds a task to be executed
     * @param task the task to be executed
     * @return the id of the task process
     */
    static public int addTask(ManagedTask task) {
        mCurrentTaskIndex ++;
        mTaskMap.put(mCurrentTaskIndex, task);
        queueTask(task);
        return mCurrentTaskIndex;
    }

    /**
     * Checks if a task has finished
     * @param id
     * @return
     */
    static public boolean isTaskFinished(int id) {
        return mTaskMap.get(id).isFinished();
    }

    /**
     * Returns the task by it's id
     * @param id
     * @return
     */
    static public ManagedTask getTask(int id) {
        if(mTaskMap.containsKey(id)) {
            return mTaskMap.get(id);
        } else {
            return null;
        }
    }

    /**
     * Removes a finished task from the manager
     * @param id
     */
    static public void clearTask(int id) {
        if(mTaskMap.containsKey(id) && mTaskMap.get(id).isFinished()) {
            mTaskMap.remove(id);
        }
    }

    public static void cancelAll() {
        ManagedTask[] runnableArray = new ManagedTask[mWorkQueue.size()];
        // Populates the array with the Runnables in the queue
        mWorkQueue.toArray(runnableArray);
        // Stores the array length in order to iterate over the array
        int len = runnableArray.length;
        /*
         * Iterates over the array of Runnables and interrupts each one's Thread.
         */
        synchronized (sInstance) {
            for (int runnableIndex = 0; runnableIndex < len; runnableIndex++) {
                Thread thread = runnableArray[runnableIndex].getThread();
                if (null != thread) {
                    thread.interrupt();
                }
            }
        }
    }

    /**
     * Adds a task to the thread pool queue
     * @param task
     */
    private static void queueTask(ManagedTask task) {
        sInstance.mThreadPool.execute(task);
    }
}