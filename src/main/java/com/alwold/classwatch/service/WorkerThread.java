package com.alwold.classwatch.service;

import com.alwold.classwatch.dao.CourseDao;
import com.alwold.classwatch.dao.NotificationDao;
import com.alwold.classwatch.dao.UserDao;
import com.alwold.classwatch.model.Course;
import com.alwold.classwatch.model.NotificationStatus;
import com.alwold.classwatch.model.School;
import com.alwold.classwatch.model.Status;
import com.alwold.classwatch.model.User;
import com.alwold.classwatch.notification.NotificationException;
import com.alwold.classwatch.notification.Notifier;
import com.alwold.classwatch.school.RetrievalException;
import com.alwold.classwatch.school.SchoolPlugin;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author alwold
 */
public class WorkerThread extends Thread {
	private static Logger logger = Logger.getLogger(WorkerThread.class);
	
	private CourseQueue courseQueue;
	private Map<Long, SchoolPlugin> plugins = new HashMap<Long, SchoolPlugin>();
	private CourseDao courseDao;
	private List<Notifier> notifiers;
	private NotificationDao notificationDao;
	private UserDao userDao;
	private boolean shutdownRequested = false;

	@Override
	public void run() {
		try {
			logger.trace("waiting for tx");
			Course course = courseQueue.take();
			while (course != null && !shutdownRequested) {
				try {
					logger.trace("checking course "+course.getId());
					SchoolPlugin plugin = getPlugin(course.getTerm().getSchool());
					try {
						Status status = plugin.getClassStatus(course.getTerm().getCode(), course.getCourseNumber());
						if (status != null) {
							courseDao.logStatus(course.getId(), status);
							if (status == Status.OPEN) {
								logger.info(course.getId()+" is open!");
								for (User user: courseDao.getActiveWatchers(course)) {
									logger.trace("notifying "+user.getEmail());
									for (Notifier notifier: notifiers) {
										if (userDao.isNotifierEnabled(user, notifier.getType())) {
											try {
												notifier.notify(user, course, plugin.getClassInfo(course.getTerm().getCode(), course.getCourseNumber()));
												notificationDao.logNotification(course, user, notifier.getType(), NotificationStatus.SUCCESS, null);
											} catch (NotificationException e) {
												notificationDao.logNotification(course, user, notifier.getType(), NotificationStatus.FAILURE, e.getMessage());
											}
										} else {
											logger.trace("skipping notification type "+notifier.getType()+" because it's not enabled");
										}
									}
									courseDao.setNotified(user, course);
								}
							}
						} else {
							logger.error("Class status came back null, doesn't exist?");
						}
					} catch (RetrievalException e) {
						logger.error(e);
					}
					logger.trace("waiting for tx");
				} catch (Throwable t) {
					logger.error("Caught exception in worker thread", t);
				}
				course = courseQueue.take();
			}
		} catch (InterruptedException e) {
		}
	}
	
	public void shutdown() {
		shutdownRequested = true;
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

	public void setCourseQueue(CourseQueue courseQueue) {
		this.courseQueue = courseQueue;
	}

	public void setCourseDao(CourseDao courseDao) {
		this.courseDao = courseDao;
	}

	public void setNotifiers(List<Notifier> notifiers) {
		this.notifiers = notifiers;
	}

	public void setNotificationDao(NotificationDao notificationDao) {
		this.notificationDao = notificationDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

}
