import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import prep.Definition;
import prep.Property;
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
       oneDefinition = new Definition("<http://nlp/resources/synsets/WordNetNounSynset#foo_bar>");
       anotherDefinition = new Definition("<http://nlp/resources/synsets/WordNetNounSynset#eggs_n_ham>");

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
