package com.aperto.magkit.export;

import com.aperto.webkit.utils.StringTools;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import static org.apache.commons.io.IOUtils.closeQuietly;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.dom4j.*;
import org.dom4j.tree.DefaultElement;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * This is an Ant task that exports every node beyond the root node into an extra xml file.
 * <br>The idea of doing this is just read the nodes and decide if they are folders or contents.
 * Call the existing API by executing a http GET request and save the received file in the output path.
 *
 * @author Matthias Heidenreich 14.02.2007
 *
 * Added I18N-Support in export process for dialogs, paragraphs and templates. The rootNode must contains
 * a modus key. Otherwise an internationalization is not possible. The property i18n switchs on the i18n process.
 * If the messagePath property is not set, no property file will be updated. 
 *
 * @author Frank Sommer 19.08.2008
 *
 */
public class ExportTask extends MatchingTask {
    private static final Logger LOGGER = Logger.getLogger(ExportTask.class);
    private static final String TYPE_LABEL = "label";
    private static final String TYPE_DESCRIPTION = "description";
    private static final String TYPE_TITLE = "title";

    private String _rootNode;
    private String _outputPath;
    private String _messagePath;
    private String _mgnlUser;
    private String _mgnlPassword;
    private String _targetHost = "localhost";
    private int _targetPort = 8888;
    private boolean _verbose;
    private String _webapp = "author";
    private boolean _i18n = false;
    private Map<String, SortedProperties> _properties = new HashMap<String, SortedProperties>(2);
    private boolean _propertiesChanged = false;

    /**
     * Enum for switching the export modus.
     */
    private static enum Modus {
        DIALOG, PARAGRAPH, TEMPLATE
    }

    public String getWebapp() {
        return _webapp.indexOf("/") == 0 ? _webapp : "/" + _webapp;
    }

    public void setWebapp(String webapp) {
        _webapp = webapp;
    }

    public String getRootNode() {
        return _rootNode;
    }

    public void setRootNode(String rootNode) {
        _rootNode = rootNode;
    }

    public String getOutputPath() {
        return _outputPath;
    }

    public void setOutputPath(String outputPath) {
        _outputPath = outputPath;
    }

    public String getMgnlUser() {
        return _mgnlUser;
    }

    public void setMgnlUser(String mgnlUser) {
        _mgnlUser = mgnlUser;
    }

    public String getMgnlPassword() {
        return _mgnlPassword;
    }

    public void setMgnlPassword(String mgnlPassword) {
        _mgnlPassword = mgnlPassword;
    }

    public boolean isVerbose() {
        return _verbose;
    }

    public void setVerbose(boolean verbose) {
        _verbose = verbose;
    }

    public String getTargetHost() {
        return _targetHost;
    }

    public void setTargetHost(String targetHost) {
        _targetHost = targetHost;
    }

    public int getTargetPort() {
        return _targetPort;
    }

    public void setTargetPort(int targetPort) {
        _targetPort = targetPort;
    }

    public boolean isI18n() {
        return _i18n;
    }

    public void setI18n(boolean i18n) {
        _i18n = i18n;
    }

    public String getMessagePath() {
        return _messagePath;
    }

    public void setMessagePath(String messagePath) {
        _messagePath = messagePath;
    }

    private String getLogin() {
        return "&mgnlUserId=" + _mgnlUser + "&mgnlUserPSWD=" + _mgnlPassword;
    }

    private Modus getModus() {
        Modus mod = Modus.TEMPLATE;
        if (_rootNode.contains(".dialogs")) {
            mod = Modus.DIALOG;
        } else if (_rootNode.contains(".paragraphs")) {
            mod = Modus.PARAGRAPH;
        }
        return mod;
    }

    /**
     * Starts a recursive export. This method will be called by the Ant runner.
     */
    public void execute() throws BuildException {
        info("try to read the repository structure to export xml.");
        info("_rootNode: " + _rootNode);
        info("_outputPath: " + _outputPath);
        info("_messagePath: " + _messagePath);
        loadProperties();
        recursiveGetSite(_rootNode);
        saveProperties();
    }

