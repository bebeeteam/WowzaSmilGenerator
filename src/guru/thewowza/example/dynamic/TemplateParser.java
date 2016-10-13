package guru.thewowza.example.dynamic;

import java.util.List;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class TemplateParser extends DefaultHandler
{

	private List<RenditionItem> renditionList = null;
	private boolean listStart = false;

	public TemplateParser (List<RenditionItem> renditionlist)
	{
		this.renditionList = renditionlist;
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes)
	{
		if (qName.equalsIgnoreCase("RenditionList"))
		{
			this.listStart = true;
		}
		
		if ( qName.equalsIgnoreCase("Rendition") && this.listStart == true )
		{
			RenditionItem thisRendition = new RenditionItem();
			if ( attributes.getValue("name") != null )
				thisRendition.setName(attributes.getValue("name"));

			if ( attributes.getValue("type") != null )
				thisRendition.setType(attributes.getValue("type"));
			
			if ( attributes.getValue("language") != null )
				thisRendition.setLanguage(attributes.getValue("language"));
			
			if ( attributes.getValue("groupname") != null )
				thisRendition.setGroup(attributes.getValue("groupname"));

			this.renditionList.add(thisRendition);
		}

	}
	
	public void endElement(String uri, String localName, String qName)
	{
	}
	
}
