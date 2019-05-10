package com.example.demo.demofeign;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.math.BigDecimal;

@RestController
public class TestFeign {

    @Resource
    public FeignService feignService;

    @RequestMapping(value = "/queryName")
    public String queryName () {
        return feignService.queryName();
    }


    /**
     * 用户下单服务
     * @param courseId
     * @param userId
     * @param count
     * @return
     */
    @RequestMapping(value = "/placeOrder")
    public String placeOrder (Integer courseId,String userId,Integer count,BigDecimal price) {
        return feignService.placeOrder(courseId,userId,count,price);
    }

}
