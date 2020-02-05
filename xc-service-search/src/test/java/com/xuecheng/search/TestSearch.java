package com.xuecheng.search;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSearch {

    @Autowired
    private RestHighLevelClient client;

    @Test
    public void testSearchAll() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //设置源字段过滤，前一个参数表示包括哪些字段，后一个字段表示不包括哪些字段
        searchSourceBuilder. fetchSource(new String[]{"name","studymodel","price","timestamp","description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高德文档
        SearchHit[] searchHits = hits.getHits();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit searchHit : searchHits) {
            //文档的主键
            String id = searchHit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String desc = (String) sourceAsMap.get("description");
            Double price = (Double) sourceAsMap.get("price");
            Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(desc);
            System.out.println(price);
            System.out.println(timestamp);
            System.out.println("------------分割线--------------");
        }
    }

    @Test
    public void testSearchPage() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        int page = 1;
        int size = 1;
        //计算出记录的起始下标
        int from = (page - 1) * size;
        searchSourceBuilder.from(from);//起始记录下标，从0开始
        searchSourceBuilder.size(size);//每页显示的记录数

        //设置源字段过滤，前一个参数表示包括哪些字段，后一个字段表示不包括哪些字段
        searchSourceBuilder. fetchSource(new String[]{"name","studymodel","price","timestamp","description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高德文档
        SearchHit[] searchHits = hits.getHits();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit searchHit : searchHits) {
            //文档的主键
            String id = searchHit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String desc = (String) sourceAsMap.get("description");
            Double price = (Double) sourceAsMap.get("price");
            Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(desc);
            System.out.println(price);
            System.out.println(timestamp);
            System.out.println("------------分割线--------------");
        }
    }

    @Test
    public void testSearchTerm() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式
        //精确查询
        searchSourceBuilder.query(QueryBuilders.termQuery("name", "spring"));
        //设置源字段过滤，前一个参数表示包括哪些字段，后一个字段表示不包括哪些字段
        searchSourceBuilder. fetchSource(new String[]{"name","studymodel","price","timestamp","description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高德文档
        SearchHit[] searchHits = hits.getHits();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit searchHit : searchHits) {
            //文档的主键
            String id = searchHit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String desc = (String) sourceAsMap.get("description");
            Double price = (Double) sourceAsMap.get("price");
            Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(desc);
            System.out.println(price);
            System.out.println(timestamp);
            System.out.println("------------分割线--------------");
        }
    }

    @Test
    public void testSearchByIds() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式
        //根据id查询
        //定义id
        String[] ids = new String[]{"1","2"};
        searchSourceBuilder.query(QueryBuilders.termsQuery("_id", ids));
        //设置源字段过滤，前一个参数表示包括哪些字段，后一个字段表示不包括哪些字段
        searchSourceBuilder. fetchSource(new String[]{"name","studymodel","price","timestamp","description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高德文档
        SearchHit[] searchHits = hits.getHits();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit searchHit : searchHits) {
            //文档的主键
            String id = searchHit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String desc = (String) sourceAsMap.get("description");
            Double price = (Double) sourceAsMap.get("price");
            Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(desc);
            System.out.println(price);
            System.out.println(timestamp);
            System.out.println("------------分割线--------------");
        }
    }

    @Test
    public void testMatchQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式
        searchSourceBuilder.query(QueryBuilders.matchQuery("description", "spring开发框架").minimumShouldMatch("60%"));

        //设置源字段过滤，前一个参数表示包括哪些字段，后一个字段表示不包括哪些字段
        searchSourceBuilder. fetchSource(new String[]{"name","studymodel","price","timestamp","description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高德文档
        SearchHit[] searchHits = hits.getHits();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit searchHit : searchHits) {
            //文档的主键
            String id = searchHit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String desc = (String) sourceAsMap.get("description");
            Double price = (Double) sourceAsMap.get("price");
            Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(desc);
            System.out.println(price);
            System.out.println(timestamp);
            System.out.println("------------分割线--------------");
        }
    }


    @Test
    public void testMultiMatchQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery("spring css", "name","description")
                .minimumShouldMatch("50%")
                .field("name",10)
        );
        //设置源字段过滤，前一个参数表示包括哪些字段，后一个字段表示不包括哪些字段
        searchSourceBuilder. fetchSource(new String[]{"name","studymodel","price","timestamp","description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高德文档
        SearchHit[] searchHits = hits.getHits();
        // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit searchHit : searchHits) {
            //文档的主键
            String id = searchHit.getId();
            //源文档内容
            float score = searchHit.getScore();
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String desc = (String) sourceAsMap.get("description");
            Double price = (Double) sourceAsMap.get("price");
            // Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(desc);
            System.out.println(price);
            System.out.println(score);
            // System.out.println(timestamp);
            System.out.println("------------分割线--------------");
        }
    }

    @Test
    public void testBoolQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        int page = 1;
        int size = 1;
        int from = (page - 1) * size;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);

        //搜索方式
        //先定义一个MultiMatchQuery
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring css", "name", "description")
                .minimumShouldMatch("50%")
                .field("name", 10);
        //在定义一个termQuery
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("studymodel", "201001");

        //定义一个boolQuery
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(multiMatchQueryBuilder);
        boolQueryBuilder.must(termQueryBuilder);

        searchSourceBuilder.query(boolQueryBuilder);
        //设置源字段过滤，前一个参数表示包括哪些字段，后一个字段表示不包括哪些字段
        searchSourceBuilder. fetchSource(new String[]{"name","studymodel","price","timestamp","description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高德文档
        SearchHit[] searchHits = hits.getHits();
        // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit searchHit : searchHits) {
            //文档的主键
            String id = searchHit.getId();
            //源文档内容
            float score = searchHit.getScore();
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String desc = (String) sourceAsMap.get("description");
            Double price = (Double) sourceAsMap.get("price");
            // Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(desc);
            System.out.println(price);
            System.out.println(score);
            // System.out.println(timestamp);
            System.out.println("------------分割线--------------");
        }
    }

    @Test
    public void testFilter() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        int page = 1;
        int size = 1;
        int from = (page - 1) * size;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);

        //搜索方式
        //先定义一个MultiMatchQuery
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring css", "name", "description")
                .minimumShouldMatch("50%")
                .field("name", 10);


        //定义一个boolQuery
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(multiMatchQueryBuilder);

        //定义一个过滤器
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel", "201001"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(90).lte(100));

        searchSourceBuilder.query(boolQueryBuilder);
        //设置源字段过滤，前一个参数表示包括哪些字段，后一个字段表示不包括哪些字段
        searchSourceBuilder. fetchSource(new String[]{"name","studymodel","price","timestamp","description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高德文档
        SearchHit[] searchHits = hits.getHits();
        // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit searchHit : searchHits) {
            //文档的主键
            String id = searchHit.getId();
            //源文档内容
            float score = searchHit.getScore();
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String desc = (String) sourceAsMap.get("description");
            Double price = (Double) sourceAsMap.get("price");
            // Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(desc);
            System.out.println(price);
            System.out.println(score);
            // System.out.println(timestamp);
            System.out.println("------------分割线--------------");
        }
    }

    @Test
    public void testSort() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //搜索方式

        //定义一个boolQuery
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //定义一个过滤器
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));

        searchSourceBuilder.query(boolQueryBuilder);

        searchSourceBuilder.sort("studymodel", SortOrder.DESC);
        searchSourceBuilder.sort("price", SortOrder.ASC);

        //设置源字段过滤，前一个参数表示包括哪些字段，后一个字段表示不包括哪些字段
        searchSourceBuilder. fetchSource(new String[]{"name","studymodel","price","timestamp","description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高德文档
        SearchHit[] searchHits = hits.getHits();
        // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit searchHit : searchHits) {
            //文档的主键
            String id = searchHit.getId();
            //源文档内容
            float score = searchHit.getScore();
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String desc = (String) sourceAsMap.get("description");
            Double price = (Double) sourceAsMap.get("price");
            // Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(desc);
            System.out.println(price);
            System.out.println(score);
            // System.out.println(timestamp);
            System.out.println("------------分割线--------------");
        }
    }

    @Test
    public void testHighlight() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //搜索方式
        //先定义一个MultiMatchQuery
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("java开发框架", "name", "description")
                .minimumShouldMatch("50%")
                .field("name", 10);
        //定义一个boolQuery
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(multiMatchQueryBuilder);

        //定义过滤器
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));
        //设置排序
        searchSourceBuilder.sort("studymodel", SortOrder.DESC);
        searchSourceBuilder.sort("price", SortOrder.ASC);

        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<tag>");
        highlightBuilder.postTags("</tag>");
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        highlightBuilder.fields().add(new HighlightBuilder.Field("description"));
        searchSourceBuilder.highlighter(highlightBuilder);

        searchSourceBuilder.query(boolQueryBuilder);
        //设置源字段过滤，前一个参数表示包括哪些字段，后一个字段表示不包括哪些字段
        searchSourceBuilder. fetchSource(new String[]{"name","studymodel","price","timestamp","description"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高德文档
        SearchHit[] searchHits = hits.getHits();
        // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit searchHit : searchHits) {
            //文档的主键
            String id = searchHit.getId();
            //源文档内容
            float score = searchHit.getScore();
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            //源文档的name字段内容
            String name = (String) sourceAsMap.get("name");
            String desc = (String) sourceAsMap.get("description");

            //取出高亮字段
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            if (highlightFields != null) {
                HighlightField nameHighlight = highlightFields.get("name");
                HighlightField descriptionHighlight = highlightFields.get("description");
                if (nameHighlight != null) {
                    Text[] fragments = nameHighlight.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (Text fragment : fragments) {
                        stringBuffer.append(fragment.toString());
                    }
                    name = stringBuffer.toString();
                }

                if (descriptionHighlight != null) {
                    Text[] descriptionHighlightFragments = descriptionHighlight.getFragments();
                    StringBuffer sb = new StringBuffer();
                    for (Text descriptionHighlightFragment : descriptionHighlightFragments) {
                        sb.append(descriptionHighlightFragment.toString());
                    }
                    desc = sb.toString();
                }
            }

            Double price = (Double) sourceAsMap.get("price");
            // Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(desc);
            System.out.println(price);
            // System.out.println(score);
            // System.out.println(timestamp);
            System.out.println("------------分割线--------------");
        }
    }
}
