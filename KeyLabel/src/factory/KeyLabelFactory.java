package factory;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class KeyLabelFactory {
	private static class KeyListener implements AWTEventListener {
		
		private final JToggleButton keyLabel;

		KeyListener(JToggleButton keyLabel) {
			this.keyLabel = keyLabel;
		}

		public void eventDispatched(AWTEvent event) {
			if (event instanceof KeyEvent) {
				KeyEvent keyEvent = (KeyEvent) event;
				String keyText = getKeyText(keyEvent);
				if (keyText != null) {
					keyLabel.setText(keyText);
				}
				keyLabel.repaint(50);
			}
		}
		private static String getKeyText(KeyEvent keyEvent) {
			if (keyEvent.getID() == KeyEvent.KEY_TYPED &&  keyEvent.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
				return null;
			}
			if (keyEvent.getID() == KeyEvent.KEY_RELEASED && 
					(
							keyEvent.getKeyCode() == KeyEvent.VK_SHIFT ||
							keyEvent.getKeyCode() == KeyEvent.VK_CONTROL ||
							keyEvent.getKeyCode() == KeyEvent.VK_ALT ||
							keyEvent.getKeyCode() == KeyEvent.VK_META
							)) {
				return null;
			}
			StringBuilder sb = new StringBuilder();
	        if (keyEvent.getKeyChar() != KeyEvent.CHAR_UNDEFINED &&
    				Character.isLetter(keyEvent.getKeyChar()) && 
	        		((!keyEvent.isAltDown()) 
	        				&& (!keyEvent.isControlDown())
	        				&& (!keyEvent.isMetaDown()))) {
	        	sb.append("" + keyEvent.getKeyChar());
	        } else if (keyEvent.getKeyCode() != KeyEvent.VK_UNDEFINED) {
	        	sb.append(getModifiersText(keyEvent.getModifiers()));
	        	switch (keyEvent.getKeyCode()) {
	        	case KeyEvent.VK_UP:
	        		sb.append("\u2191");
	        		break;
	        	case KeyEvent.VK_DOWN:
	        		sb.append("\u2193");
	        		break;
	        	case KeyEvent.VK_LEFT:
	        		sb.append("\u2190");
	        		break;
	        	case KeyEvent.VK_RIGHT:
	        		sb.append("\u2192");
	        		break;
//	        	case KeyEvent.VK_BACK_SPACE:
//	        		sb.append("BACKSPACE");
//	        		break;
//	        	case KeyEvent.VK_DELETE:
//	        		sb.append("DELETE");
//	        		break;
//	        	case KeyEvent.VK_ENTER:
//	        		sb.append("ENTER");
//	        		break;
//	        	case KeyEvent.VK_ESCAPE:
//	        		sb.append("ESCAPE");
//	        		break;
	        	case KeyEvent.VK_SHIFT:
	        	case KeyEvent.VK_CONTROL:
	        	case KeyEvent.VK_ALT:
	        	case KeyEvent.VK_ALT_GRAPH:
	        	case KeyEvent.VK_META:
	        		break;
	        	default:
	        		sb.append(getVKText(keyEvent.getKeyCode()));
	        		break;
	        	}
	        }

	        return sb.toString();
	    }
		
		private static String getModifiersText(int modifiers) {
			StringBuilder sb = new StringBuilder();
			modifiers = mapNewModifiers(mapOldModifiers(modifiers));
	        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0 ) {
	            sb.append("\u21e7");
//	            sb.append("Shift+");
	        }
	        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0 ) {
	        	sb.append("\u2303");
//	        	sb.append("Ctrl+");
	        }
	        if ((modifiers & InputEvent.META_DOWN_MASK) != 0 ) {
	        	sb.append("\u2318");
//	        	sb.append("Meta+");
	        }
	        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0 ) {
	        	sb.append("\u2325");
//	        	sb.append("Alt+");
	        }
	        return sb.toString();
	    }
		
		private static int mapOldModifiers(int modifiers) {
			if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
				modifiers |= InputEvent.SHIFT_DOWN_MASK;
			}
			if ((modifiers & InputEvent.ALT_MASK) != 0) {
				modifiers |= InputEvent.ALT_DOWN_MASK;
			}
			if ((modifiers & InputEvent.ALT_GRAPH_MASK) != 0) {
				modifiers |= InputEvent.ALT_GRAPH_DOWN_MASK;
			}
			if ((modifiers & InputEvent.CTRL_MASK) != 0) {
				modifiers |= InputEvent.CTRL_DOWN_MASK;
			}
			if ((modifiers & InputEvent.META_MASK) != 0) {
				modifiers |= InputEvent.META_DOWN_MASK;
			}

			modifiers &= InputEvent.SHIFT_DOWN_MASK
			| InputEvent.ALT_DOWN_MASK
			| InputEvent.ALT_GRAPH_DOWN_MASK
			| InputEvent.CTRL_DOWN_MASK
			| InputEvent.META_DOWN_MASK
			| InputEvent.BUTTON1_DOWN_MASK
			| InputEvent.BUTTON2_DOWN_MASK
			| InputEvent.BUTTON3_DOWN_MASK;

			return modifiers;
		}

		private static int mapNewModifiers(int modifiers) {
			if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
				modifiers |= InputEvent.SHIFT_MASK;
			}
			if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
				modifiers |= InputEvent.ALT_MASK;
			}
			if ((modifiers & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
				modifiers |= InputEvent.ALT_GRAPH_MASK;
			}
			if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
				modifiers |= InputEvent.CTRL_MASK;
			}
			if ((modifiers & InputEvent.META_DOWN_MASK) != 0) {
				modifiers |= InputEvent.META_MASK;
			}

			return modifiers;
		}
		
		static String getVKText(int keyCode) { 
	        VKCollection vkCollect = getVKCollection();
	        Integer key = new Integer(keyCode);
	        String name = vkCollect.findName(key);
	        if (name != null) {
	            return name.substring(3);
	        }
	        int expected_modifiers = 
	            (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);

	        Field[] fields = KeyEvent.class.getDeclaredFields();
	        for (int i = 0; i < fields.length; i++) {
	            try {
	                if (fields[i].getModifiers() == expected_modifiers
	                    && fields[i].getType() == Integer.TYPE
	                    && fields[i].getName().startsWith("VK_")
	                    && fields[i].getInt(KeyEvent.class) == keyCode) 
	                {
	                    name = fields[i].getName();
	                    vkCollect.put(name, key);
	                    return name.substring(3);
	                }
	            } catch (IllegalAccessException e) {
	                assert(false);
	            }
	        }
	        return "UNKNOWN";
	    }
		
	    /**
	     * Associates VK_XXX (as a String) with code (as Integer). This is
	     * done to avoid the overhead of the reflective call to find the
	     * constant.
	     */
	    private static VKCollection vks;
		
	    private static VKCollection getVKCollection() {
	        if (vks == null) {
	            vks = new VKCollection();
	        }
	        return vks;
	    }
	    
	    private static class VKCollection {
			Map<Integer, String> code2name;
		    Map<String, Integer> name2code;

		    public VKCollection() {
		        code2name = new HashMap<Integer, String>();
		        name2code = new HashMap<String, Integer>();
		    }

		    public synchronized void put(String name, Integer code) {
		        assert((name != null) && (code != null));
		        assert(findName(code) == null);
		        assert(findCode(name) == null);
		        code2name.put(code, name);
		        name2code.put(name, code);
		    }

		    public synchronized Integer findCode(String name) {
		        assert(name != null);
		        return (Integer)name2code.get(name);
		    }

		    public synchronized String findName(Integer code) {
		        assert(code != null);
		        return (String)code2name.get(code);
		    }
		}
		
	}
