/**
 * 
 */
package factory;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
class KeyLabel extends JPanel {
	
	private static String FONT_NAME;

	static {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.startsWith("linux")) {
			FONT_NAME = "Lucida Grande";
		} else if (osName.startsWith("mac")) {
			FONT_NAME = "Monaco";
		} else if (osName.startsWith("windows")) {
			FONT_NAME = "DejaVu Sans";
		}
	}
	
	private static final String TOOLTIP_TEXT = 
		"<html>" +
		"<table>" +
		"<tr><td><font face=\"" + FONT_NAME + "\">\u21e7</td><td>SHIFT</td></tr>" +
		"<tr><td><font face=\"" + FONT_NAME + "\">\u2303</td><td>CTRL</td></tr>" +
		"<tr><td><font face=\"" + FONT_NAME + "\">\u2325</td><td>ALT or OPTION</td></tr>" +
		"<tr><td><font face=\"" + FONT_NAME + "\">\u2318</td><td>META or COMMAND</td></tr>" +
		"</table>";
	
	private static final Insets insets = new Insets(1,1,1,1);
	
	private KeyListener keyListener;
	private ChangeListener changeListener;
	
	private JCheckBox listenCheckbox;
	private JComboBox historyComboBox;
	
	private MutableComboBoxModel history = new DefaultComboBoxModel();
	
	KeyLabel() {
		super(new GridBagLayout());
		
		GridBagConstraints gbc;
		
		historyComboBox = new JComboBox(history);
		historyComboBox.setToolTipText(TOOLTIP_TEXT);
		historyComboBox.setPrototypeDisplayValue("                    ");
		
		gbc = new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0);
		add(historyComboBox, gbc);
		
		JButton clearHistory = new JButton(" X ");
		clearHistory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearHistory();
				historyComboBox.setSelectedIndex(-1);
			}
		});

		clearHistory.setToolTipText("Clear Keystroke History");
		
		gbc = new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0);
		add(clearHistory, gbc);
		
		listenCheckbox = new JCheckBox("", true);
		listenCheckbox.setHorizontalAlignment(SwingConstants.LEADING);
		listenCheckbox.setFocusPainted(false);
		listenCheckbox.setToolTipText("Stop monitoring Keystrokes");
		gbc = new GridBagConstraints(2,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0);
		add(listenCheckbox, gbc);
	}
	
	public void addNotify() {
		super.addNotify();
		listenCheckbox.setToolTipText(TOOLTIP_TEXT);
		historyComboBox.setFont(new Font(FONT_NAME, Font.BOLD, getFont().getSize()));
		keyListener = new KeyListener();
		
		listenCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reconfigure();
			}
		});
		reconfigure();
		
		changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setText(e.getSource().toString());
			}
		};
		keyListener.addChangeListener(changeListener);
	}

	public void removeNotify() {
		super.removeNotify();
		if (keyListener != null) {
			if (changeListener != null) {
				keyListener.removeChangeListener(changeListener);
			}
			keyListener.dettach();
		}
	}
	
	// SHIFT, CTRL, ALT and META by themselves are uninteresting
	private static final Pattern uninteresting = Pattern.compile("^(\u21e7|\u2303|\u2318|\u2325)+$");
	
	private void setText(String keystrokeText) {
		//listenCheckbox.setText(keystrokeText);
		if (keystrokeText != null) {
			if (uninteresting.matcher(keystrokeText).matches()) {
				return;
			}
			if ("".equals(keystrokeText.trim())) {
				return;
			}
			addToHistory(keystrokeText);
			historyComboBox.setSelectedIndex(0);
		}
	}
	
	private void addToHistory(String keystrokeText) {
		history.insertElementAt(keystrokeText, 0);
		while (history.getSize() > 100) {
			history.removeElementAt(history.getSize() - 1);
		}
	}

	private void clearHistory() {
		while(history.getSize() > 0) {
			history.removeElementAt(0);
		}
	}
	
	private void reconfigure() {
		if (listenCheckbox.isSelected()) {
			keyListener.attach();
			listenCheckbox.setToolTipText("Stop monitoring Keystrokes");
		} else {
			keyListener.dettach();
			listenCheckbox.setToolTipText("Start monitoring Keystrokes");
		}
	}
}