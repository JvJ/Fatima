package FAtiMA.Core.util.parsers;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import FAtiMA.Core.util.VersionChecker;

public class CentralXMLParser extends DefaultHandler {
    
    String lastTag;
    ArrayList<ReflectXMLHandler> _parsers;

    public CentralXMLParser() {
        super();
        _parsers = new ArrayList<ReflectXMLHandler>();
    }
    
    public void addParser(ReflectXMLHandler p)
    {
    	_parsers.add(p);
    }


    
    public void characters(char[] ch, int start, int length) throws SAXException 
    {
    	for(ReflectXMLHandler parser: _parsers)
    	{
    		parser.callCharMethod(lastTag + "Characters", new String(ch).substring(start,start+length));
    	}      
        //System.out.println("start " + start + " lehngth " + length);
        //System.out.println("characters = '" + new String(ch).substring(start,start+length) + "')");
    }

    public void endDocument() {
//        System.out.println("endDocument");
    }
    
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException
    {
    	String args;
    	if (VersionChecker.runningOnAndroid())
    		args = localName + "End";
    	else
    		args = qName + "End";
    	for(ReflectXMLHandler parser: _parsers)
    	{
    		parser.callEndMethod(args);
    	}
    }

    /* Dealing with errors */
    public void error(SAXParseException e) throws SAXParseException {
    	throw getDetailedException(e);
    }

    public void fatalError(SAXParseException e) throws SAXParseException {
    	throw getDetailedException(e);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) {
//        System.out.println("start " + start + " length " + length);
//        System.out.println("whitespaces = '" + new String(ch).substring(start,start+length) + "')");
    }

    /* */
    public void notationDecl(java.lang.String name, java.lang.String publicId, java.lang.String systemId) {
    }

    public void processingInstruction(java.lang.String target, java.lang.String data) {
    }

    public InputSource resolveEntity(java.lang.String publicId, java.lang.String systemId)  {
//        System.out.println("public " + publicId + " system " + systemId);
        return null;
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() {
//        System.out.println("beginDocument");
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException 
    {
    	
    	String name;
    	if (VersionChecker.runningOnAndroid())
    	{
    		lastTag = localName;
    		name = localName;
    	}
    	else
    	{
    		lastTag = qName;
    		name = qName;
    	}
    		
    	for(ReflectXMLHandler parser: _parsers)
    	{
    		parser.callTagMethod(name,attributes);
    	}
    }

    public void unparsedEntityDecl(java.lang.String name, java.lang.String publicId, java.lang.String systemId, java.lang.String notationName) {
    }

    public void warning(SAXParseException e) {
    	System.out.println("Warning: " + getDetailedException(e).getMessage());
    }
    
    public SAXParseException getDetailedException(SAXParseException spe) {
        String systemId = spe.getSystemId();

        if (systemId == null) {
            systemId = "null";
        }

        String info = "URI=" + systemId + " Line=" 
            + spe.getLineNumber() + ": " + spe.getMessage();

        return new SAXParseException(info, spe.getPublicId(),spe.getSystemId(),spe.getLineNumber(),spe.getColumnNumber());
    }
}