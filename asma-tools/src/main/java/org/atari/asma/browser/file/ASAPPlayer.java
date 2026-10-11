package org.atari.asma.browser.file;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import net.sf.asap.ASAP;
import net.sf.asap.ASAPArgumentException;
import net.sf.asap.ASAPFormatException;
import net.sf.asap.ASAPInfo;
import net.sf.asap.ASAPSampleFormat;

/**
 * Plays modules via ASAP on a background thread, the desktop counterpart of
 * asapweb.js. All methods are called on the event dispatch thread; only
 * rendering runs on the playback thread, synchronized on the ASAP instance.
 *
 * @author Peter Dell
 */
final class ASAPPlayer {

	private static final int BUFFER_BYTES = 8192;

	private static final class Playback {
		final ASAP asap;
		final SourceDataLine line;
		final int channels;
		volatile boolean stopped;
		boolean paused;

		Playback(ASAP asap, SourceDataLine line, int channels) {
			this.asap = asap;
			this.line = line;
			this.channels = channels;
		}
	}

	private Playback playback;

	void play(String fileName, byte[] content, int song)
			throws ASAPFormatException, ASAPArgumentException, LineUnavailableException {
		stop();
		ASAP asap = new ASAP();
		asap.load(fileName, content, content.length);
		// Like asapweb.js: play endlessly, the UI handles durations and loops.
		asap.playSong(song, -1);
		int channels = asap.getInfo().getChannels();
		AudioFormat format = new AudioFormat(ASAP.SAMPLE_RATE, 16, channels, true, false);
		SourceDataLine line = AudioSystem.getSourceDataLine(format);
		line.open(format);
		line.start();

		Playback newPlayback = new Playback(asap, line, channels);
		playback = newPlayback;
		Thread thread = new Thread(() -> render(newPlayback), "ASMABrowser Player");
		thread.setDaemon(true);
		thread.start();
	}

	private static void render(Playback playback) {
		byte[] buffer = new byte[BUFFER_BYTES];
		while (!playback.stopped) {
			int length;
			synchronized (playback.asap) {
				length = playback.asap.generate(buffer, buffer.length, ASAPSampleFormat.S16_L_E);
			}
			if (length <= 0) {
				break;
			}
			// Blocks while the line is stopped (paused), returns when it is closed.
			playback.line.write(buffer, 0, length);
		}
	}

	void stop() {
		if (playback != null) {
			playback.stopped = true;
			playback.line.stop();
			playback.line.flush();
			playback.line.close();
			playback = null;
		}
	}

	void togglePause() {
		if (playback == null) {
			return;
		}
		if (playback.paused) {
			playback.line.start();
		} else {
			playback.line.stop();
		}
		playback.paused = !playback.paused;
	}

	boolean isPlaying() {
		return playback != null;
	}

	boolean isPaused() {
		return playback != null && playback.paused;
	}

	ASAPInfo getInfo() {
		return playback == null ? null : playback.asap.getInfo();
	}

	/**
	 * Gets the audible position in milliseconds: the rendered position minus
	 * what is still queued in the audio line.
	 */
	int getPosition() {
		if (playback == null) {
			return 0;
		}
		int renderedPosition;
		synchronized (playback.asap) {
			renderedPosition = playback.asap.getPosition();
		}
		int queuedBytes = playback.line.getBufferSize() - playback.line.available();
		int queuedMilliseconds = (int) (queuedBytes * 1000L / (ASAP.SAMPLE_RATE * playback.channels * 2L));
		return Math.max(0, renderedPosition - queuedMilliseconds);
	}

	void seek(int position) throws ASAPFormatException {
		if (playback == null) {
			return;
		}
		synchronized (playback.asap) {
			playback.asap.seek(position);
		}
		playback.line.flush();
	}
}
