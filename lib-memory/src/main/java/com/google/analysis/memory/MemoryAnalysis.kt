package com.google.analysis.memory

import android.content.Context
import android.os.SystemClock
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


/*
 * Created by Zachary on 2025/10/15.
 */
object MemoryAnalysis {
    private val cache: MutableList<Any> = ArrayList<Any>()
    private val executors by lazy {
        ThreadPoolExecutor(
            1000,
            Int.MAX_VALUE,
            24L,
            TimeUnit.HOURS,
            SynchronousQueue<Runnable>()
        )
    }

    private val sdf: ThreadLocal<SimpleDateFormat> by lazy {
        object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue(): SimpleDateFormat {
                return SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            }
        }
    }

    //oom
    fun analyzeOom() {
        // 分析内存
        while (true) {
            val obj = ByteArray(1024 * 1024)
            cache.add(obj)
            SystemClock.sleep(1000)
        }
    }

    fun analyzeFile(context: Context) {
        val dir = File(context.filesDir, "memory")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        repeat(1000) {
            val file = File(dir, "file_$it.txt")
            val fos = FileOutputStream(file)
            fos.write("hello world $it".toByteArray())
            cache.add(fos)
        }
        dir.listFiles()?.forEach {
            val fis = FileInputStream(it)
            val bytes = fis.readBytes()
            cache.add(bytes)
            cache.add(fis)
        }
    }

    fun analyzeThread() {
        repeat(1000) {
            executors.execute {
                val date = sdf.get().format(System.currentTimeMillis())
                Log.v("Application", date)
            }
        }
    }

    fun analyzeMaxThread() {
        val executors = Executors.newCachedThreadPool()
        repeat(100000) {
            executors.execute {
                val date = sdf.get().format(System.currentTimeMillis())
                Log.v("Application", date)
            }
        }
    }

    fun analyzeLargeString() {
        var s = "0"
        for (i in 0..9999999) {
            s += i
        }
    }

    //deadLock
    fun analyzeThreadLock() {
        val lock1 = Any()
        val lock2 = Any()
        Thread {
            synchronized(lock1) {
                SystemClock.sleep(1000)
                synchronized(lock2) {
                    Log.v("Application", "thread 1 lock 2")
                }
            }
        }.start()
        Thread {
            synchronized(lock2) {
                SystemClock.sleep(1000)
                synchronized(lock1) {
                    Log.v("Application", "thread 2 lock 1")
                }
            }
        }.start()
    }

    //oom
    fun analyzeStackOverflow() {
        fact(100000)
    }

    private fun fact(n: Int): Int {
        return n * fact(n - 1) // 没有 n == 1 时的返回条件
    }

    fun analyzeHashMap() {
        val set = HashMap<Data, Int>()
        repeat(100000) {
            set.put(Data(), 0)
        }
    }

    fun analyzeWriteLogFile(context: Context) {
        val dir = File(context.filesDir, "memory")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "large_log.txt")
        val fos = FileOutputStream(file)
        repeat(1000000) {
            val date = sdf.get().format(System.currentTimeMillis())
            fos.write("hello world $it $date".toByteArray())
        }
    }
}

private class Data {
    private var name: String = ""


    override fun equals(other: Any?): Boolean {
        return true
    }

    override fun hashCode(): Int {
        return 1
    }
}