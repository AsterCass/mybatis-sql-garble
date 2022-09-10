package com.aster.plugin.garble.test;

import com.aster.plugin.garble.service.AuthenticationCodeBuilder;
import com.aster.plugin.garble.service.AuthenticationCodeInterface;

public class UpdateAuthService  implements AuthenticationCodeInterface {


    @Override
    @AuthenticationCodeBuilder(type = 1, tables = {"user"})
    public String authenticationCodeBuilder() {
        return "1234";
    }


}
