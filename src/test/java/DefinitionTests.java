import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import prep.Definition;
import prep.Property;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DefinitionTests {
    private static Definition oneDefinition;
    private static Definition anotherDefinition;
    private static Property oneProperty;
    private static Property anotherProperty;
    private static String onePropertyValue = "Property(\"for solo voice\", \"has_diff_qual\", \"song\")";
    private static String anotherPropertyValue = "Property(\"baked\", \"has_diff_event\", \"beans\")";

    @BeforeAll public static void initialise(){
       oneDefinition = new Definition("foo_bar");
       anotherDefinition = new Definition("eggs_n_ham");

       oneProperty = mock(Property);
       anotherProperty = mock(Property);
       when(oneProperty.toString())
               .thenReturn(onePropertyValue);
       when(anotherProperty.toString()).
               thenReturn(anotherPropertyValue);

       oneDefinition.addProperty(oneProperty);
       oneDefinition.addProperty(anotherProperty);
       anotherDefinition.addProperty(oneProperty);
       anotherDefinition.addProperty(anotherProperty);
    }

    /*
    generatesValue() would be useful in case Definition would be constructed from URI rather than value
    which is a design choice I considered, however I believe that keeping the original RDF graph separate from
    Definition and Property is a better design choice
    @Test
    public void generatesValue() throws InvocationTargetException, IllegalAccessException {
        Method generateValue = Definition.class.getDeclaredMethod("generateValue");
        generateValue.setAccessible(true);
        assertEquals("foo_bar", generateValue.invoke(oneDefinition));
        assertEquals("eggs_n_ham", generateValue.invoke(anotherDefinition));
    }
    */

    @Test
    public void canBeRepresentedAsString(){
        assertEquals("Definition(\"foo_bar\", " + oneProperty + " " + anotherProperty + ")"
                    , oneDefinition.toString(), "Definition is incorrectly represented as String");
        assertEquals("Definition(\"foo_bar\", " + oneProperty + " " + anotherProperty + ")"
                    , anotherDefinition.toString(), "Definition is incorrectly represented as String");
    }

    @Test
    public void listsProperties(){
        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(oneProperty);
        expectedProperties.add(anotherProperty);
        assertEquals(expectedProperties, oneDefinition.listProperties()
                    , "Unable to list the correct properties");
        assertEquals(expectedProperties, anotherDefinition.listProperties()
                    , "Unable to list the correct properties");

    }
}
