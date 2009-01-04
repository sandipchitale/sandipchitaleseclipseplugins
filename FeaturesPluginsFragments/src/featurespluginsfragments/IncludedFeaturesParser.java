/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package featurespluginsfragments;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.update.internal.configurator.FeatureEntry;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A more complete feature parser. It adds the plugins listed to the feature.
 */
@SuppressWarnings("restriction")
public class IncludedFeaturesParser extends DefaultHandler {

	private SAXParser parser;
	private FeatureEntry feature;
	private List<String> includedFeatures;

	private final static SAXParserFactory parserFactory =
		SAXParserFactory.newInstance();

	/**
	 * Constructs a feature parser.
	 */
	public IncludedFeaturesParser(FeatureEntry feature) {
		super();
		this.feature = feature;
		try {
			parserFactory.setNamespaceAware(true);
			this.parser = parserFactory.newSAXParser();
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		}
	}
	
	/**
	 */
	public void parse(){
		InputStream in = null;
		try {
			includedFeatures = new LinkedList<String>();
			if (feature.getSite() == null)
				return;
			String resolvedURL = feature.getURL();
			URL url = null;
			if (resolvedURL.endsWith(".jar")) {
				ClassLoader classLoader = new URLClassLoader(new URL[] { new URL(feature.getSite().getResolvedURL(), resolvedURL) }, null);
				url = classLoader.getResource("feature.xml");
			} else {
				url = new URL(feature.getSite().getResolvedURL(), resolvedURL + "feature.xml");
			}
			if (url == null) {
				return;
			}
			in = url.openStream();
			parser.parse(new InputSource(in), this);
		} catch (SAXException e) {
		} catch (IOException e) {
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e1) {
				}
		}
	}
	
	public List<String> getIncludedFeatures() {
		return includedFeatures;
	}

	/**
	 * Handle start of element tags
	 * @see DefaultHandler#startElement(String, String, String, Attributes)
	 * @since 2.0
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if ("includes".equals(localName)) { //$NON-NLS-1$
			String id = attributes.getValue("id");
			String version = attributes.getValue("version");
			includedFeatures.add(id + ":" + (version == null ? "" : version));
		}
	}
}
