package nl.ou.debm.producer;


/*
    Interface containing all the functions that must be implemented to make a useful
    feature-class.

    Read the note on the constructor carefully.
 */

import java.util.List;

public interface IFeature {
     /*
        It is not possible to force a constructor declaration using an interface. However,
        every IFeature-based class is supposed to implement a constructor like this:

            final CGenerator generator;
            public [class name](CGenerator generator){
                this.generator = generator;
            }

        This will make sure that every feature knows its calling object when constructed
        by the CGenerator. This can use to request c-code.
        For example: this feature's code may want to have a random struct. If it
        does so, it will ask the generator to pick one for him. The generator will then give
        one from its database, or have a feature (possibly even the same) create one.

        Every IFeature-based class is also supposed to implement a default (parameterless) constructor,
        which will be used by the assessor.

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

    /**
     * @return  List of needed include statements for this feature. They are placed on top of the generated source
     */
    List<String> getIncludes();
}
