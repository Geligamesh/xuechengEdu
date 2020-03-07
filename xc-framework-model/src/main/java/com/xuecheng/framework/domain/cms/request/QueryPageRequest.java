package com.xuecheng.framework.domain.cms.request;

import com.xuecheng.framework.model.response.QueryResult;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class QueryPageRequest extends QueryResult {
    //接收页面查询的查询条件
    //站点Id
    @ApiModelProperty("站点id")
    private String siteId;
    //页面id
    @ApiModelProperty("页面id")
    private String pageId;
    //页面名称
    @ApiModelProperty("页面名称")
    private String pageName;
    //别名
    @ApiModelProperty("页面别名")
    private String pageAliase;
    //模板id
    @ApiModelProperty("模板id")
    private String templateId;
    //页面类型
    @ApiModelProperty("页面类型")
    private String pageType;
}
