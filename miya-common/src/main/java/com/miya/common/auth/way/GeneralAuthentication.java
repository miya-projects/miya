package com.miya.common.auth.way;

import com.miya.common.config.web.jwt.JwtPayload;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 自定义的通用Authentication
 */
@Getter
@Setter
@Builder
public class GeneralAuthentication  {

    /**
     * 认证类型
     */
    private AuthenticationType authenticationType;
    /**
     * 用户对象，作为principal
     */
    private Object user;
    /**
     * 登录信息
     */
    private LoginInfo loginInfo;

    public GeneralAuthentication(AuthenticationType authenticationType, Object user, LoginInfo loginInfo){
        this.authenticationType = authenticationType;
        this.user = user;
        this.loginInfo = loginInfo;
    }

    public static GeneralAuthenticationBuilder builder(){
        return new GeneralAuthenticationBuilder();
    }

    public static class GeneralAuthenticationBuilder {
        private AuthenticationType authenticationType;
        private Object user;
        private LoginInfo loginInfo;

  		private GeneralAuthenticationBuilder() {}

  		public GeneralAuthenticationBuilder authenticationType(AuthenticationType authenticationType) {
  			this.authenticationType = authenticationType;
  			return this;
  		}

  		public GeneralAuthenticationBuilder user(Object user) {
  			this.user = user;
  			return this;
  		}
        public GeneralAuthenticationBuilder loginInfo(LoginInfo loginInfo) {
            this.loginInfo = loginInfo;
            return this;
        }
  		public GeneralAuthentication build() {
  			return new GeneralAuthentication(authenticationType, user, loginInfo);
  		}
  	}

    /**
     * 从token获取GeneralAuthentication
     * 但不会填充user
     * @param jwtPayload
     */
    public static GeneralAuthentication getFromToken(JwtPayload jwtPayload) {
        return GeneralAuthentication.builder()
                .authenticationType(AuthenticationType.JWT)
                .loginInfo(
                        LoginInfo.builder()
                                .loginDevice(jwtPayload.getLoginDevice())
                                .loginTime(jwtPayload.getLoginTime())
                                .loginWay(jwtPayload.getLoginWay())
                                .build()
                )
                .build();
    }


}
