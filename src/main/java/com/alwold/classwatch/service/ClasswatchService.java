package com.alwold.classwatch.service;

import com.alwold.classwatch.dao.CourseDao;
import com.alwold.classwatch.dao.NotificationDao;
import com.alwold.classwatch.dao.UserDao;
import com.alwold.classwatch.notification.Notifier;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author alwold
 */
public class ClasswatchService {
	public static final int WORKER_THREAD_COUNT = 1;
	
	private static Logger logger = Logger.getLogger(ClasswatchService.class);
	
	private List<WorkerThread> workerThreads;
	private ApplicationContext applicationContext;
	
	
	public static void main(String[] args) {
		new ClasswatchService().run();
	}
	
	public ClasswatchService() {
		workerThreads = new ArrayList<WorkerThread>();
		applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml", "classwatchDao.xml");
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run() {
				shutdown();
			}
		});
	}
	
	public void run() {
		writePidFile();
		logger.trace("run");
		for (int i = 0; i < WORKER_THREAD_COUNT; i++) {
			WorkerThread thread = new WorkerThread();
			workerThreads.add(thread);
			thread.setName("WorkerThread"+i);
			thread.setCourseQueue(applicationContext.getBean(CourseQueue.class));
			thread.setCourseDao(applicationContext.getBean(CourseDao.class));
			thread.setNotifiers(new ArrayList(applicationContext.getBeansOfType(Notifier.class).values()));
			thread.setNotificationDao(applicationContext.getBean(NotificationDao.class));
			thread.setUserDao(applicationContext.getBean(UserDao.class));
			logger.trace("start worker");
			thread.start();
		}
		for (WorkerThread t: workerThreads) {
			try {
				t.join();
			} catch (InterruptedException ex) {
				logger.error(ex);
			}
		}
	}

	public void shutdown() {
		logger.trace("shutdown requested");
		for (WorkerThread t: workerThreads) {
			t.shutdown();
		}
	}
	

	private void writePidFile() {
		String pid = System.getProperty("app.pid");
		if (pid == null) {
			logger.warn("Unable to get PID, not logging");
		} else {
			try {
				File pidFile = new File("/var/run/classwatch/classwatch-service.pid");
				FileWriter fw = new FileWriter(pidFile);
				fw.write(pid);
				fw.close();
				pidFile.deleteOnExit();
			} catch (IOException e) {
				logger.error("Unable to write PID file", e);
			}
		}
	}
}
