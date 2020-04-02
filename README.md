# tencent-asr-tts-sdk

#腾讯云智能语音

####1> 修改官方传统jar包依赖 -> POM 依赖

####2> bugfix 官方http连接池 使用Timer 无线调度; 导致 执行完进程无法自动结束bug

---

###官方使用demo
    public class TtsRequestSample {
    
    	static {
    		initBaseParameters();
    	}
    
    	public static void main(String[] args) {
    		TtsRequestSample ttsRequestSample = new TtsRequestSample();
    		ttsRequestSample.start();
    	}
    
    	private void start() {
    		this.sendStringRequest();
    //		System.exit(0);
    	}
    
    	/**
    	 * 从字节数组读取语音数据，发送请求。
    	 */
    	private void sendStringRequest() {
    		// 方法1：
    		TtsSynSender ttsSynSender = new TtsSynSender(); // 创建之后可重复使用
    		String text = "早上好，今天天气真不错。";
    		TtsResponse response = ttsSynSender.request(text, "session-id-123");
    		// TtsResponse response2 = ttsSynSender.sendRequest(text);
    		printAndSaveReponse(response);
    		System.out.println("--call finished--");
    		// 方法2：
    //		String filePath = "testTtsFiles/test_article.txt";
    //		List<TtsResponse> list = ttsSynSender.requestFromFile(filePath, StandardCharsets.UTF_8, "Session-Id-xxx");
    //		for (TtsResponse ttsResponse : list) {
    //			printAndSaveReponse(ttsResponse);
    //		}
    	}
    
    	private void printAndSaveReponse(TtsResponse response) {
    		if (response != null) {
    			new File("logs").mkdirs();
    			File pcmFile = new File("logs/" + response.getSessionId() + ".pcm");
    			this.savePcmFile(response.getResponseBytes(), pcmFile);
    			File wavFile = new File("logs/" + response.getSessionId() + "_Convert.wav");
    			this.saveToWavFile(response.getResponseBytes(), pcmFile, wavFile);
    			System.out.println("Response: " + response.getSessionId() + ", length: "
    					+ response.getResponseBytes().length + ", result saved at: " + pcmFile.getAbsolutePath());
    		} else
    			System.out.println("Result is null.");
    	}
    
    	/**
    	 * 初始化基础参数, 请将下面的参数值配置成你自己的值。配置可中途修改，正常情况下立即生效。
    	 * 
    	 * 参数获取方法可参考： <a href="https://cloud.tencent.com/document/product/441/6203">签名鉴权 获取签名所需信息</a>
    	 */
    	private static void initBaseParameters() {
    		// Required
    		// AsrBaseConfig.appId = "YOUR_APP_ID_SET_HERE";
    		// AsrBaseConfig.secretId = "YOUR_SECRET_ID";
    		// AsrBaseConfig.secretKey = "YOUR_SECRET_KEY";
    
    		AsrBaseConfig.appId = "1252686663";
    		AsrBaseConfig.secretId = "AKIDoRr2wKPXVFjNp09SnwpSVKtMjbDbKeLl";
    		AsrBaseConfig.secretKey = "KPEp9D1cw0ntnSr05vVoQmrgEZu40gYC";
    
    		// optional，根据自身需求设置配置值， 不配则使用默认值。
    		TtsConfig.VOLUME = 5; // 音量大小, 范围[0，10]，默认为0，表示正常音量。
    		// TtsConfig.REQUEST_ENCODE = RequestEncode.UTF_8; // 传入的文字所采用的编码，默认为utf-8
    		// TtsConfig.SPEED = 0; // 语速，范围[-2，2]. -2: 0.6倍; -1: 0.8倍; 0:1.0倍（默认）; 1: 1.2倍; 2: 1.5倍 。其他值：1.0 倍。
    		// TtsConfig.VOICE_TYPE = 0; // 音色： 0：亲和女声（默认） 1：亲和男声 2：成熟男声 3：活力男声 4：温暖女声 5：情感女声 6：情感男声
    		// TtsConfig.SAMPLE_RATE = 16000;// 音频采样率： 16000：16k（默认）; 8000：8k
    		// TtsConfig.PRIMARY_LANGUAGE = 1;// 主语言类型： 1-中文（默认） 2-英文
    		// TtsConfig.CODEC = CodeC.PCM; // 无需修改。暂未支持Opus方式。
    	}
    
    	private void savePcmFile(byte[] response, File file) {
    		try {
    			FileOutputStream out = new FileOutputStream(file, false);
    			out.write(response);
    			out.close();
    		} catch (IOException e) {
    			System.err.println("Failed save data to: " + file + ", error: " + e.getMessage());
    		}
    
    	}
    
    	/**
    	 * 将Pcm文件转换成wav文件保存起来。请将方法中的参数改成自己的语音文件对应的值，本方法仅供参考。
    	 * 
    	 * 如需改成追加形式输出，请自行修改convert2Wav()方法中new FileOutputStream的参数。
    	 */
    	private void saveToWavFile(byte[] responseBytes, File pcmFile, File wavFile) {
    		int bitNum = TtsConfig.SAMPLE_RATE == 16000 ? 16 : 8;
    		PcmUtils.convert2Wav(pcmFile, wavFile, TtsConfig.SAMPLE_RATE, 1, bitNum);
    	}
    }

