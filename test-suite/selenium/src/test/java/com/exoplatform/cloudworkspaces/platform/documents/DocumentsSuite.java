package com.exoplatform.cloudworkspaces.platform.documents;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ UploadAndDeleteDocumentTest.class,
		CreateAndDeleteFolderTest.class, WatchUnwatchDocumentTest.class,
		RateDocumentTest.class })
public class DocumentsSuite {
}
