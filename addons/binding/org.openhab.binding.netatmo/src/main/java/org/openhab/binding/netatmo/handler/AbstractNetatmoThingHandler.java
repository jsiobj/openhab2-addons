/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

/**
 * {@link AbstractNetatmoThingHandler} is the abstract class that handles
 * common behaviors of both Devices and Modules
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 * @author Jean-Sébastien Roques - reworked to use latest Netatmo API for Thermostat - Work in progress
 *
 */
abstract class AbstractNetatmoThingHandler extends BaseThingHandler {

    // private static Logger logger = LoggerFactory.getLogger(AbstractNetatmoThingHandler.class);

    private final List<Integer> signalThresholds = new ArrayList<Integer>();
    protected final String actualApp;

    protected NetatmoBridgeHandler bridgeHandler;

    AbstractNetatmoThingHandler(Thing thing) {
        super(thing);
        Map<String, String> properties = thing.getProperties();
        List<String> thresholds = Arrays.asList(properties.get(PROPERTY_SIGNAL_LEVELS).split(","));
        for (String threshold : thresholds) {
            signalThresholds.add(Integer.parseInt(threshold));
        }
        actualApp = properties.get(PROPERTY_ACTUAL_APP);
    }

    @Override
    public void initialize() {
        // Let's put the thing OFFLINE/BRIDGE_OFFLINE until bridge is initialized
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
    }

    @Override
    public void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        super.bridgeHandlerInitialized(thingHandler, bridge);
        bridgeHandler = (NetatmoBridgeHandler) thingHandler;
    }

    // Misc tools
    protected Calendar timestampToCalendar(Integer netatmoTS) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(netatmoTS * 1000L);
        return calendar;
    }

    protected int getSignalStrength(int signalLevel) {
        // Take in account #3995
        int level;
        for (level = 0; level < signalThresholds.size(); level++) {
            if (signalLevel > signalThresholds.get(level)) {
                break;
            }
        }
        return level;
    }
}
