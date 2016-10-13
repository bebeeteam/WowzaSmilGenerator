package guru.thewowza.example.dynamic;


import com.wowza.wms.application.WMSProperties;
import com.wowza.wms.media.model.MediaCodecInfoAudio;
import com.wowza.wms.media.model.MediaCodecInfoVideo;
import com.wowza.wms.medialist.MediaListRendition;

public class LiveCodecRetrieve implements ICodecInfoRetrieve
{

	private String streamName = null;
	private MediaCodecInfoAudio thisAudioCodec = null;
	private MediaCodecInfoVideo thisVideoCodec = null;
	private int audioBitrate = 0;
	private int videoBitrate = 0;
	private RenditionItem itemRendition = null;

	public LiveCodecRetrieve(String streamname,RenditionItem item)
	{
		this.streamName = streamname;
		this.itemRendition = item;
	}
	
	public boolean getCompleted()
	{
		return true;
	}
	
	public void open()
	{
	}
	
	public int getAudioBitrate()
	{
		return this.audioBitrate;
	}
	
	public int getVideoBitrate()
	{
		return this.videoBitrate;
	}
	
	public void setAudioBitrate(int audiobitrate)
	{
		this.audioBitrate = audiobitrate;
	}
	
	public void setVideoBitrate(int videobitrate) 
	{
		this.videoBitrate = videobitrate;
	}

	public MediaCodecInfoAudio getAudioCodec()
	{
		return this.thisAudioCodec;
	}
	
	public MediaCodecInfoVideo getVideoCodec()
	{
		return this.thisVideoCodec;
	}
	
	public void setAudioCodec(MediaCodecInfoAudio audioCodec)
	{
		this.thisAudioCodec = audioCodec;
	}
	public void setVideoCodec(MediaCodecInfoVideo videoCodec)
	{
		this.thisVideoCodec = videoCodec;
	}
	
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
	
}