package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EsCourseService {

    @Value("${xuecheng.course.index}")
    private String index;
    @Value("${xuecheng.course.type}")
    private String type;
    @Value("${xuecheng.course.source_field}")
    private String source_field;
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //课程搜索
    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {

        if (courseSearchParam == null) {
            courseSearchParam = new CourseSearchParam();
        }

        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest(index);
        //设置搜索类型
        searchRequest.types(type);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //过滤源字段
        String[] source_field_array = source_field.split(",");
        sourceBuilder.fetchSource(source_field_array,new String[]{});

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //搜索条件
        //根据关键字来搜索
        if (StringUtils.isNotEmpty(courseSearchParam.getKeyword())) {
            MultiMatchQueryBuilder multiMatchQueryBuilder = new MultiMatchQueryBuilder(courseSearchParam.getKeyword(),"name","description","teachplan")
                    .minimumShouldMatch("70%")
                    .field("name", 10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }

        //过滤
        //根据分类搜索课程信息
        //根据课程难度搜索课程信息
        if (StringUtils.isNotEmpty(courseSearchParam.getMt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt", courseSearchParam.getMt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getSt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("st", courseSearchParam.getSt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getGrade())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade", courseSearchParam.getGrade()));
        }

        sourceBuilder.query(boolQueryBuilder);
        //设置分页参数
        if (page <= 0) {
            page = 1;
        }
        if (size <= 0) {
            size = 12;
        }
        int from = (page - 1) * size;
        //起始记录下标
        sourceBuilder.from(from);
        sourceBuilder.size(size);

        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        //设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        sourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(sourceBuilder);

        QueryResult<CoursePub> queryResult = new QueryResult<>();
        List<CoursePub> list = new ArrayList<>();
        //执行搜索
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            //获取响应结果
            SearchHits hits = searchResponse.getHits();
            //匹配的总记录数
            long totalHits = hits.getTotalHits();
            queryResult.setTotal(totalHits);
            //匹配的结果
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit searchHit : searchHits) {
                //获得源文档
                CoursePub coursePub = new CoursePub();
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                //取出id
                String id = (String) sourceAsMap.get("id");
                coursePub.setId(id);

                String name = (String) sourceAsMap.get("name");

                //取出高亮字段
                Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                if (highlightFields != null) {
                    HighlightField highlightFieldName = highlightFields.get("name");
                    if (highlightFieldName != null) {
                        Text[] fragments = highlightFieldName.fragments();
                        StringBuffer stringBuffer = new StringBuffer();
                        for (Text fragment : fragments) {
                            stringBuffer.append(fragment);
                        }
                        name = stringBuffer.toString();
                    }
                }
                coursePub.setName(name);
                //图片
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                Double price = null;
                if (sourceAsMap.get("price") != null) {
                    price = (Double) sourceAsMap.get("price");
                }
                coursePub.setPrice(price);

                Double price_old = null;
                if (sourceAsMap.get("price_old") != null) {
                    price_old = (Double) sourceAsMap.get("price_old");
                }
                coursePub.setPrice_old(price_old);
                list.add(coursePub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        queryResult.setList(list);
        return new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
    }
}
