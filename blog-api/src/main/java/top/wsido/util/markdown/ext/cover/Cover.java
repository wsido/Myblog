package top.wsido.util.markdown.ext.cover;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Delimited;

/**
 * @Description: A cover node containing text and other inline nodes nodes as children.
 * @Author: wsido
 * @Date: 2020-05-13
 */
public class Cover extends CustomNode implements Delimited {
	private static final String DELIMITER = "%%";

	@Override
	public String getOpeningDelimiter() {
		return DELIMITER;
	}

	@Override
	public String getClosingDelimiter() {
		return DELIMITER;
	}
}
