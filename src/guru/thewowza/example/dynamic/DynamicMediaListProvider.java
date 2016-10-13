package guru.thewowza.example.dynamic;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.media.model.MediaCodecInfoAudio;
import com.wowza.wms.media.model.MediaCodecInfoVideo;
import com.wowza.wms.medialist.MediaList;
import com.wowza.wms.medialist.MediaListRendition;
import com.wowza.wms.medialist.MediaListSegment;
import com.wowza.wms.stream.IMediaListProvider;
import com.wowza.wms.stream.IMediaListReader;
import com.wowza.wms.stream.IMediaStream;

public class DynamicMediaListProvider implements IMediaListProvider
{

	private IApplicationInstance appIns = null;	
	private HashMap<String,MediaCodecInfoAudio> mediaAudioMap = null;
	private HashMap<String,MediaCodecInfoVideo> mediaVideoMap = null;


	public DynamicMediaListProvider(IApplicationInstance appins,HashMap<String,MediaCodecInfoAudio> mediaaudiomap,HashMap<String,MediaCodecInfoVideo> mediavideomap)
	{
		this.appIns = appins;
		this.mediaAudioMap = mediaaudiomap;
		this.mediaVideoMap = mediavideomap;
	}
	
	@Override
	public MediaList resolveMediaList(IMediaListReader medialistreader, IMediaStream stream, String streamName)
	{	
		String genericTemplate = this.appIns.getStreamStorageDir()+"/"+streamName+"_template.xml";
		File templateFile = null;
		
		templateFile = new File(genericTemplate);
		if ( !templateFile.exists() )
		{
			genericTemplate = this.appIns.getStreamStorageDir()+"/amlst_template.xml";
			templateFile = new File(genericTemplate);
			if ( !templateFile.exists() )
			{
				WMSLoggerFactory.getLogger(null).info("Templates not found, tried "+streamName+"_template.xml and amlst_template.xml");
				return null;
			}
		}
		
		List<RenditionItem> renditionList = new ArrayList<RenditionItem>();
 
		try
		{
			SAXParserFactory parserFactor = SAXParserFactory.newInstance();
			SAXParser parser = parserFactor.newSAXParser();
			TemplateParser renditionParser = new TemplateParser(renditionList);
			parser.parse(new InputSource(new ByteArrayInputStream(readFile(genericTemplate).getBytes("utf-8"))), renditionParser);
		}
		catch ( Exception noParser )
		{
			WMSLoggerFactory.getLogger(null).info("Failed to parse error is '"+noParser.toString()+"'");
			renditionList.clear();
		}
		MediaList mediaList = new MediaList();
		MediaListSegment segment = new MediaListSegment();
		mediaList.addSegment(segment);
		
		Iterator<RenditionItem> renditionListI = renditionList.iterator();
		while (renditionListI.hasNext())
		{
			RenditionItem thisRenditionItem = renditionListI.next();			
			String thisRendition = thisRenditionItem.getName().replace("%s", streamName);			
			ICodecInfoRetrieve thisRetrieve = null;
						
			if ( !this.appIns.getStreamType().equalsIgnoreCase("default") && !this.appIns.getStreamType().equalsIgnoreCase("file") )
			{
				if ( this.mediaAudioMap.containsKey(thisRendition) || this.mediaAudioMap.containsKey(thisRendition) &&
						this.appIns.getStreams().getPublishStreamNames().contains(thisRendition) )
				{
					MediaListRendition rendition = new MediaListRendition();	
					thisRetrieve = new LiveCodecRetrieve(thisRendition,thisRenditionItem);
					thisRetrieve.setAudioCodec(this.mediaAudioMap.get(thisRendition));
					thisRetrieve.setVideoCodec(this.mediaVideoMap.get(thisRendition));
					thisRetrieve.setAudioBitrate(this.appIns.getStreams().getStream(thisRendition).getPublishBitrateAudio());
					thisRetrieve.setVideoBitrate(this.appIns.getStreams().getStream(thisRendition).getPublishBitrateVideo());
					thisRetrieve.fillRendition(rendition);
					segment.addRendition(rendition);
				}
				
			}
			else
			{
				thisRetrieve = new FileCodecRetrieve(this.appIns,stream,thisRendition,thisRenditionItem);
				thisRetrieve.open();
				if ( thisRetrieve.getCompleted() == true )
				{
					MediaListRendition rendition = new MediaListRendition();	
					thisRetrieve.fillRendition(rendition);
					segment.addRendition(rendition);
				}
			}
		}
		
	return mediaList;
	}

	
	String readFile(String fileName) 
	{
	BufferedReader br = null;
	try
		{
		br = new BufferedReader(new FileReader(fileName));
			try 
			{
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();

				while (line != null) 
				{
					sb.append(line);
					sb.append("\n");
					line = br.readLine();
				}
			br.close();
			
			return sb.toString();
			} 
			catch (Exception broken)
			{
	        br.close();
			}
		}
		catch (Exception noFile)
		{
			if ( br != null )
			{
				try
				{
				br.close();
				}
				catch (Exception noBr)
				{
					
				}
			}
		}
	return null;
	}
}
