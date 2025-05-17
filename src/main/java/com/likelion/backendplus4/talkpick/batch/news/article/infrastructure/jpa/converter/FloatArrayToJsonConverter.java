package com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.converter.exception.JpaConvertorException;
import com.likelion.backendplus4.talkpick.batch.news.article.infrastructure.jpa.converter.exception.error.JpaConvertorErrorCode;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * {@link AttributeConverter} 구현체로, float 배열(float[])을 JSON 문자열로 직렬화하거나
 * JSON 문자열을 float 배열로 역직렬화하여 MySQL JSON 타입 컬럼과 매핑한다.
 *
 * <p>직렬화/역직렬화 과정에서 오류가 발생하면 {@link JpaConvertorException}을 발생시키며,
 * 오류 유형은 {@link JpaConvertorErrorCode#JSON_CONVERT_ERROR}로 정의된다.
 *
 * <p>MySQL에는 배열 타입이 없으므로, 배열 데이터를 JSON 형태로 저장하고 읽어오는 데 유용하다.
 *
 * @since 2025-05-17
 */
@Converter
public class FloatArrayToJsonConverter implements AttributeConverter<float[], String> {

	private final ObjectMapper objectMapper = new ObjectMapper();


	/**
	 * float 배열을 JSON 문자열로 변환하여 DB에 저장한다.
	 *
	 * @param attribute float 배열
	 * @return JSON 문자열
	 * @author 함예정
	 * @since 2025-05-17
	 */
	@Override
	public String convertToDatabaseColumn(float[] attribute) {
		if (attribute == null) {
			return null;
		}
		return toStringFromFloatArray(attribute);
	}

	/**
	 * DB에서 조회된 JSON 문자열을 float 배열로 변환하여 엔티티에 주입한다.
	 *
	 * @param dbData DB에서 조회된 JSON 문자열
	 * @return float 배열
	 * @author 함예정
	 * @since 2025-05-17
	 */
	@Override
	public float[] convertToEntityAttribute(String dbData) {
		if (isNullDbData(dbData)) {
			return new float[0];
		}
		return toFloatArrayFromString(dbData);
	}

	/**
	 * dbData가 null 또는 빈 문자열인지 확인한다.
	 *
	 * @return boolean
	 * @author 함예정
	 * @since 2025-05-17
	 */
	private boolean isNullDbData(String dbData) {
		return dbData == null || dbData.isEmpty();
	}

	/**
	 * float 배열을 JSON 문자열로 직렬화한다.
	 *
	 * @return Json 문자열
	 * @author 함예정
	 * @since 2025-05-17
	 */
	private String toStringFromFloatArray(float[] attribute) {
		try {
			return objectMapper.writeValueAsString(attribute);
		} catch (JsonProcessingException e) {
			throw new JpaConvertorException(JpaConvertorErrorCode.JSON_CONVERT_ERROR, e);
		}
	}

	/**
	 * JSON 문자열을 float 배열로 역직렬화한다.
	 *
	 * @return float 배열
	 * @author 함예정
	 * @since 2025-05-17
	 */
	private float[] toFloatArrayFromString(String dbData) {
		try {
			return objectMapper.readValue(dbData, float[].class);
		} catch (JsonProcessingException e) {
			throw new JpaConvertorException(JpaConvertorErrorCode.JSON_CONVERT_ERROR, e);
		}
	}

}