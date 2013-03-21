/**
 * Copyright (c) 2011. Sandip Chitale.
 */
package spliteditor;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;

/**
 * This class specifies the allowed values of <code>orientation</code> 
 * command paramater.
 * 
 * @author Sandip Chitale
 *
 */
public class OrientationParameterValues implements IParameterValues {
	static final String ORIENTATION = "orientation"; //$NON-NLS-1$
	static final String HORIZONTALLY = "horizontally"; //$NON-NLS-1$
	static final String VERTICALLY = "vertically"; //$NON-NLS-1$
	
	private static final Map<String, String> parameterValues = new LinkedHashMap<String, String>();
	static
	{
		parameterValues.put(Messages.getString(HORIZONTALLY), HORIZONTALLY); //$NON-NLS-1$
		parameterValues.put(Messages.getString(VERTICALLY), VERTICALLY); //$NON-NLS-1$
	}
	
	@Override
	public Map<String, String> getParameterValues() {
		return parameterValues;
	}

}
