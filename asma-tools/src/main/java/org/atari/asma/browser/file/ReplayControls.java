package org.atari.asma.browser.file;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.atari.asma.FileInfo;
import org.atari.asma.browser.Actions;

import com.wudsn.tools.base.gui.ElementFactory;

import net.sf.asap.ASAPFormatException;
import net.sf.asap.ASAPInfo;

/**
 * The replay controls of the file panels, mirroring the website player
 * (asma.js): song buttons, playing time with seek slider, and the Play,
 * Pause, Stop and Shuffle buttons. The controls operate on the file selected
 * in the table; playback continues when the selection changes.
 *
 * @author Peter Dell
 */
final class ReplayControls {

	interface Host {
		byte[] loadContent(FileInfo fileInfo) throws IOException;

		/** Selects a random row of the current selection and returns its file info, or <code>null</code>. */
		FileInfo selectRandomFileInfo();

		void showPlaybackError(FileInfo fileInfo, String reason);
	}

	private static final int SONG_DEFAULT = -1;
	private static final int SONG_RANDOM = -2;

	// Shuffle mode plays songs without a TIME tag this long, like asma.js.
	private static final int SHUFFLE_MAXIMUM_DURATION = 2 * 60 * 1000;
	private static final int SHUFFLE_PLAYABLE_ATTEMPTS = 10;

	private final Host host;
	private final ASAPPlayer player;
	private final Random random;

	private final JPanel songButtonsPanel;
	private final List<JToggleButton> songButtons;
	private ButtonGroup songButtonGroup;
	private final JPanel playingTimePanel;
	private final JLabel playingTimeLabel;
	private final JSlider playingTimeSlider;
	private final JPanel buttonPanel;
	private final JButton playButton;
	private final JButton pauseButton;
	private final JButton stopButton;
	private final JButton shuffleButton;

	private FileInfo selectedFileInfo;
	private FileInfo playingFileInfo;
	private int playingSong;
	private boolean stopped;
	private boolean shuffleMode;
	private boolean loading;
	private int loadRequestCount;
	private boolean updatingSlider;
	private final Timer refreshTimer;

	ReplayControls(Host host) {
		if (host == null) {
			throw new IllegalArgumentException("Parameter 'host' must not be null.");
		}
		this.host = host;
		player = new ASAPPlayer();
		random = new Random();

		songButtonsPanel = new JPanel(new WrapLayout());
		songButtons = new ArrayList<JToggleButton>();
		songButtonGroup = new ButtonGroup();

		playingTimeLabel = new JLabel();
		playingTimeSlider = new JSlider(0, 0, 0);
		playingTimeSlider.setVisible(false);
		playingTimeSlider.addChangeListener(event -> seekToSlider());
		playingTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		playingTimePanel.add(playingTimeLabel);
		playingTimePanel.add(playingTimeSlider);

		playButton = ElementFactory.createButton(Actions.Replay_Play, true);
		playButton.addActionListener(event -> playSelected());
		pauseButton = ElementFactory.createButton(Actions.Replay_Pause, true);
		pauseButton.addActionListener(event -> togglePause());
		stopButton = ElementFactory.createButton(Actions.Replay_Stop, true);
		stopButton.addActionListener(event -> stop());
		shuffleButton = ElementFactory.createButton(Actions.Replay_Shuffle, true);
		shuffleButton.addActionListener(event -> startShuffle());
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
		buttonPanel.add(playButton);
		buttonPanel.add(pauseButton);
		buttonPanel.add(stopButton);
		buttonPanel.add(shuffleButton);

		refreshTimer = new Timer(500, event -> refreshPlayingTime());
		refreshTimer.start();
		updateControls();
	}

	JPanel getSongButtonsPanel() {
		return songButtonsPanel;
	}

	JPanel getPlayingTimePanel() {
		return playingTimePanel;
	}

	JPanel getButtonPanel() {
		return buttonPanel;
	}

	void setSelectedFileInfo(FileInfo fileInfo) {
		selectedFileInfo = fileInfo;
		rebuildSongButtons();
		updateControls();
	}

