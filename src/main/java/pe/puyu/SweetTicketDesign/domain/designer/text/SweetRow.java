package pe.puyu.SweetTicketDesign.domain.designer.text;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class SweetRow implements Iterable<SweetCell> {

    private final @NotNull List<@NotNull SweetCell> _row;

    public SweetRow() {
        _row = new LinkedList<>();
    }

    public void add(@NotNull SweetCell cell) {
        _row.add(cell);
    }

    public int size() {
        return _row.size();
    }

    public boolean existsIndex(int i) {
        return i >= 0 && i < _row.size();
    }

    public SweetCell get(int i) {
        return _row.get(i);
    }

    public void addAll(@NotNull List<@NotNull SweetCell> cells) {
        _row.addAll(cells);
    }

    public int countElementsByCharxelZero() {
        int count = 0;
        for (SweetCell cell : _row) {
            if (cell.stringStyle().charxels() == 0)
                ++count;
        }
        return count;
    }

    public int sumAllCharxels() {
        int sum = 0;
        for (SweetCell cell : _row) {
            sum += cell.stringStyle().charxels();
        }
        return sum;
    }

    @NotNull
    @Override
    public Iterator<SweetCell> iterator() {
        return _row.iterator();
    }

    @Override
    public void forEach(Consumer<? super SweetCell> action) {
        Iterable.super.forEach(action);
    }

    @Override
    public Spliterator<SweetCell> spliterator() {
        return Iterable.super.spliterator();
    }

}
