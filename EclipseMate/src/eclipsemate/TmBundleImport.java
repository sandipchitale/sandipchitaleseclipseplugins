/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package eclipsemate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JTextArea;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;


/**
 * Import TM bundles  (.tmbundle) into NetBeans live code templates.
 * Special support for Ruby-oriented bundles (for example it knows
 * what the shell command `snippet_paren.rb end` means.)
 *
 * @todo i18n
 * @todo progress bar
 * @todo Support transformations?
 *   http://macromates.com/textmate/manual/snippets#transformations
 * @todo Skip abbrevs that cannot be supported for the tabtrigger, such as ":"
 * @todo Sanitize filename and string name output for UTF-8
 *
 * @author Tor Norbye
 */
public class TmBundleImport {
    // UUIDs for macros we know about that we have already built in in a better way and importing
    // would clobber
    private static String[] handledBetter =
        new String[] {
            "0F940CBC-2173-49FF-B6FD-98A62863F8F2", "855FC4EF-7B1E-48EE-AD4E-5ECB8ED79D1C",
            "855FC4EF-7B1E-48EE-AD4E-5ECB8ED79D1C"
        };
    public static final String RUBY_MIME_TYPE = "text/x-ruby"; // application/x-ruby is also used a fair bit.
    public static final String RHTML_MIME_TYPE = "application/x-httpd-eruby"; // NOI18N
    public static final String YAML_MIME_TYPE = "text/x-yaml";
    private int imported;
    private StringBuilder log = new StringBuilder();
    private final String TMKEY = "key"; // NOI18N
    private final String TMSTRING = "string"; // NOI18N
    private final String TMDICT = "dict"; // NOI18N
    private final String TMCONTENT = "content"; // NOI18N
    private final String TMNAME = "name"; // NOI18N
    private final String TMSCOPE = "scope"; // NOI18N
    private final String TMTAGTRIGGER = "tabTrigger"; // NOI18N
    private final String TMUUID = "uuid"; // NOI18N
    private SortedSet<String> builtin = new TreeSet<String>();
    private SortedSet<String> nonTabSnippets = new TreeSet<String>();
    private SortedSet<String> regexpSnippets = new TreeSet<String>();
    private SortedSet<String> shellCmdSnippets = new TreeSet<String>();
    private Map<String,String> uuidMap = new HashMap<String,String>();
    private Map<String,String> nameMap = new HashMap<String,String>();
    private Map<String,Set<String>> conflicts = new HashMap<String,Set<String>>();
    
    private Map <String,Map<String,String>> propsByMime =
        new HashMap <String,Map<String,String>>();
    private String defaultMime;
    private boolean wasBinary;
    private java.util.List<String> modified = new java.util.ArrayList<String>();
    private File exportDir = new File("/tmp/tm");

    public TmBundleImport() {
    }

