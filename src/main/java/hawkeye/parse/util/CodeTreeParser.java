package hawkeye.parse.util;

import com.google.common.collect.ImmutableList;
import hawkeye.config.model.ControlCode;
import hawkeye.game.model.Game;
import hawkeye.graph.model.GraphNode;
import hawkeye.parse.exceptions.InvalidTextException;
import hawkeye.parse.model.ControlCodeUsage;
import hawkeye.parse.model.TextSyntaxTreeNode;
import hawkeye.rom.exceptions.ROMAccessException;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.joda.time.DateTime;
import org.joda.time.Minutes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

@AllArgsConstructor
public class CodeTreeParser {
    private static final ControlCode MAXIMUM_RECURSION_CONTROL_CODE = ControlCode.builder()
            .identifier(ImmutableList.of(-1))
            .description("Reached maximum recursion depth")
            .length(Optional.of(1))
            .dialogueRepresentation(Optional.empty())
            .isTerminal(true)
            .isSuppressNextTerminator(false)
            .referenceSettings(Optional.empty())
            .build();
    private static final ControlCodeUsage MAXIMUM_RECURSION_CONTROL_CODE_USAGE =
            new ControlCodeUsage(MAXIMUM_RECURSION_CONTROL_CODE, new int[] {});

    private static final int DEFAULT_MAXIMUM_RECURSION_DEPTH = 100;
    private static final int MAXIMUM_MINUTES_RUNTIME = 3;

    private TextSyntaxTreeNode syntaxTree;

    public GraphNode<ControlCodeUsage> parseToGraph(Game game, long offset)
            throws InvalidTextException, ROMAccessException {
        return parseToGraph(game, offset, DEFAULT_MAXIMUM_RECURSION_DEPTH);
    }

    public GraphNode<ControlCodeUsage> parseToGraph(Game game, long offset, int maximumRecursionDepth)
            throws InvalidTextException, ROMAccessException {
        return parseToGraph(game, new ParserState(offset, new Stack<>()), new HashMap<>(), DateTime.now(), maximumRecursionDepth);
    }

    @Value
    private static class ParserState {
        private long offset;
        private Stack<Long> callStack;
    }

    private GraphNode<ControlCodeUsage> parseToGraph(
            Game game,
            ParserState currentParserState,
            Map<ParserState, GraphNode<ControlCodeUsage>> alreadyVisitedParserStates,
            DateTime startTime,
            int maximumRecursionDepth
    ) throws InvalidTextException, ROMAccessException {

        if ((maximumRecursionDepth <= -1)
                || Minutes.minutesBetween(startTime, DateTime.now()).getMinutes() > MAXIMUM_MINUTES_RUNTIME) {
            return new GraphNode<>(MAXIMUM_RECURSION_CONTROL_CODE_USAGE);
        }
        --maximumRecursionDepth;

        GraphNode<ControlCodeUsage> rootNode = alreadyVisitedParserStates.get(currentParserState);
        if (rootNode != null) {
            return rootNode;
        }

        GraphNode<ControlCodeUsage> previousNode = null, nextNode;

        long offset = currentParserState.getOffset();
        Stack<Long> callStack = currentParserState.getCallStack();

        boolean isEnd = false;
        boolean isSuppressNextTerminator = false;
        while (!isEnd) {
            // Step 1: Get the control code that's being used at this offset
            ControlCode controlCode = parseControlCodeFromRom(game, offset);
            int operatorLength = controlCode.getIdentifier().size();
            Optional<ControlCode.ReferenceSettings> referenceSettings = controlCode.getReferenceSettings();
            isSuppressNextTerminator |= controlCode.isSuppressNextTerminator();

            // Step 2: Read the parameters for this control code instance
            int parametersLength;
            if (referenceSettings.isPresent() && referenceSettings.get().getCountOffset().isPresent()) {
                int countOffset = referenceSettings.get().getCountOffset().get();
                int numberOfNonVariableLengthParameters = countOffset + 1 - operatorLength;
                int numberOfPointers = game.read(offset + countOffset);
                parametersLength = numberOfNonVariableLengthParameters +
                        numberOfPointers * referenceSettings.get().getReferenceLength();
            } else {
                parametersLength = controlCode.getLength().get() - operatorLength;
            }
            int[] parametersList = game.readArray(offset + operatorLength, parametersLength);

            // Step 3: Create the next node in the graph that represents this control code usage
            nextNode = new GraphNode<>(new ControlCodeUsage(controlCode, parametersList));
            alreadyVisitedParserStates.put(new ParserState(offset, callStack), nextNode);

            // Step 4: Add to the graph all the references that shoot out of this control code usage
            long offsetForNextNode = offset + operatorLength + parametersLength;
            if (referenceSettings.isPresent()) {
                if (referenceSettings.get().getCountOffset().isPresent()) {
                    int countOffset = referenceSettings.get().getCountOffset().get();
                    int numberOfPointers = game.read(offset + countOffset);
                    for (int i = 0; i < numberOfPointers * referenceSettings.get().getReferenceLength();
                         i += referenceSettings.get().getReferenceLength()) {
                        addReferenceSubnode(
                                game,
                                startTime,
                                maximumRecursionDepth,
                                offset + referenceSettings.get().getReferencesOffset() + i,
                                offsetForNextNode,
                                nextNode,
                                alreadyVisitedParserStates,
                                callStack,
                                referenceSettings.get());
                    }
                } else {
                    addReferenceSubnode(
                            game,
                            startTime,
                            maximumRecursionDepth,
                            offset + referenceSettings.get().getReferencesOffset(),
                            offsetForNextNode,
                            nextNode,
                            alreadyVisitedParserStates,
                            callStack,
                            referenceSettings.get());
                }
            }

            /*
            If this control code is terminal, try to end the parsing here.

            Also, if it's an unconditional reference, try to end the parsing here.
            This works because there are only two cases for an unconditional reference:
              1) an unconditional call
              2) an unconditional goto
            For case (1), we already added a subnode that has the next offset in its call stack
            For case (2), obviously the parsing should stop here.
            */
            if (controlCode.isTerminal()) {
                if (isSuppressNextTerminator) {
                    isSuppressNextTerminator = false;
                    offset = offsetForNextNode;
                } else {
                    if (callStack.empty()) {
                        isEnd = true;
                    } else {
                        offset = callStack.pop();
                    }
                }
            } else if (referenceSettings.isPresent() && !referenceSettings.get().isConditional()) {
                if (callStack.empty()) {
                    isEnd = true;
                } else {
                    offset = callStack.pop();
                }
            } else {
                offset = offsetForNextNode;
            }

            if (previousNode == null) {
                rootNode = nextNode;
                previousNode = rootNode;
            } else {
                previousNode.addVertex(nextNode);
                previousNode = nextNode;
            }
        }

        return rootNode;
    }

