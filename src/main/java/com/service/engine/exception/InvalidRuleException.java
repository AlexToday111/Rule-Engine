package com.service.engine.exception;

public class InvalidRuleException extends RuntimeException{
    public InvalidRuleException(String message){
        super(message);
    }
}
