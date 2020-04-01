package com.tencent.cloud.asr.tts.sdk.asyn_sender;

import com.tencent.cloud.asr.realtime.sdk.cache_handler.ReceiverCache;
import com.tencent.cloud.asr.realtime.sdk.model.request.RasrBytesRequest;
import com.tencent.cloud.asr.tts.sdk.model.TtsBytesRequest;

public class TtsReceiverCache extends ReceiverCache {

	@Override
	protected RasrBytesRequest createBytesRequest() {
		return new TtsBytesRequest();
	}

	@Override
	protected RasrBytesRequest createBytesRequest(RasrBytesRequest previous) {
		return new TtsBytesRequest(previous);
	}

	public void setSessionId(String sessionId) {
		TtsBytesRequest currentRequest = (TtsBytesRequest) super.getCurrentRequest();
		currentRequest.setSessionId(sessionId);
	}
}
