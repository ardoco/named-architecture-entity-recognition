/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.naer.recognizer;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.ChatModel;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntity;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.naer.model.SoftwareArchitectureDocumentation;

/**
 * Abstract base class representing a prompt for named entity recognition.
 */
public abstract class Prompt {
    private static final Logger logger = LoggerFactory.getLogger(Prompt.class);
    protected final SystemMessage systemMessage = new SystemMessage("You are a software engineer and software architect.");
    protected String text;
    private boolean modified = false;

    /**
     * Constructs a new {@link Prompt} instance.
     *
     * @param text the text of the prompt
     * @throws IllegalArgumentException if the text is null or blank
     */
    protected Prompt(String text) {
        if (text == null || text.isBlank()) {
            logger.error("First part of prompt cannot be null or blank");
            throw new IllegalArgumentException("First part of prompt cannot be null or blank");
        }
        this.text = text;
    }

    /**
     * Gets the text of the prompt.
     *
     * @return the text of the prompt
     */
    public String getText() {
        return text;
    }

    /**
     * Appends a list of possible named entities that could be mentioned in the SAD, grouped by their types, to the existing prompt.
     *
     * @param possibleEntities a map containing named entity types as keys and sets of entity names
     *                         as values. Each type represents a category (e.g., COMPONENT, INTERFACE),
     *                         and each set contains the names of entities belonging to that type.
     *                         Must not be null or contain null keys/values.
     * @throws IllegalStateException if the prompt has already been modified (addPossibleEntities can only be called once)
     */
    public void addPossibleEntities(Map<NamedEntityType, Set<String>> possibleEntities) {
        if (modified) {
			// TODO we need to refactor the modification of the prompt!
            throw new IllegalStateException("Cannot modify prompt after already modified.");
        }

        modified = true;

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<NamedEntityType, Set<String>> entry : possibleEntities.entrySet()) {
            NamedEntityType type = entry.getKey();
            Set<String> names = entry.getValue();

            sb.append(type.toString().toLowerCase()).append(" entities: ");
            sb.append(String.join(", ", names));
            sb.append("\n");
        }

        String possibleEntitiesString = sb.toString();

        String componentSupportList = "\n\nAs support, here is a list of entities that could be mentioned in the text:\n" + possibleEntitiesString + "\n";
        this.text += componentSupportList;
    }

    /**
     * Processes the prompt using the given chat model and SAD.
     *
     * @param chatModel the chat model to use
     * @param sad       the software architecture documentation to be analyzed
     * @return the response from the chat model
     */
    public abstract String process(ChatModel chatModel, SoftwareArchitectureDocumentation sad);

    /**
     * Parses the answer from the chat model into a set of named entities.
     *
     * @param answer the answer from the chat model
     * @param sad    the software architecture documentation
     * @return a set of named entities
     * @throws IOException if the answer cannot be parsed
     */
    public abstract Set<NamedEntity> parseAnswer(String answer, SoftwareArchitectureDocumentation sad) throws IOException;

    /**
     * Returns the expected output format of the prompt.
     *
     * @return a string describing the expected format of the output
     */
    public abstract String getExpectedOutputFormat();
}
