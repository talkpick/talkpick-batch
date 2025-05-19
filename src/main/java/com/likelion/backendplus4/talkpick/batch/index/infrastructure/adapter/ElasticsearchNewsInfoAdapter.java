package com.likelion.backendplus4.talkpick.batch.index.infrastructure.adapter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.IndexedObjectInformation;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.BulkOptions;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Component;

import com.likelion.backendplus4.talkpick.batch.index.application.port.out.NewsInfoIndexRepositoryPort;
import com.likelion.backendplus4.talkpick.batch.index.domain.model.NewsInfo;
import com.likelion.backendplus4.talkpick.batch.index.infrastructure.adapter.document.NewsInfoDocument;
import com.likelion.backendplus4.talkpick.batch.index.infrastructure.adapter.mapper.NewsInfoDocumentMapper;

import jakarta.annotation.PostConstruct;

/**
 * Spring Data Elasticsearch를 이용해 뉴스 정보를 Bulk 색인하고 저장된 개수를 반환하는 어댑터
 *
 * @since 2025-05-15
 * @modified 2025-05-19
 */
@Component
public class ElasticsearchNewsInfoAdapter implements NewsInfoIndexRepositoryPort {

	private final ElasticsearchOperations esOperations;
	private final NewsInfoDocumentMapper mapper;
	private final String indexName;
	private IndexOperations indexOperations;

	public ElasticsearchNewsInfoAdapter(ElasticsearchOperations esOperations,
		NewsInfoDocumentMapper mapper,
		@Value("${news.index.name}") String indexName) {
		this.esOperations = esOperations;
		this.mapper = mapper;
		this.indexName = indexName;
	}

	/**
	 * 초기화 단계에서 인덱스를 준비하고 존재하지 않으면 생성한다.
	 *
	 * @author 정안식
	 * @since 2025-05-15
	 */
	@PostConstruct
	public void initIndex() {
		this.indexOperations = esOperations.indexOps(IndexCoordinates.of(indexName));
		ensureIndexExists(this.indexOperations);
	}

	/**
	 * 뉴스 정보 리스트를 Bulk 색인하고 색인된 개수를 반환한다.
	 *
	 * @param newsList 색인할 뉴스 정보 리스트
	 * @return 색인된 객체 정보 리스트의 크기
	 * @author 정안식
	 * @since 2025-05-15
	 */
	@Override
	public int saveAll(List<NewsInfo> newsList) {
		List<IndexQuery> queries = toIndexQueries(newsList);
		List<IndexedObjectInformation> result = bulkIndex(indexOperations, queries);

		return result.size();
	}

	/**
	 * 인덱스가 없으면 생성하고 매핑을 설정한다.
	 *
	 * @param ops 인덱스 운영 객체
	 * @author 정안식
	 * @since 2025-05-15
	 */
	private void ensureIndexExists(IndexOperations ops) {
		try {
			if (!ops.exists()) {
				ops.create();
				ops.putMapping(Document
					.create()
					.append("properties", mappingProperties()));
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to create or map index [" + indexName + "]", e);
		}
	}

	/**
	 * 문서 매핑에 사용할 Elasticsearch 프로퍼티 맵을 반환한다.
	 *
	 * @return 매핑 프로퍼티 맵
	 * @author 정안식
	 * @since 2025-05-15
	 * @modified 2025-05-19
	 * 25-05-19 - summary 및 summary_vector 필드 추가
	 */
	private Map<String, Object> mappingProperties() {
		return Map.ofEntries(
			Map.entry(NewsInfoDocument.FIELD_ID, Map.of(
				"type", "keyword")),
			Map.entry(NewsInfoDocument.FIELD_TITLE, Map.of(
				"type", "text",
				"analyzer", NewsInfoDocument.ANALYZER_NORI,
				"fields", Map.of(NewsInfoDocument.FIELD_KEYWORD, Map.of("type", "keyword")))),
			Map.entry(NewsInfoDocument.FIELD_CONTENT, Map.of(
				"type", "text",
				"analyzer", NewsInfoDocument.ANALYZER_NORI,
				"fields", Map.of(NewsInfoDocument.FIELD_KEYWORD, Map.of("type", "keyword")))),
			Map.entry(NewsInfoDocument.FIELD_PUBLISHED_AT, Map.of(
				"type", "date")),
			Map.entry(NewsInfoDocument.FIELD_IMAGE_URL, Map.of(
				"type", "keyword")),
			Map.entry(NewsInfoDocument.FIELD_CATEGORY, Map.of(
				"type", "keyword")),
			Map.entry(NewsInfoDocument.FIELD_SUMMARY, Map.of(
				"type", "text",
				"analyzer", NewsInfoDocument.ANALYZER_NORI,
				"fields", Map.of(NewsInfoDocument.FIELD_KEYWORD, Map.of("type", "keyword")))),
			Map.entry(NewsInfoDocument.FIELD_SUMMARY_VECTOR, Map.of(
				"type", "dense_vector",
				"dims", 1536,
				"index", true,
				"similarity", "cosine"))
		);
	}

	/**
	 * 도메인 객체를 Elasticsearch 색인 쿼리로 변환한다.
	 *
	 * @param newsList 도메인 객체 리스트
	 * @return 색인 쿼리 리스트
	 * @author 정안식
	 * @since 2025-05-15
	 */
	private List<IndexQuery> toIndexQueries(List<NewsInfo> newsList) {
		return newsList.stream()
			.map(n -> new IndexQueryBuilder()
				.withId(n.getNewsId())
				.withObject(mapper.toDocument(n))
				.build())
			.collect(Collectors.toList());
	}

	/**
	 * Bulk 옵션을 사용해 쿼리를 실행하고 결과 정보를 반환한다.
	 *
	 * @param indexOperations 인덱스 운영 객체
	 * @param queries 색인 쿼리 리스트
	 * @return 색인 결과 객체 정보 리스트
	 * @author 정안식
	 * @since 2025-05-15
	 */
	private List<IndexedObjectInformation> bulkIndex(IndexOperations indexOperations,
		List<IndexQuery> queries) {

		try {
			BulkOptions bulkOptions = BulkOptions.builder()
				.withRefreshPolicy(RefreshPolicy.NONE)
				.build();

			return esOperations.bulkIndex(
				queries,
				bulkOptions,
				indexOperations.getIndexCoordinates()
			);
		} catch (Exception e) {
			throw new RuntimeException("Failed to bulk index documents into [" + indexName + "]", e);
		}
	}
}
