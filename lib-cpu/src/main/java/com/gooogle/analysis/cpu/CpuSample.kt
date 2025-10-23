package com.gooogle.analysis.cpu

import java.util.concurrent.Executors
import kotlin.math.pow


object CpuSample {

    fun sampleCpu() {
        val cores = Runtime.getRuntime().availableProcessors()
        val executor = Executors.newFixedThreadPool(cores)
        for (i in 0 until cores) {
            executor.submit {
                while (true) {
                    val x = Math.random().pow(Math.random())
                }
            }
        }
    }

}
