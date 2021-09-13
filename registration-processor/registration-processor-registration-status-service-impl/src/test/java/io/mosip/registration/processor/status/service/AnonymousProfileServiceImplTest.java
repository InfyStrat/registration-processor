package io.mosip.registration.processor.status.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.QualityType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.entities.Entry;
import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonMappingException;
import io.mosip.kernel.core.util.exception.JsonParseException;
import io.mosip.registration.processor.core.exception.ApisResourceAccessException;
import io.mosip.registration.processor.core.exception.PacketManagerException;
import io.mosip.registration.processor.core.packet.dto.Document;
import io.mosip.registration.processor.core.packet.dto.FieldValue;
import io.mosip.registration.processor.status.entity.AnonymousProfileEntity;
import io.mosip.registration.processor.status.exception.TablenotAccessibleException;
import io.mosip.registration.processor.status.repositary.BaseRegProcRepository;
import io.mosip.registration.processor.status.service.impl.AnonymousProfileServiceImpl;
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest({JsonUtils.class, DateUtils.class})
public class AnonymousProfileServiceImplTest {

	@Mock
	private BaseRegProcRepository<AnonymousProfileEntity, String> anonymousProfileRepository;
	
	@InjectMocks
	private AnonymousProfileService anonymousProfileService =new AnonymousProfileServiceImpl();
	
	@Mock
	ObjectMapper mapper;
	
	@Mock 
	RestTemplate restTemplate;

	Map<String, String> fieldMap = new HashedMap();
	Map<String, String> metaInfoMap = new HashedMap();
	BiometricRecord biometricRecord = new BiometricRecord();
	
	@Before
	public void setup() {
		ReflectionTestUtils.setField(anonymousProfileService, "mandatoryLanguages", Arrays.asList("eng"));
		ReflectionTestUtils.setField(anonymousProfileService, "configServerFileStorageURL", "http://dev.mosip.net/");
		ReflectionTestUtils.setField(anonymousProfileService, "getRegProcessorIdentityJson", "registration-processor-identity.json");
		ReflectionTestUtils.setField(anonymousProfileService, "dobFormat", "yyyy/MM/dd");

		PowerMockito.mockStatic(DateUtils.class);
		PowerMockito.when(DateUtils.getUTCCurrentDateTimeString()).thenReturn("2021-09-12T06:50:19.517872400Z");
		PowerMockito.when(DateUtils.parseDateToLocalDateTime(any())).thenReturn(LocalDateTime.of(1998, 01,01, 01, 01));

		fieldMap.put("postalCode", "14022");
		fieldMap.put("dateOfBirth", "1998/01/01");
		fieldMap.put("preferredLang", "English");
		fieldMap.put("phone", "6666666666");
		fieldMap.put("gender",
				"[ {\"language\" : \"eng\",\"value\" : \"Female\"}, {\"language\" : \"ara\",\"value\" : \"أنثى\"} ]");
		fieldMap.put("zone",
				"[ {\"language\" : \"eng\",\"value\" : \"Ben Mansour\"}, {\"language\" : \"ara\",\"value\" : \"بن منصور\"} ]");

		metaInfoMap.put("documents", "[{\"documentType\" : \"CIN\"},{\"documentType\" : \"RNC\"}]");
		metaInfoMap.put("metaData",
				"[{\"label\" : \"registrationType\",\"value\" : \"NEW\"},{\"label\" : \"centerId\",\"value\" : \"1003\"}]");
		metaInfoMap.put("operationsData",
				"[{\"label\" : \"officerId\",\"value\" : \"110024\"},{\"label\" : \"officerBiometricFileName\",\"value\" : \"null\"}]");
		metaInfoMap.put("creationDate", "2021-09-01T03:48:49.193Z");

		BIR bir = new BIR();
		BDBInfo bdbInfo = new BDBInfo();
		bdbInfo.setType(Arrays.asList(BiometricType.FINGER));
		bdbInfo.setSubtype(Arrays.asList("Left", "RingFinger"));
		QualityType quality = new QualityType();
		quality.setScore((long) 80);
		bdbInfo.setQuality(quality);
		bir.setBdbInfo(bdbInfo);
		bir.setSb("eyJ4NWMiOlsiTUlJRGtEQ0NBbmlnQXdJQkFnSUVwNzo".getBytes());
		bir.setBdb("SUlSADAyMAAAACc6AAEAAQAAJyoH5AoJECYh//8Bc18wBgAAAQIDCgABlwExCA".getBytes());

		Entry entry = new Entry();
		entry.setKey("PAYLOAD");
		entry.setValue(
				"{\"digitalId\":\"eyJ4NWMiOlsiTUlJRjZqQ0NBOUtnQXdJQkFnSUJCVEFOQmdrcWhraUc5dzBC\",\"bioValue\":\"<bioValue>\",\"qualityScore\":\"80\",\"bioType\":\"Iris\"}");

		Entry entry1 = new Entry();
		entry1.setKey("EXCEPTION");
		entry1.setValue("false");

		Entry entry2 = new Entry();
		entry2.setKey("RETRIES");
		entry2.setValue("1");

		bir.setOthers(Arrays.asList(entry, entry1, entry2));
		biometricRecord.setSegments(Arrays.asList(bir));

		Mockito.when(anonymousProfileRepository.save(Mockito.any(AnonymousProfileEntity.class))).thenReturn(new AnonymousProfileEntity());
	}