	/** Plays the selected file from the start, like the Play button. */
	void playSelected() {
		if (selectedFileInfo != null && selectedFileInfo == playingFileInfo) {
			play(selectedFileInfo, playingSong);
		} else {
			play(selectedFileInfo, SONG_DEFAULT);
		}
	}

	private static boolean isPlayable(FileInfo fileInfo) {
		return fileInfo != null && FileInfo.ATARI800.equals(fileInfo.hardware);
	}

	private void play(FileInfo fileInfo, int song) {
		if (!isPlayable(fileInfo)) {
			return;
		}
		int requestNumber = ++loadRequestCount;
		loading = true;
		new SwingWorker<byte[], Void>() {
			@Override
			protected byte[] doInBackground() throws IOException {
				return host.loadContent(fileInfo);
			}

			@Override
			protected void done() {
				// A newer play request supersedes this one.
				if (requestNumber != loadRequestCount) {
					return;
				}
				loading = false;
				try {
					int effectiveSong = song;
					if (song == SONG_DEFAULT) {
						effectiveSong = fileInfo.defaultSongIndex;
					} else if (song == SONG_RANDOM) {
						effectiveSong = random.nextInt(Math.max(1, fileInfo.songs));
					}
					player.play(fileInfo.filePath, get(), effectiveSong);
					playingFileInfo = fileInfo;
					playingSong = effectiveSong;
					stopped = false;
				} catch (ExecutionException ex) {
					host.showPlaybackError(fileInfo, getReason(ex.getCause()));
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				} catch (Exception ex) {
					host.showPlaybackError(fileInfo, getReason(ex));
				}
				updateControls();
			}
		}.execute();
	}

	private static String getReason(Throwable throwable) {
		return throwable.getMessage() != null ? throwable.getMessage() : throwable.toString();
	}

	private void togglePause() {
		player.togglePause();
		updateControls();
	}

	/** Stops playing and ends the shuffle mode, like the Stop button. */
	void stop() {
		if (player.isPlaying()) {
			player.stop();
			stopped = true;
		}
		setShuffleMode(false);
		updateControls();
	}

	/** Stops playing and the refresh timer, for a window that is closed for good. */
	void dispose() {
		refreshTimer.stop();
		player.stop();
	}

	private void startShuffle() {
		setShuffleMode(true);
		playRandom();
	}

	private void setShuffleMode(boolean shuffleMode) {
		this.shuffleMode = shuffleMode;
		shuffleButton.setFont(shuffleButton.getFont().deriveFont(shuffleMode ? java.awt.Font.BOLD : java.awt.Font.PLAIN));
	}

	private void playRandom() {
		for (int attempt = 0; attempt < SHUFFLE_PLAYABLE_ATTEMPTS; attempt++) {
			FileInfo fileInfo = host.selectRandomFileInfo();
			if (fileInfo == null) {
				break;
			}
			if (isPlayable(fileInfo)) {
				play(fileInfo, SONG_RANDOM);
				return;
			}
		}
		stop();
	}

	private void rebuildSongButtons() {
		songButtonsPanel.removeAll();
		songButtons.clear();
		songButtonGroup = new ButtonGroup();
		if (isPlayable(selectedFileInfo)) {
			FileInfo fileInfo = selectedFileInfo;
			for (int song = 0; song < fileInfo.songs; song++) {
				String text = String.valueOf(song + 1);
				if (fileInfo.songs > 1 && song == fileInfo.defaultSongIndex) {
					text += " (Default)";
				}
				JToggleButton songButton = new JToggleButton(text);
				songButton.setMargin(new Insets(1, 4, 1, 4));
				int songIndex = song;
				songButton.addActionListener(event -> play(fileInfo, songIndex));
				songButtonGroup.add(songButton);
				songButtons.add(songButton);
				songButtonsPanel.add(songButton);
			}
		}
		songButtonsPanel.revalidate();
		songButtonsPanel.repaint();
	}

	private void updateControls() {
		playButton.setEnabled(isPlayable(selectedFileInfo));
		pauseButton.setEnabled(player.isPlaying());
		stopButton.setEnabled(player.isPlaying());
		if (player.isPlaying() && selectedFileInfo == playingFileInfo && playingSong < songButtons.size()) {
			songButtons.get(playingSong).setSelected(true);
		} else {
			songButtonGroup.clearSelection();
		}
		refreshPlayingTime();
	}