//    sb.append("\u21e7");
////    sb.append("Shift+");
//}
//if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0 ) {
//	sb.append("\u2303");
////	sb.append("Ctrl+");
//}
//if ((modifiers & InputEvent.META_DOWN_MASK) != 0 ) {
//	sb.append("\u2318");
////	sb.append("Meta+");
//}
//if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0 ) {
//	sb.append("\u2325");
	@SuppressWarnings("serial")
	public static JCheckBox createKeyLabel() {
		JCheckBox label = new JCheckBox("                       ", true) {
			private KeyListener keyListener;
			
			public void addNotify() {
				super.addNotify();
				setToolTipText("<html><font face=\"Lucida Grande\">" +
						"\u21e7 is SHIFT, " +
						"\u2303 is CTRL, " +
						"\u2325 is ALT or OPTION, " +
						"\u2318 is META or COMMAND"
						);
				setFont(new Font("Lucida Grande", Font.BOLD, getFont().getSize()));
				keyListener = new KeyListener(this);
				addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						reconfigure();
					}
				});
				reconfigure();
			}

			public void removeNotify() {
				super.removeNotify();
				if (keyListener != null) {
					Toolkit.getDefaultToolkit().removeAWTEventListener(keyListener);					
				}
			}
			
			private void reconfigure() {
				if (isSelected()) {
					Toolkit.getDefaultToolkit().addAWTEventListener(keyListener, AWTEvent.KEY_EVENT_MASK);
					setForeground(null);
					setText("");
				} else {
					Toolkit.getDefaultToolkit().removeAWTEventListener(keyListener);
					setForeground(SystemColor.textInactiveText);
					setText("Disabled");
				}
			}
		};
		label.setHorizontalAlignment(SwingConstants.LEADING);
		label.setFocusPainted(false);
		label.setFocusable(false);
		label.setContentAreaFilled(false);
		
		return label;
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (UnsupportedLookAndFeelException e) {
		}
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(createKeyLabel(), BorderLayout.NORTH);
		contentPane.add(new JScrollPane(new JTextArea()), BorderLayout.CENTER);
		frame.setSize(200,200);
		frame.setVisible(true);
	}
}

