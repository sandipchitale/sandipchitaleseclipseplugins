/**
 * 
 */
package factory;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
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
	
	private static final Insets insets = new Insets(2,2,2,2);
	
	private KeyListener keyListener;
	private ChangeListener changeListener;
	private JCheckBox label;
	
	private List<String> history = new LinkedList<String>();
	
	KeyLabel() {
		super(new GridBagLayout());
		setBorder(BorderFactory.createLoweredBevelBorder());
		
		GridBagConstraints gbc;

		label = new JCheckBox("                       ", true);
		label.setHorizontalAlignment(SwingConstants.LEADING);
		label.setFocusPainted(false);
		
		gbc = new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0);
		
		add(label, gbc);
		
		JButton showHistory = new JButton("...");
		showHistory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showHistory();
			}
		});

		showHistory.setToolTipText("Show Keystroke History");
		gbc = new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0);
		
		
		add(showHistory, gbc);
		
	}
	
	public void addNotify() {
		super.addNotify();
		label.setToolTipText(TOOLTIP_TEXT);
		label.setFont(new Font(FONT_NAME, Font.BOLD, getFont().getSize()));
		keyListener = new KeyListener();
		
		label.addActionListener(new ActionListener() {
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
		label.setText(keystrokeText);
		if (keystrokeText != null) {
			if (uninteresting.matcher(keystrokeText).matches()) {
				return;
			}
			if ("".equals(keystrokeText.trim())) {
				return;
			}
			history.add(0, keystrokeText);
			while (history.size() > 100) {
				history.remove(history.size() - 1);
			}
		}
	}
	
	private void showHistory() {
		final JTextArea historyTextArea = new JTextArea(20, 20) {
			public void addNotify() {
				super.addNotify();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						scrollRectToVisible(new Rectangle(0,0,1,1));
					}
				});
			}
		};
		historyTextArea.setToolTipText(TOOLTIP_TEXT);
		historyTextArea.setFont(new Font(FONT_NAME, Font.BOLD, getFont().getSize()));
		StringBuilder sb = new StringBuilder();
		for (String ks : history) {
			sb.append(ks + "\n");
		}
		historyTextArea.setText(sb.toString());
		JOptionPane.showMessageDialog(this, new JScrollPane(historyTextArea), "Keystroke History", JOptionPane.PLAIN_MESSAGE, null);
	}
	
	private void reconfigure() {
		if (label.isSelected()) {
			keyListener.attach();
			label.setForeground(null);
			label.setText("");
		} else {
			keyListener.dettach();
			label.setForeground(SystemColor.textInactiveText);
			label.setText("Disabled");
		}
	}
}