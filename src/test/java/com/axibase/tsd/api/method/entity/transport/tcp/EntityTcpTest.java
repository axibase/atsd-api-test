package com.axibase.tsd.api.method.entity.transport.tcp;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.entity.EntityTest;
import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.EntityCheck;
import com.axibase.tsd.api.model.command.EntityCommand;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.transport.tcp.TCPSender;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import java.util.Collections;


import static org.testng.AssertJUnit.*;
import static com.axibase.tsd.api.transport.tcp.TCPSender.assertBadTcpResponse;
import static com.axibase.tsd.api.transport.tcp.TCPSender.assertGoodTcpResponse;

public class EntityTcpTest extends EntityTest {

    private final static String E_TAG_1 = "e-tag-1";
    private final static String E_TAG_2 = "e-tag-2";
    private final static String E_VAL_1 = "e-val-1";
    private final static String E_VAL_1_UPD = "e-val-1-upd";
    private final static String E_VAL_2 = "e-val-2";

    @Issue("6319")
    @Test
    public void testAddNewEntityTagForExistEntity() throws Exception {
        Entity storedEntityWithTags = new Entity(Mocks.entity());
        storedEntityWithTags.addTag(E_TAG_1, E_VAL_1);
        createOrReplaceEntityCheck(storedEntityWithTags);
        storedEntityWithTags.addTag(E_TAG_2, E_VAL_2);
        PlainCommand command = new EntityCommand(storedEntityWithTags);
        assertGoodTcpResponse(TCPSender.send(command, true));
        assertEntityExisting("Entity tag isn't add for existing entity", storedEntityWithTags);
    }

    @Issue("6319")
    @Test
    public void testUpdateEntityTagsForExistEntity() throws Exception {
        Entity storedEntityUpdateTags = new Entity(Mocks.entity());
        storedEntityUpdateTags.addTag(E_TAG_1, E_VAL_1);
        createOrReplaceEntityCheck(storedEntityUpdateTags);
        storedEntityUpdateTags.setTags(Collections.singletonMap(E_TAG_1, E_VAL_1_UPD));
        PlainCommand command = new EntityCommand(storedEntityUpdateTags);
        assertGoodTcpResponse(TCPSender.send(command, true));
        assertEntityExisting("Entity tag isn't update for existing entity.", storedEntityUpdateTags);
    }

    @Issue("6319")
    @Test
    public void testAddNewEntityTagsMalformedForNewEntity() throws Exception {
        Entity entity = new Entity(Mocks.entity());
        entity.addTag("hello 1", "world");
        PlainCommand command = new EntityCommand(entity);
        assertBadTcpResponse("Malformed tag passed",TCPSender.send(command,true));
    }


    @Issue("6319")
    @Test
    public void testNewEntityTagsForNewEntity() throws Exception {
        Entity storedEntityForTags = new Entity(Mocks.entity());
        storedEntityForTags.addTag(E_TAG_1, E_VAL_1);
        PlainCommand command = new EntityCommand(storedEntityForTags);
        assertGoodTcpResponse(TCPSender.send(command, true));
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
        final Entity sourceEntity = new Entity(Mocks.entity(), Mocks.TAGS);
        sourceEntity.setInterpolationMode(InterpolationMode.PREVIOUS);
        sourceEntity.setLabel(Mocks.LABEL);
        sourceEntity.setTimeZoneID(Mocks.TIMEZONE_ID);
        sourceEntity.setEnabled(true);
        EntityCommand command = new EntityCommand(sourceEntity);

        assertGoodTcpResponse(TCPSender.send(command, true));
        String assertMessage = String.format(
                "Inserted entity doesn't exist.%nCommand: %s",
                command
        );
        assertEntityExisting(assertMessage, sourceEntity);
    }

    @Issue("6319")
    @Test
    public void testEnabled() throws Exception {
        Entity entity = new Entity(Mocks.entity());
        entity.setEnabled(true);
        EntityCommand command = new EntityCommand(entity);
        assertGoodTcpResponse(TCPSender.send(command, true));
        Checker.check(new EntityCheck(entity));
        Entity actualEntity = EntityMethod.getEntity(entity.getName());
        assertTrue("Failed to set enabled", actualEntity.getEnabled());
    }

    @Issue("6319")
    @Test
    public void testDisabled() throws Exception {
        Entity entity = new Entity(Mocks.entity());
        entity.setEnabled(false);
        EntityCommand command = new EntityCommand(entity);
        assertGoodTcpResponse(TCPSender.send(command, true));
        Checker.check(new EntityCheck(entity));
        Entity actualEntity = EntityMethod.getEntity(entity.getName());
        assertFalse("Failed to set disabled", actualEntity.getEnabled());
    }

    @Issue("6319")
    @Test
    public void testNullEnabled() throws Exception {
        Entity entity = new Entity(Mocks.entity());
        entity.setEnabled(null);
        EntityCommand command = new EntityCommand(entity);
        assertGoodTcpResponse(TCPSender.send(command, true));
        Checker.check(new EntityCheck(entity));
        Entity actualEntity = EntityMethod.getEntity(entity.getName());
        assertTrue("Failed to omit enabled", actualEntity.getEnabled());
    }

}
