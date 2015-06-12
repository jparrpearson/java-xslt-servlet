package ca.jparrpearson.xslt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.TransformerFactoryImpl;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

@WebServlet(name = "XsltServlet", urlPatterns = "/execute")
@MultipartConfig
public class XsltServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String SOURCE_PARAM = "source";
    public static final String STYLESHEET_PARAM = "stylesheet";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        // Get the parameters
        String source = req.getParameter(SOURCE_PARAM);
        String stylesheet = req.getParameter(STYLESHEET_PARAM);
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.putAll(req.getParameterMap());

        // Handle multipart requests (files)
        if (ServletFileUpload.isMultipartContent(req)) {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletContext servletContext = this.getServletConfig()
                    .getServletContext();
            File repository = (File) servletContext
                    .getAttribute("javax.servlet.context.tempdir");
            factory.setRepository(repository);
            ServletFileUpload upload = new ServletFileUpload(factory);

            try {
                List<FileItem> items = upload.parseRequest(req);
                for (FileItem item : items) {
                    String fieldName = item.getFieldName();
                    if (!item.isFormField()) {
                        // Handle files
                        String file = IOUtils.toString(item.getInputStream(),
                                "UTF-8");
                        if (fieldName.equalsIgnoreCase(SOURCE_PARAM)) {
                            source = file;
                        } else if (fieldName.equalsIgnoreCase(STYLESHEET_PARAM)) {
                            stylesheet = file;
                        }
                    } else {
                        // Handle all other form data
                        String value = item.getString();
                        if (fieldName.equalsIgnoreCase(SOURCE_PARAM)) {
                            source = value;
                        } else if (fieldName.equalsIgnoreCase(STYLESHEET_PARAM)) {
                            stylesheet = value;
                        } else {
                            params.put(fieldName, new String[] { value });
                        }
                    }
                }
            } catch (FileUploadException e) {
                throw new ServletException(
                        "Unable to parse the multipart request", e);
            }
        }

        if (stylesheet == null || stylesheet.trim().equals("")) {
            throw new ServletException("Must provide at least a stylesheet");
        }

        // Perform the transformation
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        String outputFormat = XsltServlet.execute(source, stylesheet, params,
                result);

        // Return the result, with appropriate headers
        String resultStr = result.toString("UTF-8");
        String contentType = "application/xml";
        String ext = ".xml";
        if (outputFormat != null && !outputFormat.equals("")) {
            if (outputFormat.equalsIgnoreCase("xml")) {
                contentType = "application/xml";
                ext = ".xml";
            } else if (outputFormat.equalsIgnoreCase("html")) {
                contentType = "text/html";
                ext = ".html";
            } else if (outputFormat.equalsIgnoreCase("text")) {
                contentType = "text/plain";
                ext = ".txt";
            }
        } else if (resultStr.contains("<html") && resultStr.contains("</html>")) {
            contentType = "text/html";
            ext = ".html";
        }
        res.setContentType(contentType + ";charset=UTF-8");
        res.setContentLength(resultStr.length());
        res.setHeader("Content-Disposition", "inline; filename=result" + ext);
        PrintWriter out = res.getWriter();
        out.println(resultStr);
        out.flush();
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        doGet(req, res);
    }

    /**
     * Runs the given source document against the stylesheet document, along
     * with any given parameters. Returns the output format, if it was set. The
     * XSLT result is stored in the result output stream.
     * 
     * @param source
     *            The source document string.
     * @param stylesheet
     *            The stylesheet document string.
     * @param params
     *            The map of parameters, name and values (only the first value
     *            is used).
     * @param result
     *            The result output stream to capture the result in.
     * @return The output format, if set (xsl:output/@method).
     * @throws ServletException
     */
    public static String execute(String source, String stylesheet,
            Map<String, String[]> params, OutputStream result)
            throws ServletException {
        try {
            TransformerFactory tf = new TransformerFactoryImpl();
            Source style = new StreamSource(new ByteArrayInputStream(
                    stylesheet.getBytes("UTF-8")));
            Transformer transformer = tf.newTransformer(style);
            // If no source given, use the stylesheet itself
            Source src = null;
            if (source != null && !source.trim().equals("")) {
                src = new StreamSource(new ByteArrayInputStream(
                        source.getBytes("UTF-8")));
            } else {
                src = new StreamSource(new ByteArrayInputStream(
                        stylesheet.getBytes("UTF-8")));
            }
            // Set all provided parameters, other than source and stylesheet
            for (String name : params.keySet()) {
                if (!name.equalsIgnoreCase(SOURCE_PARAM)
                        && !name.equalsIgnoreCase(STYLESHEET_PARAM)) {
                    String[] values = params.get(name);
                    if (values.length > 0) {
                        transformer.setParameter(name, values[0]);
                    }
                }
            }
            transformer.transform(src, new StreamResult(result));
            return transformer.getOutputProperty("method");
        } catch (TransformerConfigurationException
                | UnsupportedEncodingException e) {
            throw new ServletException("Unable to configure XSLT transformer",
                    e);
        } catch (TransformerException e) {
            throw new ServletException(
                    "Unable to execute the XSLT transformer", e);
        }
    }

}