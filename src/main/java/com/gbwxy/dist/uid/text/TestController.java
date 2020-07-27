package com.gbwxy.dist.uid.text;

import com.gbwxy.dist.uid.service.MLeafSnowflakeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * 描述：
 *
 * @Author wangjun
 * @Date 2019/10/8
 */
@RestController
@RequestMapping("/test/")
public class TestController {


    @RequestMapping(value = "/snowflake", method = RequestMethod.GET)
    public Object GetSnowFlake() {
        MLeafSnowflakeService service = new MLeafSnowflakeService();
        long id = service.GetId();
        return id;
    }


}


