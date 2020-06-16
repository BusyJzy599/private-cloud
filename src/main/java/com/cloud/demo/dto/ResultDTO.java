package com.cloud.demo.dto;


import com.cloud.demo.exception.InterErrorCode;
import lombok.Data;

/**
 * 用户返回json结果
 */
@Data
public class ResultDTO<T>{
    private Integer code;
    private String message;
    private T data;

    public static ResultDTO error(Integer code, String message){
        ResultDTO resultDTO = new ResultDTO();
        resultDTO.setCode(code);
        resultDTO.setMessage(message);
        return resultDTO;
    }

    public static  ResultDTO error(InterErrorCode interErrorCode){
        return  ResultDTO.error(interErrorCode.getCode(),interErrorCode.getMessage());
    }

    public static  ResultDTO success() {
         ResultDTO resultDTO = new  ResultDTO();
        resultDTO.setCode(200);
        resultDTO.setMessage("请求成功");
        return resultDTO;
    }

    public static <T>  ResultDTO success(T t) {
         ResultDTO resultDTO = new  ResultDTO();
        resultDTO.setCode(200);
        resultDTO.setMessage("请求成功");
        resultDTO.setData(t);
        return null;
    }
}