	@Test
	public void saveAnonymousProfileTest() {

		anonymousProfileService.saveAnonymousProfile("123", "SYNC", "aa");
	}

	@Test(expected = TablenotAccessibleException.class)
	public void saveAnonymousProfileDataAccessLayerExceptionTest() {

		Mockito.when(anonymousProfileRepository.save(Mockito.any()))
				.thenThrow(new DataAccessLayerException("", "", new TablenotAccessibleException()));

		anonymousProfileService.saveAnonymousProfile("123", "SYNC", "aa");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void buildJsonStringFromPacketInfoTest()
			throws ApisResourceAccessException, PacketManagerException, JSONException, IOException, JsonParseException, io.mosip.kernel.core.exception.IOException, JsonMappingException {

		String json = "{\"processName\":\"NEW\",\"processStage\":\"packetValidatorStage\",\"date\":\"2021-09-12T06:50:19.517872400Z\",\"startDateTime\":null,\"endDateTime\":null,\"yearOfBirth\":1998,\"gender\":\"Female\",\"location\":[\"zone\",\"postalCode\"],\"preferredLanguages\":null,\"channel\":[\"phone\"],\"exceptions\":[],\"verified\":null,\"biometricInfo\":[{\"type\":\"FINGER\",\"subType\":\"Left RingFinger\",\"qualityScore\":80,\"attempts\":\"1\",\"digitalId\":\"eyJ4NWMiOlsiTUlJRjZqQ0NBOUtnQXdJQkFnSUJCVEFOQmdrcWhraUc5dzBC\"}],\"device\":null,\"documents\":[\"CIN\",\"RNC\"],\"assisted\":[\"110024\"],\"enrollmentCenterId\":\"1003\",\"status\":\"PROCESSED\"}";
		Document doc1 = new Document();
		doc1.setDocumentType("CIN");
		Document doc2 = new Document();
		doc2.setDocumentType("RNC");

		org.json.simple.JSONObject mappingJsonObject = new JSONObject();
		LinkedHashMap identity = new LinkedHashMap();
		LinkedHashMap IDSchemaVersion = new LinkedHashMap();
		IDSchemaVersion.put("value", "IDSchemaVersion");
		LinkedHashMap address = new LinkedHashMap();
		address.put("value",
				"permanentAddressLine1,permanentAddressLine2,permanentAddressLine3,permanentRegion,permanentProvince,permanentCity,permanentZone,permanentPostalcode");
		LinkedHashMap phone = new LinkedHashMap();
		phone.put("value", "phone");
		LinkedHashMap email = new LinkedHashMap();
		email.put("value", "email");
		LinkedHashMap dateOfBirth = new LinkedHashMap();
		dateOfBirth.put("value", "dateOfBirth");
		LinkedHashMap gender = new LinkedHashMap();
		gender.put("value", "gender");
		LinkedHashMap locationHierarchyForProfiling = new LinkedHashMap();
		locationHierarchyForProfiling.put("value", "zone,postalCode");
		LinkedHashMap preferredLang = new LinkedHashMap();
		preferredLang.put("value", "preferredLang");

		identity.put("IDSchemaVersion", IDSchemaVersion);
		identity.put("address", address);
		identity.put("phone", phone);
		identity.put("email", email);
		identity.put("dob", dateOfBirth);
		identity.put("gender", gender);
		identity.put("locationHierarchyForProfiling", locationHierarchyForProfiling);
		mappingJsonObject.put("identity", identity);
		String mappingJsonString = "{\"identity\":{\"IDSchemaVersion\":{\"value\":\"IDSchemaVersion\"},\"address\":{\"value\":\"permanentAddressLine1,permanentAddressLine2,permanentAddressLine3,permanentRegion,permanentProvince,permanentCity,permanentZone,permanentPostalcode\"},\"phone\":{\"value\":\"phone\"},\"email\":{\"value\":\"email\"}}}";

		Mockito.when(anonymousProfileRepository.save(Mockito.any(AnonymousProfileEntity.class))).thenReturn(new AnonymousProfileEntity());
		Mockito.when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mappingJsonString);
		Mockito.when(mapper.readValue(anyString(), any(Class.class)))
				.thenReturn(new FieldValue("registrationType", "NEW")).thenReturn(mappingJsonObject).thenReturn(doc1)
				.thenReturn(doc2).thenReturn(new FieldValue("centerId", "1003"))
				.thenReturn(new FieldValue("officerId", "110024"));
		assertEquals(json, anonymousProfileService.buildJsonStringFromPacketInfo(biometricRecord, fieldMap, metaInfoMap,
				"PROCESSED", "packetValidatorStage"));
	}

}