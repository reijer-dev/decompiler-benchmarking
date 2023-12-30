package nl.ou.debm.producer;

import nl.ou.debm.common.CodeMarker;

public interface IFunctionBodyInjector {
    CodeMarker appendCodeMarkerAtStart(Function function);

    CodeMarker appendCodeMarkerAtEnd(Function function);
}
