package codeclips.templates;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

class CodeClipTemplateContextType extends TemplateContextType {
	CodeClipTemplateContextType(String scope) {
		super(scope);
		addGlobalResolvers();
	}
	
	private static class SysProp extends TemplateVariableResolver {
		/**
		 * Creates a new user name variable
		 */
		public SysProp() {
			super("sysprop", "System property template variable."); //$NON-NLS-1$
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void resolve(TemplateVariable variable, TemplateContext context) {
			String name = variable.getName();
			if (variable.getType().equals(name)) {
				Set keySet = System.getProperties().keySet();
				List<String> values = new LinkedList<String>();
				for (Object object : keySet) {
					if (object instanceof String) {
						String key = (String) object;
						values.add(System.getProperty(key, key) + " (Value of " + key + ")");
						values.add(System.getProperty(key, key));
					}
				}
				variable.setValues(values.toArray(new String[0])); //$NON-NLS-1$
			} else {
				name = name.replaceAll("DOT", ".");
				variable.setValues(new String[] { System.getProperty(name, name) });
			}
		}
		
		@Override
		protected String resolve(TemplateContext context) {
			String value = super.resolve(context);
			return System.getProperty(value, value);
		}
		
		@Override
		protected boolean isUnambiguous(TemplateContext context) {
			return false;
		}
	}
	
	private static class SysPropName extends TemplateVariableResolver {
		public SysPropName() {
			super("syspropname", "System property name template variable."); //$NON-NLS-1$
		}
		
		@Override
		public void resolve(TemplateVariable variable, TemplateContext context) {
			variable.setValues(System.getProperties().keySet().toArray(new String[0])); //$NON-NLS-1$
		}
		
		@Override
		protected boolean isUnambiguous(TemplateContext context) {
			return false;
		}
	}

	private void addGlobalResolvers() {
		// Global
		addResolver(new GlobalTemplateVariables.Cursor());
		addResolver(new GlobalTemplateVariables.WordSelection());
		addResolver(new GlobalTemplateVariables.LineSelection());
		addResolver(new GlobalTemplateVariables.Dollar());
		addResolver(new GlobalTemplateVariables.Date());
		addResolver(new GlobalTemplateVariables.Year());
		addResolver(new GlobalTemplateVariables.Time());
		addResolver(new GlobalTemplateVariables.User());
		addResolver(new ClipboardVariableResolver());
		addResolver(new SysProp());
		addResolver(new SysPropName());
		
		// Tab stops
		addResolver(new TabStopVariableResolver("1", "1st tab stop")); //$NON-NLS-1$ //$NON-NLS-2$
		addResolver(new TabStopVariableResolver("2", "2nd tab stop")); //$NON-NLS-1$ //$NON-NLS-2$
		addResolver(new TabStopVariableResolver("3", "3rt tab stop")); //$NON-NLS-1$ //$NON-NLS-2$
		addResolver(new TabStopVariableResolver("4", "4th tab stop")); //$NON-NLS-1$ //$NON-NLS-2$
		addResolver(new TabStopVariableResolver("5", "5th tab stop")); //$NON-NLS-1$ //$NON-NLS-2$
		addResolver(new TabStopVariableResolver("6", "6th tab stop")); //$NON-NLS-1$ //$NON-NLS-2$
		addResolver(new TabStopVariableResolver("7", "7th tab stop")); //$NON-NLS-1$ //$NON-NLS-2$
		addResolver(new TabStopVariableResolver("8", "8th tab stop")); //$NON-NLS-1$ //$NON-NLS-2$
		addResolver(new TabStopVariableResolver("9", "9th tab stop")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}