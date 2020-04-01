package com.tencent.cloud.asr.realtime.sdk.http.base;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.DefaultHttpResponseParserFactory;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;

import com.tencent.cloud.asr.realtime.sdk.config.AsrGlobelConfig;
import com.tencent.cloud.asr.realtime.sdk.config.AsrInternalConfig;

/**
 * HttpClient连接池和httpClient对象管理器。包含各项参数配置.管理了1个连接池对象。
 * 
 * <a href=
 * "http://hc.apache.org/httpcomponents-client-4.5.x/httpclient/examples/org/apache/http/examples/client/ClientConfiguration.java"
 * >官网资料参考</a>
 */
public class HttpPoolingManager {

	private static HttpPoolingManager httpPoolingManager = new HttpPoolingManager();

	private PoolingHttpClientConnectionManager connManager;

	private CloseableHttpClient httpClient;

	private HttpPoolingManager() {
		this.createConnectManager();
		this.httpClient = this.createHttpClient();
//		startMonitorCleanTask();

		IdleConnectionEvictor idleConnectionEvictor = new IdleConnectionEvictor(connManager, null, 60, TimeUnit.SECONDS, 60, TimeUnit.SECONDS);
		idleConnectionEvictor.start();
	}

	public static HttpPoolingManager getInstance() {
		return httpPoolingManager;
	}

	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}

	public void closeClient() {
		try {
			this.httpClient.close();
		} catch (IOException e) {
			// ignore
		}
		System.err.println("CloseableHttpClient instance closed.");
	}

	private CloseableHttpClient createHttpClient() {
		RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectTimeout(500) // 连接超时时间
				.setSocketTimeout(600 * 1000) // 读超时时间（等待数据超时时间）
				.setConnectionRequestTimeout(300) // 从池中获取连接超时时间
				/*.setStaleConnectionCheckEnabled(true)*/// 检查是否为陈旧的连接，默认为true，类似testOnBorrow
				// .setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.0.2", 1234))) //设置代理方法1
				.build();

		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(this.connManager) // 连接管理器
				/*.setProxy(new HttpHost("myproxy", 8080))*/// 设置代理方法2
				.setDefaultRequestConfig(defaultRequestConfig) // 默认请求配置
				.setConnectionManagerShared(false)// 连接池不是共享模式，这个共享是指与其它httpClient是否共享
				.evictIdleConnections(60, TimeUnit.SECONDS)// 定期回收空闲连接
				.evictExpiredConnections()// 回收过期连接
				.setConnectionTimeToLive(60, TimeUnit.SECONDS)// 连接存活时间，如果不设置，则根据长连接信息决定
				.setConnectionReuseStrategy(DefaultConnectionReuseStrategy.INSTANCE)// 连接重用策略，即是否能keepAlive
				.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)// 长连接配置，即获取长连接生产多长时间
				.setRetryHandler(new DefaultHttpRequestRetryHandler(1, true))// 设置重试次数，默认为3次，先改为1次
				.build();

		return httpClient;
	}

	private void createConnectManager() {

		// 注册访问协议相关的Socket工厂
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", SSLConnectionSocketFactory.getSocketFactory()).build();

		// HttpConnectionFactory:配置写请求/解析响应处理器
		HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connectionFactory = new ManagedHttpClientConnectionFactory(
				DefaultHttpRequestWriterFactory.INSTANCE, DefaultHttpResponseParserFactory.INSTANCE);

		// DNS解析器
		DnsResolver dnsResolver = SystemDefaultDnsResolver.INSTANCE;

		this.connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry, connectionFactory, dnsResolver);

		// 最大连接数
		this.connManager.setMaxTotal(AsrGlobelConfig.MAX_TOTAL);
		// 默认的每个路由的最大连接数
		this.connManager.setDefaultMaxPerRoute(AsrGlobelConfig.DEFAULT_MAX_PER_ROUTE);
		// 设置到某个路由的最大连接数，会覆盖defaultMaxPerRoute
		this.connManager.setMaxPerRoute(new HttpRoute(new HttpHost(AsrInternalConfig.REAL_ASR_URL)),
				AsrGlobelConfig.CUSTOM_MAX_PER_ROUTE);
		this.connManager.setValidateAfterInactivity(5 * 1000);// 从连接池获取连接时，连接不活跃多长时间后需要进行一次验证,改大一点。

		// socket配置（默认配置 和 某个host的配置）
		SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true) // 是否立即发送数据，设置为true会关闭Socket缓冲，默认为false
				.setSoReuseAddress(true) // 是否可以在一个进程关闭Socket后，即使它还没有释放端口，其它进程还可以立即重用端口
				.setSoTimeout(600000) // 接收数据的等待超时时间，单位ms
				.setSoLinger(5) // 关闭Socket时，要么发送完所有数据，要么等待5s后，就关闭连接，此时socket.close()是阻塞的
				.setSoKeepAlive(true) // 开启监视TCP连接是否有效
				.build();
		connManager.setDefaultSocketConfig(socketConfig);
		connManager.setSocketConfig(new HttpHost(AsrInternalConfig.REAL_ASR_URL), socketConfig);

		// 消息约束
		/*MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(200)
				.setMaxLineLength(2000).build();
		// Http connection相关配置
		ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE)
				.setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8)
				.setMessageConstraints(messageConstraints).build();
		// 一般不修改HTTP connection相关配置，故不设置
		connManager.setDefaultConnectionConfig(connectionConfig); 
		connManager.setConnectionConfig(new HttpHost("somehost", 80), ConnectionConfig.DEFAULT);*/
	}

	/**
	 * 参考资料： https://blog.csdn.net/hry2015/article/details/78965690
	 */
	private void startMonitorCleanTask() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				connManager.closeExpiredConnections();
				connManager.closeIdleConnections(5, TimeUnit.SECONDS);
				/*System.out.println("---> closed Idle Connections");*/
			}
		}, 0, 5000);
	}

}
