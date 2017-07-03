package top.leeys.pojo;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Data
public class OrderCreateForm {
    @NotBlank(message = "签收人不能为空")
    private String consignee;
    
    @NotBlank(message = "地址不能为空")
    private String address;
    
    private String phone;
    
    private String deliveryType;
}
