package org.example.impl;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhy
 * @date 2024/8/23 14:45
 */
@RestController
public class TencentController {
    @GetMapping(value = "/test")
    public String get(@RequestParam Map<String,Object> param){
        if(param.isEmpty()){
            return "";
        }
        Set<String> keys = param.keySet();
        List<String> list = new ArrayList<>(keys);
        return param.get(list.get(0)).toString();
    }
}
