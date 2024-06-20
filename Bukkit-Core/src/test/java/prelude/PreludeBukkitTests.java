package prelude;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
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