    private ControlCode parseControlCodeFromRom(Game game, long offset)
            throws ROMAccessException, InvalidTextException {
        TextSyntaxTreeNode syntaxNode = syntaxTree;
        Optional<ControlCode> lastKnownResult = Optional.empty();

        while (true) {
            if (!syntaxNode.hasChildren() && syntaxNode.getControlCode().isPresent()) {
                return syntaxNode.getControlCode().get();
            }

            int value = game.read(offset);
            Optional<TextSyntaxTreeNode> nextSyntaxNode = syntaxNode.getChild(value);

            if (nextSyntaxNode.isPresent() && nextSyntaxNode.get().getControlCode().isPresent()) {
                lastKnownResult = nextSyntaxNode.get().getControlCode();
            }

            if (nextSyntaxNode.isPresent() && (offset + 1 < game.getRom().size())) {
                ++offset;
                syntaxNode = nextSyntaxNode.get();
            } else if (lastKnownResult.isPresent()) {
                return lastKnownResult.get();
            } else {
                throw new InvalidTextException(
                        String.format("Could not parse invalid control code byte [%02X] at offset %X", value, offset));
            }
        }
    }

    private void addReferenceSubnode(Game game,
                                     DateTime startTime,
                                     int recursionDepth,
                                     long pointerOffset,
                                     long offsetForNextNode,
                                     GraphNode<ControlCodeUsage> node,
                                     Map<ParserState, GraphNode<ControlCodeUsage>> alreadyVisitedNodes,
                                     Stack<Long> callStack,
                                     ControlCode.ReferenceSettings referenceSettings)
            throws InvalidTextException, ROMAccessException {
        Optional<Long> pointer;
        if (referenceSettings.isAbsoluteAddressing()) {
            pointer = game.readPointer(pointerOffset);
        } else {
            pointer = game.readRelativePointer(pointerOffset, pointerOffset);
        }

        if (!pointer.isPresent()) {
            return;
        }

        Stack<Long> copyOfCallStack = (Stack<Long>) callStack.clone();
        // Note that the call stack is copied even if this is a GOTO reference. This is how EarthBound works.
        if (!referenceSettings.isGoto()) {
            copyOfCallStack.add(offsetForNextNode);
        }
        GraphNode<ControlCodeUsage> subnode = parseToGraph(
                game,
                new ParserState(pointer.get(), copyOfCallStack),
                alreadyVisitedNodes,
                startTime,
                recursionDepth);
        node.addVertex(subnode);
    }
}
