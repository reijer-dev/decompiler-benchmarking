package nl.ou.debm.common.feature1;

public enum ELoopExpressions {
    NONE,
    ONLY_INIT, ONLY_UPDATE, ONLY_TEST,
    INIT_UPDATE, INIT_TEST, UPDATE_TEST,
    ALL;

    public boolean bInitAvailable(){
        return ((this == ONLY_INIT) ||
                (this == INIT_UPDATE) ||
                (this == INIT_TEST) ||
                (this == ALL));
    }

    public boolean bUpdateAvailable(){
        return ((this == ONLY_UPDATE) ||
                (this == INIT_UPDATE) ||
                (this == UPDATE_TEST) ||
                (this == ALL));
    }

    public boolean bTestAvailable(){
        return ((this == ONLY_TEST) ||
                (this == INIT_TEST) ||
                (this == UPDATE_TEST) ||
                (this == ALL));
    }

    public boolean bAnyAvailable(){
        return (this != NONE);
    }
}
