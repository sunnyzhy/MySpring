package org.example.controller;

import org.example.model.MessageData;
import org.example.service.PluginService;
import org.example.util.HttpUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author zhy
 * @date 2024/7/22 16:12
 */
@RestController
@RequestMapping("/plugin")
public class PluginController {
    private final PluginService pluginService;

    public PluginController(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    @GetMapping
    public void all(MessageData messageData) {
        pluginService.all(messageData);
    }

    @GetMapping("/{type}")
    public void send(@PathVariable String type, MessageData messageData) {
        HttpUtil.HttpEntityVo vo = new HttpUtil.HttpEntityVo();
        vo.setUrl("http://20.0.0.48:8702/event/type/all");
        vo.setMethod("get");
        vo.getHeaders().put("tenantId","b8e83a519a41418583bd847e8b108f22");
        vo.getHeaders().put("userId",2);
        vo.getHeaders().put("userType",2);
        vo.getHeaders().put("userName","admin");
        ResponseEntity<String> request = HttpUtil.request(vo);
        System.out.println(request.getBody());
        pluginService.send(type, messageData);
    }

    @GetMapping("/unload/{type}")
    public void unload(@PathVariable String type) {
        pluginService.unload(type);
    }

}
