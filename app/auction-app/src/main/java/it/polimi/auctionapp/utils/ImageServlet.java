package it.polimi.auctionapp.utils;

import it.polimi.auctionapp.beans.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Properties;

@WebServlet("/image/*")
public class ImageServlet extends ThymeleafHTTPServlet {

    private static final String BASE_DIR = getBaseDir();

    public static String getBaseDir() {
        Properties props = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(
                SQLConnectionHandler.class.getResource("config.properties").getFile()
            );
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException(
                "Remember to create a config.properties file in the src/main/resources folder to set the destination folder for your user uploads"
            );
        }
        return props.getProperty("user.uploads.path");
    }

    public static String getImageFilename(String userSubmittedFileName, String username) {
        return (
            LocalDateTime.now().toString().toLowerCase() +
            "_" +
            username +
            userSubmittedFileName.substring(userSubmittedFileName.lastIndexOf("."))
        );
    }

    public static String getImagePath(String filename) {
        return getBaseDir() + filename;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null || path.contains("..")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            return;
        }

        File file = new File(getBaseDir() + path);
        if (!file.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mimeType = req.getServletContext().getMimeType(file.getName());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        resp.setContentType(mimeType);
        resp.setContentLengthLong(file.length());

        try (
            InputStream in = new FileInputStream(file);
            OutputStream out = resp.getOutputStream()
        ) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        }
    }

    public static String saveImage(HttpServletRequest request, String HTMLPartName)
        throws ServletException, IOException {
        Part filePart = request.getPart(HTMLPartName);
        String filename = ImageServlet.getImageFilename(
            filePart.getSubmittedFileName(),
            ((User) request.getSession().getAttribute("user")).getUsername()
        );

        for (Part part : request.getParts()) {
            part.write(ImageServlet.getImagePath(filename));
        }
        return filename;
    }
}
