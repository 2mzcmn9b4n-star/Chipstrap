package com.chipstrap.rbx.core

import com.chipstrap.rbx.data.AppPaths
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tiny file-backed logger. One-line per call, async-friendly enough for our use.
 */
object Logger {

    private lateinit var logFile: File
    private val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val lock = Any()

    fun init(logsDir: File) {
        if (!logsDir.exists()) logsDir.mkdirs()
        logFile = File(logsDir, "chipstrap.log")
        write("Logger", "init", "Logger initialized at ${logFile.absolutePath}")
    }

    fun writeLine(tag: String, msg: String) = write(tag, "info", msg)

    fun writeException(tag: String, e: Throwable) {
        write(tag, "error", "${e.javaClass.simpleName}: ${e.message}")
        e.stackTrace.take(8).forEach {
            write(tag, "stack", "    at $it")
        }
    }

    private fun write(tag: String, level: String, msg: String) {
        if (!::logFile.isInitialized) return
        val line = "${fmt.format(Date())} [$level] [$tag] $msg\n"
        synchronized(lock) {
            try {
                logFile.appendText(line)
            } catch (_: Throwable) { /* swallow */ }
        }
    }
}
