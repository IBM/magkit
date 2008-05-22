package com.aperto.magkit.export;

import com.aperto.webkit.utils.StringTools;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This is an Ant task that exports given repository as a set of its top nodes.
 * Call the existing API by executing a http GET request and save the received file in the output path.
 *
 * @author Matthias Heidenreich 14.02.2007
 * @author Jan Haderka
 */
public class RepositoryExportTask extends MatchingTask {
    /**
     * @noinspection UNUSED_SYMBOL
     */
    private static final Logger LOGGER = Logger.getLogger(RepositoryExportTask.class);

    private String _repository;
    private String _outputPath;
    private String _mgnlUser;
    private String _mgnlPassword;
    private String _targetHost = "localhost";
    private int _targetPort = 8888;
    private boolean _verbose;
    private String _webapp = "author";

    public String getWebapp() {
        return _webapp.indexOf("/") == 0 ? _webapp : "/" + _webapp;
    }

    public void setWebapp(String webapp) {
        _webapp = webapp;
    }

    public String getRepository() {
        return _repository;
    }

    public void setRepository(String repository) {
        _repository = repository;
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
     * Called by ant to execute the task.
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {
        info("try to read the repository structure to export xml.");
        info("_repository: " + _repository);
        info("_outputPath: " + _outputPath);

        String[] elements;
        elements = getChildNodes(_repository);
        for (int i = 1; i < elements.length; i++) {
            String element = elements[i];
            info("check element: " + element);
            String path = "/.magnolia/pages/export.html?mgnlRepository=" + _repository + "&mgnlPath=/" + element + "&mgnlFormat=true&ext=.xml&command=exportxml&exportxml=Export";
            URL url;
            try {
                url = new URL("http", _targetHost, _targetPort, getWebapp() + path + getLogin());
                info("access: " + url.toExternalForm());
                HttpClient httpClient = new HttpClient();
                GetMethod getMethod = new GetMethod(url.toString());
                int statusCode = httpClient.executeMethod(getMethod);
                if (statusCode != HttpStatus.SC_OK) {
                    throw new BuildException("Failed to export site properly. Return code was: " + statusCode);
                }
                File file = new File(_outputPath);
                file = new File(file, _repository + "." + element + ".xml");
                info("writing file: " + file.getAbsolutePath());
                writeToFile(getMethod, file);
            } catch (MalformedURLException e) {
                throw new BuildException("Failed to export site properly. Exception message: " + e.getMessage());
            } catch (HttpException e) {
                throw new BuildException("Failed to export site properly. Exception message: " + e.getMessage());
            } catch (IOException e) {
                throw new BuildException("Failed to export site properly. Exception message: " + e.getMessage());
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
                throw new BuildException("Unable to call debug suite properly. Return code was: " + statusCode);
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
            StringBuffer path = new StringBuffer(getWebapp() + "/.magnolia/pages/export.html?mgnlRepository=" + _repository + "&mgnlPath=");
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
                    info("writing file: " + file.getPath());
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

            byte[] buffer = new byte[10000];
            int count;
            while ((count = source.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, count);
            }
        } catch (IOException e) {
            LOGGER.error("Exception while copying files: " + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(source);
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * Logs info message.
     * @param s Message to be logged.
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
}