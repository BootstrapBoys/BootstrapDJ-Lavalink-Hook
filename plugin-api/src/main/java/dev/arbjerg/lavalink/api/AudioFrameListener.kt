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

package dev.arbjerg.lavalink.api

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import java.nio.ByteBuffer

/**
 * Receives the encoded Opus frames Lavalink sends to Discord.
 *
 * Implement this interface as a Spring bean. Lavalink discovers every bean of
 * this type and dispatches to them; no listener means no extra work in the
 * audio path.
 *
 * All callbacks run on the audio thread. They must not block or perform I/O.
 * [frame] is a read-only view that is only valid for the duration of the call;
 * copy the bytes if they are needed afterwards.
 */
interface AudioFrameListener {
    /**
     * @param guildId guild owning the player
     * @param sessionId Lavalink session id
     * @param positionMs track position at the time the frame was produced
     * @param frame read-only encoded Opus frame bytes
     */
    fun onAudioFrame(guildId: Long, sessionId: String, positionMs: Long, frame: ByteBuffer) {}

    fun onTrackStart(guildId: Long, sessionId: String, track: AudioTrack) {}

    fun onTrackEnd(
        guildId: Long,
        sessionId: String,
        track: AudioTrack,
        reason: AudioTrackEndReason
    ) {}

    fun onPlayerDestroyed(guildId: Long, sessionId: String) {}
}
