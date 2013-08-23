package com.anjuke.dw.data_profiling.job;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.anjuke.dw.data_profiling.dao.DatabaseDao;
import com.anjuke.dw.data_profiling.dao.ServerDao;
import com.anjuke.dw.data_profiling.dao.TableDao;
import com.anjuke.dw.data_profiling.dao.UpdateQueueDao;
import com.anjuke.dw.data_profiling.model.Database;
import com.anjuke.dw.data_profiling.model.Server;
import com.anjuke.dw.data_profiling.model.Table;
import com.anjuke.dw.data_profiling.model.UpdateQueue;
import com.anjuke.dw.data_profiling.service.MetaService;

@Component
public class Builder implements Runnable, ApplicationContextAware {

    private static Logger logger = Logger.getLogger(Builder.class);
    private static int THREADS_PER_SERVER = 3;

    @Autowired
    private ServerDao serverDao;
    @Autowired
    private DatabaseDao databaseDao;
    @Autowired
    private TableDao tableDao;
    @Autowired
    private UpdateQueueDao updateQueueDao;
    @Autowired
    private MetaService metaService;

    private ApplicationContext context;
    private Map<Integer, ExecutorService> executors = new HashMap<Integer, ExecutorService>();

    @Override
    public void run() {

        logger.info("Job started.");

        int lastId = 0;
        while (!Thread.interrupted()) {

            int submitted = 0;
            for (UpdateQueue updateQueue : updateQueueDao.findByStatus(UpdateQueue.STATUS_NEW, lastId)) {

                lastId = updateQueue.getId();

                Table table = null;
                int serverId = 0;
                try {
                    table = tableDao.findById(updateQueue.getTableId());
                    Database database = databaseDao.findById(table.getDatabaseId());
                    Server server = serverDao.findById(database.getServerId());
                    serverId = server.getId();
                } catch (NullPointerException e) {
                    logger.error("invalid queue item", e);
                    updateQueueDao.updateStatus(updateQueue.getId(), UpdateQueue.STATUS_ERROR);
                    continue;
                }

                ExecutorService executor = executors.get(serverId);
                if (executor == null) {
                    executor = Executors.newFixedThreadPool(THREADS_PER_SERVER);
                    executors.put(serverId, executor);
                }

                executor.submit((Runnable) context.getBean("builderTask", updateQueue));
                ++submitted;
            }

            if (submitted > 0) {
                logger.info("Submitted " + String.valueOf(submitted) + " items.");
            }

            logger.info("Sleeping");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break;
            }
        }

        logger.info("Shutting down executors.");
        for (ExecutorService executor : executors.values()) {
            executor.shutdown();
        }

        logger.info("Job ended.");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {

        this.context = applicationContext;
    }

    public static void main(String[] args) throws Exception {
        AbstractApplicationContext context = new ClassPathXmlApplicationContext("spring/job-context.xml");
        context.getBean(Builder.class).run();
        context.close();
    }

}
