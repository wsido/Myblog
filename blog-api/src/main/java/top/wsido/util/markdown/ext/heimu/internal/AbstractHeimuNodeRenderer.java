package top.wsido.util.markdown.ext.heimu.internal;

import java.util.Collections;
import java.util.Set;

import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;

import top.wsido.util.markdown.ext.heimu.Heimu;

abstract class AbstractHeimuNodeRenderer implements NodeRenderer {
    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Collections.<Class<? extends Node>>singleton(Heimu.class);
    }
}
