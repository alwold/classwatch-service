package com.alwold.classwatch.service;

import com.alwold.classwatch.dao.CourseDao;
import com.alwold.classwatch.model.Course;
import com.alwold.classwatch.model.School;
import com.alwold.classwatch.model.Term;
import com.alwold.classwatch.model.TermPk;
import java.util.concurrent.LinkedBlockingQueue;
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
	private CourseQueue courseQueue;

	public void setCourseQueue(CourseQueue courseQueue) {
		this.courseQueue = courseQueue;
	}
	
	public void scheduleClassChecks() throws InterruptedException {
		logger.trace("scheduleClasschecks");
		Course course = new Course();
		course.setId(1L);
		course.setCourseNumber("12345");
		Term term = new Term();
		TermPk termPk = new TermPk();
		termPk.setCode("2127");
		School school = new School();
		school.setName("Arizona STate University");
		school.setId(1L);
		school.setPluginClass("com.alwold.classwatch.plugin.asu.AsuSchoolPlugin");
		termPk.setSchool(school);
		term.setPk(termPk);
		course.setTerm(term);
		logger.trace("put course");
		courseQueue.put(course);
	}
	
}
