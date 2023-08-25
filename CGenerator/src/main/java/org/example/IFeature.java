package org.example;


/*
    Interface containing all the functions that must be implemented to make a useful
    feature-class.

    Read the note on the constructor carefully.
 */

public interface IFeature {

    /*
       It is not possible to force a constructor declaration using an interface. However,
       every IFeature-based class is supposed to implement a constructor like this:

            final CGenerator generator;
            public [class name](CGenerator generator){
                this.generator = generator;
            }

       This will make sure that every feature knows its calling object, which it can use to
       request c-code. For example: this feature's code may want to have a random struct. If it
       does so, it will ask the generator to pick one for him. The generator will then give
       one from its database, or have a feature (possibly even the same) create one.

     */



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
