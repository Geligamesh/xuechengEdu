package com.xuecheng.framework.domain.learning.response;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GetMediaResult extends ResponseResult {

    //视频播放地址
    private String fileUrl;

    public GetMediaResult(ResultCode resultCode,String fileUrl) {
        super(resultCode);
        this.fileUrl = fileUrl;
    }
}
