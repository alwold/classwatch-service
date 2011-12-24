package com.alwold.classwatch.service;

import com.alwold.classwatch.dao.CourseDao;
import com.alwold.classwatch.notification.Notifier;
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
	}
	
	public void run() {
		logger.trace("run");
		for (int i = 0; i < WORKER_THREAD_COUNT; i++) {
			WorkerThread thread = new WorkerThread();
			workerThreads.add(thread);
			thread.setName("WorkerThread"+i);
			thread.setCourseQueue(applicationContext.getBean(CourseQueue.class));
			thread.setCourseDao(applicationContext.getBean(CourseDao.class));
			thread.setNotifier(applicationContext.getBean(Notifier.class));
			logger.trace("start worker");
			thread.start();
		}
	}
}
