package gr.iti.openzoo.admin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface OpenZooConnection {

    //public String value();
}
