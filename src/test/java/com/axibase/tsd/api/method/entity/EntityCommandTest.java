package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.model.command.EntityCommand;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.extended.CommandSendingResult;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.axibase.tsd.api.util.Mocks.entity;
import static org.testng.AssertJUnit.assertEquals;

public class EntityCommandTest extends EntityTest {
    private final static String E_TAG_1 = "e-tag-1";
    private final static String E_TAG_2 = "e-tag-2";
    private final static String E_VAL_1 = "e-val-1";
    private final static String E_VAL_1_UPD = "e-val-1-upd";
    private final static String E_VAL_2 = "e-val-2";

    @Issue("3111")

    @Test
    public void testAddNewEntityTagForExistEntity() throws Exception {
        Entity storedEntityWithTags = new Entity("e-with-tags");
        storedEntityWithTags.addTag(E_TAG_1, E_VAL_1);
        createOrReplaceEntityCheck(storedEntityWithTags);
        storedEntityWithTags.addTag(E_TAG_2, E_VAL_2);
        PlainCommand command = new EntityCommand(storedEntityWithTags);
        CommandMethod.send(command);
        assertEntityExisting("Entity tag isn't add for existing entity",
                storedEntityWithTags
        );
    }

    @Issue("3111")

    @Test
    public void testUpdateEntityTagsForExistEntity() throws Exception {
        Entity storedEntityUpdateTags = new Entity("e-for-test-update-tags");
        storedEntityUpdateTags.addTag(E_TAG_1, E_VAL_1);
        createOrReplaceEntityCheck(storedEntityUpdateTags);
        storedEntityUpdateTags.setTags(Collections.singletonMap(E_TAG_1, E_VAL_1_UPD));
        PlainCommand command = new EntityCommand(storedEntityUpdateTags);
        CommandMethod.send(command);
        assertEntityExisting("Entity tag isn't update for existing entity.",
                storedEntityUpdateTags
        );
    }

    @Issue("3111")

    @Test
    public void testAddNewEntityTagsMailformedForNewEntity() throws Exception {
        Entity entity = new Entity("ent-for-test-add-tags-mailformed");
        entity.addTag("hello 1", "world");
        PlainCommand command = new EntityCommand(entity);
        CommandSendingResult expectedResult = new CommandSendingResult(1, 0);
        assertEquals(expectedResult, CommandMethod.send(command));
    }


    @Issue("3111")

    @Test
    public void testNewEntityTagsForNewEntity() throws Exception {
        Entity storedEntityForTags = new Entity("e-for-test-add-tags");
        storedEntityForTags.addTag(E_TAG_1, E_VAL_1);
        PlainCommand command = new EntityCommand(storedEntityForTags);
        CommandMethod.send(command);
        String assertMessage = String.format(
                "Failed to check entity with updated tags %s",
                storedEntityForTags.getTags()
        );
        assertEntityExisting(assertMessage, storedEntityForTags);
    }

    /**
     * Model test
     */
    @Test
    public void testModels() throws Exception {
        final Entity sourceEntity = new Entity(entity(), Mocks.TAGS);
        sourceEntity.setInterpolationMode(InterpolationMode.PREVIOUS);
        sourceEntity.setLabel(Mocks.LABEL);
        sourceEntity.setTimeZoneID(Mocks.TIMEZONE_ID);
        sourceEntity.setEnabled(true);
        EntityCommand command = new EntityCommand(sourceEntity);

        CommandMethod.send(command);
        String assertMessage = String.format(
                "Inserted entity doesn't exist.%nCommand: %s",
                command
        );
        assertEntityExisting(assertMessage, sourceEntity);
    }
}
