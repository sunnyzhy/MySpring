package org.example.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhy
 * @date 2024/7/30 14:00
 */
@Data
public class MessageData {
    private List<String> pluginNameList = new ArrayList<>();
    private int id;
    private String msg;
}