    private void saveProperties() {
        if (_propertiesChanged) {
            for (Map.Entry entry : _properties.entrySet()) {
                Properties prop = (Properties) entry.getValue();
                try {
                    FileOutputStream stream = new FileOutputStream(_messagePath + "/" + entry.getKey());
                    prop.store(stream, "# Add your custom strings here. the keys can be used inside the dialog definitions. For example use it as a label.");
                } catch (IOException e) {
                    info("Can not save file: " + entry.getKey());
                }
            }
        }
    }

    private void loadProperties() {
        if (StringUtils.isNotBlank(_messagePath)) {
            File messageDir = new File(_messagePath);
            File[] files = messageDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith("messages_templating_custom");
                }
            });
            for (File file : files) {
                SortedProperties prop = new SortedProperties();
                FileInputStream inStream = null;
                try {
                    inStream = new FileInputStream(file);
                    prop.load(inStream);
                    _properties.put(file.getName(), prop);
                } catch (IOException e) {
                    info("Kann " + file.getPath() + "nicht laden.");
                    closeQuietly(inStream);
                }
            }
        }
    }

    private void recursiveGetSite(String currentSite) {
        String[] elements;
        elements = getChildNodes(currentSite);
        if (elements.length > 1) {
            // there are subcontents ...
            for (int i = 1; i < elements.length; i++) {
                String element = elements[i];
                info("check element: " + element);
                recursiveGetSite(currentSite + "." + element);
            }
        } else if (elements.length == 1) {
            // this is the node to export
            info("exporting node: " + currentSite + "." + elements[0]);
            exportNode(currentSite);
        } else {
            info("Should not appear here: " + currentSite);
            // ... this should never happen.
        }
    }

    private String[] getChildNodes(String currentSite) {
        URL url;
        String[] elements;
        try {
            url = new URL("http", _targetHost, _targetPort, getWebapp() + "/magkit/get_node_children.jsp?currentNode=" + currentSite + getLogin());
            info("access: " + url.toExternalForm());
            HttpClient httpClient = new HttpClient();
            GetMethod getMethod = new GetMethod(url.toString());
            int statusCode = httpClient.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
                throw new BuildException("Unable to call magkit suite properly. Return code was: " + statusCode);
            }
            String body = getMethod.getResponseBodyAsString();
            body = body.trim();
            body = StringTools.replaceAll(body, "\n", "");
            elements = body.split(";");
        } catch (IOException e) {
            throw new BuildException(e);
        }
        return elements;
    }

    private void exportNode(String exportSite) {
        URL url;
        String[] elements = exportSite.split("\\.");
        if (elements.length > 0) {
            StringBuffer path = new StringBuffer(getWebapp() + "/.magnolia/pages/export.html?mgnlRepository=config&mgnlPath=");
            String repository = elements[0];
            if (repository != null) {
                try {
                    for (int i = 1; i < elements.length; i++) {
                        String s = elements[i];
                        path.append("/");
                        path.append(s);
                    }
                    path.append("&mgnlFormat=true&ext=.xml&command=exportxml&exportxml=Export");
                    path.append(getLogin());
                    url = new URL("http", _targetHost, _targetPort, path.toString());
                    HttpClient httpClient = new HttpClient();
                    GetMethod getMethod = new GetMethod(url.toString());
                    info("export: " + url.toExternalForm());
                    int statusCode = httpClient.executeMethod(getMethod);
                    if (statusCode != HttpStatus.SC_OK) {
                        throw new BuildException("Unable to call debug suite properly. Return code was: " + statusCode);
                    }
                    File file = new File(_outputPath);
                    file = new File(file, exportSite + ".xml");
                    info("writing file: " + file.getAbsolutePath());
                    writeToFile(getMethod, file);
                } catch (IOException e) {
                    throw new BuildException(e);
                }
            }
        } else {
            info("skip exporting: " + exportSite);
        }
    }

    private void writeToFile(GetMethod getMethod, File file) {
        FileOutputStream out = null;
        InputStream source = null;
        try {
            source = getMethod.getResponseBodyAsStream();
            out = new FileOutputStream(file);
            if (_i18n) {
                SAXReader reader = new SAXReader();
                Document document = reader.read(source);
                String elementName = checkRootElement(document);
                internationalizeDocument(document, elementName);

                OutputFormat format = OutputFormat.createPrettyPrint();
                XMLWriter writer = new XMLWriter(out, format);
                writer.write(document);
            } else {
                byte[] buffer = new byte[10000];
                int count;
                while ((count = source.read(buffer, 0, buffer.length)) != -1) {
                    out.write(buffer, 0, count);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Exception while copying files: " + e.getMessage(), e);
        } catch (DocumentException e) {
            LOGGER.error("Can not parse xml.", e);
        } finally {
            closeQuietly(source);
            closeQuietly(out);
        }
    }

    private String checkRootElement(Document document) {
        Element root = document.getRootElement();
        String elementName = root.attribute("name").getValue();
        List list = document.selectNodes("/sv:node/sv:property[@sv:name]");
        boolean labelFound = false;
        boolean descriptionFound = false;
        for (Object obj : list) {
            Element element = (Element) obj;
            String value = element.attribute("name").getValue();
            if (value.equals(TYPE_LABEL) || value.equals(TYPE_TITLE)) {
                labelFound = true;
                if (getModus().name().toLowerCase().equals(Modus.DIALOG)) {
                    checkValue(element, TYPE_LABEL, elementName);
                } else {
                    checkValue(element, TYPE_TITLE, elementName);
                }
                break;
            } else if (value.equals(TYPE_DESCRIPTION)) {
                descriptionFound = true;
                checkValue(element, TYPE_DESCRIPTION, elementName);
            }
        }

        if (!labelFound) {
            if (getModus().equals(Modus.DIALOG)) {
                root.add(createI18nNode(TYPE_LABEL, elementName));
            } else {
                root.add(createI18nNode(TYPE_TITLE, elementName));
            }
        }
        if (!descriptionFound && !getModus().equals(Modus.DIALOG)) {
            root.add(createI18nNode(TYPE_DESCRIPTION, elementName));
        }
        return elementName;
    }

    /**
     * Adds a label or description node.
     * <code>
     * <sv:property sv:name="label" sv:type="String">
     *  <sv:value>bshkw.dialog.articleImage.label</sv:value>
     * </sv:property>
     * </code>
     */
    private Element createI18nNode(String type, String... keyParts) {
        Element element = new DefaultElement("sv:property");
        element.addAttribute("sv:name", type);
        element.addAttribute("sv:type", "String");
        Element valueElement = new DefaultElement("sv:value");
        String i18nKey = generateI18nKey(type, keyParts);
        valueElement.addText(i18nKey);
        element.add(valueElement);
        for (Map.Entry entry : _properties.entrySet()) {
            _propertiesChanged = true;
            ((Properties) entry.getValue()).setProperty(i18nKey, "");
        }
        return element;
    }

    private String generateI18nKey(String type, String... keyParts) {
        StringBuilder text = new StringBuilder(12);
        text.append(getModus().name().toLowerCase());
        for (String part : keyParts) {
            text.append(".").append(part);
        }
        text.append(".").append(type);
        return text.toString();
    }

    private void internationalizeDocument(Document document, String dialogName) {
        if (document != null) {
            List list = document.selectNodes("//sv:node[@sv:name]");
            for (Object aList : list) {
                Element parentNode = (Element) aList;
                String nodeName = parentNode.attribute("name").getValue();

                if (!parentNode.isRootElement() && !"MetaData".equals(nodeName)) {
                    List properties = parentNode.selectNodes("sv:property");
                    Map<String, Node> sortedProperties = new TreeMap<String, Node>();
                    checkNode(properties, sortedProperties, dialogName, nodeName);

                    List<Node> remainingNodes = new ArrayList<Node>();
                    for (Object o : parentNode.elements()) {
                        Node child = (Node) o;
                        remainingNodes.add(child);
                        child.detach();
                    }

                    // re-attach sorted properties first
                    for (Map.Entry<String, Node> entry : sortedProperties.entrySet()) {
                        parentNode.add(entry.getValue());
                    }
                    // re-attach remaining nodes in original order
                    for (Object remainingNode : remainingNodes) {
                        Node child = (Node) remainingNode;
                        parentNode.add(child);
                    }
                }
            }
        }
    }

    private void checkNode(List properties, Map<String, Node> sortedProperties, String... keyParts) {
        boolean labelFound = false;
        boolean descriptionFound = false;
        boolean isTab = false;

        for (Object obj : properties) {
            Element property = (Element) obj;
            // select parentNode name
            String name = property.attribute("name").getValue();
            if (name.equals(TYPE_LABEL) || name.equals(TYPE_TITLE)) {
                labelFound = true;
                if (getModus().name().toLowerCase().equals(Modus.DIALOG)) {
                    checkValue(property, TYPE_LABEL, keyParts);
                } else {
                    checkValue(property, TYPE_TITLE, keyParts);                            
                }
            } else if (name.equals(TYPE_DESCRIPTION)) {
                descriptionFound = true;
                checkValue(property, TYPE_DESCRIPTION, keyParts);
            } else if (name.equals("controlType")) {
                Node valueNode = property.selectSingleNode("sv:value");
                if (valueNode.getText().equals("tab")) {
                    isTab = true;
                }
            }
            // sort nodes after parentNode name
            sortedProperties.put(name, property);
            // detach original parentNode from parent
            property.detach();
        }

        if (!labelFound) {
            sortedProperties.put(TYPE_LABEL, createI18nNode(TYPE_LABEL, keyParts));

        }
        if (!isTab && !descriptionFound) {
            sortedProperties.put(TYPE_DESCRIPTION, createI18nNode(TYPE_DESCRIPTION, keyParts));
        }
    }

    private void checkValue(Element property, String type, String... keyParts) {
        Node valueNode = property.selectSingleNode("sv:value");
        String valueText = valueNode.getText();
        if (isNotAlreadyI18nKey(valueText)) {

            String i18nKey = generateI18nKey(type, keyParts);
            valueNode.setText(i18nKey);
            for (Map.Entry entry : _properties.entrySet()) {
                _propertiesChanged = true;
                ((Properties) entry.getValue()).setProperty(i18nKey, valueText);
            }
        }
    }

    private boolean isNotAlreadyI18nKey(String valueText) {
        return !valueText.startsWith(Modus.DIALOG.name().toLowerCase()) &&
                !valueText.startsWith(Modus.PARAGRAPH.name().toLowerCase()) &&
                !valueText.startsWith(Modus.TEMPLATE.name().toLowerCase());
    }

    /**
     * If this Ant task is executed with the <code>verbose</code> attribute set to true,
     * log output will be generated by this method.
     */
    public void info(String s) {
        if (isVerbose()) {
            try {
                log(s);
            } catch (NullPointerException e) {
                LOGGER.info(s);
            }
        }
    }

    // Main method for testing.
    /*public static void main(String[] args) {
        ExportTask exportTask = new ExportTask();
        exportTask.setI18n(true);
        exportTask.setTargetPort(8001);
        exportTask.setTargetHost("localhost");
        exportTask.setVerbose(true);
        exportTask.setWebapp("bsh-cafe-webapp");
        exportTask.setRootNode("config.modules.bsh-cafe.paragraphs");
        exportTask.setOutputPath("P:\\magkit_multi\\magkit/src/main/resources/mgnl-bootstrap");
        exportTask.setMessagePath("P:\\magkit_multi\\magkit/src/main/resources/info/magnolia/module/admininterface");
        exportTask.setMgnlUser("superuser");
        exportTask.setMgnlPassword("superuser");
        exportTask.execute(); 
    } */
}