package codeclips.templates;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;

import codeclips.Activator;


class CodeClipsCompletionProcessor extends TemplateCompletionProcessor {


	public CodeClipsCompletionProcessor() {
	}

	@Override
	protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
		return new CodeClipTemplateContextType("");
	}

	@Override
	protected Image getImage(Template template) {
		return Activator.getDefault().getImage(Activator.CODECLIP);
	}

	@Override
	protected Template[] getTemplates(String contextTypeId) {
		Template[] templates = Activator.getDefault().getTemplateStore().getTemplates();
		List<CodeClipTemplate> codeClipTemplates = new LinkedList<CodeClipTemplate>();
		for (Template template : templates) {
			codeClipTemplates.add(new CodeClipTemplate(template));
		}
		return codeClipTemplates.toArray(templates);
	}

	private static final String SPACES= "\\s*+"; //$NON-NLS-1$

	@SuppressWarnings("unused")
	private static String processExpansion(String expansion) {
		// cursor $ or ${0} to ${cursor}
		expansion = expansion.replaceAll(Pattern.quote("$0"), Matcher.quoteReplacement("${cursor}")); //$NON-NLS-1$  //$NON-NLS-2$
		expansion = expansion.replaceAll(Pattern.quote("${0}"), Matcher.quoteReplacement("${cursor}")); //$NON-NLS-1$  //$NON-NLS-2$

		// transform ${n:default value} to ${default value:n} where n is a digit
		expansion = expansion.replaceAll(
				  "\\$\\{"        //$NON-NLS-1$
				+ SPACES
				+ "(\\d)"         //$NON-NLS-1$
				+ SPACES
				+ ":"             //$NON-NLS-1$
				+ SPACES
				+ "(\\w+)"        //$NON-NLS-1$
				+ "\\}"           //$NON-NLS-1$
				,"\\${$2:$1}");   //$NON-NLS-1$
		return expansion;
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		ICompletionProposal[] completionProposals = super.computeCompletionProposals(viewer, offset);
		for (int i = 0; i < completionProposals.length; i++) {
			if (completionProposals[i] instanceof CodeClipTemplateProposal) {
				CodeClipTemplateProposal snippetTemplateProposal = (CodeClipTemplateProposal) completionProposals[i];
				snippetTemplateProposal.setTemplateProposals(completionProposals);
				Template template = snippetTemplateProposal.getTemplateSuper();
				char triggerChar = ' ';
				if (i < 9) {
					triggerChar = (char)('1'+i);
					snippetTemplateProposal.setTriggerChar(triggerChar);
				}
				if (FIXED_WIDTH_STYLER_WITH_FOREGROUND_COLOR == null) {
					Color foregroundColor = viewer.getTextWidget().getDisplay().getSystemColor(SWT.COLOR_WHITE);
					Color backgroundColor = viewer.getTextWidget().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
					FIXED_WIDTH_STYLER_WITH_FOREGROUND_COLOR = new CustomStyler(foregroundColor, backgroundColor);
				}
				StyledString styledString = new StyledString(" " + String.valueOf(triggerChar) + " (", FIXED_WIDTH_STYLER);
				String suffix = template.getName() + " \u00BB";
				styledString.append(new StyledString(" " + suffix + " ", FIXED_WIDTH_STYLER_WITH_FOREGROUND_COLOR)); //$NON-NLS-1$);
//				int trailingSpaces = 10 - suffix.length();
//				if (trailingSpaces > 0) {
//					styledString.append(new StyledString(String.format("%1$" + trailingSpaces + "." + trailingSpaces + "s", " "), FIXED_WIDTH_STYLER)); //$NON-NLS-1$);
//				}
				styledString.append(new StyledString(") - " + template.getDescription() + "            ", FIXED_WIDTH_STYLER)); //$NON-NLS-1$
				snippetTemplateProposal.setStyledDisplayString(styledString);
			}
		}
		return completionProposals;
	}

	@Override
	protected ICompletionProposal createProposal(Template template, TemplateContext context, IRegion region, int relevance) {
		return new CodeClipTemplateProposal(template, context, region, getImage(template), relevance);
	}

	@Override
	protected TemplateContext createContext(ITextViewer viewer, IRegion region) {
		TemplateContextType contextType= getContextType(viewer, region);
		if (contextType != null) {
			IDocument document= viewer.getDocument();
			return new DocumentCodeClipsTemplateContext(contextType, document, region.getOffset(), region.getLength());
		}
		return null;
	}

	// Allow any non-whitespace as a prefix.
	protected String extractPrefix(ITextViewer viewer, int offset) {
		int i= offset;
		IDocument document= viewer.getDocument();
		if (i > document.getLength())
			return ""; //$NON-NLS-1$

		try {
			while (i > 0) {
				char ch= document.getChar(i - 1);
				if (Character.isWhitespace(ch))
					break;
				i--;
			}

			return document.get(i, offset - i);
		} catch (BadLocationException e) {
			return ""; //$NON-NLS-1$
		}
	}

	private static class CustomStyler extends Styler {
		private Color foregroundColor;
		private Color backgroundColor;

		CustomStyler() {
			this(null, null);
		}

		CustomStyler(Color foregroundColor, Color backgroundColor) {
			this.foregroundColor = foregroundColor;
			this.backgroundColor = backgroundColor;
		}

		public void applyStyles(TextStyle textStyle) {
			if (foregroundColor != null) {
				textStyle.foreground = foregroundColor;
			}
			if (backgroundColor != null) {
				textStyle.background = backgroundColor;
			}

			textStyle.font = JFaceResources.getFontRegistry().get("org.eclipse.jface.textfont"); //$NON-NLS-1$
		}
	}

	private static Styler FIXED_WIDTH_STYLER = new CustomStyler();
	private static Styler FIXED_WIDTH_STYLER_WITH_FOREGROUND_COLOR;
}