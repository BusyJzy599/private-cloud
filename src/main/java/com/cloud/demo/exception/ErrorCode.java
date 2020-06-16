package com.cloud.demo.exception;

public enum ErrorCode implements InterErrorCode {
    PASSWORD_ERROR(2002,"输入密码错误"),
    PASSWORD_NULL(2003,"密码不为空！"),
    FAILED_DELETE(2004,"删除失败"),
    FAILED_ADD(2005,"添加失败"),
    FAILED_UPLOAD(2005,"上传失败"),
    ERROR_UNDEFINED(2006,"未知错误"),
    FAILED_CHANGE(2007,"修改失败"),
    ;
    private Integer code;
    private String message;

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
