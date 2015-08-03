package org.xydra.core.serialize.json;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.minio.MiniWriter;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XNumberValue;
import org.xydra.core.serialize.AbstractXydraOut;


@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class JsonOut extends AbstractXydraOut {

    public static final String CONTENT_TYPE_JSON = "application/json";

    private final String callback;

    public JsonOut() {
        this((String)null);
    }

    public JsonOut(final MiniWriter writer) {
        this(writer, null);
    }

    public JsonOut(final MiniWriter writer, final String callback) {
        super(writer);
        this.callback = callback;
        init();
    }

    public JsonOut(final String callback) {
        super();
        this.callback = callback;
        init();
    }

    private void begin(final Frame frame, final boolean newline) {
        if(frame.hasContent || frame.hasSpecialContent) {
            // This is not the first child element
            write(',');
        }
        if(newline) {
            frame.hasContent = true;
        } else {
            frame.hasSpecialContent = true;
        }
        if(frame.type != Type.Root && frame.type != Type.Child && frame.type != Type.Entry) {
            if(newline) {
                whitespace('\n');
                indent(frame.depth + 1);
            } else {
                whitespace(' ');
            }
        }
    }

    private void beginContainer(final Frame container, final char c, final boolean newline) {
        begin(container.parent, newline || !container.parent.hasContent);
        write(c);
        container.depth = container.parent.depth + 1;
    }

    private void endContainer(final Frame container, final char c) {
        if(container.hasContent) {
            whitespace('\n');
            indent(container.depth);
        } else {
            whitespace(' ');
        }
        write(c);
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE_JSON;
    }

    private void init() {
        if(this.callback != null) {
            write(this.callback);
            write('(');
        }
    }

    @Override
    protected void outpuEnd() {
        if(this.callback != null) {
            write(");");
        }
        whitespace('\n');
    }

    private <T> void output(final T value) {
        if(value instanceof Boolean || value instanceof Number || value instanceof XBooleanValue
                || value instanceof XNumberValue) {
            write(value.toString());
        } else {
            write('"');
            write(JsonEncoder.encode(value.toString()));
            write('"');
        }
    }

    @Override
    protected <T> void outputAttribute(final Frame element, final String name, final T value) {
        outputName(element, name);
        output(value);
    }

    @Override
    protected void outputBeginArray(final Frame array) {
        beginContainer(array, '[', false);
    }

    @Override
    protected void outputBeginMap(final Frame map) {
        beginContainer(map, '{', false);
    }

    @Override
    protected void outputChild(final Frame child) {
        outputName(child.parent, child.name);
        child.depth = child.parent.depth;
    }

    @Override
    protected void outputCloseElement(final Frame element) {
        endContainer(element, '}');
    }

    @Override
    protected void outputEndArray(final Frame array) {
        endContainer(array, ']');
    }

    @Override
    protected void outputEndMap(final Frame map) {
        endContainer(map, '}');
    }

    @Override
    protected void outputEntry(final Frame entry) {
        outputChild(entry);
    }

    private void outputName(final Frame frame, final String name) {
        begin(frame, true);
        write('"');
        write(name);
        write("\":");
        whitespace(' ');
    }

    @Override
    protected void outputNullElement(final Frame container) {
        begin(container, true);
        write("null");
    }

    @Override
    protected void outputOpenElement(final Frame element) {
        final boolean saveType = !element.name.equals(element.parent.getChildType());
        beginContainer(element, '{', saveType && element.parent.type != Type.Child);
        if(saveType) {
            whitespace(' ');
            write('"');
            write(JsonEncoder.PROPERTY_TYPE);
            write("\":");
            whitespace(' ');
            write('"');
            write(element.name);
            write('"');
            element.hasSpecialContent = true;
        }
    }

    @Override
    protected <T> void outputValue(final Frame container, final T value) {
        begin(container, false);
        if(value == null) {
            write("null");
        } else {
            output(value);
        }
    }

}
