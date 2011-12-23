package com.alwold.classwatch.service;

import com.alwold.classwatch.dao.CourseDao;
import com.alwold.classwatch.model.Course;
import com.alwold.classwatch.model.School;
import com.alwold.classwatch.model.Status;
import com.alwold.classwatch.school.RetrievalException;
import com.alwold.classwatch.school.SchoolPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import org.apache.log4j.Logger;

/**
 *
 * @author alwold
 */
public class WorkerThread extends Thread {
	private static Logger logger = Logger.getLogger(WorkerThread.class);
	
	private BlockingQueue<Course> transactionManager;
	private Map<Long, SchoolPlugin> plugins = new HashMap<Long, SchoolPlugin>();
	private CourseDao courseDao;

	public void setTransactionManager(BlockingQueue<Course> transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void setCourseDao(CourseDao courseDao) {
		this.courseDao = courseDao;
	}
	
	@Override
	public void run() {
		try {
			Course course = transactionManager.take();
			while (course != null) {
				SchoolPlugin plugin = getPlugin(course.getTerm().getPk().getSchool());
				try {
					Status status = plugin.getClassStatus(course.getTerm().getPk().getCode(), course.getCourseNumber());
					courseDao.logStatus(course.getId(), status);
					if (status == Status.OPEN) {
						logger.info(course.getId()+" is open!");
						// TODO notify watchers
					}
				} catch (RetrievalException e) {
					logger.error(e);
				}
				course = transactionManager.take();
			}
		} catch (InterruptedException e) {
		}
	}
	
	private SchoolPlugin getPlugin(School school) {
		if (plugins.containsKey(school.getId())) {
			return plugins.get(school.getId());
		} else {
			try {
				SchoolPlugin plugin = (SchoolPlugin) Class.forName(school.getPluginClass()).newInstance();
				plugins.put(school.getId(), plugin);
				return plugin;
			} catch (ClassNotFoundException e) {
				logger.error("Unable to instantiate school plugin", e);
				return null;
			} catch (IllegalAccessException e) {
				logger.error("Unable to instantiate school plugin", e);
				return null;
			} catch (InstantiationException e) {
				logger.error("Unable to instantiate school plugin", e);
				return null;
			}
		}
	}
}
