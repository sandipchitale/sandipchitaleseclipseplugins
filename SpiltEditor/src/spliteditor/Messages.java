/**
 * Copyright (c) 2011. Sandip Chitale.
 */
package spliteditor;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Sandip Chitale
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "spliteditor.messages"; //$NON-NLS-1$
	public static String Handler_horizontally;
	public static String Handler_orientation;
	public static String Handler_vertically;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
