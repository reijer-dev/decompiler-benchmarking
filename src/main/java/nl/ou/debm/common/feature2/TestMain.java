package nl.ou.debm.common.feature2;

public class TestMain {

    public static void main(String[] args) throws Exception
    {
        var marker = new DataStructureCodeMarker(ETypeCategory.struct);
        marker.setProperty("definition", "struct, { int i; }");
        System.out.println(
                marker.strPrintf()
        );
        System.out.println(
                marker.getTypeCategory()
        );
    }
}
