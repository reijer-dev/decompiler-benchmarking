package nl.ou.debm.common.feature1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrthogonalArray
{
    private int m_iNRows;
    private int m_iNColumns;

    public static class ColumnDefinition{
        public final int m_iNPossibilities;
        public final String m_strLabel;
        protected int m_iColumnNumber=0;
        public ColumnDefinition(int iNPossibilities, String strLabel){
            m_iNPossibilities = iNPossibilities;
            m_strLabel = strLabel;
        }
        public ColumnDefinition(ColumnDefinition rhs){
            m_iNPossibilities = rhs.m_iNPossibilities;
            m_strLabel = rhs.m_strLabel;
            m_iColumnNumber = rhs.m_iColumnNumber;
        }
        public String toString(){
            return "c= " + m_iColumnNumber + ", N=" + m_iNPossibilities + " (" + m_strLabel + ")";
        }
    }

    private final List<ColumnDefinition> m_columnInfo = new ArrayList<>();

    private int [] m_iResult;

    public OrthogonalArray(List<ColumnDefinition> definitions){
        for (var item : definitions) {
            m_columnInfo.add(new ColumnDefinition(item));
        }
        calcArray();
    }

    public int iGetNRows(){
        return m_iNRows;
    }
    public int iGetNColumns(){
        return m_iNColumns;
    }
    public ColumnDefinition getColumnDefinition(int iColumn){
        return m_columnInfo.get(iColumn);
    }

    private int offset(int iRow, int iColumn){
        if ((iColumn<m_iNColumns) &&
            (iColumn>=0) &&
            (iRow<m_iNRows) &&
            (iRow>=0)) {
               return iColumn + (iRow * m_iNColumns);
        }
        return 0;
    }

    public int iGetResult(int iRow, int iColumn){
        return m_iResult[offset(iRow,iColumn)];
    }

    private void calcSize(){
        // define number of columns
        m_iNColumns = m_columnInfo.size();
        // define number of rows
        int maxMax=-1, maxMin=-1;
        for (var item : m_columnInfo){
            if (item.m_iNPossibilities>maxMax){
                maxMin=maxMax;
                maxMax=item.m_iNPossibilities;
            }
            else if (item.m_iNPossibilities>maxMin){
                maxMin=item.m_iNPossibilities;
            }
        }
        m_iNRows = maxMax * maxMin;
        m_iResult = new int[m_iNRows * m_iNColumns];
        // reset table
        Arrays.fill(m_iResult, -1);
    }

    private void setColumnNumbers(){
        int ci=0;
        for (var item : m_columnInfo){
            item.m_iColumnNumber=ci++;
        }
    }

    private void calcArray(){
        // arrange size calculation
        calcSize();
        // set column numbers
        setColumnNumbers();
        // copy array
        List<ColumnDefinition> cd = new ArrayList<>();
        for (var item : m_columnInfo){
            cd.add(new ColumnDefinition(item));
        }
        // sort array
        for (int i=0; i<(cd.size()-1); ++i){
            for (int j=i+1; j<cd.size() ; ++j){
                if (cd.get(i).m_iNPossibilities < cd.get(j).m_iNPossibilities){
                    ColumnDefinition t = cd.get(i);
                    cd.set(i, cd.get(j));
                    cd.set(j, t);
                }
            }
        }

        // set longest value range
        int row, col;
        col = cd.get(0).m_iColumnNumber;
        for (row=0; row < m_iNRows; ++row){
            m_iResult[offset(row, col)] = row % cd.get(0).m_iNPossibilities;
        }

        // set other ranges
        for (int sortedDestCol = 1; sortedDestCol< cd.size() ; ++sortedDestCol){
            int absDestCol = cd.get(sortedDestCol).m_iColumnNumber;
            int v=0;
            for (row = 0; row < m_iNRows ; ++row) {
                m_iResult[offset(row, absDestCol)] = v;
                ++v;
                if (iGetResult(row, cd.get(sortedDestCol-1).m_iColumnNumber) == (cd.get(sortedDestCol-1).m_iNPossibilities-1)){
                    ++v;
                }
                v%=cd.get(sortedDestCol).m_iNPossibilities;
            }
        }
    }

    public void SortResultOnColumn(int[] col){
        for (int i=0; i<m_iNRows-1; ++i){
            for (int j=i+1; j<m_iNRows; ++j){
                boolean bSwitch = false;
                for (int k : col) {
                    int v1 = iGetResult(i, k);
                    int v2 = iGetResult(j, k);
                    if (v1 > v2) {
                        bSwitch = true;
                        break;
                    } else if (v1 < v2) {
                        break;
                    }
                }
                if (bSwitch){
                    for (int c=0;c<m_iNColumns; ++c){
                        int t=m_iResult[offset(i,c)];
                        m_iResult[offset(i,c)] = m_iResult[offset(j,c)];
                        m_iResult[offset(j,c)] = t;
                    }
                }
            }
        }
    }

}
