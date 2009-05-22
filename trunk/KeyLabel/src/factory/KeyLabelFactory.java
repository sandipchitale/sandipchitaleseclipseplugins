package factory;

import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class KeyLabelFactory {
	
	private static String FONT_NAME;
	static {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.startsWith("linux")) {
			FONT_NAME = "Lucida Grande";
		} else if (osName.startsWith("mac")) {
			FONT_NAME = "Monaco";
		} else if (osName.startsWith("windows")) {
			FONT_NAME = "Monospaced";
		}
	}
	
	@SuppressWarnings("serial")
	public static JCheckBox createKeyLabel() {
		JCheckBox label = new JCheckBox("                       ", true) {
			private KeyListener keyListener;
			ChangeListener changeListener = new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					setText(e.getSource().toString());
				}
			};
			
			public void addNotify() {
				super.addNotify();
				setToolTipText(
						"<html>" +
						"<table>" +
						"<tr><td><font face=\"" + FONT_NAME + "\">\u21e7</td><td>SHIFT</td></tr>" +
						"<tr><td><font face=\"" + FONT_NAME + "\">\u2303</td><td>CTRL</td></tr>" +
						"<tr><td><font face=\"" + FONT_NAME + "\">\u2325</td><td>ALT or OPTION</td></tr>" +
						"<tr><td><font face=\"" + FONT_NAME + "\">\u2318</td><td>META or COMMAND</td></tr>" +
						"</table>"
						);
				setFont(new Font(FONT_NAME, Font.BOLD, getFont().getSize()));
				keyListener = new KeyListener();
				
				addActionListener(new ActionListener() {
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
			
			private void reconfigure() {
				if (isSelected()) {
					keyListener.attach();
					setForeground(null);
					setText("");
				} else {
					keyListener.dettach();
					setForeground(SystemColor.textInactiveText);
					setText("Disabled");
				}
			}
		};
		label.setHorizontalAlignment(SwingConstants.LEADING);
		label.setFocusPainted(false);
		
		return label;
	}

}

