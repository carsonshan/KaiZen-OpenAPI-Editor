package com.reprezen.swagedit.model;

import static com.reprezen.swagedit.model.NodeDeserializer.ATTRIBUTE_MODEL;
import static com.reprezen.swagedit.model.NodeDeserializer.ATTRIBUTE_PARENT;
import static com.reprezen.swagedit.model.NodeDeserializer.ATTRIBUTE_POINTER;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Strings;
import com.reprezen.swagedit.json.JsonSchemaManager;
import com.reprezen.swagedit.json.JsonType2;
import com.reprezen.swagedit.json.JsonType2.ObjectType;

public class Model {

    private final Map<JsonPointer, AbstractNode> nodes = new LinkedHashMap<>();

    public static Model parseYaml(String text) throws IOException {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(AbstractNode.class, new NodeDeserializer());
        mapper.registerModule(module);

        Model model = new Model();
        mapper.reader() //
                .withAttribute(ATTRIBUTE_MODEL, model) //
                .withAttribute(ATTRIBUTE_PARENT, null) //
                .withAttribute(ATTRIBUTE_POINTER, JsonPointer.compile("")) //
                .withType(AbstractNode.class) //
                .readValue(text);

        return model.resolveTypes();
    }

    protected Model resolveTypes() {
        ObjectType baseType = new ObjectType();
        baseType.schema = new JsonSchemaManager().getSwaggerSchema();
        baseType.containingProperty = null;
        baseType.definition = baseType.schema.asJson();
        baseType.parent = null;

        AbstractNode root = getRoot();
        root.type2 = baseType;

        resolveType(root, baseType);

        return this;
    }

    protected void resolveType(AbstractNode element, JsonType2 baseType) {
        JsonType2 type = JsonType2.findType(element, element.getPointer(), baseType);
        element.type2 = type;

        for (AbstractNode el : element.elements()) {
            resolveType(el, baseType);
        }
    }

    public AbstractNode find(String path) {
        if (Strings.emptyToNull(path) == null || path.equals(":")) {
            return getRoot();
        }

        return nodes.get(pointer(path));
    }

    protected JsonPointer pointer(String pointer) {
        return JsonPointer.compile(pointer.replaceAll("/", "~1").replaceAll(":", "/"));
    }

    public void add(AbstractNode node) {
        nodes.put(node.getPointer(), node);
    }

    public AbstractNode getRoot() {
        return nodes.get(JsonPointer.compile(""));
    }

    public String getPath(int line, int column) {
        AbstractNode node = getNode(line, column);
        if (node != null) {
            return node.getPointer().toString();
        }
        return "";
    }

    public AbstractNode getNode(int line, int column) {
        if (column == 0) {
            return getRoot();
        }

        AbstractNode found = forLine(line);
        if (found != null) {

            int c = startColumn(found) + Strings.nullToEmpty(found.getProperty()).length();
            if (column >= c) {
                return found;
            } else {
                return found.getParent();
            }

        } else {
            found = findBeforeLine(line, column);

            if (found != null) {
                if (startColumn(found) <= column) {
                    return found;
                } else {
                    return found.getParent();
                }
            }
        }

        return null;
    }

    protected AbstractNode forLine(int line) {
        final AbstractNode root = getRoot();
        for (AbstractNode node : nodes.values()) {
            if (node != root && startLine(node) == line) {
                return node;
            }
        }
        return null;
    }

    protected AbstractNode findBeforeLine(int line, int column) {
        AbstractNode root = getRoot();
        AbstractNode found = null, before = null;
        Iterator<AbstractNode> it = nodes.values().iterator();

        while (found == null && it.hasNext()) {
            AbstractNode current = it.next();
            if (root == current) {
                continue;
            }

            if (startLine(current) < line) {
                if (contentColumn(current) <= column) {
                    before = current;
                }
            } else {
                found = before;
            }
        }

        if (found == null && before != null) {
            found = before;
        }

        return found;
    }

    protected int startLine(AbstractNode n) {
        return n.getStart().getLineNr() - 1;
    }

    protected int startColumn(AbstractNode n) {
        return n.getStart().getColumnNr() - 1;
    }

    protected int endLine(AbstractNode n) {
        return n.getEnd().getLineNr() - 1;
    }

    protected int endColumn(AbstractNode n) {
        return n.getEnd().getColumnNr() - 1;
    }

    protected int contentLine(AbstractNode n) {
        return n.getLocation().getLineNr() - 1;
    }

    protected int contentColumn(AbstractNode n) {
        return n.getLocation().getColumnNr() - 1;
    }

    public Iterable<AbstractNode> allNodes() {
        return nodes.values();
    }
}
