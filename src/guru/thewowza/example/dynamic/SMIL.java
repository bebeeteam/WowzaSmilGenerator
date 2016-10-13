package guru.thewowza.example.dynamic;

import java.util.HashMap;

import com.wowza.wms.amf.AMFPacket;
import com.wowza.wms.application.*;
import com.wowza.wms.media.model.MediaCodecInfoAudio;
import com.wowza.wms.media.model.MediaCodecInfoVideo;
import com.wowza.wms.module.*;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.IMediaStreamActionNotify3;

public class SMIL extends ModuleBase
{
  
	HashMap<String,MediaCodecInfoAudio> mediaAudioMap = new HashMap<String,MediaCodecInfoAudio>();
	HashMap<String,MediaCodecInfoVideo> mediaVideoMap = new HashMap<String,MediaCodecInfoVideo>();

	public void onAppStart(IApplicationInstance appInstance)
	{
		String fullname = appInstance.getApplication().getName() + "/" + appInstance.getName();
		getLogger().info("onAppStart: " + fullname);
		getLogger().info("Starting dynamic amlst provider");
		appInstance.setMediaListProvider(new DynamicMediaListProvider(appInstance, this.mediaAudioMap, this.mediaVideoMap));
	}
 
	public void onAppStop(IApplicationInstance appInstance)
	{
		String fullname = appInstance.getApplication().getName() + "/" + appInstance.getName();
		getLogger().info("onAppStop: " + fullname);
	}

	
	class StreamListener implements IMediaStreamActionNotify3
	{
		private HashMap<String,MediaCodecInfoAudio> mediaAudioMap = null;
		private HashMap<String,MediaCodecInfoVideo> mediaVideoMap = null;

		public StreamListener(HashMap<String,MediaCodecInfoAudio> mediaaudiomap,HashMap<String,MediaCodecInfoVideo> mediavideomap)
		{
			this.mediaAudioMap = mediaaudiomap;
			this.mediaVideoMap = mediavideomap;
		}
		
		public void onMetaData(IMediaStream stream, AMFPacket metaDataPacket){}
		public void onPauseRaw(IMediaStream stream, boolean isPause, double location){}
		public void onPause(IMediaStream stream, boolean isPause, double location){	}
		public void onPlay(IMediaStream stream, String streamName, double playStart, double playLen, int playReset){}
		public void onPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend){}
		public void onSeek(IMediaStream stream, double location){}
		public void onStop(IMediaStream stream){}
		public void onUnPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend)
		{
			synchronized(this.mediaAudioMap)
			{
				if ( this.mediaAudioMap.containsKey(stream.getName()) )
				{
					this.mediaAudioMap.remove(stream.getName());
				}
			}
			synchronized(this.mediaVideoMap)
			{
				if ( this.mediaVideoMap.containsKey(stream.getName()) )
				{
					this.mediaVideoMap.remove(stream.getName());
				}
			}
		}
		public void onCodecInfoAudio(IMediaStream stream,MediaCodecInfoAudio codecInfoAudio) 
		{
			synchronized(this.mediaAudioMap)
			{
				if ( this.mediaAudioMap.containsKey(stream.getName()) )
				{
					this.mediaAudioMap.remove(stream.getName());
				}
				this.mediaAudioMap.put(stream.getName(), codecInfoAudio);
			}
		}

		public void onCodecInfoVideo(IMediaStream stream, MediaCodecInfoVideo codecInfoVideo) 
		{
			synchronized(this.mediaVideoMap)
			{
				if ( this.mediaVideoMap.containsKey(stream.getName()) )
				{
					this.mediaVideoMap.remove(stream.getName());
				}
				this.mediaVideoMap.put(stream.getName(), codecInfoVideo);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void onStreamCreate(IMediaStream stream)
	{
		IMediaStreamActionNotify3 actionNotify = new StreamListener(this.mediaAudioMap, this.mediaVideoMap);
		WMSProperties props = stream.getProperties();
		synchronized (props)
		{
			props.put("streamActionNotifier", actionNotify);
		}
		stream.addClientListener(actionNotify);
	}

	public void onStreamDestroy(IMediaStream stream)
	{
		IMediaStreamActionNotify3 actionNotify = null;
		WMSProperties props = stream.getProperties();
		synchronized (props)
		{
			actionNotify = (IMediaStreamActionNotify3) stream.getProperties().get("streamActionNotifier");
		}
		if (actionNotify != null)
		{
			stream.removeClientListener(actionNotify);
		}
	}

}
