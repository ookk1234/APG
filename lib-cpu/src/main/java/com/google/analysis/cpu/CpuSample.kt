package com.google.analysis.cpu

/*
 * Created by Zachary on 2025/10/15.
 */
object CpuSample {

    fun sampleCpu() {
        while (true) {
            val obj = Any()
            println(obj)
        }
    }

    fun sampleCpu2() {
        var i = 1
        while (i != 10) {
            i += 2
        }
    }

}