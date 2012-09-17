package com.aperto.magkit.export;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.ArrayUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.StringUtils.split;

/**
 * This is an Ant task that exports every node beyond the root node into an extra xml file.
 *
 * @author Frank Sommer
 * @since 09.05.2012 reimplementation
 */
public class ExportTask extends MatchingTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportTask.class);
    private static final int DEFAULT_TARGET_PORT = 8001;

    private String _rootNode;
    private String _outputPath;
    private String _mgnlUser;
    private String _mgnlPassword;
    private String _targetHost = "localhost";
    private int _targetPort = DEFAULT_TARGET_PORT;
    private boolean _verbose;
    private String _webapp = "author";

    public String getWebapp() {
        return _webapp.indexOf('/') == 0 ? _webapp : "/" + _webapp;
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

    private String getLogin() {
        return "&mgnlUserId=" + _mgnlUser + "&mgnlUserPSWD=" + _mgnlPassword;
    }

    /**
     * Starts a recursive export. This method will be called by the Ant runner.
     */
    public void execute() {
        info("try to read the repository structure to export xml.");
        info("_rootNode: " + _rootNode);
        info("_outputPath: " + _outputPath);
        exportNodes();
    }

    /**
     * Request magnolia page to export configs.
     * E.g. http://localhost:8001/author/.magnolia/pages/developmentUtils.html?command=backupChildren&parentpath=/modules/wmam-webapp/templates&repository=config&rootdir=WEB-INF/bootstrap/common
     */
    private void exportNodes() {
        StringBuilder path = new StringBuilder(getWebapp());
        path.append("/.magnolia/pages/developmentUtils.html?command=backupChildren");
        String[] elements = split(getRootNode(), '.');
        if (ArrayUtils.isEmpty(elements)) {
            LOGGER.warn("Root node config was not set.");
        } else {
            path.append("&repository=").append(elements[0]);
            path.append("&parentpath=").append("/").append(join(ArrayUtils.remove(elements, 0), "/"));
            path.append("&rootdir=").append(getOutputPath());
            path.append(getLogin());
            try {
                URL url = new URL("http", _targetHost, _targetPort, path.toString());
                HttpClient httpClient = new HttpClient();
                GetMethod getMethod = new GetMethod(url.toString());
                info("export: " + url.toExternalForm());
                int statusCode = httpClient.executeMethod(getMethod);
                if (statusCode != HttpStatus.SC_OK) {
                    throw new BuildException("Unable to call debug suite properly. Return code was: " + statusCode);
                }
            } catch (IOException e) {
                LOGGER.error("Could not export nodes.", e);
            }
        }
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

    /**
     * Main method for testing.
     */
    /*public static void main(String[] args) {
        ExportTask exportTask = new ExportTask();
        exportTask.setTargetPort(8001);
        exportTask.setTargetHost("localhost");
        exportTask.setVerbose(true);
        exportTask.setWebapp("author");
        exportTask.setRootNode("config.modules.wmam-webapp.templates.pages");
        exportTask.setOutputPath("../../logs");
        exportTask.setMgnlUser("superuser");
        exportTask.setMgnlPassword("superuser");
        exportTask.execute(); 
    } */
}