package sampler;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import sampler.widget.ColorSampler;

public class ColorSamplerControlContribution extends WorkbenchWindowControlContribution {
	private ColorSampler sampler;

	public ColorSamplerControlContribution() {
	}

	public ColorSamplerControlContribution(String id) {
		super(id);
	}

	protected Control createControl(Composite parent) {
		// Fonts
		Font textFont = null;
		FontDescriptor textFontDescriptor = JFaceResources.getTextFontDescriptor();
		if (textFontDescriptor != null) {
			FontData[] fontData = textFontDescriptor.getFontData();
			if (fontData != null) {
				for (FontData fontDatum : fontData) {
					fontDatum.setHeight(fontDatum.getHeight()-1);
				}
				textFont = new Font(parent.getDisplay(), fontData);
			}
		}

		sampler = new ColorSampler(parent);

		if (textFont != null) {
			sampler.setFont(textFont);
		}
		
		GridData samplerGridData = new GridData(SWT.LEFT, SWT.TOP, false, false);
		sampler.setLayoutData(samplerGridData);
		
		return sampler;
	}

}
