package codeclips.templates;

import org.eclipse.jface.text.templates.Template;

public class CodeClipTemplate extends Template {

	public CodeClipTemplate() {
	}

	public CodeClipTemplate(Template template) {
		super(template);
	}

	public CodeClipTemplate(String name, String description,
			String contextTypeId, String pattern, boolean isAutoInsertable) {
		super(name, description, contextTypeId, pattern, isAutoInsertable);
	}


	@Override
	public boolean matches(String prefix, String contextTypeId) {
		boolean matches = super.matches(prefix, contextTypeId);
		if (!matches) {
			return matches;
		}
		return prefix != null && prefix.length() != 0 && getName().toLowerCase().startsWith(prefix.toLowerCase());
	}
}
