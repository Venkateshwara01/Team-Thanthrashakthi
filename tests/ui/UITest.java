package tests.ui;

import org.junit.jupiter.api.Test;
import ui.SimulatorUI;

import static org.junit.jupiter.api.Assertions.*;

public class UITest {

    @Test
    public void testUIInstantiation() {
        assertDoesNotThrow(() -> {
            SimulatorUI ui = new SimulatorUI();
            assertNotNull(ui);
        });
    }
}
