package org.example;


/*
    Interface containing all the functions that must be implemented to make a useful
    feature-class
 */

public interface IFeature {

    /**
     * Test whether or not a feature has done all its work.
     * @return  true when the class has emitted all of its code, false when not.
     */
    boolean isSatisfied();

    /**
     * Return a fixed short string containing a prefix that can be used in the code.
     * It makes sure that code produced by different features cannot use the same
     * identifiers accidentally.
     * For standardization purposes: implement using the FeaturePrefix enumeration
     * @return  short prefix for this feature
     */
    String getPrefix();
}
