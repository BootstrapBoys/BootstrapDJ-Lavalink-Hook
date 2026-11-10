/*
 * Copyright (c) 2021 Freya Arbjerg and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package lavalink.server.player

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import dev.arbjerg.lavalink.api.AudioFrameListener
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong

/**
 * Fans encoded frames and player lifecycle events out to plugin listeners.
 *
 * The audio thread must never be taken down by a plugin: listener exceptions
 * are caught and logged at most once every 30 seconds, and a listener gets a
 * read-only duplicate so it cannot disturb the buffer written to Discord.
 */
class AudioFrameDispatcher(private val listeners: List<AudioFrameListener>) {

    companion object {
        private val log = LoggerFactory.getLogger(AudioFrameDispatcher::class.java)
    }

    private val lastErrorLog = AtomicLong(0)

    val hasListeners: Boolean
        get() = listeners.isNotEmpty()

    fun frame(guildId: Long, sessionId: String, positionMs: Long, frame: ByteBuffer) {
        if (listeners.isEmpty()) return
        for (listener in listeners) {
            try {
                listener.onAudioFrame(guildId, sessionId, positionMs, frame.asReadOnlyBuffer())
            } catch (error: Exception) {
                reportFailure("frame", guildId, error)
            }
        }
    }

    fun trackStart(guildId: Long, sessionId: String, track: AudioTrack) {
        if (listeners.isEmpty()) return
        for (listener in listeners) {
            try {
                listener.onTrackStart(guildId, sessionId, track)
            } catch (error: Exception) {
                reportFailure("trackStart", guildId, error)
            }
        }
    }

    fun trackEnd(guildId: Long, sessionId: String, track: AudioTrack, reason: AudioTrackEndReason) {
        if (listeners.isEmpty()) return
        for (listener in listeners) {
            try {
                listener.onTrackEnd(guildId, sessionId, track, reason)
            } catch (error: Exception) {
                reportFailure("trackEnd", guildId, error)
            }
        }
    }

    fun playerDestroyed(guildId: Long, sessionId: String) {
        if (listeners.isEmpty()) return
        for (listener in listeners) {
            try {
                listener.onPlayerDestroyed(guildId, sessionId)
            } catch (error: Exception) {
                reportFailure("playerDestroyed", guildId, error)
            }
        }
    }

    private fun reportFailure(operation: String, guildId: Long, error: Exception) {
        val now = System.currentTimeMillis()
        val previous = lastErrorLog.get()
        if (now - previous > 30_000L && lastErrorLog.compareAndSet(previous, now)) {
            log.error("Audio frame listener {} failed for guild {}", operation, guildId, error)
        }
    }
}
