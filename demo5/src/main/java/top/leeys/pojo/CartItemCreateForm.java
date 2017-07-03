package top.leeys.pojo;

import javax.validation.constraints.Min;

import lombok.Data;

@Data
public class CartItemCreateForm {
    int id;
    @Min(value = 1, message = "商品数量不能为空")
    int num;
}
