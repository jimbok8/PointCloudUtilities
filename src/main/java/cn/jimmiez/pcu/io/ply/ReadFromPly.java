package cn.jimmiez.pcu.io.ply;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User can decorate a List getter in their entity class with this annotation,
 * then PCU will inject data into this field
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadFromPly {

    /**
     * the property describing the vertex
     * @return the array of name of declared properties in a element
     */
    String[] properties() default {"x", "y", "z"};

    /**
     * once you specify the name of element, the ply reader will extract data of this element from ply file.
     * Vertices data will be added into the List as an array by default,
     * @return the name of declared element
     */
    String element() default "vertex";

}
