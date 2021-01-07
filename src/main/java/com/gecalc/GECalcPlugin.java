package com.gecalc;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor
(
        name = "GE Calc",
        description = "Use maths to set price and quantity in GE!",
        tags = {"ge", "grand", "exchange", "price", "prices", "math", "maths", "calc", "calculator"}
)
public class GECalcPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private KeyManager keyManager;

    @Inject
    private GECalcKeyHandler keyInputHandler;

    @Override
    protected void startUp() throws Exception
    {
        keyManager.registerKeyListener(keyInputHandler);
        log.info("GE Calc - Started!");
    }

    @Override
    protected void shutDown() throws Exception
    {
        keyManager.unregisterKeyListener(keyInputHandler);
        log.info("GE Calc - Stopped!");
    }
}