    public Map /*String,Map<String,String>>*/ importBundle(File file, String defaultMime) {
        this.defaultMime = defaultMime;

        File snippets = new File(file, "Snippets");

        if (!snippets.exists()) {
            log.append(file.getPath() + " does not exist - is this really a TM bundle?\n");

            return null;
        }

        log.append("IMPORT SUMMARY\n------------------------------\n\n");

        // TODO - Macros, binaries, others?
        File[] snippetFiles = snippets.listFiles();

        for (File f : snippetFiles) {
            if (f.getName().endsWith(".plist") || f.getName().endsWith(".tmSnippet")) {
                importFile(f);
                
                if (wasBinary) {
                    break;
                }
            }
        }

        log.append("\n\n");

        if (conflicts.size() > 0) {
            log.append("The following snippets were skipped because they were all\nassigned to the same tab trigger:\n");
            for (String trigger : conflicts.keySet()) {
                log.append("[" + trigger + "] : ");
                boolean first = true;
                for (String name : conflicts.get(trigger)) {
                    if (!first) {
                        log.append(", ");
                    } else {
                       first = false;
                    }
                    log.append(name);
                }
                log.append("\n");
            }
        }

        if (nonTabSnippets.size() > 0) {
            log.append("The following  " + nonTabSnippets.size() +
                " snippets were skipped because they were not bound to the Tab key:\n");
            log.append(nonTabSnippets.toString());
            log.append("\n");
        }

        if (builtin.size() > 0) {
            log.append("The following  " + builtin.size() +
                " snippets were skipped because they are already included in the IDE:\n");
            log.append(builtin.toString());
            log.append("\n");
        }

        if (regexpSnippets.size() > 0) {
            log.append("The following  " + regexpSnippets.size() +
                " snippets were skipped because they use regular expression\n" +
                "transformations which is not yet supported:\n");
            log.append(regexpSnippets.toString());
            log.append("\n");
        }

        if (shellCmdSnippets.size() > 0) {
            log.append("The following  " + shellCmdSnippets.size() +
                " snippets were skipped because they use shell commands " +
                "which is not yet supported:\n");
            log.append(shellCmdSnippets.toString());
            log.append("\n");
        }

        log.append("\n\n");
        //log.append(NbBundle.getMessage(TmBundleImport.class, "TmImportSummary", imported));
        log.append("Imported " + imported + " snippets.\n");
        int skipped =  nonTabSnippets.size() + builtin.size() + regexpSnippets.size() +
            shellCmdSnippets.size();
        log.append("Skipped " + skipped + " snippets.\n");

        if (modified.size() > 0) {
            log.append("\n\nDetails on modified snippets:\n");
            for (String info : modified) {
                log.append(info);
                log.append("\n");
            }
        }
        
        JTextArea text = new JTextArea();
        text.setColumns(60);
        text.setRows(15);
        text.setText(log.toString());
        text.setCaretPosition(0);

        if (exportDir != null) {
            exportCodeTemplates();
        }
        
        // Display InfoDIalog
        // TODO

        return propsByMime;
    }
    
    private void exportCodeTemplates() {
        if (!exportDir.exists()) {
            log.append("Warning - export dir " + exportDir.getPath() + " doesn't exist");
            return;
        }
        
        try {
            FileWriter bundle = new FileWriter(new File(exportDir, "codetemplates-Bundle.properties"));
            FileWriter summary = new FileWriter(new File(exportDir, "codetemplates-summary.txt"));
            
            // Emit code templates
            for (String mime : propsByMime.keySet()) {
                String fileName = "codetemplates-" + mime.replace("/", "-") + ".xml";
                FileWriter fw = new FileWriter(new File(exportDir, fileName));
            
                bundle.write("# Mime Type " + mime + "\n");

                String xmlHeader = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + // NOI18N
                "<!DOCTYPE codetemplates PUBLIC \"-//NetBeans//DTD Editor Code Templates settings 1.0//EN\" \"http://www.netbeans.org/dtds/EditorCodeTemplates-1_0.dtd\">\n"; // NOI18N
                fw.write(xmlHeader);
                fw.write("<codetemplates>\n"); // NOI18N
                
                Map<String,String> abbrevs = propsByMime.get(mime);
                List<String> abbrevNames = new ArrayList<String>(abbrevs.keySet());
                Collections.sort(abbrevNames);
                for (String tabTrigger : abbrevNames) {
                    String displayName = nameMap.get(tabTrigger);
                    String code = abbrevs.get(tabTrigger);
                    String uuid = uuidMap.get(tabTrigger);
                    
                    fw.write("  <codetemplate abbreviation=\""); // NOI18N
                    fw.write(tabTrigger);
                    fw.write("\""); // NOI18N
                    if (displayName != null) {
                        String bundleKey = "ct_" + tabTrigger;
                        fw.write(" descriptionId=\"");
                        fw.write(bundleKey);
                        fw.write("\"");
                        bundle.write(bundleKey);
                        bundle.write("=");
                        // The display name should be HTML-safe
                        assert displayName.indexOf('\n') == -1;
//                        try {
//                            displayName = XMLUtil.toElementContent(displayName);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                        bundle.write(displayName);
                        bundle.write("\n");
                    }
                    if (uuid != null) {
                        fw.write(" uuid=\"");
                        fw.write(uuid);
                        fw.write("\"");
                    }
                    fw.write(">\n    <code>\n<![CDATA[");
                    fw.write(code.replace("||", "|"));
                    fw.write("]]>\n");
                    fw.write("    </code>\n");
                    fw.write("  </codetemplate>\n"); // NOI18N
                }
                
                fw.write("</codetemplates>\n");
                fw.close();
            }
            
            summary.write(log.toString());
            summary.close();

            bundle.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Get text within an element */
    private String getText(Element element) {
        StringBuilder sb = new StringBuilder();
        NodeList nl = element.getChildNodes();

        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);

            if (n.getNodeType() == Node.TEXT_NODE) {
                sb.append(n.getNodeValue());
            }
        }

        return sb.toString();
    }

