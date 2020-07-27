package com.gbwxy.dist.uid.mt.snowflake;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLBackgroundPathAndBytesable;


import org.apache.curator.retry.RetryUntilElapsed;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 描述：
 *
 * @Author wangjun
 * @Date 2020/7/21
 */
public class SnowflakeZookeeperHolder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SnowflakeZookeeperHolder.class);
    private String zk_AddressNode;
    private String listenAddress;
    private int workerID;
    private static String PREFIX_ZK_PATH = "/snowflake";
    private static String PATH_FOREVER;
    private String ip;
    private String port;
    private String connectionString;
    private long lastUpdateTime;

    public SnowflakeZookeeperHolder(String ip, String port, String connectionString) {
        this.zk_AddressNode = null;
        this.listenAddress = null;
        this.ip = ip;
        this.port = port;
        this.listenAddress = ip + ":" + port;
        this.connectionString = connectionString + ":" + port;
        PATH_FOREVER = PREFIX_ZK_PATH + "/forever";
    }

    public SnowflakeZookeeperHolder(String ip, String port, String connectionString, String serviceTag) {
        this(ip, port, connectionString);
        this.ip = ip;
        this.port = port;
        this.listenAddress = ip + ":" + port;
        this.connectionString = connectionString + ":" + port;
        PREFIX_ZK_PATH = PREFIX_ZK_PATH + "/" + serviceTag;
        PATH_FOREVER = PREFIX_ZK_PATH + "/forever";
    }

    public boolean init() {
        try {
            CuratorFramework curator = this.createWithOptions(this.connectionString,
                    new RetryUntilElapsed(1000, 4), 10000, 600000);
            curator.start();
            //判断是否存在目录
            Stat stat = curator.checkExists().forPath(PATH_FOREVER);
            //如果不存在则创建
            if (stat == null) {
                //创建目录
                this.zk_AddressNode = this.createNode(curator);
                //定时更新目录下数据
                this.scheduledUploadData(curator, this.zk_AddressNode);
                return true;
            } else {
                Map<String, Integer> nodeMap = new HashMap();
                Map<String, String> realNode = new HashMap();
                List<String> keys = curator.getChildren().forPath(PATH_FOREVER);
                Iterator var6 = keys.iterator();

                String newNode;
                String[] nodeKey;
                while (var6.hasNext()) {
                    newNode = (String) var6.next();
                    nodeKey = newNode.split("-");
                    //获取ip  map<IP,IP-序列号>
                    realNode.put(nodeKey[0], newNode);
                    //获取序列号  map<IP,序列号>
                    nodeMap.put(nodeKey[0], Integer.parseInt(nodeKey[1]));
                }

                Integer workerid = (Integer) nodeMap.get(this.listenAddress);
                if (workerid != null) {
                    this.zk_AddressNode = PATH_FOREVER + "/" + (String) realNode.get(this.listenAddress);
                    this.workerID = workerid;
                    if (!this.checkInitTimeStamp(curator, this.zk_AddressNode)) {
                        throw new RuntimeException("init timestamp check error,forever node timestamp gt this node time");
                    }
                    //创建一个守护线程-定期更新数据
                    this.doService(curator);
                    LOGGER.info("[Old NODE]find forever node have this endpoint ip-{} port-{} workid-{} childnode and start SUCCESS", new Object[]{this.ip, this.port, this.workerID});
                } else {
                    //创建目录
                    newNode = this.createNode(curator);
                    this.zk_AddressNode = newNode;
                    //获取序列号  map<IP,序列号>
                    nodeKey = newNode.split("-");
                    this.workerID = Integer.parseInt(nodeKey[1]);
                    //创建一个守护线程-定期更新数据
                    this.doService(curator);
                    LOGGER.info("[New NODE]can not find node on forever node that endpoint ip-{} port-{} workid-{},create own node on forever node and start SUCCESS ", new Object[]{this.ip, this.port, this.workerID});
                }

                return true;
            }
        } catch (Exception var9) {
            LOGGER.error("Start node ERROR {}", var9);
            return false;
        }
    }

    private void doService(CuratorFramework curator) {
        this.scheduledUploadData(curator, this.zk_AddressNode);
    }


    /**
     * 创建一个守护线程
     * 定期更新zk目录下的内容
     *
     * @param curator
     * @param zk_AddressNode
     */
    private void scheduledUploadData(final CuratorFramework curator, final String zk_AddressNode) {
        Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "schedule-upload-time");
                //守护线程
                thread.setDaemon(true);
                return thread;
            }
        }).scheduleWithFixedDelay(new Runnable() {
            //定期更新数据
            public void run() {
                SnowflakeZookeeperHolder.this.updateNewData(curator, zk_AddressNode);
            }
        }, 1L, 3L, TimeUnit.SECONDS);//初始1s后执行，每3s执行一次
    }

    private boolean checkInitTimeStamp(CuratorFramework curator, String zk_AddressNode) throws Exception {
        byte[] bytes = (byte[]) curator.getData().forPath(zk_AddressNode);
        SnowflakeZookeeperHolder.Endpoint endPoint = this.deBuildData(new String(bytes));
        return endPoint.getTimestamp() <= System.currentTimeMillis();
    }

    /**
     * 创建数据节点
     * PERSISTENT：持久化
     * PERSISTENT_SEQUENTIAL：持久化并且带序列号
     * EPHEMERAL：临时
     * EPHEMERAL_SEQUENTIAL：临时并且带序列号
     *
     * @param curator
     * @return
     * @throws Exception
     */
    private String createNode(CuratorFramework curator) throws Exception {
        try {
            return (String) ((ACLBackgroundPathAndBytesable) curator.create()
                    .creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL))
                    .forPath(PATH_FOREVER + "/" + this.listenAddress + "-", this.buildData().getBytes());
        } catch (Exception var3) {
            LOGGER.error("create node error msg {} ", var3.getMessage());
            throw var3;
        }
    }

    /**
     * 更新zk目录下数据
     *
     * @param curator
     * @param path
     */
    private void updateNewData(CuratorFramework curator, String path) {
        try {
            //防止时钟回拨
            if (System.currentTimeMillis() < this.lastUpdateTime) {
                return;
            }

            curator.setData().forPath(path, this.buildData().getBytes());
            this.lastUpdateTime = System.currentTimeMillis();
        } catch (Exception var4) {
            LOGGER.info("update init data error path is {} error is {}", path, var4);
        }

    }

    private String buildData() throws JsonProcessingException {
        SnowflakeZookeeperHolder.Endpoint endpoint = new SnowflakeZookeeperHolder.Endpoint(this.ip, this.port, System.currentTimeMillis());
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(endpoint);
        return json;
    }

    private SnowflakeZookeeperHolder.Endpoint deBuildData(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SnowflakeZookeeperHolder.Endpoint endpoint = (SnowflakeZookeeperHolder.Endpoint) mapper.readValue(json, SnowflakeZookeeperHolder.Endpoint.class);
        return endpoint;
    }

    private CuratorFramework createWithOptions(String connectionString, RetryPolicy retryPolicy, int connectionTimeoutMs, int sessionTimeoutMs) {
        return CuratorFrameworkFactory.builder()
                .connectString(connectionString)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs).build();
    }

    public String getZk_AddressNode() {
        return this.zk_AddressNode;
    }

    public void setZk_AddressNode(String zk_AddressNode) {
        this.zk_AddressNode = zk_AddressNode;
    }

    public String getListenAddress() {
        return this.listenAddress;
    }

    public void setListenAddress(String listenAddress) {
        this.listenAddress = listenAddress;
    }

    public int getWorkerID() {
        return this.workerID;
    }

    public void setWorkerID(int workerID) {
        this.workerID = workerID;
    }

    static class Endpoint {
        private String ip;
        private String port;
        private long timestamp;

        public Endpoint() {
        }

        public Endpoint(String ip, String port, long timestamp) {
            this.ip = ip;
            this.port = port;
            this.timestamp = timestamp;
        }

        public String getIp() {
            return this.ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getPort() {
            return this.port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public long getTimestamp() {
            return this.timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
