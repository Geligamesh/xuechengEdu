package com.xuecheng.framework.domain.cms.response;

import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CmsPostPageResult extends ResponseResult {

    private String pageUrl;
    public CmsPostPageResult(CommonCode commonCode,String pageUrl) {
        super(commonCode);
        this.pageUrl = pageUrl;
    }
}

