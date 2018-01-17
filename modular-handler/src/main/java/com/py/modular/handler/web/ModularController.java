package com.py.modular.handler.web;

import com.py.modular.common.database.entity.Record;
import com.py.modular.handler.service.ZeroService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * http请求处理控制器
 *
 * @author PYSASUKE
 */
@RestController
@RequestMapping(value = "/modular", produces = "application/json; charset=utf-8")
public class ModularController {

    @Autowired
    private ZeroService zeroService;

    /**
     * 测试
     *
     * @return
     */
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    @ResponseBody
    public void test() {
        Record record = new Record();
        record.setDescribe("这只是个测试");
        zeroService.test(record);
    }
}
