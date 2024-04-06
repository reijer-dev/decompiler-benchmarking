package nl.ou.debm.common.feature3;

public enum AsmType{
    StackAllocation,
    StackDeallocation,
    NonVolatileRegisterSave,
    NonVolatileRegisterLoad,
    Pseudo,
    Other,
    FunctionLabel,
    OtherLabel,
    Call,
    Return,
    BaseToStackPointer,
    SaveBasePointer,
    RegisterHoming,
    RegisterMove
}
