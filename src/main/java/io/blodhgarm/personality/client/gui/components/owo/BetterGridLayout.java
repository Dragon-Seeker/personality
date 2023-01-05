package io.blodhgarm.personality.client.gui.components.owo;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.*;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Arrays;

public class BetterGridLayout extends GridLayout {

    protected int rowDividingLineWidth = -1;
    protected int columnDividingLineWidth = -1;

    protected Color rowDividingLineColor = Color.BLACK;
    protected Color columnDividingLineColor = Color.BLACK;

    public BetterGridLayout(Sizing horizontalSizing, Sizing verticalSizing, int rows, int columns) {
        super(horizontalSizing, verticalSizing, rows, columns);
    }

    public BetterGridLayout setRowDividingLine(int width){
        this.rowDividingLineWidth = width;

        return this;
    }

    public BetterGridLayout setColumnDividingLine(int width){
        this.columnDividingLineWidth = width;

        return this;
    }

    public BetterGridLayout setRowDividingLineColor(Color color){
        this.rowDividingLineColor = color;

        return this;
    }

    public BetterGridLayout setColumnDividingLineColor(Color color){
        this.columnDividingLineColor = color;

        return this;
    }

    @Override
    public void layout(Size space) {
        int[] columnSizes = new int[this.columns];
        int[] rowSizes = new int[this.rows];

        var childSpace = this.calculateChildSpace(space);
        for (var child : this.children) {
            if (child != null) {
                child.inflate(childSpace);
            }
        }

        this.determineSizes(columnSizes, false);
        this.determineSizes(rowSizes, true);

        var mountingOffset = this.childMountingOffset();
        var layoutX = new MutableInt(this.x + mountingOffset.width());
        var layoutY = new MutableInt(this.y + mountingOffset.height());

        var addedColumnDividers = false;

        var addColumnDividers = columnDividingLineWidth > 0;
        var addRowDividers = rowDividingLineWidth > 0;

        Insets columnMargin = Insets.horizontal(3);
        Insets rowMargin = Insets.vertical(3);

        int totalColumnSize = Arrays.stream(columnSizes).sum() + ((columnMargin.horizontal() + columnDividingLineWidth) * columns);
        int totalRowSize = Arrays.stream(rowSizes).sum() + ((rowMargin.vertical() + rowDividingLineWidth) * rows);

        for (int row = 0; row < this.rows; row++) {
            layoutX.setValue(this.x + mountingOffset.width());

            int rowSize = rowSizes[row];

            for (int column = 0; column < this.columns; column++) {
                int columnSize = columnSizes[column];

                this.mountChild(this.getChild(row, column), childSpace, child -> {
                    child.mount(
                            this,
                            layoutX.intValue() + child.margins().get().left() + this.horizontalAlignment().align(child.fullSize().width(), columnSize),
                            layoutY.intValue() + child.margins().get().top() + this.verticalAlignment().align(child.fullSize().height(), rowSize)
                    );
                });

                layoutX.add(columnSizes[column]);

                if(addColumnDividers && column + 1 != this.columns) {
                    if(!addedColumnDividers) {
                        Component columnDividerBox = Components.box(Sizing.fixed(columnDividingLineWidth), Sizing.fixed(totalRowSize))
                                .color(columnDividingLineColor)
                                .margins(Insets.horizontal(3));

                        columnDividerBox.inflate(space);

                        this.nonNullChildren.add(0, columnDividerBox);

                        this.mountChild(columnDividerBox, childSpace, child -> {
                            child.mount(
                                    this,
                                    layoutX.intValue() + child.margins().get().left(),
                                    layoutY.intValue() + child.margins().get().top()
                            );
                        });
                    }

                    layoutX.add(columnDividingLineWidth + columnMargin.horizontal());
                }
            }

            if(addColumnDividers && !addedColumnDividers) addedColumnDividers = true;

            layoutY.add(rowSizes[row]);

            if(addRowDividers && row + 1 != this.rows){
                layoutX.setValue(this.x + mountingOffset.width());

                Component rowDividerBox = Components.box(Sizing.fixed(totalColumnSize), Sizing.fixed(rowDividingLineWidth))
                        .color(rowDividingLineColor)
                        .margins(row == 0 ? Insets.of(0) : rowMargin);

                rowDividerBox.inflate(space);

                this.nonNullChildren.add(0, rowDividerBox);

                this.mountChild(rowDividerBox, childSpace, child -> {
                    child.mount(
                            this,
                            layoutX.intValue() + child.margins().get().left(),
                            layoutY.intValue() + child.margins().get().top()
                    );
                });

                layoutY.add(rowDividingLineWidth + rowMargin.vertical());
            }
        }

        this.contentSize = Size.of(layoutX.intValue() - this.x, layoutY.intValue() - this.y);
    }

}
