/*
 * Copyright (c) 2005-2010 Grameen Foundation USA
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 *  See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 *  explanation of the license and how it is applied.
 */

package org.mifos.application.master.persistence;

import org.mifos.application.questionnaire.migration.QuestionnaireMigration;
import org.mifos.customers.surveys.business.Survey;
import org.mifos.customers.surveys.helpers.SurveyType;
import org.mifos.customers.surveys.persistence.SurveysPersistence;
import org.mifos.framework.components.logger.LoggerConstants;
import org.mifos.framework.components.logger.MifosLogManager;
import org.mifos.framework.components.logger.MifosLogger;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.persistence.Upgrade;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Upgrade1283341654 extends Upgrade {
    private QuestionnaireMigration questionnaireMigration;
    private SurveysPersistence surveysPersistence;
    private MifosLogger mifosLogger;

    public Upgrade1283341654() {
        super();
        mifosLogger = MifosLogManager.getLogger(LoggerConstants.FRAMEWORKLOGGER);
    }

    // Should only be used from tests to inject mocks
    public Upgrade1283341654(QuestionnaireMigration questionnaireMigration, SurveysPersistence surveysPersistence) {
        this();
        this.questionnaireMigration = questionnaireMigration;
        this.surveysPersistence = surveysPersistence;
    }

    @Override
    public void upgrade(Connection connection) throws IOException, SQLException {
        try {
            initializeDependencies();
            migrateSurveys();
        } catch (PersistenceException e) {
            throw new SQLException(e);
        }
    }

    private void migrateSurveys() throws PersistenceException {
        migrateSurveys(SurveyType.CLIENT);
    }

    private List<Integer> migrateSurveys(SurveyType surveyType) throws PersistenceException {
        List<Survey> surveys = getSurveys(surveyType);
        return questionnaireMigration.migrateSurveys(surveys);
    }

    private List<Survey> getSurveys(SurveyType surveyType) {
        List<Survey> surveys = new ArrayList<Survey>(0);
        try {
            surveys = surveysPersistence.retrieveSurveysByType(surveyType);
        } catch (PersistenceException e) {
            mifosLogger.error(String.format("Unable to retrieve surveys of type %s", surveyType), e);
        }
        return surveys;
    }

    private void initializeDependencies() {
        if(questionnaireMigration == null) questionnaireMigration = (QuestionnaireMigration) upgradeContext.getBean("questionnaireMigration");
        if (surveysPersistence == null) surveysPersistence = (SurveysPersistence) upgradeContext.getBean("surveysPersistence");
    }
}
