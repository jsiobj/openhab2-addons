/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.Map;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.config.NetatmoModuleConfiguration;

/**
 * {@link NetatmoModuleHandler} is the handler for a given
 * module device accessed through the Netatmo Device
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 * @author Jean-Sébastien Roques - reworked to use latest Netatmo API for Thermostat - Work in progress
 *
 */
public abstract class NetatmoModuleHandler extends AbstractNetatmoThingHandler {

    // private static Logger logger = LoggerFactory.getLogger(NetatmoModuleHandler.class);
    private final NetatmoModuleConfiguration configuration;

    protected final int batteryMin;
    protected final int batteryLow;
    protected final int batteryMax;

    protected NetatmoModuleHandler(Thing thing) {
        super(thing);
        Map<String, String> properties = thing.getProperties();
        this.batteryMax = Integer.parseInt(properties.get(PROPERTY_BATTERY_MAX));
        this.batteryMin = Integer.parseInt(properties.get(PROPERTY_BATTERY_MIN));
        this.batteryLow = Integer.parseInt(properties.get(PROPERTY_BATTERY_LOW));
        this.configuration = this.getConfigAs(NetatmoModuleConfiguration.class);
    }

    @Override
    public void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        super.bridgeHandlerInitialized(thingHandler, bridge);
    }

    protected State getNAChannelValue(String channelId) {
        return null;
    }

    public String getParentId() {
        return configuration.getParentId();
    }

    public String getId() {
        return configuration.getEquipmentId();
    }

}
