/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.discovery;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.netatmo.handler.NetatmoBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.model.NADevice;
import io.swagger.client.model.NADeviceListResponse;
import io.swagger.client.model.NAModule;
import io.swagger.client.model.NAPlug;
import io.swagger.client.model.NAThermostat;
import io.swagger.client.model.NAThermostatDataResponse;

/**
 * The {@link NetatmoModuleDiscoveryService} searches for available Netatmo
 * devices and modules connected to the API console
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Jean-Sébastien Roques - reworked to use latest Netatmo API for Thermostat - Work in progress
 *
 */
public class NetatmoModuleDiscoveryService extends AbstractDiscoveryService {
    private static Logger logger = LoggerFactory.getLogger(NetatmoModuleDiscoveryService.class);
    private final static int SEARCH_TIME = 2;
    private NetatmoBridgeHandler netatmoBridgeHandler;

    public NetatmoModuleDiscoveryService(NetatmoBridgeHandler netatmoBridgeHandler) {
        super(SUPPORTED_DEVICE_THING_TYPES_UIDS, SEARCH_TIME);
        this.netatmoBridgeHandler = netatmoBridgeHandler;
    }

    private void screenThermostatDevicesAndModules(NAThermostatDataResponse thermostatData) {
        if (thermostatData != null) {
            List<NAPlug> naPlugList = thermostatData.getBody().getDevices();
            if (naPlugList != null) {
                for (NAPlug naPlug : naPlugList) {
                    onPlugAddedInternal(naPlug);
                    logger.debug("Found device : {}", naPlug.getStationName());
                    List<NAThermostat> naThermostatList = naPlug.getModules();
                    if (naThermostatList != null) {
                        for (NAThermostat naThermostat : naThermostatList) {
                            logger.debug("Found module : " + naThermostat.getModuleName());
                            onThermostatAddedInternal(naThermostat, naPlug);
                        }
                    } else {
                        logger.warn("No Thermostat found for Plug {}", naPlug.getStationName());
                    }
                }
            } else {
                logger.warn("No Plug found !");
            }
        } else {
            logger.error("Something bad happened ! Thermostat data is empty !");
        }
    }

    private void screenStationDevicesAndModules(NADeviceListResponse deviceList) {
        if (deviceList != null) {
            List<NADevice> devices = deviceList.getBody().getDevices();
            if (devices != null) {
                for (NADevice naDevice : devices) {
                    onStationAddedInternal(naDevice);
                    List<NAModule> modules = deviceList.getBody().getModules();
                    if (modules != null) {
                        for (NAModule naModule : modules) {
                            onStationModuleAddedInternal(naModule);
                        }
                    }
                }
            }
        }
    }

    private void onStationAddedInternal(NADevice naDevice) {
        ThingUID thingUID = findThingUID(naDevice.getType(), naDevice.getId());
        Map<String, Object> properties = new HashMap<>(1);

        properties.put(EQUIPMENT_ID, naDevice.getId());

        String name = naDevice.getModuleName();

        addDiscoveredThing(thingUID, properties, (name == null) ? naDevice.getStationName() : name);
    }

    private void onStationModuleAddedInternal(NAModule naModule) {
        ThingUID thingUID = findThingUID(naModule.getType(), naModule.getId());
        Map<String, Object> properties = new HashMap<>(2);

        properties.put(EQUIPMENT_ID, naModule.getId());
        properties.put(PARENT_ID, naModule.getMainDevice());

        addDiscoveredThing(thingUID, properties, naModule.getModuleName());
    }

    private void onPlugAddedInternal(NAPlug naPlug) {
        ThingUID thingUID = findThingUID(naPlug.getType(), naPlug.getId());
        Map<String, Object> properties = new HashMap<>(1);
        properties.put(EQUIPMENT_ID, naPlug.getId());
        addDiscoveredThing(thingUID, properties, naPlug.getStationName());
    }

    private void onThermostatAddedInternal(NAThermostat naThermostat, NAPlug naPlug) {
        ThingUID thingUID = findThingUID(naThermostat.getType(), naThermostat.getId());
        Map<String, Object> properties = new HashMap<>(2);
        properties.put(EQUIPMENT_ID, naThermostat.getId());
        properties.put(PARENT_ID, naPlug.getId());
        addDiscoveredThing(thingUID, properties, naThermostat.getModuleName());
    }

    @Override
    public void startScan() {
        NAThermostatDataResponse thermostatData;

        try {
            // TODO : update discovery for WeatherStation once swagger api is updated
            if (netatmoBridgeHandler.stationApi != null) {
                NADeviceListResponse deviceList = netatmoBridgeHandler.getStationApi().devicelist("app_station", null,
                        false);
                screenStationDevicesAndModules(deviceList);
            }

            if (netatmoBridgeHandler.thermostatApi != null) {
                thermostatData = netatmoBridgeHandler.getThermostatApi().getthermostatsdata(null);
                screenThermostatDevicesAndModules(thermostatData);
            }

        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        stopScan();
    }

    private void addDiscoveredThing(ThingUID thingUID, Map<String, Object> properties, String displayLabel) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withBridge(netatmoBridgeHandler.getThing().getUID()).withLabel(displayLabel).build();

        thingDiscovered(discoveryResult);
    }

    private ThingUID findThingUID(String thingType, String thingId) throws IllegalArgumentException {
        for (ThingTypeUID supportedThingTypeUID : getSupportedThingTypes()) {
            String uid = supportedThingTypeUID.getId();

            if (uid.equalsIgnoreCase(thingType)) {
                return new ThingUID(supportedThingTypeUID, netatmoBridgeHandler.getThing().getUID(),
                        thingId.replaceAll("[^a-zA-Z0-9_]", ""));
            }
        }
        throw new IllegalArgumentException("Unsupported device type discovered :" + thingType);
    }

}
