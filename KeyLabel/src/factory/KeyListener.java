/**
 * 
 */
package factory;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

class KeyListener implements AWTEventListener {

	public KeyListener() {
	}
	
	public void attach() {
		Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
	}
	
	public void dettach() {
		Toolkit.getDefaultToolkit().removeAWTEventListener(this);
	}
	
	/** A list of event listeners for this component. */
	protected EventListenerList listenerList = new EventListenerList();

	/**
	 * Adds a <code>ChangeListener</code> to the button.
	 * 
	 * @param l
	 *            the listener to be added
	 */
	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);
	}

	/**
	 * Removes a ChangeListener from the button.
	 * 
	 * @param l
	 *            the listener to be removed
	 */
	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
	}

	/**
	 * Returns an array of all the <code>ChangeListener</code>s added to this
	 * AbstractButton with addChangeListener().
	 * 
	 * @return all of the <code>ChangeListener</code>s added or an empty array
	 *         if no listeners have been added
	 * @since 1.4
	 */
	public ChangeListener[] getChangeListeners() {
		return (ChangeListener[]) (listenerList.getListeners(ChangeListener.class));
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created.
	 * 
	 * @see EventListenerList
	 */
	private void fireChanged(String keyText) {
		if (keyText == null) {
			return;
		}
		ChangeEvent changeEvent = new ChangeEvent(keyText);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChangeListener.class) {
				((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
			}
		}
	}

	public void eventDispatched(AWTEvent event) {
		if (event instanceof KeyEvent) {
			fireChanged(getKeyText((KeyEvent) event));
		}
	}

	private static String getKeyText(KeyEvent keyEvent) {
		char keyChar = keyEvent.getKeyChar();
		int keyCode = keyEvent.getKeyCode();
		if (keyEvent.getID() == KeyEvent.KEY_TYPED && keyChar == KeyEvent.CHAR_UNDEFINED) {
			return null;
		}
		if (keyEvent.getID() == KeyEvent.KEY_RELEASED && (keyCode == KeyEvent.VK_SHIFT || keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_ALT || keyCode == KeyEvent.VK_META)) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		if (keyChar != KeyEvent.CHAR_UNDEFINED && keyChar != KeyEvent.VK_BACK_SPACE && keyChar != KeyEvent.VK_DELETE && keyChar != KeyEvent.VK_ESCAPE && ((!keyEvent.isAltDown()) && (!keyEvent.isControlDown()) && (!keyEvent.isMetaDown()))) {
			sb.append("" + keyChar);
		} else if (keyCode != KeyEvent.VK_UNDEFINED) {
			sb.append(getModifiersText(keyEvent.getModifiers()));
			switch (keyCode) {
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
			case KeyEvent.VK_SHIFT:
			case KeyEvent.VK_CONTROL:
			case KeyEvent.VK_ALT:
			case KeyEvent.VK_ALT_GRAPH:
			case KeyEvent.VK_META:
				break;
			default:
				sb.append(getVKText(keyCode));
				break;
			}
		}

		return sb.toString();
	}

	private static String getModifiersText(int modifiers) {
		StringBuilder sb = new StringBuilder();
		modifiers = mapNewModifiers(mapOldModifiers(modifiers));
		if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
			sb.append("\u21e7");
			// sb.append("Shift+");
		}
		if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
			sb.append("\u2303");
			// sb.append("Ctrl+");
		}
		if ((modifiers & InputEvent.META_DOWN_MASK) != 0) {
			sb.append("\u2318");
			// sb.append("Meta+");
		}
		if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
			sb.append("\u2325");
			// sb.append("Alt+");
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

		modifiers &= InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK | InputEvent.ALT_GRAPH_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK | InputEvent.BUTTON2_DOWN_MASK
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
		KeyListener.VKCollection vkCollect = getVKCollection();
		Integer key = new Integer(keyCode);
		String name = vkCollect.findName(key);
		if (name != null) {
			return name.substring(3);
		}
		int expected_modifiers = (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);

		Field[] fields = KeyEvent.class.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			try {
				if (fields[i].getModifiers() == expected_modifiers && fields[i].getType() == Integer.TYPE && fields[i].getName().startsWith("VK_") && fields[i].getInt(KeyEvent.class) == keyCode) {
					name = fields[i].getName();
					vkCollect.put(name, key);
					return name.substring(3);
				}
			} catch (IllegalAccessException e) {
				assert (false);
			}
		}
		return "UNKNOWN";
	}

	/**
	 * Associates VK_XXX (as a String) with code (as Integer). This is done to
	 * avoid the overhead of the reflective call to find the constant.
	 */
	private static KeyListener.VKCollection vks;

	private static KeyListener.VKCollection getVKCollection() {
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
			assert ((name != null) && (code != null));
			assert (findName(code) == null);
			assert (findCode(name) == null);
			code2name.put(code, name);
			name2code.put(name, code);
		}

		public synchronized Integer findCode(String name) {
			assert (name != null);
			return (Integer) name2code.get(name);
		}

		public synchronized String findName(Integer code) {
			assert (code != null);
			return (String) code2name.get(code);
		}
	}

}