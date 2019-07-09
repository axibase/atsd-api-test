package com.axibase.tsd.api.method.entity;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.EntityCheck;
import com.axibase.tsd.api.model.command.EntityCommand;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.command.StringCommand;
import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.transport.Transport;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Collections;


import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.testng.AssertJUnit.*;

public class EntityCommandTest extends EntityTest {
    private final static String E_TAG_1 = "e-tag-1";
    private final static String E_TAG_2 = "e-tag-2";
    private final static String E_VAL_1 = "e-val-1";
    private final static String E_VAL_1_UPD = "e-val-1-upd";
    private final static String E_VAL_2 = "e-val-2";

    private final Transport transport;

    @Factory(dataProvider = "transport", dataProviderClass = Transport.class)
    public EntityCommandTest(final Transport transport) {
        this.transport = transport;
    }

    @Issue("3111")
    @Issue("6319")
    @Test
    public void testAddNewEntityTagForExistingEntity() throws Exception {
        Entity storedEntityWithTags = new Entity(Mocks.entity());
        storedEntityWithTags.addTag(E_TAG_1, E_VAL_1);
        createOrReplaceEntityCheck(storedEntityWithTags);
        storedEntityWithTags.addTag(E_TAG_2, E_VAL_2);
        PlainCommand command = new EntityCommand(storedEntityWithTags);
        String assertMessage = "Entity tag isn't added for existing entity with " + transport;
        assertTrue(assertMessage, transport.send(command));
        assertEntityExisting(assertMessage,
                storedEntityWithTags);
    }

    @Issue("3111")
    @Issue("6319")
    @Test
    public void testUpdateEntityTagsForExistingEntity() throws Exception {
        Entity storedEntityUpdateTags = new Entity(Mocks.entity());
        storedEntityUpdateTags.addTag(E_TAG_1, E_VAL_1);
        createOrReplaceEntityCheck(storedEntityUpdateTags);
        storedEntityUpdateTags.setTags(Collections.singletonMap(E_TAG_1, E_VAL_1_UPD));
        PlainCommand command = new EntityCommand(storedEntityUpdateTags);
        String assertMessage = "Entity tag isn't updated for existing entity with " +transport;
        assertTrue(assertMessage, transport.send(command));
        assertEntityExisting(assertMessage,
                storedEntityUpdateTags
        );
    }

    @Issue("3111")
    @Issue("6319")
    @Test(
            description = "Malformed parameter - tag with whitespace"
    )
    public void testAddNewEntityTagsMalformedForNewEntity() throws Exception {
        Entity entity = new Entity(Mocks.entity());
        entity.addTag("hello 1", "world");
        PlainCommand command = new EntityCommand(entity);
        String assertMessage = "Malformed response was accepted";
        assertFalse(assertMessage, transport.send(command));
    }


    @Issue("3111")
    @Issue("6319")
    @Test
    public void testNewEntityTagsForNewEntity() throws Exception {
        Entity storedEntityForTags = new Entity(Mocks.entity());
        storedEntityForTags.addTag(E_TAG_1, E_VAL_1);
        PlainCommand command = new EntityCommand(storedEntityForTags);
        String assertMessage = String.format(
                "Failed to check entity with updated tags %s",
                storedEntityForTags.getTags()
        );
        assertTrue(assertMessage, transport.send(command));
        assertEntityExisting(assertMessage, storedEntityForTags);
    }

    /**
     * Model test
     */
    @Issue("6319")
    @Test
    public void testModels() throws Exception {
        final Entity sourceEntity = new Entity(Mocks.entity(), Mocks.TAGS);
        sourceEntity.setInterpolationMode(InterpolationMode.PREVIOUS);
        sourceEntity.setLabel(Mocks.LABEL);
        sourceEntity.setTimeZoneID(Mocks.TIMEZONE_ID);
        sourceEntity.setEnabled(true);
        EntityCommand command = new EntityCommand(sourceEntity);
        String assertMessage = String.format(
                "Inserted entity doesn't exist.%nCommand: %s",
                command
        );
        assertTrue(assertMessage, transport.send(command));
        assertEntityExisting(assertMessage, sourceEntity);
    }

    @Issue("3550")
    @Issue("6319")
    @Test
    public void testEnabled() throws Exception {
        Entity entity = new Entity(Mocks.entity());
        entity.setEnabled(true);
        EntityCommand command = new EntityCommand(entity);
        String assertMessage = "Failed to set enabled";
        assertTrue(assertMessage, transport.send(command));
        Checker.check(new EntityCheck(entity));
        Entity actualEntity = EntityMethod.getEntity(entity.getName());
        assertTrue(assertMessage, actualEntity.getEnabled());
    }

    @Issue("3550")
    @Issue("6319")
    @Test
    public void testDisabled() throws Exception {
        Entity entity = new Entity(Mocks.entity());
        entity.setEnabled(false);
        EntityCommand command = new EntityCommand(entity);
        String assertMessage = "Failed to set disabled";
        assertTrue(assertMessage, transport.send(command));
        Checker.check(new EntityCheck(entity));
        Entity actualEntity = EntityMethod.getEntity(entity.getName());
        assertFalse(assertMessage, actualEntity.getEnabled());
    }

    @Issue("3550")
    @Issue("6319")
    @Test
    public void testNullEnabled() throws Exception {
        Entity entity = new Entity(Mocks.entity());
        entity.setEnabled(null);
        EntityCommand command = new EntityCommand(entity);
        String assertMessage = "Failed to omit enabled";
        assertTrue(assertMessage, transport.send(command));
        Checker.check(new EntityCheck(entity));
        Entity actualEntity = EntityMethod.getEntity(entity.getName());
        assertTrue(assertMessage, actualEntity.getEnabled());
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
    @Issue("6319")
    @Test(
            dataProvider = "incorrectEnabledProvider"
    )
    public void testIncorrectEnabled(String enabled) throws Exception {
        String entityName = Mocks.entity();
        StringCommand command = new StringCommand( String.format("entity  e:%s b:%s", entityName, enabled));
        String assertMessage = "Bad entity was accepted :: " + command.toString();
        assertFalse(assertMessage, transport.send(command));
        Response serverResponse = EntityMethod.getEntityResponse(entityName);
        assertEquals(assertMessage, NOT_FOUND.getStatusCode(), serverResponse.getStatus());
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
    @Issue("6319")
    @Test(dataProvider = "correctEnabledProvider")
    public void testRawEnabled(String enabled) throws Exception {
        String entityName = Mocks.entity();
        Entity entity = new Entity(entityName);
        StringCommand command = new StringCommand( String.format("entity  e:%s b:%s", entityName, enabled));
        String assertMessage = "Failed to set enabled (raw)";
        assertTrue(assertMessage, transport.send(command));
        Checker.check(new EntityCheck(entity));
        Entity actualEntity = EntityMethod.getEntity(entityName);
        assertEquals(assertMessage, enabled.replaceAll("[\\'\\\"]", ""), actualEntity.getEnabled().toString());
    }
}
