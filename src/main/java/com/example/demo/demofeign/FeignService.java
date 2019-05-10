package com.example.demo.demofeign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Service
@FeignClient(value = "myClient")
public interface FeignService {

   @GetMapping(value = "test/queryName")
   String queryName();

   @GetMapping(value = "test/placeOrder")
   String placeOrder(@RequestParam("courseId") Integer courseId, @RequestParam("userId") String userId, @RequestParam("count") Integer count, @RequestParam("price") BigDecimal price);
}
