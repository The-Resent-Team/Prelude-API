/*
 * Prelude-API is a plugin to implement features for the Client.
 * Copyright (C) 2024 cire3, Preva1l
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.resentclient.prelude;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.resentclient.prelude.bukkit.PreludePlugin;
import org.junit.jupiter.api.*;

@DisplayName("Plugin Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PreludeBukkitTests {

    private static PreludePlugin plugin;

    @BeforeAll
    @DisplayName("Test Plugin Initialization")
    public static void setUpPlugin() {
        ServerMock server = MockBukkit.mock();
        server.addPlayer("Test_Player");
        plugin = MockBukkit.load(PreludePlugin.class);
    }

    @AfterAll
    @DisplayName("Tear down Plugin")
    public static void tearDownPlugin() {
        MockBukkit.unmock();
    }

    @Order(1)
    @Test
    @DisplayName("Test Adapter")
    public void testVersionAdapter() {
        Assertions.assertNotNull(plugin.getAdapter());
    }
}