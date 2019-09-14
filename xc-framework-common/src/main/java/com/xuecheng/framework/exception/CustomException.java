package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自定义异常类型
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomException extends RuntimeException{

    //错误代码
    private ResultCode resultCode;
}
