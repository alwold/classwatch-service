package com.alwold.classwatch.service;

import com.alwold.classwatch.dao.CourseDao;
import com.alwold.classwatch.model.Course;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author alwold
 */
public class ScheduleClassChecks {
	private static Logger logger = Logger.getLogger(ScheduleClassChecks.class);
	
	@Autowired
	private CourseDao courseDao;
	@Autowired
	private CourseQueue courseQueue;

	public void setCourseQueue(CourseQueue courseQueue) {
		this.courseQueue = courseQueue;
	}

	public void setCourseDao(CourseDao courseDao) {
		this.courseDao = courseDao;
	}
	
	public void scheduleClassChecks() throws InterruptedException {
		logger.trace("scheduleClasschecks");
		for (Course course: courseDao.getCourses("alwold@gmail.com")) {
			logger.trace("put course");
			courseQueue.put(course);
		}
	}
	
}
