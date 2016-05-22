/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.netatmo.config.NetatmoDeviceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import io.swagger.client.model.NADevice;
//import io.swagger.client.model.NAPlace;

/**
 * {@link NetatmoDeviceHandler} is the handler for a given
 * device accessed through the Netatmo Bridge
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 * @author Jean-Sébastien Roques - reworked to use latest Netatmo API for Thermostat - Work in progress
 *
 */
public abstract class NetatmoDeviceHandler extends AbstractNetatmoThingHandler {
    private static Logger logger = LoggerFactory.getLogger(NetatmoDeviceHandler.class);

    private NetatmoDeviceConfiguration configuration;

    public NetatmoDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        logger.debug("Initialiazing bridge for thing : {}", this.getThing().getLabel());
        super.bridgeHandlerInitialized(thingHandler, bridge);
        this.configuration = this.getConfigAs(NetatmoDeviceConfiguration.class);

        logger.debug("Scheduling data refresh");
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                updateChannels();
            }
        }, 1, configuration.refreshInterval, TimeUnit.MILLISECONDS);
    }

    protected String getId() {
        return configuration.getEquipmentId();
    }
}