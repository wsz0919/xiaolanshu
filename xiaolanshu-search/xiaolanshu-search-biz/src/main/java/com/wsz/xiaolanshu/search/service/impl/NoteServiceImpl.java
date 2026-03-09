package com.wsz.xiaolanshu.search.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import com.wsz.framework.common.constant.DateConstants;
import com.wsz.framework.common.response.PageResponse;
import com.wsz.framework.common.response.Response;
import com.wsz.framework.common.util.DateUtils;
import com.wsz.framework.common.util.JsonUtils;
import com.wsz.framework.common.util.NumberUtils;
import com.wsz.xiaolanshu.search.constant.RedisConstants;
import com.wsz.xiaolanshu.search.domain.vo.SearchNoteReqVO;
import com.wsz.xiaolanshu.search.domain.vo.SearchNoteRspVO;
import com.wsz.xiaolanshu.search.dto.RebuildNoteDocumentReqDTO;
import com.wsz.xiaolanshu.search.dto.SearchNoteDTO;
import com.wsz.xiaolanshu.search.enums.NotePublishTimeRangeEnum;
import com.wsz.xiaolanshu.search.enums.NoteSortTypeEnum;
import com.wsz.xiaolanshu.search.index.NoteIndex;
import com.wsz.xiaolanshu.search.mapper.SelectMapper;
import com.wsz.xiaolanshu.search.service.NoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FieldValueFactorFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 笔记搜索业务实现
 */
