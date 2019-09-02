package com.pinyougou.manager.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/itemSearch")
public class ItemSearchController {

    @Reference
    private ItemSearchService itemSearchService;

    @RequestMapping("/search")
    public Map<String,Object> search(@RequestBody Map<String,Object> searchMap){


        return itemSearchService.search(searchMap);
    }
}