    // TODO: only set this to empty if a first parse fails?
    public static class NullEntityResolver implements EntityResolver {
        public org.xml.sax.InputSource resolveEntity(String pubid, String sysid) {
            return new org.xml.sax.InputSource(new ByteArrayInputStream(new byte[0]));
        }
    }
    
    private void importFile(File file) {
//        try {
            Element r = null;

//            try {
//                InputSource inputSource = new InputSource(new FileReader(file));
//                
//                org.w3c.dom.Document doc = XMLUtil.parse(inputSource, false, false, null, new NullEntityResolver());
//                r = doc.getDocumentElement();
//            } catch (SAXParseException spe) {
//                BufferedReader f = new BufferedReader(new FileReader(file));
//                String s = f.readLine();
//                if (s != null && s.startsWith("bplist")) {
//                    wasBinary = true;
//                    log.append("Binary plist files!\n\nYour snippets are in a binary plist format.\nTo " +
//                            "be imported, they must be in XML format.\n\n" +
//                            "First make a backup of your bundle tree (zip -r bundle.zip whatever.bundle)\n" +
//                            "and then run \"plutil -convert xml1 snippet.plist\" to convert each file.\n" +
//                            "To perform this for a directory tree, use this:\n\n" +
//                            "find . -name \"*.plist\" -exec plutil -convert xml1 {} \\;\n\n" +
//                            "(To change back, replace -convert xml1 with -convert binary1");
//                    return;
//                }
//                
//                log.append("Parsing error in \"" + file.getName() + "\"; skipping\n  " +
//                    spe.getMessage());
//
//                return;
//            }

            NodeList dicts = r.getElementsByTagName(TMDICT); // NOI18N

            if (dicts.getLength() != 1) {
                log.append(("Unexpected number of <dict> elements in plist \"" + file.getName() +
                    "\"\n"));

                return;
            }

            Element dict = (Element)dicts.item(0);
            NodeList properties = dict.getChildNodes();
            Map<String, String> map = new HashMap<String, String>();
            String key = null;

            for (int i = 0; i < properties.getLength(); i++) {
                Node node = properties.item(i);

                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                Element e = (Element)node;

                if (e.getTagName().equals(TMKEY)) { // NOI18N
                    key = getText(e);
                } else if (e.getTagName().equals(TMSTRING)) { // NOI18N

                    String value = getText(e);

                    if (key == null) {
                        log.append("Abort: Unexpected <string> (missing key) for " + value +
                            " in \"" + file.getName() + "\"\n");

                        return;
                    }

                    map.put(key, value);
                }
            }

            if (map.containsKey(TMTAGTRIGGER) && map.containsKey(TMCONTENT)) {
                String tabTrigger = map.get(TMTAGTRIGGER);
                String content = map.get(TMCONTENT);

                String name = map.get(TMNAME);

                // Not yet used...
                String scope = map.get(TMSCOPE);
                String uuid = map.get(TMUUID);

                if (skipKnownRubyTemplates(tabTrigger, content, name, scope, uuid)) {
                    builtin.add(tabTrigger);

                    return;
                }

                String netbeansAbbrev = convertTmContent(name, content, log, tabTrigger);

                if (netbeansAbbrev == null) {
                    // Couldn't convert - might contain unsuppored syntax
                    return;
                }

                String mimeType = getMimeType(scope);

                addAbbrev(mimeType, tabTrigger, name, netbeansAbbrev, uuid);
            } else {
                String name = map.get(TMNAME);

                if (name == null) {
                    name = file.getName();
                }

                nonTabSnippets.add(name);

                return;
            }
//        } catch (IOException ioe) {
//            // TODO
//        } catch (SAXException se) {
//            // TODO
//        }
    }

