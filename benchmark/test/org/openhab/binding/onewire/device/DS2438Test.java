/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.onewire.device;


import LightSensorType.ELABNET_V1;
import LightSensorType.ELABNET_V2;
import OnOffType.ON;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.device.DS2438;
import org.openhab.binding.onewire.test.AbstractDeviceTest;


/**
 * Tests cases for {@link DS2438}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class DS2438Test extends AbstractDeviceTest {
    @Test
    public void temperatureChannel() {
        instantiateDevice();
        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(ON);
            Mockito.when(mockBridgeHandler.readDecimalType(ArgumentMatchers.eq(testSensorId), ArgumentMatchers.any())).thenReturn(new DecimalType(10.0));
            testDevice.enableChannel(CHANNEL_TEMPERATURE);
            testDevice.configureChannels();
            inOrder.verify(mockThingHandler).getThing();
            testDevice.refresh(mockBridgeHandler, true);
            inOrder.verify(mockBridgeHandler).readDecimalType(ArgumentMatchers.eq(testSensorId), ArgumentMatchers.any());
            inOrder.verify(mockThingHandler).postUpdate(ArgumentMatchers.eq(CHANNEL_TEMPERATURE), ArgumentMatchers.eq(new org.eclipse.smarthome.core.library.types.QuantityType("10.0 ?C")));
            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void humidityChannel() {
        instantiateDevice();
        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(ON);
            Mockito.when(mockBridgeHandler.readDecimalType(ArgumentMatchers.eq(testSensorId), ArgumentMatchers.any())).thenReturn(new DecimalType(10.0));
            testDevice.enableChannel(CHANNEL_HUMIDITY);
            testDevice.enableChannel(CHANNEL_ABSOLUTE_HUMIDITY);
            testDevice.enableChannel(CHANNEL_DEWPOINT);
            testDevice.configureChannels();
            inOrder.verify(mockThingHandler).getThing();
            testDevice.refresh(mockBridgeHandler, true);
            inOrder.verify(mockBridgeHandler, Mockito.times(2)).readDecimalType(ArgumentMatchers.eq(testSensorId), ArgumentMatchers.any());
            inOrder.verify(mockThingHandler).postUpdate(ArgumentMatchers.eq(CHANNEL_HUMIDITY), ArgumentMatchers.eq(new org.eclipse.smarthome.core.library.types.QuantityType("10.0 %")));
            inOrder.verify(mockThingHandler).postUpdate(ArgumentMatchers.eq(CHANNEL_ABSOLUTE_HUMIDITY), ArgumentMatchers.eq(new org.eclipse.smarthome.core.library.types.QuantityType("0.9381970824113001000 g/m?")));
            inOrder.verify(mockThingHandler).postUpdate(ArgumentMatchers.eq(CHANNEL_DEWPOINT), ArgumentMatchers.eq(new org.eclipse.smarthome.core.library.types.QuantityType("-20.31395053870025 ?C")));
            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void voltageChannel() {
        instantiateDevice();
        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(ON);
            Mockito.when(mockBridgeHandler.readDecimalType(ArgumentMatchers.eq(testSensorId), ArgumentMatchers.any())).thenReturn(new DecimalType(2.0));
            testDevice.enableChannel(CHANNEL_VOLTAGE);
            testDevice.configureChannels();
            inOrder.verify(mockThingHandler).getThing();
            testDevice.refresh(mockBridgeHandler, true);
            inOrder.verify(mockBridgeHandler).readDecimalType(ArgumentMatchers.eq(testSensorId), ArgumentMatchers.any());
            inOrder.verify(mockThingHandler).postUpdate(ArgumentMatchers.eq(CHANNEL_VOLTAGE), ArgumentMatchers.eq(new org.eclipse.smarthome.core.library.types.QuantityType("2.0 V")));
            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void currentChannel() {
        instantiateDevice();
        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(ON);
            Mockito.when(mockBridgeHandler.readDecimalType(ArgumentMatchers.eq(testSensorId), ArgumentMatchers.any())).thenReturn(new DecimalType(2.0));
            testDevice.enableChannel(CHANNEL_CURRENT);
            testDevice.configureChannels();
            inOrder.verify(mockThingHandler).getThing();
            testDevice.refresh(mockBridgeHandler, true);
            inOrder.verify(mockBridgeHandler).readDecimalType(ArgumentMatchers.eq(testSensorId), ArgumentMatchers.any());
            inOrder.verify(mockThingHandler).postUpdate(ArgumentMatchers.eq(CHANNEL_CURRENT), ArgumentMatchers.eq(new org.eclipse.smarthome.core.library.types.QuantityType("2.0 mA")));
            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void lightChannel() {
        instantiateDevice();
        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(ON);
            Mockito.when(mockBridgeHandler.readDecimalType(ArgumentMatchers.eq(testSensorId), ArgumentMatchers.any())).thenReturn(new DecimalType(0.1));
            testDevice.enableChannel(CHANNEL_LIGHT);
            testDevice.configureChannels();
            inOrder.verify(mockThingHandler).getThing();
            ((DS2438) (testDevice)).setLightSensorType(ELABNET_V1);
            testDevice.refresh(mockBridgeHandler, true);
            inOrder.verify(mockBridgeHandler).readDecimalType(ArgumentMatchers.eq(testSensorId), ArgumentMatchers.any());
            inOrder.verify(mockThingHandler).postUpdate(ArgumentMatchers.eq(CHANNEL_LIGHT), ArgumentMatchers.eq(new org.eclipse.smarthome.core.library.types.QuantityType("97442 lx")));
            ((DS2438) (testDevice)).setLightSensorType(ELABNET_V2);
            testDevice.refresh(mockBridgeHandler, true);
            inOrder.verify(mockBridgeHandler).readDecimalType(ArgumentMatchers.eq(testSensorId), ArgumentMatchers.any());
            inOrder.verify(mockThingHandler).postUpdate(ArgumentMatchers.eq(CHANNEL_LIGHT), ArgumentMatchers.eq(new org.eclipse.smarthome.core.library.types.QuantityType("134 lx")));
            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void supplyVoltageChannel() {
        instantiateDevice();
        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(ON);
            Mockito.when(mockBridgeHandler.readDecimalType(ArgumentMatchers.eq(testSensorId), ArgumentMatchers.any())).thenReturn(new DecimalType(2.0));
            testDevice.enableChannel(CHANNEL_SUPPLYVOLTAGE);
            testDevice.configureChannels();
            inOrder.verify(mockThingHandler).getThing();
            testDevice.refresh(mockBridgeHandler, true);
            inOrder.verify(mockBridgeHandler).readDecimalType(ArgumentMatchers.eq(testSensorId), ArgumentMatchers.any());
            inOrder.verify(mockThingHandler).postUpdate(ArgumentMatchers.eq(CHANNEL_SUPPLYVOLTAGE), ArgumentMatchers.eq(new org.eclipse.smarthome.core.library.types.QuantityType("2.0 V")));
            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void noChannel() {
        instantiateDevice();
        try {
            Mockito.when(mockBridgeHandler.checkPresence(testSensorId)).thenReturn(ON);
            Mockito.when(mockBridgeHandler.readDecimalType(ArgumentMatchers.eq(testSensorId), ArgumentMatchers.any())).thenReturn(new DecimalType(2.0));
            testDevice.configureChannels();
            inOrder.verify(mockThingHandler).getThing();
            testDevice.refresh(mockBridgeHandler, true);
            inOrder.verifyNoMoreInteractions();
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }
}
