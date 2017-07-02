package top.leeys.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import top.leeys.exception.BaseException;
import top.leeys.pojo.Message;

@RestControllerAdvice
public class ExceptionController {
    
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Message> handleBaseException(BaseException e) {
        return new ResponseEntity<>(new Message(e.code(), e.message()), HttpStatus.OK);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Message> handleAllException(Exception e) {
        return new ResponseEntity<>(new Message(500, "服务器异常"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
