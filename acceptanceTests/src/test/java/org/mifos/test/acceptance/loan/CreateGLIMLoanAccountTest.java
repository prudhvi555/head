/*
 * Copyright (c) 2005-2011 Grameen Foundation USA
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */

package org.mifos.test.acceptance.loan;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.mifos.test.acceptance.framework.MifosPage;
import org.mifos.test.acceptance.framework.UiTestCaseBase;
import org.mifos.test.acceptance.framework.loan.CreateLoanAccountEntryPage;
import org.mifos.test.acceptance.framework.loan.CreateLoanAccountSearchParameters;
import org.mifos.test.acceptance.framework.loan.CreateLoanAccountSubmitParameters;
import org.mifos.test.acceptance.framework.loan.DisburseLoanParameters;
import org.mifos.test.acceptance.framework.loan.EditLoanAccountInformationParameters;
import org.mifos.test.acceptance.framework.loan.EditLoanAccountStatusParameters;
import org.mifos.test.acceptance.framework.loan.LoanAccountPage;
import org.mifos.test.acceptance.framework.testhelpers.LoanTestHelper;
import org.mifos.test.acceptance.remote.DateTimeUpdaterRemoteTestingService;
import org.mifos.test.acceptance.util.ApplicationDatabaseOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.SQLException;

@SuppressWarnings("PMD")
@ContextConfiguration(locations = {"classpath:ui-test-context.xml"})
@Test(singleThreaded = true, groups = {"loan", "acceptance", "ui", "no_db_unit"})
public class CreateGLIMLoanAccountTest extends UiTestCaseBase {

    private LoanTestHelper loanTestHelper;
    @Autowired
    private ApplicationDatabaseOperation applicationDatabaseOperation;

    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    // one of the dependent methods throws Exception
    @BeforeMethod(alwaysRun=true)
    public void setUp() throws Exception {
        super.setUp();
        applicationDatabaseOperation.updateGLIM(1);
        loanTestHelper = new LoanTestHelper(selenium);
    }

    @AfterMethod
    public void logOut() throws SQLException {
        applicationDatabaseOperation.updateGLIM(0);
        (new MifosPage(selenium)).logout();
    }

    /*
     * This test is to verify that you can edit a GLIM loan account after it has been
     * dibursed without getting an invalid disbursal date error. See MIFOS-2597.
     */
    @Test(enabled = true)
    // http://mifosforge.jira.com/browse/MIFOSTEST-132
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void checkGLIMLoanCreatedBySubmitForApproval() throws Exception {
        //Given
        DateTimeUpdaterRemoteTestingService dateTimeUpdaterRemoteTestingService = new DateTimeUpdaterRemoteTestingService(selenium);
        DateTime targetTime = new DateTime(2011, 03, 1, 13, 0, 0, 0);
        dateTimeUpdaterRemoteTestingService.setDateTime(targetTime);
        //When
        LoanAccountPage loanAccountPage = loanTestHelper.createLoanAccountForMultipleClientsInGroup(true);
        String accountId = loanAccountPage.getAccountId();
        dateTimeUpdaterRemoteTestingService.setDateTime(new LocalDate(2011, 3, 8).toDateTimeAtStartOfDay());
        EditLoanAccountStatusParameters statusParameters = new EditLoanAccountStatusParameters();
        statusParameters.setStatus(EditLoanAccountStatusParameters.APPROVED);
        statusParameters.setNote("Test");
        loanTestHelper.changeLoanAccountStatus(accountId, statusParameters);
        DisburseLoanParameters params = new DisburseLoanParameters();
        params.setDisbursalDateDD("8");
        params.setDisbursalDateMM("3");
        params.setDisbursalDateYYYY("2011");
        params.setPaymentType(DisburseLoanParameters.CASH);
        loanTestHelper.disburseLoan(accountId, params);
        dateTimeUpdaterRemoteTestingService.setDateTime(new LocalDate(2011, 3, 15).toDateTimeAtStartOfDay());
        EditLoanAccountInformationParameters editLoanAccountInformationParameters = new EditLoanAccountInformationParameters();
        editLoanAccountInformationParameters.setExternalID("ID2323ID");
        loanTestHelper.changeLoanAccountInformation(accountId, new CreateLoanAccountSubmitParameters(), editLoanAccountInformationParameters);
    }

    @Test(enabled = true)
    // http://mifosforge.jira.com/browse/MIFOSTEST-133
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void checkGLIMLoanCreatedBySaveForLater() throws Exception {
        DateTimeUpdaterRemoteTestingService dateTimeUpdaterRemoteTestingService = new DateTimeUpdaterRemoteTestingService(selenium);
        DateTime targetTime = new DateTime(2011, 03, 1, 13, 0, 0, 0);
        dateTimeUpdaterRemoteTestingService.setDateTime(targetTime);
        loanTestHelper.createLoanAccountForMultipleClientsInGroup(false);
    }