    /** Get a mime type to use for the given TM scope */
    private String getMimeType(String scope) {
        if (scope == null || scope.startsWith("source.ruby")) { // NOI18N

            return RUBY_MIME_TYPE;
        }

        if (scope.startsWith("text.html.ruby")) { // NOI18N

            return RHTML_MIME_TYPE;
        }

        if (scope.startsWith("source.yaml")) { // NOI18N

            return YAML_MIME_TYPE;
        }

        // TODO
        // text.html, (string.quoted.double.ruby|string.interpolated.ruby) - string source.
        // TODO - TM macros completely unrelated to Ruby - e.g. python etc.
        return defaultMime;
    }

    /**
     * Avoid importing templates that are already built in and handled in a better way than the
     * importer will
     */
    private boolean skipKnownRubyTemplates(String tabTrigger, String content, String name,
        String scope, String uuid) {
        for (String id : handledBetter) {
            if (id.equals(uuid)) {
                return true;
            }
        }

        return false;
    }

    /** For unit testing */
    public static String testConversion(String content) {
        return new TmBundleImport().convertTmContent(null, content, new StringBuilder(), null);
    }

    private String convertTmContent(String name, String content, StringBuilder log,
        String tabTrigger) {
        StringBuilder sb = new StringBuilder();

        // Convert the abbreviation in content into a NetBeans-style abbreviation

        // This means changing the escaping rules (in NetBeans, | must be written as || right now),
        // changing variable name references,
        // and handling TM-specific stuff like backquotes (execute command), etc.

        // collect { |${1:e}| $0 }   =>    collect { ||${1 default="e"}|| ${cursor}

        // $num => tabStop$num
        // Tricky:
        // open("path;or;url", "w") do |doc| .. end (ope).plist:   
        //   <string>open(${1:"${2:path/or/url/or/pipe}"}${3/(^[rwab+]+$)|.*/(?1:, ")/}${3:w}${3/(^[rwab+]+$)|.*/(?1:")/}) { |${4:io}| $0 }</string>

        // Not handled: nested expressions ${1::${something}} - look in opt parse
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            // Escaped chars - \$ for example should insert "$"
            if (c == '\\') {
                if (i < (content.length() - 1)) {
                    i++;
                    c = content.charAt(i);
                }

                sb.append(c);

                continue;
            } else if (c == '|') {
                // NetBeans requires || for | (because | used to mean the cursor position)
                sb.append("||");

                continue;
            } else if ((c == '$') && (i < (content.length() - 1))) {
                char peek = content.charAt(i + 1);

                if (Character.isDigit(peek)) {
                    // It's $0, $1, $2, .... these are tabstops.
                    i++; // Skip both
                    sb.append('$');
                    sb.append('{');
                    sb.append(getTabStopString(peek));
                    sb.append('}');
                } else if ((peek == '{') && (i < (content.length() - 3))) {
                    // Special variable section:
                    // ${0:foo} --> ${foo}
                    char peek2 = content.charAt(i + 2);
                    char peek3 = content.charAt(i + 3);

                    if (Character.isDigit(peek2) && (peek3 == ':')) {
                        // Change {$1:foo "bar"} to ${1 default="foo \"bar\""},
                        // but if it contains nested braces, just strip the whole variable
                        StringBuilder s = new StringBuilder();

                        s.append("${");
                        i += 4;

                        s.append(peek2);
                        s.append(" default=\"");
                        
                        int nesting = 0;
                        for (; i < content.length(); i++) {
                            char k = content.charAt(i);
                            if (k == '{') {
                                nesting++;
                                s = null;
                            } else if (k == '}') {
                                if (nesting == 0) {
                                    break;
                                }
                                nesting--;
                            } else if (k == '"') {
                                if (s != null) {
                                    s.append("\\");
                                }
                            }
                            if (s != null) {
                                s.append(k);
                            }
                        }
                        
                        if (s != null) {
                            sb.append(s.toString());
                            sb.append("\"}");
                        } else {
                            modified.add(tabTrigger + ": Stripped out tabStop " + peek2 + " - contains nested evaluation");
                        }
                        continue;
                    } else if (Character.isDigit(peek2) && (peek3 == '/')) {
                        //// Regexp
                        //// Strip to the end
                        //sb.append("${");
                        //sb.append(getTabStopString(peek2));
                        //for (; i < content.length(); i++) {
                        //    c = content.charAt(i);
                        //    if (c == '}') {
                        //        sb.append('}');
                        //        break;
                        //    }
                        //}
                        regexpSnippets.add(tabTrigger);

                        return null;
                    } else {
                        // ${ but not ${[digit]: - just put in verbatim
                        sb.append(c);
                    }
                } else if (peek == 'T') {
                    // Look for special variables - TM_SELECTED_WORD, TM_
                    String SELECTED_TEXT = "TM_SELECTED_TEXT";

                    if (content.regionMatches(false, i + 1, SELECTED_TEXT, 0, SELECTED_TEXT.length())) {
                        i += SELECTED_TEXT.length();
                        sb.append("${selection line allowSurround}");

                        continue;
                    }

                    //http://macromates.com/textmate/manual/snippets
                    // TODO:  TM_FILENAME, TM_FILEPATH, TM_CURRENT_LINE, TM_COLUMN_NUMBER,
                    // TM_CURRENT_WORD, TM_PROJECT_DIRECTORY, TM_DIRECTORY, TM_LINE_NUMBER,
                    // TM_SOFT_TABS, TM_TAB_SIZE, TM_MINIMIZE_PAREN
                    // Turns out, only TM_SELECTED_TEXT seems to be used at least in the Ruby and Rails bundles I've
                    // seen so I won't work too hard to support this
                    sb.append(c);
                } else {
                    sb.append(c);
                }
            } else if (c == '`') {
                // Special case: minimize parens. Later I can consider doing something
                // more clever here where I also do conditional parenthesis control
                String END_PAREN = "`snippet_paren.rb end`"; // NOI18N

                if (content.regionMatches(false, i, END_PAREN, 0, END_PAREN.length())) {
                    i += END_PAREN.length();
                    i--; // compensate for loop iteration increment
                    sb.append(")");

                    continue;
                }

                String BEGIN_PAREN = "`snippet_paren.rb`"; // NOI18N

                if (content.regionMatches(false, i, BEGIN_PAREN, 0, BEGIN_PAREN.length())) {
                    i += BEGIN_PAREN.length();
                    i--; // compensate for loop iteration increment
                    sb.append("(");

                    continue;
                }

                // Command execution: Not supported!  (But in the Ruby bundle I'm looking at,
                // this isn't used so shouldn't be a huge problem)
                shellCmdSnippets.add(tabTrigger);

                return null;
            } else {
                sb.append(c);
            }
        }

