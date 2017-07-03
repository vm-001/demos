package top.leeys.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DataBindingException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import top.leeys.config.ErrorCode;
import top.leeys.exception.BaseException;
import top.leeys.pojo.Message;


/**
 * 处理所有处理器抛出的异常
 * 其实可以只处理一个BaseExceptin, 因为错误信息已经封装在了BaseException
 * @author leeys.top@gmail.com
 *
 */
@RestControllerAdvice
@Slf4j
public class ExceptionController {
	/**
	 * 处理全局数据绑定异常
	 */
	@ExceptionHandler(DataBindingException.class)
	public ResponseEntity<Message> handleDataBindingException(BaseException e) {
        Message message = new Message(e.code(), e.message());
        return ResponseEntity.ok(message);
	}
	
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Message> handleBaseException(BaseException e) {
        return ResponseEntity.ok(new Message(e.code(), e.message()));
    }
	
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Message> handleParameterException(MissingServletRequestParameterException e) {
        log.error("参数不正确", e);
        Message message = new Message(ErrorCode.PARAMS_ERROR, "参数错误");
        return ResponseEntity.ok(message);
    } 
    
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Message> handleAllException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
	    log.error("服务器内部异常：", e);
	    Message message = new Message(500, "服务器内部错误");
	    return ResponseEntity.ok(message);
	}
	
}
