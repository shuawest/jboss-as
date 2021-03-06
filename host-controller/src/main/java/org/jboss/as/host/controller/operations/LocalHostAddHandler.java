/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.host.controller.operations;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.PathElement;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CONNECTIONS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MANAGEMENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;

import java.util.Locale;

import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SECURITY_REALMS;
import org.jboss.as.host.controller.HostModelUtil;
import org.jboss.dmr.ModelNode;

/**
 * The handler to add the local host definition to the DomainModel.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class LocalHostAddHandler implements OperationStepHandler, DescriptionProvider {

    public static final String OPERATION_NAME = "add-host";

    private final LocalHostControllerInfoImpl hostControllerInfo;

    public static LocalHostAddHandler getInstance(final LocalHostControllerInfoImpl hostControllerInfo) {
        return new LocalHostAddHandler(hostControllerInfo);
    }

    private LocalHostAddHandler(final LocalHostControllerInfoImpl hostControllerInfo) {
        this.hostControllerInfo = hostControllerInfo;
    }

    @Override
    public ModelNode getModelDescription(Locale locale) {
        // This is a private operation, so this op will not be called
        return new ModelNode();
    }

    /**
     * {@inheritDoc}
     */
    public void execute(OperationContext context, ModelNode operation) {
        if (!context.isBooting()) {
            throw new IllegalStateException(String.format("Invocations of %s after HostController boot are not allowed", OPERATION_NAME));
        }

        final ModelNode model = context.readModelForUpdate(PathAddress.EMPTY_ADDRESS);
        HostModelUtil.initCoreModel(model);

        // Create the empty managmeent security resources
        context.createResource(PathAddress.pathAddress(PathElement.pathElement(MANAGEMENT, SECURITY_REALMS)));
        context.createResource(PathAddress.pathAddress(PathElement.pathElement(MANAGEMENT, CONNECTIONS)));

        final String localHostName = operation.require(NAME).asString();
        model.get(NAME).set(localHostName);

        hostControllerInfo.setLocalHostName(localHostName);

        context.completeStep();
    }
}