        String s = sb.toString();
        
        // If there are multiple versions of any of the stops, mark subsequent versions as noneditable
        assert s.indexOf("${10") == -1; // Make sure we don't have more than 9 since below code assumes single-digit stops
        for (int i = 0; i < 9; i++) {
            String stop = "${" + i;
            int first = s.indexOf(stop);
            if (first != -1 && s.indexOf(stop, first+1) != -1) {
                // Gotta replace
                StringBuilder sb2 = new StringBuilder();
                int offset = first+3;
                sb2.append(s.substring(0, first+3));
                while (offset < s.length()) {
                    int next = s.indexOf(stop, offset);
                    if (next == -1) {
                        sb2.append(s.substring(offset));
                        break;
                    } else {
                        sb2.append(s.substring(offset, next+3));
                        sb2.append(" editable=\"false\"");
                        offset = next+3;
                    }
                }
                s = sb2.toString();
            }
        }
        
        return s;
    }

    private static String getTabStopString(char digit) {
        assert Character.isDigit(digit);

        if (digit == '0') {
            return "cursor"; // NOI18N
        } else {
            return "tabStop" + digit + " default=\"\""; // NOI18N
        }
    }

    /**
     * @todo Do something about desc?
     * @todo Add a UID to easy in import duplicate avoidance?
     */
    private void addAbbrev(String mime, String key, String desc, String content, String uuid) {
        if (nameMap.containsKey(key)) {
            Set<String> others = conflicts.get(key);
            if (others == null) {
                others = new HashSet<String>();
                conflicts.put(key, others);
                others.add(nameMap.get(key));
            }
            others.add(desc);
            
            // Skip this one
            return;
        }

        Map<String,String> map = propsByMime.get(mime);

        if (map == null) {
            map = new HashMap<String,String>();
            propsByMime.put(mime, map);
        }

        map.put(key, content);

        nameMap.put(key, desc);
        uuidMap.put(key, uuid);

        imported++;
    }
}
