package net.unicon.pdfscanner;

import java.io.IOException;
import java.util.Collection;

public interface Writer<E> {

    void write(Collection<E> collection) throws IOException;

}
