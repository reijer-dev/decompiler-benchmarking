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
    StringLabel,
    Call,
    Return,
    BaseToStackPointer,
    SaveBasePointer,
    RegisterHoming,
    RegisterMove,
    LoadStringInRegister,
    PushStringToStack,
    Jump
}
