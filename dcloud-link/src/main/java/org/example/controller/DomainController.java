package org.example.controller;


import java.util.Arrays;
import java.util.List;

import org.example.service.DomainService;
import org.example.util.JsonData;
import org.example.vo.DomainVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/domain/v1")
public class DomainController {

    @Autowired
    private DomainService domainService;

    /**
     * 列举全部可用域名列表
     */
    @GetMapping("list")
    public JsonData listAll() {
        List<DomainVO> list = domainService.listAll();
        return JsonData.buildSuccess(list);
    }


//    @GetMapping("test")
//    public JsonData test(@RequestParam(name = "code") String code, @RequestParam(name = "accountNo") Long accountNo) {
//
//        //key1是短链码，ARGV[1]是accountNo,ARGV[2]是过期时间
//        String script =
//            "if redis.call('EXISTS',KEYS[1])==0 then " +
//                "redis.call('set',KEYS[1],ARGV[1]); " +
//                "redis.call('expire',KEYS[1],ARGV[2]); " +
//                "return 1; " +
//                "elseif redis.call('get',KEYS[1]) == ARGV[1] then " +
//                "return 2; " +
//                "else return 0; " +
//                "end;";
//
//        Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(code),
//            accountNo, 100);
//        return JsonData.buildSuccess(result);
//    }
}

