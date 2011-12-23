package com.alwold.classwatch.service;

import com.alwold.classwatch.dao.CourseDao;
import com.alwold.classwatch.model.Course;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author alwold
 */
public class ClasswatchService {
	public static final int WORKER_THREAD_COUNT = 1;
	private List<WorkerThread> workerThreads;
	private BlockingQueue<Course> transactionManager;
	private ApplicationContext applicationContext;
	
	public static void main(String[] args) {
		new ClasswatchService().run();
	}
	
	public ClasswatchService() {
		transactionManager = new LinkedBlockingQueue<Course>();
		workerThreads = new ArrayList<WorkerThread>();
		applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml", "classwatchDao.xml");
	}
	
	public void run() {
		for (int i = 0; i < WORKER_THREAD_COUNT; i++) {
			WorkerThread thread = new WorkerThread();
			workerThreads.add(thread);
			thread.setTransactionManager(transactionManager);
			thread.setCourseDao(applicationContext.getBean(CourseDao.class));
			thread.start();
		}
	}
}
