package com.lpmoon.agent.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 列表: 用于标准化输出，暂时只支持英文，如果出现中文可能会导致格式混乱
 */
public class Table {

    /**
     * 列数
     */
    private int columnSize;

    /**
     * 列信息
     */
    private List<Column> columns;

    /**
     * 行数据
     */
    private List<Row> rows;

    /**
     * 列信息是否初始化完成
     */
    private boolean columnInitialComplete = false;

    public Table(int columnSize) {
        this.columnSize = columnSize;
        this.columns = new ArrayList<>(columnSize);
        this.rows = new ArrayList<>();
    }

    /**
     * 列表异常
     */
    public class TableException extends Exception {
        TableException(String message) {
            super(message);
        }
    }

    /**
     * 添加列信息
     * @param name 列名称
     * @param width 列宽度
     * @param selfAdaption 是否自适应宽度，如果为true，则width失效
     * @throws TableException
     */
    public void addColumn(String name, int width, boolean selfAdaption) throws TableException{
        synchronized (this) {
            if (!columnInitialComplete) {
                if (width <= 0 && !selfAdaption) {
                    throw new TableException("Column width must be greater than 0");
                }

                columns.add(new Column(name, width, selfAdaption));
                if (columns.size() == columnSize) {
                    columnInitialComplete = true;

                    // 列表头当做特殊的一行
                    List<String> columnName = new ArrayList<>();
                    for (Column column : columns) {
                        columnName.add(column.name);
                    }

                    this.addRow(columnName.toArray(new String[columnName.size()]));
                }
            } else {
                throw new TableException("Can't add new column");
            }
        }
    }

    /**
     * 添加行数据，需要注意的时候数据的个数必须等于table初始化的columnSize
     * @param contents
     * @throws TableException
     */
    public void addRow(String ... contents) throws TableException{
        if (!columnInitialComplete) {
            throw new TableException("Column doesn't initial complete");
        }

        if (contents.length != columnSize) {
            throw new TableException("Column of row must be equal to " + columnSize);
        }

        Row row = new Row();
        row.height = 1;

        for (int i = 0; i < contents.length; i++) {
            String content = contents[i];
            Cell cell = new Cell();
            Column column = columns.get(i);
            if (column.selfAdaption) {
                cell.content = Arrays.asList(content);
                column.width = Math.max(column.width, content.length());
            } else {
                cell.content = StringUtils.splitByConstantLength(content, column.width);
                row.height = Math.max(row.height, cell.content.size());
            }

            row.add(cell);
        }

        rows.add(row);
    }

    /**
     * 列信息
     */
    private class Column {
        String name;
        int width;
        boolean selfAdaption;

        Column(String name, int width, boolean selfAdaption) {
            this.name = name;
            this.width = width;
            this.selfAdaption = selfAdaption;
        }
    }

    /**
     * 行数据
     */
    private class Row {
        List<Cell> cellList = new ArrayList<>(columnSize);
        int height;

        void add(Cell cell) {
            this.cellList.add(cell);
        }
    }

    /**
     * 行里的每个单元格
     */
    private class Cell {
        List<String> content;
        Cell() {
        }
    }

    public String print() throws TableException {
        if (!columnInitialComplete) {
            throw new TableException("Column doesn't initial complete");
        }

        StringBuilder sb = new StringBuilder();

        // 打印列表横线
        printSeparateLine(sb);

        // 打印列表头
        printRowInfo(0, sb);

        // 打印列表横线
        printSeparateLine(sb);

        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            // 打印行数据
            printRowInfo(rowIndex, sb);

            // 打印列表横线
            printSeparateLine(sb);
        }

        return sb.toString();
    }

    private void printRowInfo(int rowIndex, StringBuilder sb) {
        Row row = rows.get(rowIndex);
        for (int h = 0; h < row.height; h++) {
            for (int columnIndex = 0; columnIndex < columnSize; columnIndex++) {
                sb.append("| ");
                Column column = columns.get(columnIndex);
                List<String> contents = row.cellList.get(columnIndex).content;
                String content = "";
                if (h < contents.size()) {
                    content = contents.get(h);
                }
                sb.append(String.format("%-" + column.width + "s", content));
                sb.append(" ");
            }

            sb.append("|\n");
        }
    }

    private void printSeparateLine(StringBuilder sb) {
        for (int columnIndex = 0; columnIndex < columnSize; columnIndex++) {
            sb.append("+");
            Column column = columns.get(columnIndex);
            sb.append("-");
            for (int j = 0; j < column.width; j++) {
                sb.append("-");
            }
            sb.append("-");
        }
        sb.append("+\n");
    }

    public static void main(String[] args) {
        Table table = new Table(5);
        try {
            table.addColumn("t122222222222222222222", 5, true);
            table.addColumn("t1", 10, true);
            table.addColumn("t1", 15, true);
            table.addColumn("t1", 20, true);
            table.addColumn("t1", 25, true);

            table.addRow("aa", "bb22222222222222222222222222222", "cc", "dd", "ee");
            table.addRow("aa2222222222222", "bb", "cc", "dd", "ee");
            System.out.println(table.print());
        } catch (TableException e) {
            e.printStackTrace();
        }
    }
}
