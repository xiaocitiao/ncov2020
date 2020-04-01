package com.tencent.cloud.asr.realtime.sdk.utils;

/**
 * <p>
 * 该类是各“服务类”的基类。“服务类”被定义为实现了某一个特定的功能，并且是一个大的功能中相对独立的一部分，有自己单独的配置文件。
 * 
 * 它可以通过管理界面（如telnet等)在后台被单独启动或者停止。通常一个项目可由多个“服务”组成。
 * <p>
 * 通过start()方法启动该服务类，将自动在内部打开一个新线程来运行实际的业务逻辑。
 * <p>
 * 线程安全：该类线程安全。相关的方法已经做了适当的同步处理。
 * 
 * @author Scofield, iantang
 * @version 1.1
 * @since 1.0
 */
public abstract class ServiceSupport {

	public static interface ExceptionHandler {

		public void onStartException(ServiceSupport service, Exception e);

		public void onStopException(ServiceSupport service, Exception e);

	}

	private Thread workingThread;

	private ExceptionHandler exceptionHandler;

	/**
	 * 服务的启动函数。它预定义了启动的流程：先通常配置文件判断该服务是否被开启。如果被开启且没有启动，则启动；否则忽略。如果可以启动，则发送<code>StartEvent</code>。如果之后启动失败，则发送
	 * <code>EndEvent</code>。子类自定义的启动逻辑，可在_start()方法中完成。
	 */
	public synchronized final void start() {
		if (!isEnabled()) {
			System.out.println(getName() + " is disabled, skiped.");
			return;
		}
		if (isStarting()) {
			System.out.println(getName() + " is already running.");
			return;
		}
		System.out.println("ServiceSupport is starting : " + getName() + " (" + getDescription() + ")");
		workingThread = new Thread(getName()) {
			@Override
			public void run() {
				try {
					_start();
				} catch (Exception e) {
					System.err.println(getName() + " (" + getDescription() + ") failed to start: " + e.getMessage());
					e.printStackTrace();
					if (exceptionHandler != null) {
						exceptionHandler.onStartException(ServiceSupport.this, e);
					}
					try {
						_stop();
					} catch (Exception e1) {
						e.printStackTrace();
						if (exceptionHandler != null) {
							exceptionHandler.onStopException(ServiceSupport.this, e);
						}
					}
				}
			}
		};

		workingThread.start();
	}

	/**
	 * 该服务的停止函数。如果该函数没有启动，则直接返回。否则将停止它，并发送<code>StopEvent</code>事件。实际的关闭逻辑，子类可在_stop()方法中完成。
	 */
	public synchronized final void stop() {
		if (!isStarting()) {
			System.out.println(getName() + " is already stopped.");
			return;
		}
		System.out.println(getName() + " is stopping...");
		try {
			_stop();
		} catch (Exception e) {
			e.printStackTrace();
			if (exceptionHandler != null) {
				exceptionHandler.onStopException(ServiceSupport.this, e);
			}
		}
		workingThread.interrupt();
		workingThread = null;
	}

	protected abstract void _start() throws Exception;

	protected abstract void _stop() throws Exception;

	public abstract boolean isEnabled();

	public abstract String getName();

	public abstract String getDescription();

	/**
	 * 该服务是否正在启动或者已经启动成功。
	 */
	public synchronized boolean isStarting() {
		return workingThread != null;
	}

	public synchronized void setExceptionHandler(ExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

}
