package hawkeye.parse.util;

import com.google.common.collect.ImmutableList;
import hawkeye.config.model.ControlCode;
import hawkeye.config.model.TextTable;
import hawkeye.parse.model.TextSyntaxTreeNode;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CodeSyntaxTreeFactoryTest {
    private static final ControlCode CC_01 = ControlCode.builder()
            .identifier(ImmutableList.of(0x01))
            .description("description 01")
            .length(Optional.of(1))
            .referenceSettings(Optional.empty())
            .dialogueRepresentation(Optional.empty())
            .build();
    private static final ControlCode CC_01_02 = ControlCode.builder()
            .identifier(ImmutableList.of(0x01, 0x02))
            .description("description 01 02")
            .length(Optional.of(2))
            .referenceSettings(Optional.empty())
            .dialogueRepresentation(Optional.empty())
            .build();
    private static final ControlCode CC_1F_01 = ControlCode.builder()
            .identifier(ImmutableList.of(0x1f, 0x01))
            .description("description 1f 01")
            .length(Optional.of(2))
            .referenceSettings(Optional.empty())
            .dialogueRepresentation(Optional.empty())
            .build();
    private static final ControlCode CC_1F_02 = ControlCode.builder()
            .identifier(ImmutableList.of(0x1f, 0x02))
            .description("description 1f 02")
            .length(Optional.of(2))
            .referenceSettings(Optional.empty())
            .dialogueRepresentation(Optional.empty())
            .build();
    private static final TextTable EMPTY_TEXT_TABLE = new TextTable();

    private CodeSyntaxTreeFactory factory = new CodeSyntaxTreeFactory();

    @Test
    public void testCreateTree_Empty() throws Exception {
        TextSyntaxTreeNode root = factory.createTree(Collections.emptyList(), EMPTY_TEXT_TABLE);
        assertFalse(root.hasChildren());
        assertFalse(root.getControlCode().isPresent());
        for (int i = 0; i < 255; ++i) {
            assertFalse(root.getChild(i).isPresent());
        }
    }

    @Test
    public void testCreateTree_SingleEntry() throws Exception {
        TextSyntaxTreeNode root = factory.createTree(ImmutableList.of(CC_01), EMPTY_TEXT_TABLE);
        assertTrue(root.hasChildren());
        assertFalse(root.getControlCode().isPresent());

        assertTrue(root.getChild(1).isPresent());
        assertFalse(root.getChild(1).get().hasChildren());
        Assert.assertEquals(CC_01, root.getChild(1).get().getControlCode().get());
    }

    @Test
    public void testCreateTree_FullTree() throws Exception {
        TextSyntaxTreeNode root = factory.createTree(ImmutableList.of(CC_01, CC_1F_01, CC_1F_02), EMPTY_TEXT_TABLE);
        assertTrue(root.hasChildren());
        assertFalse(root.getControlCode().isPresent());

        assertTrue(root.getChild(1).isPresent());
        assertFalse(root.getChild(1).get().hasChildren());
        assertFalse(root.getChild(1).get().getChild(0).isPresent());
        Assert.assertEquals(CC_01, root.getChild(1).get().getControlCode().get());

        assertTrue(root.getChild(0x1f).isPresent());
        assertTrue(root.getChild(0x1f).get().hasChildren());

        assertTrue(root.getChild(0x1f).get().getChild(0x01).isPresent());
        assertFalse(root.getChild(0x1f).get().getChild(0x01).get().hasChildren());
        Assert.assertEquals(CC_1F_01, root.getChild(0x1f).get().getChild(0x01).get().getControlCode().get());

        assertTrue(root.getChild(0x1f).get().getChild(0x02).isPresent());
        assertFalse(root.getChild(0x1f).get().getChild(0x02).get().hasChildren());
        Assert.assertEquals(CC_1F_02, root.getChild(0x1f).get().getChild(0x02).get().getControlCode().get());
    }

    @Test
    public void testCreateTree_BranchWithControlCode() throws Exception {
        TextSyntaxTreeNode root = factory.createTree(ImmutableList.of(CC_01, CC_01_02), EMPTY_TEXT_TABLE);
        assertTrue(root.hasChildren());
        assertFalse(root.getControlCode().isPresent());

        assertTrue(root.getChild(1).isPresent());
        assertTrue(root.getChild(1).get().hasChildren());
        Assert.assertEquals(CC_01, root.getChild(1).get().getControlCode().get());

        assertTrue(root.getChild(1).get().getChild(2).isPresent());
        Assert.assertEquals(CC_01_02, root.getChild(1).get().getChild(2).get().getControlCode().get());
        assertFalse(root.getChild(1).get().getChild(2).get().hasChildren());
    }
}
