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

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.netatmo.config.NetatmoModuleConfiguration;
import org.openhab.binding.netatmo.internal.NAModuleAdapter;

/**
 * {@link NetatmoModuleHandler} is the handler for a given
 * module device accessed through the Netatmo Device
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public abstract class NetatmoModuleHandler<X extends NetatmoModuleConfiguration>
        extends AbstractNetatmoThingHandler<X> {
    private final int batteryMin;
    private final int batteryLow;
    private final int batteryMax;
    protected NAModuleAdapter module;

    protected NetatmoModuleHandler(Thing thing, Class<X> configurationClass) {
        super(thing, configurationClass);
        Map<String, String> properties = thing.getProperties();
        this.batteryMax = Integer.parseInt(properties.get(PROPERTY_BATTERY_MAX));
        this.batteryMin = Integer.parseInt(properties.get(PROPERTY_BATTERY_MIN));
        this.batteryLow = Integer.parseInt(properties.get(PROPERTY_BATTERY_LOW));
    }

    private int getBatteryPercent() {
        // when batteries are freshly changed, API may return a value superior to batteryMax !
        int correctedVp = Math.min(module.getBatteryVp(), batteryMax);
        return (100 * (correctedVp - batteryMin) / (batteryMax - batteryMin));
    }

    @Override
    protected State getNAThingProperty(String chanelId) {
        switch (chanelId) {
            case CHANNEL_BATTERY_LEVEL:
                return toDecimalType(getBatteryPercent());
            case CHANNEL_LOW_BATTERY:
                return module.getBatteryVp() < batteryLow ? OnOffType.ON : OnOffType.OFF;
            case CHANNEL_LAST_MESSAGE:
                return toDateTimeType(module.getLastMessage());
            case CHANNEL_RF_STATUS:
                Integer rfStatus = module.getRfStatus();
                return toDecimalType(getSignalStrength(rfStatus));
            default:
                return super.getNAThingProperty(chanelId);
        }
    }

    public void updateChannels(NetatmoBridgeHandler bridgeHandler, NAModuleAdapter module) {
        this.module = module;
        super.updateChannels(configuration.getParentId());
    }

}
