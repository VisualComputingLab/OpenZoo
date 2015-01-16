package gr.iti.openzoo.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 *
 * @author Michalis Lazaridis <michalis.lazaridis@iti.gr>
 */
@SupportedAnnotationTypes("gr.iti.openzoo.admin.OpenZooConnection")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class OpenZooConnectionProcessor extends AbstractProcessor {

    private static int output_id = 0;
    
    //public OpenZooConnectionProcessor() {}
            
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                
        for (Element e: roundEnv.getElementsAnnotatedWith(OpenZooConnection.class))
        {   
            //Check if the type of the annotated element is not a field. If yes, return a warning.
            if (e.getKind() != ElementKind.FIELD)
            {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Type of annotated element is not a field", e);
                continue;
            }
            
            String type = e.asType().toString();
            int connection_id;
                        
            //Check if the class of the annotated element is not acceptable. If yes, return a warning.
            if (type.equals("gr.iti.openzoo.impl.OpenZooInputConnection"))
            {
                type = "in";
                connection_id = 0;
            }
            else if (type.equals("gr.iti.openzoo.impl.OpenZooOutputConnection"))
            {
                type = "out";
                connection_id = ++output_id;
            }
            else
            {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "This is not a connection instance", e);
                continue;
            }
                        
            OpenZooConnection ozc = e.getAnnotation(OpenZooConnection.class);
            //String connection_name = ozc.value();
            String var_name = e.getSimpleName().toString();
            String calling_class = e.getEnclosingElement().toString();            
            
            //System.out.println("Var: " + var_name);
                        
            //Generate a file with a specified class name. 
            try
            {
                FileObject f = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", calling_class + "_" + connection_id + "." + type);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Creating " + f.toUri());
                Writer w = f.openWriter();
                //Add the content to the newly generated file.
                try
                {
                    PrintWriter pw = new PrintWriter(w);
                    //pw.println("" + UUID.randomUUID());
                    pw.println("" + connection_id);
                    pw.println(var_name);
                    pw.flush();
                }
                finally
                {
                    w.close();
                }
            } catch (IOException x)
            {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, x.toString());
            }
        }
        
        return true;
    }

}
