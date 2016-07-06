/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler.thermostat;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.netatmo.config.NetatmoDeviceConfiguration;
import org.openhab.binding.netatmo.handler.NetatmoBridgeHandler;
import org.openhab.binding.netatmo.handler.NetatmoDeviceHandler;
import org.openhab.binding.netatmo.internal.NADeviceAdapter;
import org.openhab.binding.netatmo.internal.NAPlugAdapter;

import io.swagger.client.model.NAThermostatDataBody;

/**
 * {@link NAPlugHandler} is the class used to handle the plug
 * device of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NAPlugHandler extends NetatmoDeviceHandler<NetatmoDeviceConfiguration> {
    public NAPlugHandler(Thing thing) {
        super(thing, NetatmoDeviceConfiguration.class);
    }

    @Override
    protected NADeviceAdapter<?> updateReadings(NetatmoBridgeHandler bridgeHandler, String equipmentId) {
        NAThermostatDataBody thermostatDataBody = bridgeHandler.getThermostatsDataBody(equipmentId);
        return new NAPlugAdapter(thermostatDataBody);
    }

}
