package org.titiplex.persistence.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccreditationPermissionTypeTest {

    @Test
    void enumContainsScenarioPermissions() {
        assertEquals(
                AccreditationPermissionType.SCENARIO_EDIT,
                AccreditationPermissionType.valueOf("SCENARIO_EDIT")
        );

        assertEquals(
                AccreditationPermissionType.SCENARIO_MODERATE,
                AccreditationPermissionType.valueOf("SCENARIO_MODERATE")
        );
    }
}