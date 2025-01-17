/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.cellery.observability.model.generator;

import io.cellery.observability.model.generator.exception.GraphStoreException;
import io.cellery.observability.model.generator.internal.ServiceHolder;
import io.cellery.observability.model.generator.model.Node;
import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is the Siddhi extension which generates the dependency graph using request source and target information.
 */
@Extension(
        name = "modelGenerator",
        namespace = "observe",
        description = "This generates the dependency model based on request source and target information",
        examples = @Example(description = "This updates the dependency model based on the request"
                , syntax = "observe:modelGenerator(sourceNamespace, sourceInstance, sourceComponent, "
                + "sourceInstanceKind, destinationNamespace, destinationInstance, destinationComponent,"
                + "destinationInstanceKind)\n"
                + "select *\n"
                + "insert into outputStream;")
)
public class ModelGenerationExtension extends StreamProcessor {

    private static final Logger log = Logger.getLogger(ModelGenerationExtension.class);

    private ExpressionExecutor runtimeExecutor;
    private ExpressionExecutor sourceNamespaceExecutor;
    private ExpressionExecutor sourceInstanceExecutor;
    private ExpressionExecutor sourceComponentExecutor;
    private ExpressionExecutor sourceInstanceKindExecutor;
    private ExpressionExecutor destinationNamespaceExecutor;
    private ExpressionExecutor destinationInstanceExecutor;
    private ExpressionExecutor destinationComponentExecutor;
    private ExpressionExecutor destinationInstanceKindExecutor;

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        while (streamEventChunk.hasNext()) {
            try {
                StreamEvent incomingStreamEvent = streamEventChunk.next();
                String runtime = (String) runtimeExecutor.execute(incomingStreamEvent);
                String sourceNamespace = (String) sourceNamespaceExecutor.execute(incomingStreamEvent);
                String sourceInstance = (String) sourceInstanceExecutor.execute(incomingStreamEvent);
                String sourceComponent = (String) sourceComponentExecutor.execute(incomingStreamEvent);
                String sourceInstanceKind = (String) sourceInstanceKindExecutor.execute(incomingStreamEvent);
                String destinationNamespace = (String) destinationNamespaceExecutor.execute(incomingStreamEvent);
                String destinationInstance = (String) destinationInstanceExecutor.execute(incomingStreamEvent);
                String destinationComponent = (String) destinationComponentExecutor.execute(incomingStreamEvent);
                String destinationInstanceKind = (String) destinationInstanceKindExecutor.execute(incomingStreamEvent);

                Node sourceNode = this.getOrGenerateNode(runtime, sourceNamespace, sourceInstance, sourceComponent,
                        sourceInstanceKind);
                Node destinationNode = this.getOrGenerateNode(runtime, destinationNamespace, destinationInstance,
                        destinationComponent, destinationInstanceKind);
                ServiceHolder.getModelManager().addEdge(runtime, sourceNode, destinationNode);
            } catch (Throwable throwable) {
                log.error("Unexpected error occurred while processing the event in the model processor",
                        throwable);
            }
        }
        try {
            ServiceHolder.getModelStoreManager().storeCurrentModel();
        } catch (GraphStoreException e) {
            log.error("Failed to persist current dependency model", e);
        }
        if (streamEventChunk.getFirst() != null) {
            nextProcessor.process(streamEventChunk);
        }
    }

    private Node getOrGenerateNode(String runtime, String namespace, String instance, String component,
                                   String instanceKind) {
        Node node = ServiceHolder.getModelManager().getOrGenerateNode(runtime, namespace, instance, component);
        node.setInstanceKind(instanceKind);
        return node;
    }

    @Override
    protected List<Attribute> init(AbstractDefinition abstractDefinition, ExpressionExecutor[] expressionExecutors,
                                   ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        if (expressionExecutors.length != 9) {
            throw new SiddhiAppCreationException("Eight arguments are required");
        } else {
            if (expressionExecutors[0].getReturnType() == Attribute.Type.STRING) {
                runtimeExecutor = expressionExecutors[0];
            } else {
                throw new SiddhiAppCreationException("Expected a field with String return type for the runtime "
                        + "field, but found a field with return type - "
                        + expressionExecutors[0].getReturnType());
            }

            if (expressionExecutors[1].getReturnType() == Attribute.Type.STRING) {
                sourceNamespaceExecutor = expressionExecutors[1];
            } else {
                throw new SiddhiAppCreationException("Expected a field with String return type for the source "
                        + "namespace field, but found a field with return type - "
                        + expressionExecutors[1].getReturnType());
            }

            if (expressionExecutors[2].getReturnType() == Attribute.Type.STRING) {
                sourceInstanceExecutor = expressionExecutors[2];
            } else {
                throw new SiddhiAppCreationException("Expected a field with String return type for the source "
                        + "instance field, but found a field with return type - "
                        + expressionExecutors[2].getReturnType());
            }

            if (expressionExecutors[3].getReturnType() == Attribute.Type.STRING) {
                sourceComponentExecutor = expressionExecutors[3];
            } else {
                throw new SiddhiAppCreationException("Expected a field with String return type for the source "
                        + "component field, but found a field with return type - "
                        + expressionExecutors[3].getReturnType());
            }

            if (expressionExecutors[4].getReturnType() == Attribute.Type.STRING) {
                sourceInstanceKindExecutor = expressionExecutors[4];
            } else {
                throw new SiddhiAppCreationException("Expected a field with Long return type for the source "
                        + "instance kind field, but found a field with return type - "
                        + expressionExecutors[4].getReturnType());
            }

            if (expressionExecutors[5].getReturnType() == Attribute.Type.STRING) {
                destinationNamespaceExecutor = expressionExecutors[5];
            } else {
                throw new SiddhiAppCreationException("Expected a field with Long return type for the destination "
                        + "namespace field, but found a field with return type - "
                        + expressionExecutors[5].getReturnType());
            }

            if (expressionExecutors[6].getReturnType() == Attribute.Type.STRING) {
                destinationInstanceExecutor = expressionExecutors[6];
            } else {
                throw new SiddhiAppCreationException("Expected a field with String return type for the destination "
                        + "instance field, but found a field with return type - "
                        + expressionExecutors[6].getReturnType());
            }

            if (expressionExecutors[7].getReturnType() == Attribute.Type.STRING) {
                destinationComponentExecutor = expressionExecutors[7];
            } else {
                throw new SiddhiAppCreationException("Expected a field with String return type for the destination "
                        + "component field, but found a field with return type - "
                        + expressionExecutors[7].getReturnType());
            }

            if (expressionExecutors[8].getReturnType() == Attribute.Type.STRING) {
                destinationInstanceKindExecutor = expressionExecutors[8];
            } else {
                throw new SiddhiAppCreationException("Expected a field with String return type for the destination "
                        + "instance kind field, but found a field with return type - "
                        + expressionExecutors[8].getReturnType());
            }
        }
        return new ArrayList<>(0);
    }

    @Override
    public void start() {   // Do Nothing
    }

    @Override
    public void stop() {    // Do Nothing
    }

    @Override
    public Map<String, Object> currentState() {     // Do Nothing
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> map) { // Do Nothing
    }
}
