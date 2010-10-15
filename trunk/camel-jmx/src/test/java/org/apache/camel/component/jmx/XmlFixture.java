package org.apache.camel.component.jmx;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class XmlFixture {
	
	public static Document toDoc(String aXmlString) throws Exception {
		return XMLUnit.buildControlDocument(aXmlString);
	}
	
	public static Document toDoc(File aFile) throws Exception {
		return XMLUnit.buildControlDocument(new InputSource(aFile.toString()));
	}
	
    public static void assertXMLIgnorePrefix(String aMessage, Document aExpected, Document aActual) throws Exception {
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        
        Diff diff = new Diff(aExpected, aActual);
        diff.overrideDifferenceListener(new DifferenceListener() {
			
			@Override
			public void skippedComparison(Node aArg0, Node aArg1) {
			}
			
			@Override
			public int differenceFound(Difference aDifference) {
		        if (aDifference.getId() == DifferenceConstants.NAMESPACE_PREFIX_ID)
		            return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
		        return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
			}
		});
        try {
            XMLAssert.assertXMLEqual(diff, true);
        } catch (Throwable t) {
            XMLUnit.getTransformerFactory().newTransformer().transform(new DOMSource(aActual), new StreamResult(System.out));
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            fail(sw.toString());
        }
    }
    
    public static Document stripTimestamp(Document aDocument) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        InputStream in = XmlFixture.class.getResourceAsStream("/stripTimestamp.xsl");
        Source src = new StreamSource(in);
        Transformer t = tf.newTransformer(src);
        DOMResult result = new DOMResult();
        t.transform(new DOMSource(aDocument), result);
        return (Document) result.getNode();
    }
}
