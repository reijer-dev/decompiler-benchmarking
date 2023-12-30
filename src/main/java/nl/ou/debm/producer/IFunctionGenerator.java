package nl.ou.debm.producer;

import java.util.List;

public interface IFunctionGenerator {
    Function getNewFunction(int currentDepth, DataType type, Boolean withParameters);
}

