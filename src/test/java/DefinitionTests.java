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
    private static String oneDefinitionURI = "<http://nlp/resources/synsets/WordNetNounSynset#foo_bar>";
    private static String anotherDefinitionURI = "<http://nlp/resources/synsets/WordNetNounSynset#eggs_n_ham>";
    private static String oneDefinitionValue = "foo_bar";
    private static String anotherDefinitionValue = "eggs_n_ham";
    private static String onePropertyValue = "Property(\"for solo voice\", \"has_diff_qual\", \"song\")";
    private static String anotherPropertyValue = "Property(\"baked\", \"has_diff_event\", \"beans\")";

    @BeforeAll public static void initialise() throws NoSuchMethodException{
       oneDefinition = new Definition(oneDefinitionURI);
       anotherDefinition = new Definition(anotherDefinitionURI);

       oneProperty = mock(Property.class);
       anotherProperty = mock(Property.class);
       when(oneProperty.toString())
               .thenReturn(onePropertyValue);
       when(anotherProperty.toString()).
               thenReturn(anotherPropertyValue);

       oneDefinition.addProperty(oneProperty);
       oneDefinition.addProperty(anotherProperty);
       anotherDefinition.addProperty(oneProperty);
       anotherDefinition.addProperty(anotherProperty);
    }

    @Test
    public void generatesValue() throws Exception{
        Method generateValue = Definition.class.getDeclaredMethod("generateValue");
        generateValue.setAccessible(true);
        assertEquals(oneDefinitionValue, generateValue.invoke(null, oneDefinitionURI),
                "generateValue() incorrectly extracts the local name from definition's URI");
        assertEquals(anotherDefinitionValue, generateValue.invoke(null, anotherDefinitionURI),
                "generateValue() incorrectly extracts the local name from definition's URI");
    }

    @Test
    public void canBeRepresentedAsString(){
        assertEquals("Definition(\"foo_bar\", " + oneProperty + ", " + anotherProperty + ")"
                    , oneDefinition.toString(), "Definition is incorrectly represented as String");
        assertEquals("Definition(\"foo_bar\", " + oneProperty + ", " + anotherProperty + ")"
                    , anotherDefinition.toString(), "Definition is incorrectly represented as String");
    }

    @Test
    public void listsProperties(){
        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(oneProperty);
        expectedProperties.add(anotherProperty);
        assertEquals(expectedProperties, oneDefinition.getProperties()
                    , "Unable to list the correct properties");
        assertEquals(expectedProperties, anotherDefinition.getProperties()
                    , "Unable to list the correct properties");

    }
}
