package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.EntityCheck;
import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.model.command.EntityCommand;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.extended.CommandSendingResult;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import io.qameta.allure.Issue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Collections;

import static com.axibase.tsd.api.util.Mocks.entity;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.testng.AssertJUnit.*;

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

    @Issue("3550")
    @Test
    public void testEnabled() throws Exception {
        Entity entity = new Entity(entity());
        entity.setEnabled(true);
        EntityCommand command = new EntityCommand(entity);
        CommandMethod.send(command);
        Checker.check(new EntityCheck(entity));
        Entity actualEntity = EntityMethod.getEntity(entity.getName());
        assertTrue("Failed to set enabled", actualEntity.getEnabled());
    }

    @Issue("3550")
    @Test
    public void testDisabled() throws Exception {
        Entity entity = new Entity(entity());
        entity.setEnabled(false);
        EntityCommand command = new EntityCommand(entity);
        CommandMethod.send(command);
        Checker.check(new EntityCheck(entity));
        Entity actualEntity = EntityMethod.getEntity(entity.getName());
        assertFalse("Failed to set disabled", actualEntity.getEnabled());
    }

    @Issue("3550")
    @Test
    public void testNullEnabled() throws Exception {
        Entity entity = new Entity(entity());
        entity.setEnabled(null);
        EntityCommand command = new EntityCommand(entity);
        CommandMethod.send(command);
        Checker.check(new EntityCheck(entity));
        Entity actualEntity = EntityMethod.getEntity(entity.getName());
        assertTrue("Failed to omit enabled", actualEntity.getEnabled());
    }

    @DataProvider(name = "incorrectEnabledProvider")
    public Object[][] provideIncorrectEnabledData() {
        return new Object[][]{
                {"y"},
                {"Y"},
                {"yes"},
                {"да"},
                {"non"},
                {"1"},
                {"+"},
                {"azazaz"},
                {"longvalue"},
                {"tr\tue"},
                {"tr\u0775ue"},
                {"'true'"},
                {"'false'"}
        };
    }

    @Issue("3550")
    @Test(dataProvider = "incorrectEnabledProvider")
    public void testIncorrectEnabled(String enabled) throws Exception {
        String entityName = entity();
        String command = String.format("entity  e:%s b:%s", entityName, enabled);
        CommandMethod.send(command);
        Response serverResponse = EntityMethod.getEntityResponse(entityName);
        assertEquals("Bad entity was accepted :: " + command, NOT_FOUND.getStatusCode(), serverResponse.getStatus());
    }

    @DataProvider(name = "correctEnabledProvider")
    public Object[][] provideCorrectEnabledData() {
        return new Object[][]{
                {"true"},
                {"false"},
                {"\"true\""},
                {"\"false\""}
        };
    }

    @Issue("3550")
    @Test(dataProvider = "correctEnabledProvider")
    public void testRawEnabled(String enabled) throws Exception {
        String entityName = entity();
        Entity entity = new Entity(entityName);
        String command = String.format("entity  e:%s b:%s", entityName, enabled);
        CommandMethod.send(command);
        Checker.check(new EntityCheck(entity));
        Entity actualEntity = EntityMethod.getEntity(entityName);
        assertEquals("Failed to set enabled (raw)", enabled.replaceAll("[\\'\\\"]", ""), actualEntity.getEnabled().toString());
    }
}
