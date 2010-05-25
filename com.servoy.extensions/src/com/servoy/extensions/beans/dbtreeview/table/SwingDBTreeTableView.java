/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2010 Servoy BV

 This program is free software; you can redistribute it and/or modify it under
 the terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along
 with this program; if not, see http://www.gnu.org/licenses or write to the Free
 Software Foundation,Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
*/
package com.servoy.extensions.beans.dbtreeview.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.servoy.extensions.beans.dbtreeview.FoundSetTreeModel;
import com.servoy.extensions.beans.dbtreeview.SwingDBTree;
import com.servoy.extensions.beans.dbtreeview.SwingDBTreeView;
import com.servoy.j2db.plugins.IClientPluginAccess;


public class SwingDBTreeTableView extends SwingDBTreeView implements ITreeTableScriptMethods
{
	private static final long serialVersionUID = 1L;

	private final JTable treeTable = new JTable()
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed)
		{
			TreePath selectedPath;

			switch (e.getKeyCode())
			{
				case KeyEvent.VK_LEFT :
					selectedPath = SwingDBTreeTableView.this.tree.getSelectionPath();
					if (selectedPath != null)
					{
						SwingDBTreeTableView.this.tree.collapsePath(selectedPath);
						SwingDBTreeTableView.this.repaint();
					}
					return true;
				case KeyEvent.VK_RIGHT :
					selectedPath = SwingDBTreeTableView.this.tree.getSelectionPath();
					if (selectedPath != null)
					{
						int selectedRow = getSelectedRow();
						SwingDBTreeTableView.this.tree.expandPath(selectedPath);
						resizeAndRepaint();
						if (selectedRow != -1)
						{
							SwingDBTreeTableView.this.tree.setSelectionRow(selectedRow);
						}
					}
					return true;
				case KeyEvent.VK_ENTER :
					return true;
				default :
					return super.processKeyBinding(ks, e, condition, pressed);
			}
		}

		/**
		 * Overridden to message super and forward the method to the tree. Since the tree is not actually in the component hieachy it will never receive this
		 * unless we forward it in this manner.
		 */
		@Override
		public void updateUI()
		{
			super.updateUI();
			if (tree != null)
			{
				tree.updateUI();
			}
			// Use the tree's default foreground and background colors in the
			// table.
			LookAndFeel.installColorsAndFont(this, "Tree.background", "Tree.foreground", "Tree.font");
		}


		/*
		 * Workaround for BasicTableUI anomaly. Make sure the UI never tries to paint the editor. The UI currently uses different techniques to paint the
		 * renderers and editors and overriding setBounds() below is not the right thing to do for an editor. Returning -1 for the editing row in this case,
		 * ensures the editor is never painted.
		 */
		@Override
		public int getEditingRow()
		{
			return (getColumnClass(editingColumn) == SwingDBTree.class) ? -1 : editingRow;
		}

		/**
		 * Overridden to pass the new rowHeight to the tree.
		 */
		@Override
		public void setRowHeight(int rowHeight)
		{
			super.setRowHeight(rowHeight);
			if (tree != null && tree.getRowHeight() != rowHeight)
			{
				tree.setRowHeight(getRowHeight());
			}
		}
	};

	private final AbstractTableModel treeTableModel = new AbstractTableModel()
	{
		private static final long serialVersionUID = 1L;

		public int getColumnCount()
		{
			return SwingDBTreeTableView.this.dbTreeTableView.getColumns().size() + 1;
		}

		@Override
		public String getColumnName(int column)
		{

			if (column == 0) return SwingDBTreeTableView.this.dbTreeTableView.getTreeColumnHeader();

			ArrayList columns = SwingDBTreeTableView.this.dbTreeTableView.getColumns();

			if (columns.size() >= column)
			{
				String columnName = ((Column)columns.get(column - 1)).getHeader();

				if (columnName != null) return columnName;
			}

			return super.getColumnName(column);
		}

		public int getRowCount()
		{
			return SwingDBTreeTableView.this.tree.getRowCount();
		}

		public Object getValueAt(int rowIndex, int columnIndex)
		{
			if (columnIndex > 0)
			{

				TreePath rowTreePath = tree.getPathForRow(rowIndex);

				if (rowTreePath != null)
				{
					Object lastComp = rowTreePath.getLastPathComponent();
					if (lastComp instanceof FoundSetTreeModel.UserNode)
					{
						FoundSetTreeModel.UserNode un = (FoundSetTreeModel.UserNode)lastComp;
						ArrayList columns = SwingDBTreeTableView.this.dbTreeTableView.getColumns();

						if (columns.size() >= columnIndex)
						{
							Column column = (Column)columns.get(columnIndex - 1);

							return SwingDBTreeTableView.this.bindingInfo.getText(un, column.getDataprovider(), column.getTableName());
						}
					}

				}
			}

			return "";

		}

		@Override
		public Class getColumnClass(int columnIndex)
		{
			if (columnIndex == 0)
			{
				return SwingDBTree.class;
			}
			else
			{
//				return super.getColumnClass(columnIndex);
				return String.class;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return columnIndex == 0;
		}
	};


	private final DBTreeTableView dbTreeTableView;

	protected SwingDBTreeTableView(IClientPluginAccess application, DBTreeTableView dbTreeTableView)
	{
		super(application);
		this.dbTreeTableView = dbTreeTableView;
		// set the viewport to table		
		setViewportView(treeTable);

		tree.setOpaque(false);
		tree.setTable(treeTable);
		treeTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		treeTable.getTableHeader().setReorderingAllowed(false);
		treeTable.setShowGrid(false);
		treeTable.setIntercellSpacing(new Dimension(0, 0));
		treeTable.setOpaque(false);

		if (tree.getRowHeight() < 1)
		{
			// Metal looks better like this.
			treeTable.setRowHeight(18);
		}

		treeTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer()
		{
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
			{
				if (isSelected) this.setOpaque(true);
				else this.setOpaque(table.isOpaque());

				Font nodeFont = null;
				if (SwingDBTreeTableView.this.tree.getPathForRow(row).getLastPathComponent() instanceof FoundSetTreeModel.UserNode)
				{
					FoundSetTreeModel.UserNode node = (FoundSetTreeModel.UserNode)SwingDBTreeTableView.this.tree.getPathForRow(row).getLastPathComponent();
					nodeFont = bindingInfo.getFont(node);
				}

				if (nodeFont == null) nodeFont = SwingDBTreeTableView.this.getFont();

				Component cellRenderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				cellRenderer.setFont(nodeFont);

				return cellRenderer;
			}

		});

		treeTable.setDefaultRenderer(SwingDBTree.class, tree);
		treeTable.setDefaultEditor(SwingDBTree.class, new SwingDBTreeTableEditor(tree));


		treeTable.setModel(treeTableModel);
		treeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		treeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{

			public void valueChanged(ListSelectionEvent e)
			{
				int selectedRow = SwingDBTreeTableView.this.treeTable.getSelectedRow();
				SwingDBTreeTableView.this.tree.setSelectionRow(selectedRow);
			}
		});

		treeTable.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int clickedColumnIdx = treeTable.getColumnModel().getColumnIndexAtX(e.getX());
				if (clickedColumnIdx == 0) SwingDBTreeTableView.this.mouseClicked(e); // it is the tree column
			}
		});
	}

	/*
	 * DBTreeTabeView's JTable component
	 */
	public JTable getTreeTable()
	{
		return treeTable;
	}

	@Override
	public void js_setRoots(Object[] vargs)
	{
		super.js_setRoots(vargs);
//		resizeTreeColumn();
	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event)
	{
		super.treeCollapsed(event);
		treeTable.repaint();
//		resizeTreeColumn();
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event)
	{
		super.treeExpanded(event);
		treeTable.revalidate();
		treeTable.repaint();
//		resizeTreeColumn();
	}

