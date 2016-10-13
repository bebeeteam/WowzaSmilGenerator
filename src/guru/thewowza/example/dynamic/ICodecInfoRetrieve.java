package guru.thewowza.example.dynamic;

import com.wowza.wms.media.model.MediaCodecInfoAudio;
import com.wowza.wms.media.model.MediaCodecInfoVideo;
import com.wowza.wms.medialist.MediaListRendition;

public interface ICodecInfoRetrieve
{
	public abstract void open();
	public abstract int getAudioBitrate();
	public abstract int getVideoBitrate();
	public abstract void setAudioBitrate(int audiobitrate);
	public abstract void setVideoBitrate(int videobitrate);	
	public abstract MediaCodecInfoAudio getAudioCodec();
	public abstract MediaCodecInfoVideo getVideoCodec();
	public abstract void setAudioCodec(MediaCodecInfoAudio audioCodec);
	public abstract void setVideoCodec(MediaCodecInfoVideo videoCodec);	
	public abstract void fillRendition(MediaListRendition rendition);
	public abstract boolean getCompleted();
}
