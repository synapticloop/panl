/*
 * Copyright (c) 2008-2025 synapticloop.
 *
 * https://github.com/synapticloop/panl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package com.synapticloop.panl.editor.tab.collection;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class PanlFieldRenderer extends JPanel implements TableCellRenderer {
	private static final String[] FIELD_TYPES = { "facet", "field", "unused" };

	public static final Dimension SPACER = new Dimension(4, 4);

	public PanlFieldRenderer() {
		super(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		setOpaque(true);
	}

	@Override public Component getTableCellRendererComponent(
		JTable table, Object value, boolean isSelected,
		boolean hasFocus, int row, int column) {

		Box optionsBox = Box.createVerticalBox();
		optionsBox.setAlignmentX(-1.0f);

		optionsBox.add(new JLabel(
			"# <field \"indexed\"=\"true\" \"stored\"=\"true\" \"name\"=\"colours\" \"type\"=\"string\" " +
				"\"multiValued\"=\"true\" />"));

		Box panlFieldProperty = Box.createHorizontalBox();
		panlFieldProperty.setAlignmentX(-1.0f);
		panlFieldProperty.add(new JLabel("panl."));
		JComboBox<String> fieldType = new JComboBox<>(FIELD_TYPES);
		fieldType.setSelectedIndex(2);
		panlFieldProperty.add(fieldType);
		panlFieldProperty.add(new JLabel("."));
		JComboBox<String> lpseCodes = new JComboBox<>(FIELD_TYPES);
		lpseCodes.setEditable(true);
		panlFieldProperty.add(lpseCodes);
		panlFieldProperty.setAlignmentX(-1.0f);
		panlFieldProperty.add(Box.createHorizontalGlue());

		optionsBox.add(panlFieldProperty);

		this.add(optionsBox, BorderLayout.CENTER);

		return(this);
	}
}
