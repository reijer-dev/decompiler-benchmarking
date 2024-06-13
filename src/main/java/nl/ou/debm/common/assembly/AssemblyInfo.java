package nl.ou.debm.common.assembly;

import nl.ou.debm.common.feature3.AsmLineInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AssemblyInfo {
    public List<AsmLineInfo> lines = new ArrayList<>();
    public ArrayList<String> homedRegisters = new ArrayList<>();
    public HashMap<String,String> registerMap = new HashMap<>();

}
