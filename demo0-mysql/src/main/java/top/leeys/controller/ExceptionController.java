package top.leeys.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import top.leeys.exception.BaseException;
import top.leeys.pojo.Message;

@ControllerAdvice
public class ExceptionController {
    
    @ResponseBody
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Message> handleBaseException(BaseException e) {
        return new ResponseEntity<>(new Message(e.code(), e.message()), HttpStatus.OK);
    }
}
