package nl.ou.debm.common.feature3;

public enum AsmType{
    StackAllocation,
    StackDeallocation,
    NonVolatileRegisterSave,
    NonVolatileRegisterLoad,
    Pseudo,
    Other,
    FunctionLabel,
    Call,
    Return,
    BaseToStackPointer,
    SaveBasePointer
}
