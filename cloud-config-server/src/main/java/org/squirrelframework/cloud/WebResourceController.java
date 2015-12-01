package org.squirrelframework.cloud;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by kailianghe on 11/22/15.
 */
@Controller
public class WebResourceController {

    @RequestMapping(value = "/")
    public String index() {
        return "index";
    }
}
