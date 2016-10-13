package guru.thewowza.example.dynamic;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.wowza.wms.amf.AMFData;
import com.wowza.wms.amf.AMFDataArray;
import com.wowza.wms.amf.AMFDataList;
import com.wowza.wms.amf.AMFDataMixedArray;
import com.wowza.wms.amf.AMFDataObj;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.application.WMSProperties;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.media.model.MediaCodecInfoAudio;
import com.wowza.wms.media.model.MediaCodecInfoVideo;
import com.wowza.wms.medialist.MediaListRendition;
import com.wowza.wms.stream.IMediaReader;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.MediaReaderFactory;
import com.wowza.wms.vhost.IVHost;

public class FileCodecRetrieve implements ICodecInfoRetrieve
{
	private IMediaReader mediaReader = null;
	private IApplicationInstance appIns = null;
	private IMediaStream stream = null;
	private String streamName = null;
	private MediaCodecInfoAudio thisAudioCodec = null;
	private MediaCodecInfoVideo thisVideoCodec = null;
	private int audioBitrate = 0;
	private int videoBitrate = 0;
	private HashMap<String,String> thisMap = new HashMap<String,String>();
	private RenditionItem itemRendition = null;
	private boolean completed = false;
	
	
	public FileCodecRetrieve(IApplicationInstance appins,IMediaStream stream, String streamname,RenditionItem item)
	{
		this.appIns = appins;
		this.stream = stream;
		this.streamName = streamname;
		this.thisVideoCodec = new MediaCodecInfoVideo();
		this.thisAudioCodec = new MediaCodecInfoAudio();
		this.itemRendition = item;

	}

	public boolean getCompleted()
	{
		return this.completed;
	}
	
	public void open()
	{
		try
		{
			String mediaReaderType = "mp4";
			WMSProperties myProps = new WMSProperties();
			myProps.setProperty("mp4CalculateDatarate", true);
			IMediaReader ret = MediaReaderFactory.getInstance(this.appIns, this.appIns.getVHost().getMediaReaders(), mediaReaderType);
			ret.setProperties(myProps);
			if (ret != null)
			{
		    	String basePath = this.appIns.getStreamStoragePath();
				ret.init(this.appIns, this.stream, mediaReaderType, basePath, streamName);
				ret.open(basePath, streamName);
				this.mediaReader = ret;
				
			}
		}
		catch (Exception e)
		{
			WMSLoggerFactory.getLogger(null).info("Broke it "+e.toString());
		}
			
		
		if ( this.mediaReader != null )
		{
			completed = true;
			List<?> metaData = this.mediaReader.getMetadata();
			AMFDataList dataList = new AMFDataList();			
			dataList.deserialize((ByteBuffer)metaData.get(0));
			AMFDataMixedArray amfArray = (AMFDataMixedArray)dataList.get(1);
			@SuppressWarnings("unchecked")
			List<String> names = amfArray.getKeys();
			Iterator<String> iter = names.iterator();
			while(iter.hasNext())
			{
				String name = iter.next();
				AMFData value = amfArray.get(name);
				int type = value.getType();
				if ( type == AMFData.DATA_TYPE_NUMBER || type == AMFData.DATA_TYPE_STRING )
				{
					this.thisMap.put(name,value.toString());
				}
				
				if ( type == AMFData.DATA_TYPE_AMF3_OBJECT )
				{
					AMFDataArray amfDataArray = (AMFDataArray)value;
					for ( int count = 0 ; count<amfDataArray.size(); count ++ )
					{
						if ( amfDataArray.get(count).getType() == AMFData.DATA_TYPE_OBJECT )
							{
							AMFDataObj amfObj = (AMFDataObj)amfDataArray.get(count);
							@SuppressWarnings("unchecked")
							List<String> namest = amfObj.getKeys();
							Iterator<String> itert = namest.iterator();
							while(itert.hasNext())
							{
								String namet = itert.next();
								AMFData valuet = amfObj.get(namet);
								this.thisMap.put(namet, valuet.toString());
							}

						}
					}
				}
			}

		mapVideoCodec(this.thisMap,this.thisVideoCodec);
		mapAudioCodec(this.thisMap,this.thisAudioCodec);			
		this.mediaReader.close();
		}
		
	}
	
	public int getAudioBitrate()
	{
		return this.audioBitrate;
	}
	
	public int getVideoBitrate()
	{
		return this.videoBitrate;
	}
	
	public void setAudioBitrate(int audiobitrate) {}
	public void setVideoBitrate(int videobitrate) {}

	public MediaCodecInfoAudio getAudioCodec()
	{
		return this.thisAudioCodec;
	}
	
