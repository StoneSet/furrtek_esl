package net.sourceforge.zbar;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SymbolIterator implements Iterator<Symbol> {
    private Symbol current;

    SymbolIterator(Symbol symbol) {
        this.current = symbol;
    }

    public boolean hasNext() {
        return this.current != null;
    }

    public Symbol next() {
        if (this.current == null) {
            throw new NoSuchElementException("access past end of SymbolIterator");
        }
        Symbol symbol = this.current;
        long next = this.current.next();
        if (next != 0) {
            this.current = new Symbol(next);
        } else {
            this.current = null;
        }
        return symbol;
    }

    public void remove() {
        throw new UnsupportedOperationException("SymbolIterator is immutable");
    }
}
