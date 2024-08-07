package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.mapper.AMapper;
import org.example.model.A;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
@Slf4j
public class AService {
    private final AMapper mapper;
    private final CommService server;

    public AService(AMapper mapper, CommService server) {
        this.mapper = mapper;
        this.server = server;
    }

    @Scheduled(fixedDelay = 10000)
    @Cacheable(value = "aList",key = "#id")
    public List<A> get(@PathVariable int id){
        List<A> list = mapper.selectAll();
        return list;
    }

    public void del(){
        server.doDel();
    }

}