	public MediaCodecInfoVideo getVideoCodec()
	{
		return this.thisVideoCodec;
	}
	
	public void setAudioCodec(MediaCodecInfoAudio audioCodec) {}
	public void setVideoCodec(MediaCodecInfoVideo videoCodec) {}
	
	public void fillRendition(MediaListRendition rendition)
	{
		rendition.setName(this.streamName);
		rendition.setBitrateAudio(getAudioBitrate());
		rendition.setBitrateVideo(getVideoBitrate());
		rendition.setWidth(getVideoCodec().getFrameWidth());
		rendition.setHeight(getVideoCodec().getFrameHeight());
		rendition.setAudioCodecId(getAudioCodec().toCodecsStr());
		rendition.setVideoCodecId(getVideoCodec().toCodecsStr());
		
		if ( this.itemRendition.getLanguage().length()> 0)
			rendition.setLanguage(this.itemRendition.getLanguage());
		
		if ( this.itemRendition.getType().equalsIgnoreCase("audioonly") )
		{
			WMSProperties renditionProps = rendition.getProperties(true);
			renditionProps.setProperty("audioOnly", "true");
			renditionProps.setProperty("cupertinoTag", "EXT-X-MEDIA");
			String group = "default";
			if ( this.itemRendition.getGroup().length()> 0 )
				group = this.itemRendition.getGroup();
			
			renditionProps.setProperty("cupertinoTag.GROUP-ID", group);
			renditionProps.setProperty("cupertinoTag.name", this.streamName);
		}
	}
	
	public void mapVideoCodec(HashMap<String,String> meta, MediaCodecInfoVideo codecHolder )
	{
		// (int)Double.parseDouble(decimalstring);
		
		if ( meta.containsKey("frameHeight") )
			{ codecHolder.setVideoHeight((int)Double.parseDouble(meta.get("frameHeight"))); }

		if ( meta.containsKey("frameWidth") )
			{ codecHolder.setVideoWidth((int)Double.parseDouble(meta.get("frameWidth"))); }
		
		if ( meta.containsKey("displayHeight") )
			{ codecHolder.setDisplayHeight((int)Double.parseDouble(meta.get("displayHeight"))); }

		if ( meta.containsKey("displayWidth") )
			{ codecHolder.setDisplayHeight((int)Double.parseDouble(meta.get("displayWidth"))); }

		if ( meta.containsKey("displayWidth") )
			{ codecHolder.setFrameRate((int)Double.parseDouble(meta.get("videoframerate"))); }
		
		if ( meta.containsKey("avclevel") )
			{ codecHolder.setVideoLevel((int)Double.parseDouble(meta.get("avclevel"))); }
		
		
		if ( meta.containsKey("videocodecid") )
			{
				String codecID = meta.get("videocodecid");
				codecHolder.setCodecId(IVHost.CODEC_VIDEO_H264);
				if (codecID.contains("hvc1") || (codecID.contains("hev1")))
				{
					codecHolder.setCodecId(IVHost.CODEC_VIDEO_H265);
				}
			}
		
		if ( meta.containsKey("videodatarate") )
		{ 
			int videodatarate = (int)Double.parseDouble(meta.get("videodatarate"));
			videodatarate = videodatarate * 1000;
			this.videoBitrate = videodatarate;
		}

	}
	
	public void mapAudioCodec(HashMap<String,String> meta, MediaCodecInfoAudio codecHolder )
	{
		if ( meta.containsKey("audiochannels") )
			{ codecHolder.setAudioChannels((int)Double.parseDouble(meta.get("audiochannels"))); }

		if ( meta.containsKey("audiocodecid") )
			{ 
				String codecID = meta.get("audiocodecid");
				codecHolder.setCodecId(IVHost.CODEC_AUDIO_AAC);
				if ( codecID.contains(".mp3") || codecID.contains("mp4a") )
				{
					codecHolder.setCodecId(IVHost.CODEC_AUDIO_MP3);
				}
			}
		
		if ( meta.containsKey("audiosamplerate") )
			{ codecHolder.setAudioSampleFrequency((int)Double.parseDouble(meta.get("audiosamplerate"))); }
		
		if ( meta.containsKey("aacaot") )
			{ codecHolder.setAudioObjectType((int)Double.parseDouble(meta.get("aacaot"))); }
		
		if ( meta.containsKey("audiodatarate") )
		{ 
			int audiodatarate = (int)Double.parseDouble(meta.get("audiodatarate"));
			audiodatarate = audiodatarate * 1000;
			this.audioBitrate = audiodatarate;
		}


	}
	
}
