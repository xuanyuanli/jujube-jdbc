package org.jujubeframework.jdbc.web;

import org.jujubeframework.jdbc.persistence.UserDao;
import org.jujubeframework.util.Jsons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    UserDao userDao;

    @RequestMapping("/list")
    public String test(){
        return Jsons.toPrettyJson(userDao.findAll());
    }
}