//	private void resizeTreeColumn()
//	{
//		treeTable.getTableHeader().getColumnModel().getColumn(0).setPreferredWidth((int)this.tree.getPreferredSize().getWidth());		
//	}

	@Override
	public void setFont(Font f)
	{
		super.setFont(f);
		if (treeTable != null) treeTable.setFont(f);
	}

	public Column js_createColumn(String servername, String tablename, String header, String fieldname)
	{
		Column column = new Column();
		column.setServerName(servername);
		column.setTableName(tablename);
		column.setHeader(header);
		column.setDataprovider(fieldname);

		dbTreeTableView.addColumn(column);
		treeTableModel.fireTableStructureChanged();

		return column;
	}

	public void js_removeAllColumns()
	{
		dbTreeTableView.removeAllColumns();
		treeTableModel.fireTableStructureChanged();
	}

	@Override
	public void js_refresh()
	{
		Object[] selectionPath = js_getSelectionPath();
		super.js_refresh();

		treeTableModel.fireTableDataChanged();
		js_setSelectionPath(selectionPath);
	}

	/*
	 * Tree node checkbox state listener
	 * 
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e)
	{
		int editingRow = treeTable.getSelectedRow();
		TreePath editingPath = tree.getPathForRow(editingRow);

		if (editingPath != null)
		{
			DefaultMutableTreeNode tn = (DefaultMutableTreeNode)editingPath.getLastPathComponent();
			tree.stopEditing();
			int state = e.getStateChange();
			callMethodOnCheckBoxChange(tn, state);
		}
	}

	/*
	 * bgcolor---------------------------------------------------
	 */
	@Override
	public void setBackground(Color c)
	{
		super.setBackground(c);
		if (treeTable != null)
		{
			treeTable.setBackground(c);
		}
	}

	/*
	 * fgcolor---------------------------------------------------
	 */
	@Override
	public void setForeground(Color c)
	{
		super.setForeground(c);
		if (treeTable != null)
		{
			treeTable.setForeground(c);
		}
	}

	@Override
	public void setOpaque(boolean isOpaque)
	{
		getViewport().setOpaque(isOpaque);
		if (treeTable != null)
		{
			treeTable.setOpaque(isOpaque);
		}
	}

	@Override
	public Class[] getAllReturnedTypes()
	{
		return DBTreeTableView.getAllReturnedTypes();
	}

	public void js_setTreeColumnHeader(String treeColumnHeader)
	{
		dbTreeTableView.setTreeColumnHeader(treeColumnHeader);
		treeTableModel.fireTableStructureChanged();
	}

	@Override
	public void tableChanged(TableModelEvent e)
	{
		super.tableChanged(e);
		repaint();
	}

}

class SwingDBTreeTableEditor extends AbstractCellEditor implements TableCellEditor
{
	private static final long serialVersionUID = 1L;

	private final SwingDBTree tree;

	SwingDBTreeTableEditor(SwingDBTree tree)
	{
		this.tree = tree;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		return tree;
	}

	public Object getCellEditorValue()
	{
		return null;
	}
}