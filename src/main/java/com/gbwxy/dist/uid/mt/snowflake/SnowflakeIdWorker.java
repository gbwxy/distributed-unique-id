package com.gbwxy.dist.uid.mt.snowflake;



import com.gbwxy.dist.uid.mt.common.NetworkUtils;
import com.gbwxy.dist.uid.mt.common.ParamValidateUtils;
import com.gbwxy.dist.uid.mt.common.PropertyFactory;
import com.gbwxy.dist.uid.mt.entity.Result;
import com.gbwxy.dist.uid.mt.entity.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Random;

/**
 * 描述：
 *
 * @Author wangjun
 * @Date 2020/7/21
 */
public class SnowflakeIdWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnowflakeIdWorker.class);
    private volatile Boolean zkInitFlag;
    private final long startTimestamp;
    private final long workerIdBits = 10L;
    private final long maxWorkerId = 1023L;
    private final long sequenceBits = 12L;
    private final long workerIdShift = 12L;
    private final long timestampLeftShift = 22L;
    private final long sequenceMask = 4095L;
    private long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    private static final Random RANDOM = new Random();


    public SnowflakeIdWorker(String zkAddress, String port) {
        Properties properties = PropertyFactory.getProperties();
        this.startTimestamp = Long.parseLong(properties.getProperty("id.snowflake.start.timestamp"));
        ParamValidateUtils.checkArgument(this.timeGen() > this.startTimestamp, "gvwxy.unique.id.Snowflake not support startTimestamp gt currentTime");
        String ip = NetworkUtils.getIp();
        SnowflakeZookeeperHolder holder = new SnowflakeZookeeperHolder(ip, port, zkAddress);
        LOGGER.info("startTimestamp:{}, ip:{} ,zkAddress:{} port:{}", new Object[]{this.startTimestamp, ip, zkAddress, port});
        boolean initFlag = holder.init();
        if (!initFlag) {
            LOGGER.error("gvwxy.unique.id.Snowflake Id Gen is not init ok, zkAddress:{}, zkPort:{}", zkAddress, port);
            throw new IllegalArgumentException("gvwxy.unique.id.Snowflake Id Gen is not init ok, zkAddress:" + zkAddress + ", port:" + port);
        } else {
            this.workerId = (long) holder.getWorkerID();
            this.zkInitFlag = Boolean.TRUE;
            LOGGER.info("START SUCCESS USE ZK WORKERID-{}", this.workerId);
            ParamValidateUtils.checkArgument(this.workerId >= 0L && this.workerId <= 1023L, "workerID must gte 0 and lte 1024");
        }
    }

    public SnowflakeIdWorker(String zkAddress, String port, String serviceTag) {
        Properties properties = PropertyFactory.getProperties();
        this.startTimestamp = Long.parseLong(properties.getProperty("id.snowflake.start.timestamp"));
        ParamValidateUtils.checkArgument(this.timeGen() > this.startTimestamp, "gvwxy.unique.id.Snowflake not support startTimestamp gt currentTime");
        String ip = NetworkUtils.getIp();
        SnowflakeZookeeperHolder holder = new SnowflakeZookeeperHolder(ip, port, zkAddress, serviceTag);
        LOGGER.info("startTimestamp:{}, ip:{} ,zkAddress:{} port:{}, serviceTag:{}", new Object[]{this.startTimestamp, ip, zkAddress, port, serviceTag});
        boolean initFlag = holder.init();
        if (!initFlag) {
            LOGGER.error("gvwxy.unique.id.Snowflake Id Gen is not init ok, zkAddress:{}, zkPort:{}", zkAddress, port);
            throw new IllegalArgumentException("gvwxy.unique.id.Snowflake Id Gen is not init ok, zkAddress:" + zkAddress + ", port:" + port);
        } else {
            this.workerId = (long) holder.getWorkerID();
            this.zkInitFlag = Boolean.TRUE;
            LOGGER.info("START SUCCESS USE ZK WORKERID-{}", this.workerId);
            ParamValidateUtils.checkArgument(this.workerId >= 0L && this.workerId <= 1023L, "workerID must gte 0 and lte 1024");
        }
    }

    public boolean init() {
        return this.zkInitFlag;
    }

    public synchronized Result getID() {
        long timestamp = this.timeGen();
        long id;
        if (timestamp < this.lastTimestamp) {
            id = this.lastTimestamp - timestamp;
            if (id > 5L) {
                return new Result(-3L, Status.EXCEPTION);
            }

            try {
                this.wait(id << 1);
                timestamp = this.timeGen();
                if (timestamp < this.lastTimestamp) {
                    return new Result(-1L, Status.EXCEPTION);
                }
            } catch (InterruptedException var6) {
                LOGGER.error("wait interrupted");
                return new Result(-2L, Status.EXCEPTION);
            }
        }

        if (this.lastTimestamp == timestamp) {
            this.sequence = this.sequence + 1L & 4095L;
            if (this.sequence == 0L) {
                this.sequence = (long) RANDOM.nextInt(100);
                timestamp = this.tilNextMillis(this.lastTimestamp);
            }
        } else {
            this.sequence = (long) RANDOM.nextInt(100);
        }

        this.lastTimestamp = timestamp;
        id = timestamp - this.startTimestamp << 22 | this.workerId << 12 | this.sequence;
        return new Result(id, Status.SUCCESS);
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp;
        for (timestamp = this.timeGen(); timestamp <= lastTimestamp; timestamp = this.timeGen()) {
            ;
        }

        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }

    public long getWorkerId() {
        return this.workerId;
    }
}