@Service
@Slf4j
public class NoteServiceImpl implements NoteService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Resource
    private SelectMapper selectMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 一级本地缓存 (Caffeine)
     * 针对热点搜索词，设置较短的过期时间，防止瞬时并发击穿 Redis
     */
    private static final Cache<String, PageResponse<SearchNoteRspVO>> SEARCH_NOTE_LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.MINUTES) // 本地缓存 1 分钟
            .build();

    /**
     * 搜索笔记 (带多级缓存)
     */
    @Override
    public PageResponse<SearchNoteRspVO> searchNote(SearchNoteReqVO searchNoteReqVO) {
        // 1. 构建复合缓存 Key
        String cacheKeyPart = buildSearchCacheKey(searchNoteReqVO);

        // 2. 尝试从本地缓存获取
        PageResponse<SearchNoteRspVO> localResult = SEARCH_NOTE_LOCAL_CACHE.getIfPresent(cacheKeyPart);
        if (Objects.nonNull(localResult)) {
            log.info("==> 命中本地缓存, key: {}", cacheKeyPart);
            return localResult;
        }

        // 3. 尝试从 Redis 二级缓存获取
        String redisKey = RedisConstants.SEARCH_NOTE_KEY_PREFIX + cacheKeyPart;
        String redisValue = (String) redisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isNotBlank(redisValue)) {
            log.info("==> 命中 Redis 缓存, key: {}", redisKey);
            PageResponse<SearchNoteRspVO> redisResult = JsonUtils.parseObject(redisValue, PageResponse.class);
            if (Objects.nonNull(redisResult)) {
                // 回填本地缓存
                SEARCH_NOTE_LOCAL_CACHE.put(cacheKeyPart, redisResult);
                return redisResult;
            }
        }

        // 4. 缓存未命中，执行 Elasticsearch 搜索
        PageResponse<SearchNoteRspVO> searchResult = executeSearchFromEs(searchNoteReqVO);

        // 5. 将有效结果写入缓存
        if (searchResult.isSuccess() && CollUtil.isNotEmpty(searchResult.getData())) {
            // 写入 Redis，设置 5 分钟有效期
            redisTemplate.opsForValue().set(redisKey, JsonUtils.toJsonString(searchResult), 5, TimeUnit.MINUTES);
            // 写入本地缓存
            SEARCH_NOTE_LOCAL_CACHE.put(cacheKeyPart, searchResult);
        }

        return searchResult;
    }

    /**
     * 核心 Elasticsearch 查询逻辑
     */
    private PageResponse<SearchNoteRspVO> executeSearchFromEs(SearchNoteReqVO searchNoteReqVO) {
        String keyword = searchNoteReqVO.getKeyword();
        Integer pageNo = searchNoteReqVO.getPageNo();
        Integer type = searchNoteReqVO.getType();
        Integer sort = searchNoteReqVO.getSort();
        Integer publishTimeRange = searchNoteReqVO.getPublishTimeRange();

        SearchRequest searchRequest = new SearchRequest(NoteIndex.NAME);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 构建基础查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(
                QueryBuilders.multiMatchQuery(keyword)
                        .field(NoteIndex.FIELD_NOTE_TITLE, 2.0f)
                        .field(NoteIndex.FIELD_NOTE_TOPIC)
        );

        // 过滤：笔记类型
        if (Objects.nonNull(type)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(NoteIndex.FIELD_NOTE_TYPE, type));
        }

        // 过滤：发布时间范围
        NotePublishTimeRangeEnum rangeEnum = NotePublishTimeRangeEnum.valueOf(publishTimeRange);
        if (Objects.nonNull(rangeEnum)) {
            String endTime = LocalDateTime.now().format(DateConstants.DATE_FORMAT_Y_M_D_H_M_S);
            String startTime = null;
            switch (rangeEnum) {
                case DAY -> startTime = DateUtils.localDateTime2String(LocalDateTime.now().minusDays(1));
                case WEEK -> startTime = DateUtils.localDateTime2String(LocalDateTime.now().minusWeeks(1));
                case HALF_YEAR -> startTime = DateUtils.localDateTime2String(LocalDateTime.now().minusMonths(6));
            }
            if (StringUtils.isNoneBlank(startTime)) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery(NoteIndex.FIELD_NOTE_CREATE_TIME).gte(startTime).lte(endTime));
            }
        }

        // 排序逻辑
        NoteSortTypeEnum sortTypeEnum = NoteSortTypeEnum.valueOf(sort);
        if (Objects.nonNull(sortTypeEnum)) {
            switch (sortTypeEnum) {
                case LATEST -> sourceBuilder.sort(new FieldSortBuilder(NoteIndex.FIELD_NOTE_CREATE_TIME).order(SortOrder.DESC));
                case MOST_LIKE -> sourceBuilder.sort(new FieldSortBuilder(NoteIndex.FIELD_NOTE_LIKE_TOTAL).order(SortOrder.DESC));
                case MOST_COMMENT -> sourceBuilder.sort(new FieldSortBuilder(NoteIndex.FIELD_NOTE_COMMENT_TOTAL).order(SortOrder.DESC));
                case MOST_COLLECT -> sourceBuilder.sort(new FieldSortBuilder(NoteIndex.FIELD_NOTE_COLLECT_TOTAL).order(SortOrder.DESC));
            }
            sourceBuilder.query(boolQueryBuilder);
        } else {
            // 综合排序：Function Score 评分
            sourceBuilder.sort(new FieldSortBuilder("_score").order(SortOrder.DESC));
            FunctionScoreQueryBuilder.FilterFunctionBuilder[] functions = new FunctionScoreQueryBuilder.FilterFunctionBuilder[] {
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder(new FieldValueFactorFunctionBuilder(NoteIndex.FIELD_NOTE_LIKE_TOTAL).factor(0.5f).modifier(FieldValueFactorFunction.Modifier.SQRT).missing(0)),
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder(new FieldValueFactorFunctionBuilder(NoteIndex.FIELD_NOTE_COLLECT_TOTAL).factor(0.3f).modifier(FieldValueFactorFunction.Modifier.SQRT).missing(0)),
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder(new FieldValueFactorFunctionBuilder(NoteIndex.FIELD_NOTE_COMMENT_TOTAL).factor(0.2f).modifier(FieldValueFactorFunction.Modifier.SQRT).missing(0))
            };
            sourceBuilder.query(QueryBuilders.functionScoreQuery(boolQueryBuilder, functions).scoreMode(FunctionScoreQuery.ScoreMode.SUM).boostMode(CombineFunction.SUM));
        }

        // 分页设置
        int pageSize = 10;
        sourceBuilder.from((pageNo - 1) * pageSize).size(pageSize);

        // 高亮设置
        sourceBuilder.highlighter(new HighlightBuilder().field(NoteIndex.FIELD_NOTE_TITLE).preTags("<strong>").postTags("</strong>"));

        searchRequest.source(sourceBuilder);
        List<SearchNoteRspVO> results = Lists.newArrayList();
        long total = 0;

        try {
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            total = response.getHits().getTotalHits().value;
            for (SearchHit hit : response.getHits()) {
                Map<String, Object> map = hit.getSourceAsMap();
                String highlight = null;
                if (hit.getHighlightFields().containsKey(NoteIndex.FIELD_NOTE_TITLE)) {
                    highlight = hit.getHighlightFields().get(NoteIndex.FIELD_NOTE_TITLE).fragments()[0].string();
                }

                results.add(SearchNoteRspVO.builder()
                        .noteId(Long.valueOf(map.get(NoteIndex.FIELD_NOTE_ID).toString()))
                        .cover((String) map.get(NoteIndex.FIELD_NOTE_COVER))
                        .title((String) map.get(NoteIndex.FIELD_NOTE_TITLE))
                        .videoUri((String) map.get(NoteIndex.FIELD_NOTE_VIDEO))
                        .highlightTitle(highlight)
                        .avatar((String) map.get(NoteIndex.FIELD_NOTE_AVATAR))
                        .nickname((String) map.get(NoteIndex.FIELD_NOTE_NICKNAME))
                        .updateTime(DateUtils.formatRelativeTime(LocalDateTime.parse((String) map.get(NoteIndex.FIELD_NOTE_UPDATE_TIME), DateConstants.DATE_FORMAT_Y_M_D_H_M_S)))
                        .likeTotal(NumberUtils.formatNumberString((Integer) map.get(NoteIndex.FIELD_NOTE_LIKE_TOTAL)))
                        .commentTotal(NumberUtils.formatNumberString((Integer) map.get(NoteIndex.FIELD_NOTE_COMMENT_TOTAL)))
                        .collectTotal(NumberUtils.formatNumberString((Integer) map.get(NoteIndex.FIELD_NOTE_COLLECT_TOTAL)))
                        .build());
            }
        } catch (IOException e) {
            log.error("==> ES 搜索异常: ", e);
        }

        return PageResponse.success(results, pageNo, total);
    }

    /**
     * 生成缓存 Key
     */
    private String buildSearchCacheKey(SearchNoteReqVO vo) {
        return String.format("%s:%d:%s:%s:%s",
                vo.getKeyword(),
                vo.getPageNo(),
                Objects.toString(vo.getType(), "all"),
                Objects.toString(vo.getSort(), "default"),
                Objects.toString(vo.getPublishTimeRange(), "any"));
    }

    @Override
    public Response<Long> rebuildDocument(RebuildNoteDocumentReqDTO dto) {
        List<Map<String, Object>> result = selectMapper.selectEsNoteIndexData(dto.getId(), null);
        for (Map<String, Object> map : result) {
            IndexRequest request = new IndexRequest(NoteIndex.NAME)
                    .id(String.valueOf(map.get(NoteIndex.FIELD_NOTE_ID)))
                    .source(map);
            try {
                restHighLevelClient.index(request, RequestOptions.DEFAULT);
            } catch (IOException e) {
                log.error("==> 重建笔记文档失败: ", e);
            }
        }
        return Response.success();
    }

    @Override
    public Response<List<SearchNoteDTO>> searchNotesByIds(List<Long> noteIds) {
        if (CollUtil.isEmpty(noteIds)) {
            return Response.success(Lists.newArrayList());
        }

        SearchRequest searchRequest = new SearchRequest(NoteIndex.NAME);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 构建 terms 查询 (等同于 SQL 的 in 操作)
        sourceBuilder.query(QueryBuilders.termsQuery(NoteIndex.FIELD_NOTE_ID, noteIds));
        // 设置 size 为传入 ID 的数量，确保能查出全部
        sourceBuilder.size(noteIds.size());

        searchRequest.source(sourceBuilder);

        List<SearchNoteDTO> results = Lists.newArrayList();
        try {
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            for (SearchHit hit : response.getHits()) {
                Map<String, Object> map = hit.getSourceAsMap();
                SearchNoteDTO dto = SearchNoteDTO.builder()
                        .noteId(((Number) map.get(NoteIndex.FIELD_NOTE_ID)).longValue())
                        .title((String) map.get(NoteIndex.FIELD_NOTE_TITLE))
                        .type((Integer) map.get(NoteIndex.FIELD_NOTE_TYPE))
                        .cover((String) map.get(NoteIndex.FIELD_NOTE_COVER))
                        .videoUri((String) map.get(NoteIndex.FIELD_NOTE_VIDEO))
                        .creatorId(((Number) map.get(NoteIndex.FIELD_NOTE_CREATOR_ID)).longValue())
                        .nickname((String) map.get(NoteIndex.FIELD_NOTE_NICKNAME))
                        .avatar((String) map.get(NoteIndex.FIELD_NOTE_AVATAR))
                        .likeTotal(((Number) map.getOrDefault(NoteIndex.FIELD_NOTE_LIKE_TOTAL, 0)).intValue())
                        .build();
                results.add(dto);
            }
        } catch (Exception e) {
            log.error("==> ES 批量查询笔记失败: ", e);
        }
        return Response.success(results);
    }
}