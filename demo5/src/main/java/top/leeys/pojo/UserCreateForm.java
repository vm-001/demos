package top.leeys.pojo;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;


/**
 * 根据需要做正则表达式校验
 */
@Data
public class UserCreateForm {
    
    @NotBlank
//    @Pattern(regexp = "")
    private String email;
    
    @NotBlank
    private String emailCode;
    
    @NotBlank
    private String name;
    
    @NotBlank
//    @Pattern(regexp = "")
    private String password;
}
