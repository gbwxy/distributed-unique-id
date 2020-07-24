package gbwxy.distributed.id.service;


import gbwxy.distributed.id.Snowflake.SnowflakeIdWorker;
import gbwxy.distributed.id.common.DigitalCalculateUtils;
import gbwxy.distributed.id.common.ParamValidateUtils;
import gbwxy.distributed.id.common.PropertyFactory;
import gbwxy.distributed.id.entity.Result;
import gbwxy.distributed.id.entity.Status;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * 描述：
 *
 * @Author wangjun
 * @Date 2020/7/22
 */
@Service
public class SnowflakeService {
    private Logger logger = LoggerFactory.getLogger(SnowflakeService.class);
    private SnowflakeIdWorker serviceIdGen;
    private SnowflakeIdWorker instanceIdGen;

    public SnowflakeService(Environment... environments) {
        Properties properties = PropertyFactory.getProperties();
        String zkAddress = properties.getProperty("id.snowflake.zk.address");
        String zkPort = properties.getProperty("id.snowflake.zk.port");
        String serviceTag = "";
        if (environments.length >= 1) {
            Environment environment = environments[0];
            zkAddress = environment.getProperty("id.zk.address");
            zkPort = environment.getProperty("id.zk.port");
            serviceTag = environment.getProperty("id.service.tag");
            if (StringUtils.isBlank(zkAddress) || StringUtils.isBlank(zkPort)) {
                zkAddress = properties.getProperty("id.snowflake.zk.address");
                zkPort = properties.getProperty("id.snowflake.zk.port");
                this.logger.info("read default zk configurations, zkAddress:[{}], zkPort:[{}]", zkAddress, zkPort);
            }

            this.logger.info("with custom configuration:zkAddress:[{}], zkPort:[{}], serviceTag:[{}]", new Object[]{zkAddress, zkPort, serviceTag});
        }

        this.instanceIdGen = new SnowflakeIdWorker(zkAddress, zkPort);
        if (StringUtils.isBlank(serviceTag)) {
            serviceTag = "defaultService";
        }

        this.serviceIdGen = new SnowflakeIdWorker(zkAddress, zkPort, serviceTag);
        if (this.instanceIdGen.init() && this.serviceIdGen.init()) {
            this.logger.info("Snowflake Service Init Successfully");
        } else {
            throw new RuntimeException("Snowflake Service Init Fail, zkAddress:" + zkAddress + ", zkPort:" + zkPort + ", serviceTag:" + serviceTag);
        }
    }

    public String getInstanceId_Condition(String productionName) {
        if (!ParamValidateUtils.validateProdName(productionName)) {
            throw new RuntimeException("No productionName! Your bad param is:[" + productionName + "], param rule:mix of [0~9] or [a~z] or [-] or [_], and digital first or letter first and length between [1-5].");
        } else {
            Result idInstance = this.instanceIdGen.getID();
            if (idInstance.getStatus().equals(Status.EXCEPTION)) {
                throw new RuntimeException(idInstance.toString());
            } else {
                String snowflakeId = DigitalCalculateUtils.transferFrom10to36(Long.valueOf(idInstance.getId()));
                return productionName + "-" + snowflakeId;
            }
        }
    }

    public Long getInstanceId_Max19() {
        Result idInstance = this.instanceIdGen.getID();
        if (idInstance.getStatus().equals(Status.EXCEPTION)) {
            throw new RuntimeException(idInstance.toString());
        } else {
            return idInstance.getId();
        }
    }

    public String getInstanceId_Max13() {
        Result idInstance = this.instanceIdGen.getID();
        if (idInstance.getStatus().equals(Status.EXCEPTION)) {
            throw new RuntimeException(idInstance.toString());
        } else {
            return DigitalCalculateUtils.transferFrom10to36(Long.valueOf(idInstance.getId()));
        }
    }

    public String getInstanceID_Max6() {
        Result idInstance = this.serviceIdGen.getID();
        if (idInstance.getStatus().equals(Status.EXCEPTION)) {
            throw new RuntimeException(idInstance.toString());
        } else {
            return DigitalCalculateUtils.transferFrom10to6Characters(idInstance.getId());
        }
    }

    public String getInstanceID_Specify(int expectLen) {
        if (expectLen >= 3 && expectLen <= 8) {
            Result idInstance = this.serviceIdGen.getID();
            if (idInstance.getStatus().equals(Status.EXCEPTION)) {
                throw new RuntimeException(idInstance.toString());
            } else {
                return DigitalCalculateUtils.transferFrom10toSpecifyLength(idInstance.getId(), expectLen);
            }
        } else {
            throw new RuntimeException("illegal param, please input: [3~8]");
        }
    }
}