	private void refreshPlayingTime() {
		String text = "";
		boolean sliderVisible = false;
		int sliderMaximum = 0;
		int sliderValue = 0;

		ASAPInfo info = player.getInfo();
		if (info != null) {
			int duration = info.getDuration(playingSong);
			int position = player.getPosition();
			if (shuffleMode && !loading && position > (duration > 0 ? duration : SHUFFLE_MAXIMUM_DURATION)) {
				playRandom();
				return;
			}
			if (duration > 0) {
				position %= duration;
			}
			text = getDurationString(position);
			if (player.isPaused()) {
				text += " (Paused)";
			}
			// Without a TIME tag there is nothing to seek in, so the slider stays hidden.
			if (duration > 0) {
				text += " / " + getDurationString(duration);
				sliderVisible = true;
				sliderMaximum = duration;
				sliderValue = position;
			}
			if (info.getLoop(playingSong)) {
				text += " (Loop)";
			}
		} else if (stopped) {
			text = "Stopped";
		}

		playingTimeLabel.setText(text);
		playingTimeSlider.setVisible(sliderVisible);
		// Do not yank the thumb away from the user while they are dragging.
		if (sliderVisible && !playingTimeSlider.getValueIsAdjusting()) {
			updatingSlider = true;
			playingTimeSlider.setMaximum(sliderMaximum);
			playingTimeSlider.setValue(sliderValue);
			updatingSlider = false;
		}
	}

	private void seekToSlider() {
		if (updatingSlider || playingTimeSlider.getValueIsAdjusting() || !player.isPlaying()) {
			return;
		}
		try {
			player.seek(playingTimeSlider.getValue());
		} catch (ASAPFormatException ex) {
			host.showPlaybackError(playingFileInfo, getReason(ex));
		}
	}

	private static String getDurationString(int milliseconds) {
		int seconds = milliseconds / 1000;
		return String.format("%d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60);
	}

	/**
	 * A flow layout that wraps into further rows and reports the matching
	 * preferred height, so files with many songs (up to 32 in the archive)
	 * show all song buttons.
	 */
	private static final class WrapLayout extends FlowLayout {

		WrapLayout() {
			super(FlowLayout.LEFT, 2, 1);
		}

		@Override
		public Dimension preferredLayoutSize(Container target) {
			return getLayoutSize(target);
		}

		@Override
		public Dimension minimumLayoutSize(Container target) {
			Dimension size = getLayoutSize(target);
			size.width -= getHgap() + 1;
			return size;
		}

		private Dimension getLayoutSize(Container target) {
			synchronized (target.getTreeLock()) {
				Container container = target;
				while (container.getSize().width == 0 && container.getParent() != null) {
					container = container.getParent();
				}
				int targetWidth = container.getSize().width;
				if (targetWidth == 0) {
					targetWidth = Integer.MAX_VALUE;
				}
				Insets insets = target.getInsets();
				int maximumWidth = targetWidth - (insets.left + insets.right + getHgap() * 2);

				int width = 0;
				int height = 0;
				int rowWidth = 0;
				int rowHeight = 0;
				for (Component component : target.getComponents()) {
					if (!component.isVisible()) {
						continue;
					}
					Dimension componentSize = component.getPreferredSize();
					if (rowWidth > 0 && rowWidth + getHgap() + componentSize.width > maximumWidth) {
						width = Math.max(width, rowWidth);
						height += rowHeight + getVgap();
						rowWidth = 0;
						rowHeight = 0;
					}
					if (rowWidth > 0) {
						rowWidth += getHgap();
					}
					rowWidth += componentSize.width;
					rowHeight = Math.max(rowHeight, componentSize.height);
				}
				width = Math.max(width, rowWidth);
				height += rowHeight;
				return new Dimension(width + insets.left + insets.right + getHgap() * 2,
						height + insets.top + insets.bottom + getVgap() * 2);
			}
		}
	}
}
