package ca.jparrpearson.xslt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class XsltServletTest {

    @Test
    public void testExecute() throws ServletException, IOException {
        String source = getFileAsString("countries.xml");
        String stylesheet = getFileAsString("countries.xsl");
        String note = "Made by Jeremy";
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("note", new String[] { note });

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        String outputFormat = XsltServlet.execute(source, stylesheet, params,
                result);
        String resultStr = result.toString("UTF-8");

        Assert.assertEquals("html", outputFormat);
        Assert.assertTrue(resultStr.contains(note));
    }

    private String getFileAsString(String filename) throws IOException {
        // Assumes the test/resources directory is on the classpath
        return IOUtils.toString(XsltServletTest.class.getClassLoader()
                .getResourceAsStream(filename), "UTF-8");
    }

}
