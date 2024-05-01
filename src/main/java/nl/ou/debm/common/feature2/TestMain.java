package nl.ou.debm.common.feature2;

import nl.ou.debm.common.BaseCodeMarker;
import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.task.ProcessTask;

import java.util.ArrayList;
import java.util.function.Consumer;

//een programmatje om dingen gerelateerd aan feature2 te testen tijdens ontwikkeling
public class TestMain {

    public static void main(String[] args) throws Exception
    {
        // test reinterpretation of a datastructure codemarker as a general codemarker. I need to recover only the ID. The functionality of the DataStructureCodeMarker class is only necessary in the producer.
        var cm = new DataStructureCodeMarker("varname");
        var id = cm.lngGetID();
        var str = cm.toString();
        var recovered = new BaseCodeMarker(str);
        var id_recovered = recovered.lngGetID();
        System.out.println("original ID: " + id);
        System.out.println("recovered ID: " + id_recovered);
    }
}