    // http://mifosforge.jira.com/browse/MIFOSTEST-134
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void verifyLoanAccountCreationPipelineWhenGlimIsEnabled() throws Exception {
        //Given
        DateTimeUpdaterRemoteTestingService dateTimeUpdaterRemoteTestingService = new DateTimeUpdaterRemoteTestingService(selenium);
        DateTime targetTime = new DateTime(2009,7,11,13,0,0,0);
        dateTimeUpdaterRemoteTestingService.setDateTime(targetTime);

        CreateLoanAccountSearchParameters searchParameters = new CreateLoanAccountSearchParameters();
        searchParameters.setSearchString("Stu1233266063395 Client1233266063395");
        searchParameters.setLoanProduct("ClientEmergencyLoan");
        String[] locators = {   "name=prdOfferingId",
                                "loancreationdetails.input.sumLoanAmount",
                                "loancreationdetails.input.interestRate",
                                "loancreationdetails.input.numberOfInstallments",
                                "disbursementDateDD",
                                "disbursementDateMM",
                                "disbursementDateYY",
                                "name=loanOfferingFund",
                                "name=businessActivityId",
                                "name=collateralTypeId",
                                "name=collateralNote",
                                "name=externalId",
                                "name=selectedFee[0].feeId",
                                "name=selectedFee[0].amount",
                                "name=selectedFee[1].feeId",
                                "name=selectedFee[1].amount",
                                "name=selectedFee[2].feeId",
                                "name=selectedFee[2].amount",
                                "loancreationdetails.button.continue",
                                "loancreationdetails.button.cancel"
                            };
        //When / Then
        loanTestHelper.navigateToCreateLoanAccountEntryPage(searchParameters).verifyAllElementsArePresent(locators);
   }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void newWeeklyGLIMAccount() throws Exception {
        CreateLoanAccountSearchParameters searchParameters = new CreateLoanAccountSearchParameters();
        searchParameters.setSearchString("Default Group");
        searchParameters.setLoanProduct("WeeklyGroupFlatLoanWithOnetimeFee");
        CreateLoanAccountEntryPage loanAccountEntryPage = loanTestHelper.navigateToCreateLoanAccountEntryPage(searchParameters);
        loanAccountEntryPage.selectGLIMClients(0, "Stu1233266299995 Client1233266299995 \n Client Id: 0002-000000012", "301");
        loanAccountEntryPage.selectGLIMClients(2, "Stu1233266319760 Client1233266319760 \n Client Id: 0002-000000014", "401");
        loanAccountEntryPage.submitAndNavigateToGLIMLoanAccountConfirmationPage();
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void checkGLIMAccountTotalCalculationWithDecimal() throws Exception {
        CreateLoanAccountSearchParameters searchParameters = new CreateLoanAccountSearchParameters();
        searchParameters.setSearchString("Default Group");
        searchParameters.setLoanProduct("WeeklyGroupFlatLoanWithOnetimeFee");
        CreateLoanAccountEntryPage loanAccountEntryPage = loanTestHelper.navigateToCreateLoanAccountEntryPage(searchParameters);
        loanAccountEntryPage.selectGLIMClients(0, "Stu1233266299995 Client1233266299995 \n Client Id: 0002-000000012", "9999.9");
        loanAccountEntryPage.selectGLIMClients(1, "Stu1233266309851 Client1233266309851 \n Client Id: 0002-000000013", "99999.9");
        loanAccountEntryPage.selectGLIMClients(2, "Stu1233266319760 Client1233266319760 \n Client Id: 0002-000000014", "99999.9");
        loanAccountEntryPage.checkTotalAmount("209999.7");
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void checkGLIMAccountTotalCalculationWholeNumber() throws Exception {
        CreateLoanAccountSearchParameters searchParameters = new CreateLoanAccountSearchParameters();
        searchParameters.setSearchString("Default Group");
        searchParameters.setLoanProduct("WeeklyGroupFlatLoanWithOnetimeFee");
        CreateLoanAccountEntryPage loanAccountEntryPage = loanTestHelper.navigateToCreateLoanAccountEntryPage(searchParameters);
        loanAccountEntryPage.selectGLIMClients(0, "Stu1233266299995 Client1233266299995 \n Client Id: 0002-000000012", "9999");
        loanAccountEntryPage.selectGLIMClients(1, "Stu1233266309851 Client1233266309851 \n Client Id: 0002-000000013", "99999");
        loanAccountEntryPage.selectGLIMClients(2, "Stu1233266319760 Client1233266319760 \n Client Id: 0002-000000014", "99999");
        loanAccountEntryPage.checkTotalAmount("209997");
    }
}
