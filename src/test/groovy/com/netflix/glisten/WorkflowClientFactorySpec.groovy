/*
 * Copyright 2013 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.glisten

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow
import com.amazonaws.services.simpleworkflow.flow.DynamicWorkflowClientExternal
import com.amazonaws.services.simpleworkflow.flow.ManualActivityCompletionClient
import com.amazonaws.services.simpleworkflow.flow.WorkflowClientExternal
import com.amazonaws.services.simpleworkflow.model.WorkflowExecution
import com.netflix.glisten.example.trip.BayAreaTripWorkflow
import spock.lang.Specification

class WorkflowClientFactorySpec extends Specification {

    WorkflowClientFactory workflowClientFactory = new WorkflowClientFactory(Mock(AmazonSimpleWorkflow))

    def 'should get new workflow client'() {\
        DynamicWorkflowClientExternal dynamicWorkflowClient = Mock(DynamicWorkflowClientExternal)

        when:
        def client = workflowClientFactory.getNewWorkflowClient(BayAreaTripWorkflow, null)

        then:
        client instanceof InterfaceBasedWorkflowClient
        client.workflowExecution != null
        client.schedulingOptions.tagList == null

        when:
        def workflow = client.asWorkflow(null, dynamicWorkflowClient)

        then:
        workflow instanceof WorkflowClientExternalToWorkflowInterfaceAdapter

        when:
        workflow.start('John Snow', [])

        then:
        1 * dynamicWorkflowClient.startWorkflowExecution(['John Snow', []] as Object[], _)
    }

    def 'should get an existing workflow client'() {
        expect:
        workflowClientFactory.getWorkflowClient(new WorkflowExecution()) instanceof WorkflowClientExternal
    }

    def 'should get a ManualActivityCompletionClient'() {
        expect:
        workflowClientFactory.getManualActivityCompletionClient('123') instanceof ManualActivityCompletionClient
    }

}

