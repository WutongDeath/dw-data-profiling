package com.anjuke.dw.data_profiling.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.anjuke.dw.data_profiling.dao.DatabaseDao;
import com.anjuke.dw.data_profiling.dao.ServerDao;
import com.anjuke.dw.data_profiling.model.Database;
import com.anjuke.dw.data_profiling.model.Server;

@Service
public class CommonService {

    @Autowired
    private ServerDao serverDao;

    @Autowired
    private DatabaseDao databaseDao;

    public class NaviServer {
        private Integer id;
        private String name;
        private List<NaviDatabase> databaseList;
        private Boolean isExpanded;
        public Integer getId() {
            return id;
        }
        public void setId(Integer id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public List<NaviDatabase> getDatabaseList() {
            return databaseList;
        }
        public void setDatabaseList(List<NaviDatabase> databaseList) {
            this.databaseList = databaseList;
        }
        public Boolean getIsExpanded() {
            return isExpanded;
        }
        public void setIsExpanded(Boolean isExpanded) {
            this.isExpanded = isExpanded;
        }
    }

    public class NaviDatabase {
        private Integer id;
        private String name;
        private Boolean isChosen;
        public Integer getId() {
            return id;
        }
        public void setId(Integer id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public Boolean getIsChosen() {
            return isChosen;
        }
        public void setIsChosen(Boolean isChosen) {
            this.isChosen = isChosen;
        }
    }

    public List<NaviServer> getNavi(Integer databaseId) {

        Integer serverId = null;
        Map<Integer, List<NaviDatabase>> serverIdDatabaseList = new HashMap<Integer, List<NaviDatabase>>();
        for (Database database : databaseDao.findAll()) {
            List<NaviDatabase> databaseList = serverIdDatabaseList.get(database.getServerId());
            if (databaseList == null) {
                databaseList = new ArrayList<NaviDatabase>();
                serverIdDatabaseList.put(database.getServerId(), databaseList);
            }
            NaviDatabase naviDatabase = new NaviDatabase();
            naviDatabase.setId(database.getId());
            naviDatabase.setName(database.getName());
            if (database.getId() == databaseId) {
                naviDatabase.setIsChosen(true);
                serverId = database.getServerId();
            } else {
                naviDatabase.setIsChosen(false);
            }
            databaseList.add(naviDatabase);
        }

        List<NaviServer> serverList = new ArrayList<NaviServer>();
        for (Server server : serverDao.findAll()) {
            NaviServer naviServer = new NaviServer();
            naviServer.setId(server.getId());
            naviServer.setName(server.getName());
            naviServer.setDatabaseList(serverIdDatabaseList.get(server.getId()));
            naviServer.setIsExpanded(server.getId() == serverId);
            serverList.add(naviServer);
        }

        return serverList;

    }

}
