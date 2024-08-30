package org.example.controller;

import org.example.model.MessageData;
import org.example.service.PluginService;
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
        pluginService.send(type, messageData);
    }

    @GetMapping("/unload/{type}")
    public void unload(@PathVariable String type) {
        pluginService.unload(type);
    }

}
