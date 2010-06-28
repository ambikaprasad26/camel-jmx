package com.massfords.camel.jmx;

import java.io.File;

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
        XMLAssert.assertXMLEqual(diff, true);
    }

}
