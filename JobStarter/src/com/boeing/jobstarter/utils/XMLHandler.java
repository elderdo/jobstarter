package com.boeing.jobstarter.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.boeing.jobstarter.model.Action;
import com.boeing.jobstarter.model.Parameter;

public class XMLHandler {

    public static final String SETTINGS_FILE = "settings/settings.xml";

    private static final String COMMANDS_FILE = "settings/commands.xml";

    public static Document getXMLDocument(String source) throws Exception {
        Document doc = null;
        File fXmlFile = new File(source);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();
        return doc;
    }

    public static Action getActionCommand(String command) throws Exception {
        return findAction(getAllNodesByTag(COMMANDS_FILE, "commands"), command, "", 0);
    }

    public static Parameter getActionParameter(String param) throws Exception {
        return findParameter(getAllNodesByTag(COMMANDS_FILE, "parameters"), param, "", 0);
    }

    public static Action findAction(NodeList nodes, String command, String path, int level) {
        Action action = null;
        int i = 0;
        while (i < nodes.getLength() && action == null) {
            Node currentNode = nodes.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                path = level == 1 ? "" : path;
                Element element = (Element) currentNode;
                path += element.getAttribute("id");
                if (path.equals(command)) {
                    action = new Action();
                    action.setId(element.getAttribute("id"));
                    action.setValue(element.getAttribute("value"));
                    action.setActionType(element.getAttribute("type"));
                    action.setName(element.getAttribute("name"));
                    action.setClassName(element.getAttribute("class"));
                    action.setJarFile(element.getAttribute("jar"));
                    if (currentNode.hasChildNodes()) {
                        action.setRequire(getRequiredParam(currentNode.getChildNodes()));
                    }
                } else if (currentNode.hasChildNodes()) {
                    action = findAction(currentNode.getChildNodes(), command, path, level + 1);
                }
                path = path.substring(0, path.length() - element.getAttribute("id").length());
            }
            i++;
        }
        return action;
    }

    public static Parameter findParameter(NodeList nodes, String param, String path, int level) {
        Parameter parameter = null;
        int i = 0;
        while (i < nodes.getLength() && parameter == null) {
            Node currentNode = nodes.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                path = level == 1 ? "" : path;
                Element element = (Element) currentNode;
                path += element.getAttribute("id");
                if (path.equals(param)) {
                    parameter = new Parameter();
                    parameter.setId(element.getAttribute("id"));
                    parameter.setValue(element.getAttribute("value"));
                    parameter.setName(element.getAttribute("name"));
                    parameter.setRequire(element.getAttribute("require"));
                } else if (currentNode.hasChildNodes()) {
                    parameter = findParameter(currentNode.getChildNodes(), param, path, level + 1);
                }
                path = path.substring(0, path.length() - element.getAttribute("id").length());
            }
            i++;
        }
        return parameter;
    }

    public static List<String> getRequiredParam(NodeList childs) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < childs.getLength(); i++) {
            Node node = childs.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                list.add(((Element) node).getTextContent());
            }
        }
        return list;
    }

    /**
     * @author Walter de Sola
     * @param connName
     * @return Connection string for specified name
     * @throws Exception
     */
    public static Element getConnectionString(String connName) throws Exception {

        NodeList nList = getAllNodesByTag(SETTINGS_FILE, "connection");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                if (eElement.getAttribute("id").equalsIgnoreCase(connName)) {
                    return eElement;
                }
            }
        }
        return null;
    }

    public static int countConnectionString() throws Exception {

        Document doc = getXMLDocument(SETTINGS_FILE);
        return doc.getElementsByTagName("connection").getLength();

    }

    /**
     * @author Walter de Sola
     * @param none
     * @return Default connection string
     * @throws Exception
     */
    public static Element getConnectionString() throws Exception {

        NodeList nList = getAllNodesByTag(SETTINGS_FILE, "connection");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                if (eElement.getAttribute("default").equalsIgnoreCase(Globals.Answer.Y.toString())) {
                    return eElement;
                }
            }
        }
        return null;
    }

    public static NodeList getAllNodesByTag(String xmlSource, String tagName) throws Exception {

        Document doc = getXMLDocument(xmlSource);
        NodeList nList = doc.getElementsByTagName(tagName);
        return nList;
    }

    public static void DeleteConnectionString(String connName) throws Exception {

        NodeList nList = getAllNodesByTag(SETTINGS_FILE, "connection");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                if (eElement.getAttribute("id").equalsIgnoreCase(connName)) {
                    Node parent = eElement.getParentNode();
                    parent.removeChild(eElement);
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(eElement.getOwnerDocument());
                    StreamResult result = new StreamResult(new File(SETTINGS_FILE));
                    transformer.transform(source, result);
                }
            }
        }
    }

    public static int getConnectionTimeOut() throws Exception {
        String value = getTagContent("connection-timeout");
        return Integer.parseInt(value == null ? "60000" : value);
    }

    public static int getBulkSize() throws Exception {
        String value = getTagContent(Globals.TagNames.BULK_SIZE_TAG);
        return Integer.parseInt(value == null ? "50000" : value);
    }

    public static void addNodeToParent(String parentNodeName, Node newNode) throws Exception {
        Document doc = newNode.getOwnerDocument();
        Node node = doc.getElementsByTagName(parentNodeName).item(0);
        node.appendChild(newNode);
        doc.normalize();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(SETTINGS_FILE));
        transformer.transform(source, result);
    }

    public static void clearDefault() throws Exception {
        Element e = getConnectionString();
        if (e != null) {
            Document doc = e.getOwnerDocument();
            e.setAttribute("default", Globals.Answer.N.toString());
            doc.normalize();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(SETTINGS_FILE));
            transformer.transform(source, result);
        }
    }

    public static String getDirectories(String name) throws Exception {
        Document doc = getXMLDocument(SETTINGS_FILE);
        String path = null;
        Element element = (Element) doc.getElementsByTagName(name).item(0);
        if (element != null) {
            NodeList dirList = doc.getElementsByTagName("path");
            for (int temp = 0; temp < dirList.getLength(); temp++) {
                Node nNode = dirList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    if (eElement.getAttribute("id").equalsIgnoreCase(element.getTextContent())) {
                        path = eElement.getAttribute("value");
                        Pattern p = Pattern.compile("\\[(.*?)\\]");
                        Matcher m = p.matcher(eElement.getAttribute("value"));
                        while (m.find()) {
                            path = eElement.getAttribute("value").replace("[" + m.group(1) + "]",
                                    Globals.getVariableValue(m.group(1)));
                        }

                    }
                }
            }
        }
        return path.replace("|", FileUtils.fileSeparator());
    }

    public static String getTagContent(String tagName) throws Exception {

        Document doc = getXMLDocument(SETTINGS_FILE);

        Element element = (Element) doc.getElementsByTagName(tagName).item(0);
        if (element != null) {
            return element.getTextContent();
        }
        return null;
    }

    public static String getMailingList() throws Exception {
        NodeList nList = getAllNodesByTag(SETTINGS_FILE, "address");
        StringBuilder mails = new StringBuilder();
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                mails.append(eElement.getTextContent()).append(",");
            }
        }
        return mails.deleteCharAt(mails.length() - 1).toString();
    }
}
