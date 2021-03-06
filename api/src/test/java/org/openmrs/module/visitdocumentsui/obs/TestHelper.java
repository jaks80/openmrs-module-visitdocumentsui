package org.openmrs.module.visitdocumentsui.obs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang.RandomStringUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.visitdocumentsui.ComplexObsSaver;
import org.openmrs.module.visitdocumentsui.VisitDocumentsActivator;
import org.openmrs.module.visitdocumentsui.VisitDocumentsConstants;
import org.openmrs.module.visitdocumentsui.VisitDocumentsContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.ui.framework.WebConstants;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;

@Component
public class TestHelper {

	@Autowired
	protected VisitDocumentsActivator activator;

	@Autowired
	@Qualifier(VisitDocumentsConstants.COMPONENT_VDUI_CONTEXT)
	protected VisitDocumentsContext context;

	@Autowired
	@Qualifier(VisitDocumentsConstants.COMPONENT_COMPLEXDATA_HELPER)
	protected ComplexDataHelper complexDataHelper;

	@Autowired
	@Qualifier(VisitDocumentsConstants.COMPONENT_COMPLEXOBS_SAVER)
	protected ComplexObsSaver obsSaver;

	protected Path complexObsDir;

	protected String fileName = "mock_file_name";
	protected String fileExt = "ext";

	protected MockMultipartFile multipartFile = new MockMultipartFile(fileName, fileName + "." + fileExt, "application/octet-stream", "mock_content".getBytes());

	public MockMultipartFile getTestMultipartFile() {
		return multipartFile;
	}

	public String getTestFileName() {
		return fileName;
	}

	public String getTestFileNameWithExt() {
		return getTestFileName() + "." + fileExt;
	}

	/**
	 * This initialization routine configure a lot of boilerplate settings to mimic the actual environment.
	 * @throws IOException
	 */
	public void init() throws IOException {
		activator.started(); // so our concepts complex are created

		String conceptComplexUuidMap = "{\"IMAGE\":\"" + VisitDocumentsConstants.CONCEPT_IMAGE_UUID + "\",\"OTHER\":\"" + VisitDocumentsConstants.CONCEPT_DEFAULT_UUID + "\"}";
		context.getAdministrationService().saveGlobalProperty( new GlobalProperty(VisitDocumentsConstants.GP_CONCEPT_COMPLEX_UUID_MAP, conceptComplexUuidMap) );

		complexObsDir = Files.createTempDirectory(null);
		context.getAdministrationService().saveGlobalProperty( new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR, getComplexObsDir()) );

		WebConstants.CONTEXT_PATH = "openmrs";

		context.getAdministrationService().saveGlobalProperty( new GlobalProperty(VisitDocumentsConstants.GP_CONCEPT_COMPLEX_UUID_LIST, "[\"7cac8397-53cd-4f00-a6fe-028e8d743f8e\",\"42ed45fd-f3f6-44b6-bfc2-8bde1bb41e00\"]" ) );
		context.getAdministrationService().saveGlobalProperty( new GlobalProperty(VisitDocumentsConstants.GP_MAX_STORAGE_FILE_SIZE, "1.2" ) );
		context.getAdministrationService().saveGlobalProperty( new GlobalProperty(VisitDocumentsConstants.GP_MAX_UPLOAD_FILE_SIZE, "5.0" ) );
		context.getAdministrationService().saveGlobalProperty( new GlobalProperty(VisitDocumentsConstants.GP_ALLOW_NO_CAPTION, "false" ) );
		context.getAdministrationService().saveGlobalProperty( new GlobalProperty(VisitDocumentsConstants.GP_WEBCAM_ALLOWED, "true" ) );
		context.getAdministrationService().saveGlobalProperty( new GlobalProperty(RestConstants.MAX_RESULTS_DEFAULT_GLOBAL_PROPERTY_NAME, "50" ) );

		context.getAdministrationService().saveGlobalProperty( new GlobalProperty(VisitDocumentsConstants.GP_ENCOUNTER_TYPE_UUID, VisitDocumentsConstants.ENCOUNTER_TYPE_UUID) );
	}

	public Obs getTestComplexObs() throws IOException {
		init();

		Patient patient = Context.getPatientService().getPatient(2);
		Visit visit = Context.getVisitService().getVisit(1);
		EncounterService encounterService = Context.getEncounterService();
		EncounterType encounterType = encounterService.getEncounterType(1);
		Provider provider = Context.getProviderService().getProvider(1);

		context.getAdministrationService().saveGlobalProperty( new GlobalProperty(VisitDocumentsConstants.GP_ENCOUNTER_TYPE_UUID, encounterType.getUuid()) );
		Encounter encounter = context.getVisitDocumentEncounter(patient, visit, provider);

		String fileCaption = RandomStringUtils.randomAlphabetic(12);
		return obsSaver.saveOtherDocument(visit, patient, encounter, fileCaption, getTestMultipartFile(), ValueComplex.INSTRUCTIONS_DEFAULT);
	}

	public String getComplexObsDir() {
		return complexObsDir.toAbsolutePath().toString();
	}

	/**
	 * @return The file path of the file 'behind' the complex obs.
	 */
	public String getTestComplexObsFilePath() {
		return getComplexObsDir() + "/" + getTestFileNameWithExt();
	}
}
