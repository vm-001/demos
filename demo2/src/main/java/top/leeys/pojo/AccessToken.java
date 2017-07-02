package top.leeys.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 请求新浪access_token返回的对象
 */
@Data
public class AccessToken {
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("remind_in")
    private String remindIn;
    
    @JsonProperty("expires_in")
    private String expiresIn;
    
    private String uid;
}
