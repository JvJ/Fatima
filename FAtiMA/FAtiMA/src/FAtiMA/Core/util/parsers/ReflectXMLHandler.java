/** 
 * ReflectXMLHandler.java - Implements a XML Handler with class reflection
 * 
 * Company: GAIPS/INESC-ID
 * Project: FAtiMA
 * Created: 17/01/2004 
 * @author: unspecified
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 17/01/2004 - File created
 */
package FAtiMA.Core.util.parsers;

import java.lang.reflect.Method;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import FAtiMA.Core.util.VersionChecker;

public abstract class ReflectXMLHandler extends DefaultHandler {
    // used for reflect
    Class<?>[] argTypes = {Attributes.class};
    Class<?>[] charArgTypes = {String.class};
    // used for reflect
    Class<? extends ReflectXMLHandler> cl;
    String lastTag;

    public ReflectXMLHandler() {
        super();
        cl = this.getClass();
    }

    public void callCharMethod(String methodName, String str) throws SAXException
    {
    	Method meth = null;
    
    	try
    	{
    		// Fetches the method
    	    meth = cl.getMethod(methodName, charArgTypes);
    	    Object args[] = {str};
    	    // invokes the method
    	    meth.invoke(this,args);
    	}
    	catch(java.lang.NoSuchMethodException e)
    	{
    	}
    	catch(Exception e)
    	{
    		throw new SAXException(e);
    	}    
    }

    public void callEndMethod(String methodName) throws SAXException 
    {
    	try
    	{
			Method meth = null;
			// Fetches the method
			meth = cl.getMethod(methodName,(Class<?>[])null);
			meth.invoke(this,(Object[])null);
    	}
    	catch(java.lang.NoSuchMethodException e)
    	{
    	}
    	catch(Exception e)
    	{
    		throw new SAXException(e);
    	}    
    }

    public void callTagMethod(String methodName, Attributes attributes) throws SAXException
    {
    	try{
	    	Method meth = null;
	     
	        // Fetches the method
	        meth = cl.getMethod(methodName,argTypes);
	        Object args[] = {attributes};
	        meth.invoke(this,args);
    	}
    	catch(java.lang.NoSuchMethodException e)
    	{
    	}
    	catch(Exception e)
    	{
    		throw new SAXException(e);
    	}    
    }
    
    public void characters(char[] ch, int start, int length) throws SAXException 
    {
        callCharMethod(lastTag + "Characters", new String(ch).substring(start,start+length));
        //System.out.println("start " + start + " lehngth " + length);
        //System.out.println("characters = '" + new String(ch).substring(start,start+length) + "')");
    }

    public void endDocument() {
//        System.out.println("endDocument");
    }
    
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException
    {
    	if (VersionChecker.runningOnAndroid())
    		callEndMethod(localName + "End");
    	else
    		callEndMethod(qName + "End");
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
    	if (VersionChecker.runningOnAndroid())
    	{
    		callTagMethod(localName, attributes);
    		lastTag = localName;
    	}
    	else
    	{
    		callTagMethod(qName, attributes);
    		lastTag = qName;    		
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