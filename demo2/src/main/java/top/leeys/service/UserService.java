package top.leeys.service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdurmont.emoji.EmojiParser;

import lombok.NonNull;
import top.leeys.config.AppConfig;
import top.leeys.domain.User;
import top.leeys.dto.UserDTO;
import top.leeys.pojo.AccessToken;
import top.leeys.repository.CustomRepository;
import top.leeys.repository.UserRepository;
import top.leeys.util.CommonUtils;

@Service
public class UserService {
	@Autowired UserRepository userRepository; 
	@Autowired CustomRepository customRepository;
	@Autowired RestTemplate rest;
	@Autowired ObjectMapper mapper;
	
	public User save(@NonNull User user) {
	    return userRepository.save(user);
	}
	
	@SuppressWarnings("unchecked")
    public User save(@NonNull AccessToken result) throws JsonParseException, JsonMappingException, IOException {
	    //根据access_token获取用户的个人信息
	    String json = rest.getForObject(AppConfig.OAuth.Weibo.USER_INFO_URL, String.class, 
	            result.getAccessToken(), result.getUid());
	    HashMap<String, Object> map = mapper.readValue(json, HashMap.class);
	    User user = new User();
	    user.setUuid(CommonUtils.getUUID());
	    user.setUsername((String)map.get("name"));
	    user.setAvatarUrl((String)map.get("profile_image_url"));
	    user.setDescription((String)map.get("description"));
	    user.setUid(result.getUid());
	    user.setAccessToken(result.getAccessToken());
	    user.setCreatedTime(new Date());
	    user.setExtra(EmojiParser.removeAllEmojis(json));  //删除emoji表情
	    user.setToken(CommonUtils.getToken());
	    System.out.println("长度："+json.length() +":"+ user.getUsername() + " " + user.getUid() + " " + user.getDescription());

	    return save(user);
	}
	
    public User findByUid(@NonNull String uid) {
        return userRepository.findByUid(uid);
    }
    
    public User findByToken(@NonNull String token) {
        return userRepository.findByToken(token);
    }
    
    
    public UserDTO getUserInfo(String uuid) {
       UserDTO userDTO = customRepository.getUserInfo(uuid);
       if (userDTO.getUser() == null) {
           return null;
       }
       return userDTO;
    }
    
    

}
