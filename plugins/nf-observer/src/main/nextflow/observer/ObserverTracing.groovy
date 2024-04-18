/*
 * Copyright 2021, Seqera Labs
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

package nextflow.observer

import java.nio.file.Path

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.http.HttpResponse
import nextflow.Session
import nextflow.processor.TaskHandler
import nextflow.trace.TraceObserver
import nextflow.trace.TraceRecord
import nextflow.trace.WorkflowStats
import nextflow.trace.WorkflowStatsObserver
import nextflow.util.Duration

import nextflow.pushgateway.PushgatewayClient


/**
 * Example workflow events observer
 *
 * @author Daniel Santos <daniel.santoso@invitae.com>
 */
@Slf4j
@CompileStatic
class ObserverTracing implements TraceObserver {

    private Session session

    private long startTimestamp

    private long endTimestamp

    private volatile boolean stopped

    private volatile boolean started

    private PushgatewayClient pushgatewayClient = new PushgatewayClient()

    @Override
    void onProcessStart(TaskHandler handler, TraceRecord trace) {
        log.info "Process started! '${handler.task.name}'"
    }

    @Override
    void onProcessComplete(TaskHandler handler, TraceRecord trace) {
        log.info "I completed a task! It's name is '${handler.task.name}'"
        for (Map.Entry<String, String> entry : TraceRecord.FIELDS.entrySet()) {
        String fieldName = entry.getKey()
        Object value = trace.get(fieldName)

        if (value instanceof Number) {
            String stringValue = (Number) value;

            if (stringValue.matches("\\b\\d+\\b")) {
                log.info "${fieldName}: ${stringValue}"
                HttpResponse response = pushgatewayClient.postData(fieldName, stringValue);
            }
        }
    }
    }

    @Override
    void onProcessCached(TaskHandler handler, TraceRecord trace) {
        log.info "I found a task in the cache! It's name is '${handler.task.name}'"
    }
    
    @Override
    void onFlowError(TaskHandler handler, TraceRecord trace) {
        log.info "Uh oh, something went wrong..."
    }

    @Override
    void onFlowCreate(Session session){
        log.info "Hi, the Pipeline is starting! 🚀 now by drsantos20"
        this.started = true
        this.session = session
        this.startTimestamp = System.currentTimeMillis()
        log.info "Pipeline started at '${new Date(startTimestamp)}'"
    }

    @Override
    void onFlowComplete(){
        stopped = true
        endTimestamp = System.currentTimeMillis()
        log.info "Pipeline finished at '${new Date(endTimestamp)}'"
        log.info "Pipeline complete! 👋 bye!"
    }

}
