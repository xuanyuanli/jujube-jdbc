package org.dazao.web;

import org.dazao.persistence.UserDao;
import org.dazao.util.Jsons;
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
