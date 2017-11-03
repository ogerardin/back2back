package org.ogerardin.b2b.jnlp;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;

public class SelectPath {

    private static final String USER_AGENT = "Mozilla/5.0";

    public static void main(String args[]) throws UnavailableServiceException, IOException {
        String sourceId = args[0];

        // let the user select a directory
        File selectedDir = chooseDirectory();
        if (selectedDir == null) { // user cancelled
            return;
        }

        // post the selected directory to the server
        BasicService basicService = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
        postPathSelection(basicService.getCodeBase(), sourceId, selectedDir);
    }

    private static File chooseDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(null, "Select dir");

        return fileChooser.getSelectedFile();
    }

    private static void postPathSelection(URL codeBase, String sourceId, File selectedFile) throws IOException {
        URL url = new URL(codeBase, MessageFormat.format("/api/sources/{0}/path", sourceId));

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("User-Agent", USER_AGENT);

        String requestBody = selectedFile.getCanonicalPath();

        // Send post request
        urlConnection.setDoOutput(true);
        try (OutputStream outputStream = urlConnection.getOutputStream()) {
            outputStream.write(requestBody.getBytes());
        }
        int responseCode = urlConnection.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("POST to " + url.toString() +
                    " failed, HTTP response code: " + responseCode);
        }
    }
}